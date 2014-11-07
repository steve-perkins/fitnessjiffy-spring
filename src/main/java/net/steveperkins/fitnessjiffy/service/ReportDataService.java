package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.ReportData;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import net.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.ReportDataRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReportDataService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private FoodEatenRepository foodEatenRepository;

    @Autowired
    private ExercisePerformedRepository exercisePerformedRepository;

    @Autowired
    private ReportDataRepository reportDataRepository;

    /**
     * By default, update tasks should be scheduled for 5 minutes in the future (i.e. 300000 milliseconds).  However,
     * this can be overwritten in the "application.properties" config file... primarily so that unit tests can use
     * a much shorter value.
     */
    @Value("${reportdata.update-delay-in-millis:300000}")
    private long scheduleDelayInMillis;

    /**
     * By default, a background thread should prune outdated entries from the "scheduledUserUpdates" map once every
     * hour.  This can be overwritten in the "application.properties" config file, primarily so that unit tests can
     * use a much shorter value.
     */
    @Value("${reportdata.cleanup-frequency-in-millis:3600000}")
    private long cleanupFrequencyInMillis;

    private final ScheduledThreadPoolExecutor reportDataUpdateThreadPool = new ScheduledThreadPoolExecutor(1);
    private final Map<UUID, ReportDataUpdateEntry> scheduledUserUpdates = new ConcurrentHashMap<>();

    /**
     * The constructor spawns a background thread, which periodically iterates through the "scheduledUserUpdates" map
     * and prunes outdated entries.
     */
    public ReportDataService() {
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
                        Thread.sleep(cleanupFrequencyInMillis);
                    } catch (InterruptedException e) {
                        System.out.println("Exception thrown while sleeping in between runs of the ReportData cleanup thread");
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(backgroundCleanupThread).start();
    }

    /**
     * Update the ReportData records for a given user, starting on a given date and ending after today's date (in the
     * most common use case, it will be a one-day range consisting of today anyway).
     */
    public synchronized final Future updateUserFromDate(
            @Nonnull final UUID userId,
            @Nonnull final Date date
    ) {
        final ReportDataUpdateEntry existingEntry = scheduledUserUpdates.get(userId);
        if (existingEntry != null) {
            if (existingEntry.getFuture().isCancelled() || existingEntry.getFuture().isDone()) {
                // There was an update recently scheduled for this user, but it has completed and its entry can be cleaned up.
                scheduledUserUpdates.remove(userId);
            } else if (existingEntry.getStartDate().after(date)) {
                // There is an update still pending for this user, but its date range is superseded by that of the new
                // update and therefore can be cancelled and cleaned up.
                existingEntry.getFuture().cancel(false);
                scheduledUserUpdates.remove(userId);
            } else {
                // There is an update still pending for this user, and it supersedes the new one here.  Do nothing, and
                // let the pending schedule stand.
                return null;
            }
        }

        // Schedule an update for this user, and add an entry in the conflicts list.
        final User user = userRepository.findOne(userId);
        System.out.printf("Scheduling a ReportData update for user [%s] from date [%s] in %d milliseconds%n", user.getEmail(), date, scheduleDelayInMillis);
        final ReportDataUpdateTask task = new ReportDataUpdateTask(user, date);
        final Future future = reportDataUpdateThreadPool.schedule(task, scheduleDelayInMillis, TimeUnit.MILLISECONDS);
        final ReportDataUpdateEntry newUpdateEntry = new ReportDataUpdateEntry(date, future);
        scheduledUserUpdates.put(userId, newUpdateEntry);
        return future;
    }

    public synchronized final boolean isIdle() {
        System.out.printf("%d active threads, %d queued tasks%n", reportDataUpdateThreadPool.getActiveCount(), scheduledUserUpdates.size());
        return reportDataUpdateThreadPool.getActiveCount() == 0 && scheduledUserUpdates.isEmpty();
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
            final Date today = new Date(System.currentTimeMillis());
            Date currentDate = startDate;

            // Iterate through all dates from the start date through today.
            while (currentDate.toString().compareTo(today.toString()) <= 0) {  // TODO: Using string-based comparison rather than true object-level comparison, because of time zone wonkiness that needs to be sorted out across the board.

                System.out.printf("Creating or updating ReportData record for user [%s] on date [%s]%n", user.getEmail(), currentDate);

                // Get the user's weight on this date, and initialize accumulator variables to hold this date's net calories and net points.
                final Weight mostRecentWeight = weightRepository.findByUserMostRecentOnDate(user, currentDate);
                int netCalories = 0;
                double netPoints = 0.0;

                // Iterate over all foods eaten on this date, updating the net calories and net points.
                final List<FoodEaten> foodsEaten = foodEatenRepository.findByUserEqualsAndDateEquals(user, currentDate);
                for (final FoodEaten foodEaten : foodsEaten) {
                    netCalories += foodEaten.getCalories();
                    netPoints += foodEaten.getPoints();
                }

                // Iterator over all exercises performed on this date, updating the net calories and net points.
                final List<ExercisePerformed> exercisesPerformed = exercisePerformedRepository.findByUserEqualsAndDateEquals(user, currentDate);
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

                // Create a ReportData entry for this date if none already exists, or else updating the existing record for this date.
                final List<ReportData> existingReportDataList = reportDataRepository.findByUserAndDate(user, currentDate);
                if (existingReportDataList.isEmpty()) {
                    final ReportData reportData = new ReportData(//NOPMD
                            UUID.randomUUID(),
                            user,
                            currentDate,
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

                // Increment the date to the next day.
                currentDate = new Date(//NOPMD
                        new LocalDate(currentDate.getTime(), DateTimeZone.UTC).plusDays(1).toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis()
                );
            }

            user.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);

            System.out.printf("ReportData update complete for user [%s] from date [%s] to the day prior to [%s]%n", user.getEmail(), startDate, currentDate);
        }
    }

}
