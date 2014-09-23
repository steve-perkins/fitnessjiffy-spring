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

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
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

    private ScheduledThreadPoolExecutor reportDataUpdateThreadPool = new ScheduledThreadPoolExecutor(1);

    /**
     * Update the ReportData records for a given user, starting on a given date and ending after today's date (in the
     * most common use case, it will be a one-day range consisting of today anyway).
     */
    public void updateUserFromDate(
            @Nonnull final UUID userId,
            @Nonnull final Date date
    ) {
        final User user = userRepository.findOne(userId);
        final ReportDataUpdateTask task = new ReportDataUpdateTask(user, date);

        // Start by determining whether or not there is already a task scheduled for this user.  The "BlockingQueue.contains()"
        // method uses a hash to check for matches, and is very fast.  If this approach finds no match, then we can schedule
        // this task to run in five minutes...
        if (!reportDataUpdateThreadPool.getQueue().contains(task)) {
            reportDataUpdateThreadPool.schedule(task, 5, TimeUnit.MINUTES);
        } else {

            // ... but if there IS already a task scheduled for this user, then we have to examine its starting date.
            // If this date range of this new task is no wider than that of the task already scheduled, then we can
            // return and let the already-scheduled task handle it.  However, if this new task covers a wider date range,
            // then it must be scheduled instead.
            //
            // Unfortunately, checking the date of the already-scheduled task requires a reference to that object, which
            // is more expensive to obtain than the "contains()" method uses in the first screen.  To get an object
            // reference, we have to iterate over (a copy of) the contents of the queue.
            final Iterator queueIterator = reportDataUpdateThreadPool.getQueue().iterator();
            while (queueIterator.hasNext()) {
                final Object objectInQueue = queueIterator.next();
                if (objectInQueue instanceof ReportDataUpdateTask) {
                    final ReportDataUpdateTask taskInQueue = (ReportDataUpdateTask) objectInQueue;
                    if (date.compareTo(taskInQueue.startDate) < 0) {

                        // The new task covers a wider date range than the already-scheduled task!

                        // Try to remove the previously scheduled task from the queue.  There is a slim chance that
                        // in the few microseconds we spent making a copy of the queue's contents and iterating over
                        // it, that task has already been removed from the queue and executed anyway.  That's not
                        // ideal, but it isn't a huge problem... it just means that a bit of work will be duplicated
                        // for that user.
                        reportDataUpdateThreadPool.getQueue().remove(taskInQueue);

                        reportDataUpdateThreadPool.schedule(task, 5, TimeUnit.MINUTES);
                    }
                }
            }
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
     * requests that come through in a short period of time.  Any re-design would need to accound for and provide the
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
            final Date today = new Date(new LocalDate().toDateTimeAtStartOfDay().getMillis());
            Date currentDate = startDate;

            // Iterate through all dates from the start date through today.
            while (currentDate.compareTo(today) <= 0) {

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
        }

        /**
         * The overridden "equals()" method deliberately ignores everything except the "user" field.  That's because
         * this class is meant to be placed in the "BlockingQueue" component of a "ScheduledThreadPoolExecutor" thread
         * pool, and we only want the thread pool to contain one task for any given user at any one time.  We rely
         * on "equals()" to detect user-collisions, so that we can then examine the "startDate" fields and determine
         * which task should supersede the other.
         */
        @Override
        public boolean equals(final Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            final ReportDataUpdateTask that = (ReportDataUpdateTask) other;

            if (!user.equals(that.user)) return false;

            return true;
        }

        /**
         * The overridden "hashCode()" method deliberately ignores everything except the "user" field.  That's because
         * this class is meant to be placed in the "BlockingQueue" component of a "ScheduledThreadPoolExecutor" thread
         * pool, and we only want the thread pool to contain one task for any given user at any one time.  We rely
         * on "hashCode()" to detect user-collisions, so that we can then examine the "startDate" fields and determine
         * which task should supersede the other.
         */
        @Override
        public int hashCode() {
            return user.hashCode();
        }
    }
}
