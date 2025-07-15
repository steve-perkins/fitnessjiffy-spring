package com.steveperkins.fitnessjiffy.service;

import com.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import com.steveperkins.fitnessjiffy.domain.FoodEaten;
import com.steveperkins.fitnessjiffy.domain.ReportData;
import com.steveperkins.fitnessjiffy.domain.User;
import com.steveperkins.fitnessjiffy.domain.Weight;
import com.steveperkins.fitnessjiffy.dto.ReportDataDTO;
import com.steveperkins.fitnessjiffy.dto.converter.ReportDataToReportDataDTO;
import com.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import com.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import com.steveperkins.fitnessjiffy.repository.ReportDataRepository;
import com.steveperkins.fitnessjiffy.repository.UserRepository;
import com.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Service
public final class ReportDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDataService.class);

    private final UserRepository userRepository;
    private final WeightRepository weightRepository;
    private final FoodEatenRepository foodEatenRepository;
    private final ExercisePerformedRepository exercisePerformedRepository;
    private final ReportDataRepository reportDataRepository;
    private final ReportDataToReportDataDTO reportDataDTOConverter;

    /**
     * When an update will affect more than 7 days of data, it is scheduled for asynchronous execution in a
     * background thread.  This field controls how far out in the future that thread's execution will be
     * scheduled (i.e. so that in case a user is taking multiple actions that will each trigger large updates,
     * subsequent ones can supersede the next scheduled update, rather than scheduling additional separate
     * updates).
     * <p/>
     * This value can be overwritten in the "application.properties" config file... primarily so
     * that unit tests can use a much shorter value.
     */
    @Value("${reportdata.update-delay-in-seconds:60}")
    private long scheduleDelayInSeconds;

    /**
     * A background thread should prune outdated entries from the "scheduledUserUpdates" periodically.  This
     * frequency can be overwritten in the "application.properties" config file, primarily so that unit tests can
     * use a much shorter value.
     */
    @Value("${reportdata.cleanup-frequency-in-seconds:300}")
    private long cleanupFrequencyInSeconds;

    private final ScheduledThreadPoolExecutor reportDataUpdateThreadPool = new ScheduledThreadPoolExecutor(1);
    private final Map<UUID, ReportDataUpdateEntry> scheduledUserUpdates = new ConcurrentHashMap<>();

    /**
     * The constructor spawns a background thread, which periodically iterates through the "scheduledUserUpdates" map
     * and prunes outdated entries.
     */
    @Autowired
    public ReportDataService(
            @Nonnull final UserRepository userRepository,
            @Nonnull final WeightRepository weightRepository,
            @Nonnull final FoodEatenRepository foodEatenRepository,
            @Nonnull final ExercisePerformedRepository exercisePerformedRepository,
            @Nonnull final ReportDataRepository reportDataRepository,
            @Nonnull final ReportDataToReportDataDTO reportDataDTOConverter
    ) {
        this.userRepository = userRepository;
        this.weightRepository = weightRepository;
        this.foodEatenRepository = foodEatenRepository;
        this.exercisePerformedRepository = exercisePerformedRepository;
        this.reportDataRepository = reportDataRepository;
        this.reportDataDTOConverter = reportDataDTOConverter;

        final Runnable backgroundCleanupThread = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (Map.Entry<UUID, ReportDataUpdateEntry> entry : scheduledUserUpdates.entrySet()) {
                        final Future future = entry.getValue().getFuture();
                        if (future == null || future.isDone() || future.isCancelled()) {
                            scheduledUserUpdates.remove(entry.getKey());
                        }
                    }
                    try {
                        Thread.sleep(cleanupFrequencyInSeconds * 1000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Exception thrown while sleeping in between runs of the ReportData cleanup thread", e);
                    }
                }
            }
        };
        new Thread(backgroundCleanupThread).start();
    }

    @Nonnull
    public List<ReportDataDTO> findByUser(@Nonnull final UUID userId) {
        final Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        final List<ReportData> reportData = reportDataRepository.findByUserOrderByDateAsc(user.get());
        return reportData.stream()
                .map(reportDataDTOConverter::convert)
                .collect(toList());
    }

    /**
     * Update the ReportData records for a given user, starting on a given date and ending after today's date (in the
     * most common use case, it will be a one-day range consisting of today anyway).
     *
     * I'm not happy about the fact that this method takes as a parameter a User object rather than a UserDTO, as
     * all methods in the service tier should accept and return DTO's rather than actual entities.  However, there is
     * a strange bug in which updates to a user's profile (from the UserService.createUser() and updateUser() methods).
     *
     * The transaction in either of those methods apparently does not commit prior to calling
     * ReportDataService.updateUserFromDate()... and so when this method looks up the user in the database from the DTO,
     * it sees outdated data.  This outdated data is then committed at the end of the ReportData update process,
     * overwriting the original user update.
     *
     * I've tried taking the database save operation in UserService, and breaking it off into its own method annotated
     * with @Transactional.  Although that seems to help when running locally, the problem still persists in production.
     * So simply passing the User object directly (and avoiding the lookup-from-DTO) is an "impure" ploy to fix the
     * problem until I'm able to focus on it again at some point.  At least this method is called only by other classes
     * in the service tier, and not from anywhere in the controller tier that should never touch raw entities.
     */
    @Nullable
    public synchronized final Future updateUserFromDate(
            @Nonnull final User user,
            @Nonnull final Date date
    ) {
        // The date is adjusted by the current time in the user's specific time zone.  For example, 2015-03-01 1:00 am
        // on a GMT clock is actually 2015-02-28 8:00 pm if the user is in the "America/New_York" time zone.  So we
        // must ensure that start from 02-28 rather than 03-01.
        //
        // Unfortunately, whenever the user is within that "date-straddling" window of time, this logic will push the
        // date backwards by one day even when dealing with past historic dates rather than the current date.  We
        // might be able to resolve that with even more logic, but I don't think it's that big of a deal for now.
        // Most updates should normally be for the current date rather than a historical revision, so a little extra
        // work in an edge case scenerio may be justified by keeping the logic more simple.
        final Date adjustedDate = adjustDateForTimeZone(date, ZoneId.of(user.getTimeZone()));

        final ReportDataUpdateEntry existingEntry = scheduledUserUpdates.get(user.getId());
        if (existingEntry != null) {
            if (existingEntry.getFuture().isCancelled() || existingEntry.getFuture().isDone()) {
                // There was an update recently scheduled for this user, but it has completed and its entry can be cleaned up.
                scheduledUserUpdates.remove(user.getId());
            } else if (existingEntry.getStartDate().after(adjustedDate)) {
                // There is an update still pending for this user, but its date range is superseded by that of the new
                // update and therefore can be cancelled and cleaned up.
                existingEntry.getFuture().cancel(false);
                scheduledUserUpdates.remove(user.getId());
            } else {
                // There is an update still pending for this user, and it supersedes the new one here.  Do nothing, and
                // let the pending schedule stand.
                return null;
            }
        }

        // If the adjustedDate is within the past 7 days, perform the update synchronously.
        LocalDate adjustedLocalDate = adjustedDate.toLocalDate();
        LocalDate todayLocalDate = LocalDate.now(ZoneId.of(user.getTimeZone()));
        if (!adjustedLocalDate.isBefore(todayLocalDate.minusDays(7))) {
            LOGGER.info("Performing a ReportData update synchronously for user [{}] from date [{}] (after commit)", user.getEmail(), adjustedDate);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        performReportDataUpdate(user, adjustedDate);
                    }
                });
            } else {
                // No active transaction, run immediately
                performReportDataUpdate(user, adjustedDate);
            }
            return null;
        }

        // Otherwise, schedule an asynchronous update.
        LOGGER.info("Scheduling a ReportData update for user [{}] from date [{}] in {} seconds", user.getEmail(), adjustedDate, scheduleDelayInSeconds);
        final ReportDataUpdateTask task = new ReportDataUpdateTask(user, adjustedDate);
        final Future future = reportDataUpdateThreadPool.schedule(task, scheduleDelayInSeconds, TimeUnit.SECONDS);
        final ReportDataUpdateEntry newUpdateEntry = new ReportDataUpdateEntry(adjustedDate, future);
        scheduledUserUpdates.put(user.getId(), newUpdateEntry);
        return future;
    }

    public synchronized final boolean isIdle() {
        System.out.printf("%d active threads, %d queued tasks%n", reportDataUpdateThreadPool.getActiveCount(), scheduledUserUpdates.size());
        return reportDataUpdateThreadPool.getActiveCount() == 0 && scheduledUserUpdates.isEmpty();
    }

    /**
     * When the input date is "today", then this method returns the current date in the given time zone (e.g. the input
     * date might be early in the morning of 2015-03-01 in standard GMT, yet still late in the evening of 2015-02-28 in
     * New York).  This method does not modify historic dates earlier than today, because the current time of day today
     * shouldn't cause historic dates to change.
     */
    @Nonnull
    public final Date adjustDateForTimeZone(final Date date, final ZoneId timeZone) {
        final LocalDateTime localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        final LocalDateTime today = LocalDateTime.now();
        Date adjustedDate = (Date) date.clone();
        if (localDate.getDayOfYear() == today.getDayOfYear()) {
            final ZonedDateTime zonedDateTime = ZonedDateTime.now(timeZone);
            adjustedDate = new Date(zonedDateTime.toLocalDate().atStartOfDay(timeZone).toInstant().toEpochMilli());
        }
        return adjustedDate;
    }

    /**
     * A container holding the date range and Future reference for a scheduled user update task.  Used to detect
     * whether or not subsequent tasks supersede those previously scheduled for a user, and to cancel them if so.
     */
    static class ReportDataUpdateEntry {

        private final Date startDate;
        private final Future future;

        public ReportDataUpdateEntry(@Nonnull final Date startDate, @Nonnull final Future future) {
            this.startDate = startDate;
            this.future = future;
        }

        @Nonnull
        public final Date getStartDate() {
            return startDate;
        }

        @Nonnull
        public final Future getFuture() {
            return future;
        }
    }

    /**
     * A task that can be scheduled in a background thread, to create or update rows in the ReportData table for a
     * given user, starting from a given date and stopping on the present day.
     *
     * In the future this process could be further optimized, to support:
     *
     * [1] Updating records only between a specific date range (e.g. when the user updates an historical Weight record,
     *     only update ReportData from the date of that record to the date of the next known Weight record).
     * [2] Updating records only on a specific single date (e.g. when the user updates the nutritional information for
     *     a custom food, update that user's ReportData rows only for the dates on which that food had been eaten).
     *
     * However, it's expected that by a wide margin the typical use case will only call this task for today's date.
     * The next most common use case would be where the user has gone some number of days without logging in at all,
     * and so it would be a date range ending on today anyway.  Users making edits to older historical records on
     * arbitrary dates should be an edge case, and probably isn't worth adding further complexity to this design.  A
     * main benefit of this design is that it makes it easier to filter out and discard multiple redundant update
     * requests that come through in a short period of time.  Any re-design would need to account for and provide the
     * same benefit.
     */
    class ReportDataUpdateTask implements Runnable {

        private final User user;
        private final Date startDate;

        public ReportDataUpdateTask(
                @Nonnull final User user,
                @Nonnull final Date startDate
        ) {
            this.user = user;
            this.startDate = startDate;
        }

        @Override
        public void run() {
            performReportDataUpdate(user, startDate);
        }
    }

    /**
     * Performs the report data update logic synchronously for the given user and start date.
     */
    private void performReportDataUpdate(@Nonnull final User user, @Nonnull final Date startDate) {
        final Date today = adjustDateForTimeZone(new Date(new java.util.Date().getTime()), ZoneId.of(user.getTimeZone()));
        LocalDate currentDate = startDate.toLocalDate();
        while (currentDate.toString().compareTo(today.toString()) <= 0) {
            LOGGER.info("Creating or updating ReportData record for user [{}] on date [{}]", user.getEmail(), currentDate);
            final Weight mostRecentWeight = weightRepository.findByUserMostRecentOnDate(user, Date.valueOf(currentDate));
            if (mostRecentWeight == null) {
                LOGGER.warn("No weight record found for user [{}] on date [{}]. Skipping ReportData update for this date.", user.getEmail(), currentDate);
                currentDate = currentDate.plusDays(1);
                continue;
            }
            int netCalories = 0;
            double netPoints = 0.0;
            final List<FoodEaten> foodsEaten = foodEatenRepository.findByUserEqualsAndDateEquals(user, Date.valueOf(currentDate));
            for (final FoodEaten foodEaten : foodsEaten) {
                netCalories += foodEaten.getCalories();
                netPoints += foodEaten.getPoints();
            }
            final List<ExercisePerformed> exercisesPerformed = exercisePerformedRepository.findByUserEqualsAndDateEquals(user, Date.valueOf(currentDate));
            for (final ExercisePerformed exercisePerformed : exercisesPerformed) {
                netCalories -= ExerciseService.calculateCaloriesBurned(
                        exercisePerformed.getExercise().getMetabolicEquivalent(),
                        exercisePerformed.getMinutes(),
                        mostRecentWeight.getPounds()
                );
                netPoints -= ExerciseService.calculatePointsBurned(
                        exercisePerformed.getExercise().getMetabolicEquivalent(),
                        exercisePerformed.getMinutes(),
                        mostRecentWeight.getPounds()
                );
            }
            final List<ReportData> existingReportDataList = reportDataRepository.findByUserAndDateOrderByDateAsc(user, Date.valueOf(currentDate));
            if (existingReportDataList.isEmpty()) {
                final ReportData reportData = new ReportData(
                        UUID.randomUUID(),
                        user,
                        Date.valueOf(currentDate),
                        mostRecentWeight.getPounds(),
                        netCalories,
                        netPoints
                );
                reportDataRepository.save(reportData);
            } else {
                final ReportData reportData = existingReportDataList.get(0);
                reportData.setPounds(mostRecentWeight.getPounds());
                reportData.setNetCalories(netCalories);
                reportData.setNetPoints(netPoints);
                reportDataRepository.save(reportData);
            }
            currentDate = currentDate.plusDays(1);
        }
        user.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
        LOGGER.info("ReportData update complete for user [{}] from date [{}] to the day prior to [{}]", user.getEmail(), startDate, currentDate);
    }


}
