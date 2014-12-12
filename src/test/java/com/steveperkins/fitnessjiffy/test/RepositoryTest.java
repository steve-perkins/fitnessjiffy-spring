package com.steveperkins.fitnessjiffy.test;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;

import com.steveperkins.fitnessjiffy.domain.Exercise;
import com.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import com.steveperkins.fitnessjiffy.domain.Food;
import com.steveperkins.fitnessjiffy.domain.FoodEaten;
import com.steveperkins.fitnessjiffy.domain.ReportData;
import com.steveperkins.fitnessjiffy.domain.User;
import com.steveperkins.fitnessjiffy.domain.Weight;
import com.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import com.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import com.steveperkins.fitnessjiffy.repository.FoodRepository;
import com.steveperkins.fitnessjiffy.repository.ReportDataRepository;
import com.steveperkins.fitnessjiffy.repository.UserRepository;
import com.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryTest extends AbstractTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private FoodEatenRepository foodEatenRepository;

    @Autowired
    private ExercisePerformedRepository exercisePerformedRepository;

    @Autowired
    private ReportDataRepository reportDataRepository;

    @Test
    public void testUserRepository() {
        // Test that the database is already populated with one user
        final User existingUser = userRepository.findAll().iterator().next();
        assertNotNull(existingUser);

        // Test creation of a new user
        final UUID userId = UUID.randomUUID();
        final User newUser = new User(
                userId,
                User.Gender.MALE,
                new Date(new java.util.Date().getTime()),
                70,
                User.ActivityLevel.SEDENTARY,
                "fake@address.com",
                null,
                "Jane",
                "Doe",
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(new java.util.Date().getTime())
        );
        userRepository.save(newUser);
        final User retrievedNewUser = userRepository.findOne(userId);
        assertNotNull(retrievedNewUser);

        // Test update of a user
        newUser.setLastName("Married");
        userRepository.save(newUser);
        final User retrievedUpdatedUser = userRepository.findOne(userId);
        assertEquals("Married", retrievedUpdatedUser.getLastName());

        // Test removal of a user
        userRepository.delete(newUser);
        assertNull(userRepository.findOne(userId));
        assertEquals(1, userRepository.count());
    }

    @Test
    public void testWeightRepository() {
        final User user = userRepository.findAll().iterator().next();
        assertNotNull(user);

        final Date today = new Date(new java.util.Date().getTime());
        final Weight mostRecentWeight = weightRepository.findByUserMostRecentOnDate(user, today);
        assertNotNull(mostRecentWeight);

        final Weight todaysWeight = weightRepository.findByUserAndDate(user, today);
        assertNull(todaysWeight);
    }

    @Test
    public void testFoodRepository() {
        // Test that the database is already populated with global foods
        List<Food> globalFoods = foodRepository.findByOwnerIsNull();
        assertEquals(419, globalFoods.size());

        // Test that the database has at least one user, but no user-owned foods
        final User existingUser = userRepository.findAll().iterator().next();
        assertNotNull(existingUser);
        List<Food> userFoods = foodRepository.findByOwner(existingUser);
        assertEquals(0, userFoods.size());

        // Test creating a global food
        final Food globalFood = globalFoods.get(0);
        final Food globalCopyFood = new Food(
                UUID.randomUUID(),
                null,
                "Test Global Food",
                globalFood.getDefaultServingType(),
                globalFood.getServingTypeQty(),
                globalFood.getCalories(),
                globalFood.getFat(),
                globalFood.getSaturatedFat(),
                globalFood.getCarbs(),
                globalFood.getFiber(),
                globalFood.getSugar(),
                globalFood.getProtein(),
                globalFood.getSodium(),
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(new java.util.Date().getTime())
        );
        foodRepository.save(globalCopyFood);
        globalFoods = foodRepository.findByOwnerIsNull();
        assertEquals(420, globalFoods.size());

        // Test creating user-owned food, with and without names that match a global food name
        final Food userCopyFood = new Food(
                UUID.randomUUID(),
                existingUser,
                globalFood.getName(),
                globalFood.getDefaultServingType(),
                globalFood.getServingTypeQty(),
                globalFood.getCalories(),
                globalFood.getFat(),
                globalFood.getSaturatedFat(),
                globalFood.getCarbs(),
                globalFood.getFiber(),
                globalFood.getSugar(),
                globalFood.getProtein(),
                globalFood.getSodium(),
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(new java.util.Date().getTime())
        );
        final Food userNewFood = new Food(
                UUID.randomUUID(),
                existingUser,
                "Test User-Owned Food",
                globalFood.getDefaultServingType(),
                globalFood.getServingTypeQty(),
                globalFood.getCalories(),
                globalFood.getFat(),
                globalFood.getSaturatedFat(),
                globalFood.getCarbs(),
                globalFood.getFiber(),
                globalFood.getSugar(),
                globalFood.getProtein(),
                globalFood.getSodium(),
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(new java.util.Date().getTime())
        );
        foodRepository.save(userCopyFood);
        foodRepository.save(userNewFood);
        final List<Food> userOnlyFoods = foodRepository.findByOwner(existingUser);
        assertEquals(2, userOnlyFoods.size());

        // Test that user-owned foods do not show up as global...
        globalFoods = foodRepository.findByOwnerIsNull();
        assertEquals(420, globalFoods.size());

        // ... and that global foods with the same name as user-owned food are excluded from the list of foods visible
        // to that user
        userFoods = foodRepository.findVisibleByOwner(existingUser);
        assertEquals(421, userFoods.size());
        assertEquals(422, foodRepository.count());

        // Test that name-collisions for user-owned foods are detected
        final List<Food> foodsWithNameCollision = foodRepository.findByOwnerEqualsAndNameEquals(existingUser, "Test User-Owned Food");
        assertEquals(1, foodsWithNameCollision.size());

        // Test delete food
        foodRepository.delete(globalCopyFood);
        foodRepository.delete(userCopyFood);
        assertEquals(420, foodRepository.count());
    }

    @Test
    public void testFoodEatenRepository() throws ParseException {
        // Grab the first test user
        final List<User> userList = new LinkedList<>();
        final Iterable<User> usersIterator = userRepository.findAll();
        for (final User user : usersIterator) {
            userList.add(user);
        }
        assertEquals(1, userList.size());
        final User existingUser = userList.get(0);
        assertNotNull(existingUser);

        // Confirm that they have foods eaten...
        final List<FoodEaten> allFoodsEaten = foodEatenRepository.findByUserEqualsOrderByDateAsc(existingUser);
        assertEquals(19307, allFoodsEaten.size());
        final FoodEaten earliestFoodEaten = allFoodsEaten.get(0);
        assertEquals(new Date(simpleDateFormat.parse("2008-01-22").getTime()), earliestFoodEaten.getDate());
        assertEquals("Ham (lean only)", earliestFoodEaten.getFood().getName());

        // ... and that we can detect the earliest date on which a particular food was first eaten.
        final List<FoodEaten> hamEatenRecords = foodEatenRepository.findByUserEqualsAndFoodEqualsOrderByDateAsc(
                existingUser,
                earliestFoodEaten.getFood()
        );
        assertEquals(new Date(simpleDateFormat.parse("2008-01-22").getTime()), hamEatenRecords.get(0).getDate());

        // Test "recently eaten foods" query
        final Date currentDate = new Date(simpleDateFormat.parse("2013-12-01").getTime());
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -14);
        final Date twoWeeksAgo = new Date(calendar.getTime().getTime());
        final List<Food> recentFoods = foodEatenRepository.findByUserEatenWithinRange(
                existingUser,
                new Date(twoWeeksAgo.getTime()),
                new Date(currentDate.getTime())
        );
        assertEquals(71, recentFoods.size());

        // Test a save
        final FoodEaten foodEaten = foodEatenRepository.findAll().iterator().next();
        final FoodEaten copyFoodEaten = new FoodEaten(
                UUID.randomUUID(),
                existingUser,
                foodEaten.getFood(),
                new Date(new java.util.Date().getTime()),
                foodEaten.getServingType(),
                foodEaten.getServingQty()
        );
        foodEatenRepository.save(copyFoodEaten);
        assertEquals(19308, foodEatenRepository.count());

        // Test delete
        foodEatenRepository.delete(copyFoodEaten);
        assertEquals(19307, foodEatenRepository.count());
    }

    @Test
    public void testExercisePerformedRepository() throws ParseException {
        // Grab the first test user, and confirm that they have exercises performed
        final List<User> userList = new LinkedList<>();
        final Iterable<User> usersIterator = userRepository.findAll();
        for (final User user : usersIterator) {
            userList.add(user);
        }
        assertEquals(1, userList.size());
        final User existingUser = userList.get(0);
        assertNotNull(existingUser);
        assertEquals(518, exercisePerformedRepository.count());

        // Test "exercises performed on a date" query
        final Date exercisePerformedDate = new Date(simpleDateFormat.parse("2012-06-30").getTime());
        final List<ExercisePerformed> exercisePerformedList = exercisePerformedRepository.findByUserEqualsAndDateEquals(existingUser, exercisePerformedDate);
        assertEquals(1, exercisePerformedList.size());

        // Text "exercises performed within a date range" query
        final Date exercisePerformedStartDate = new Date(simpleDateFormat.parse("2012-06-15").getTime());
        final Date exercisePerformedEndDate = new Date(simpleDateFormat.parse("2012-06-30").getTime());
        final List<Exercise> exerciseRangeList = exercisePerformedRepository.findByUserPerformedWithinRange(existingUser, exercisePerformedStartDate, exercisePerformedEndDate);
        assertEquals(4, exerciseRangeList.size());

        // Test a save
        final ExercisePerformed newExercisePerformed = exercisePerformedList.get(0);
        newExercisePerformed.setId(UUID.randomUUID());
        newExercisePerformed.setDate(new Date(simpleDateFormat.parse("2014-06-30").getTime()));
        exercisePerformedRepository.save(newExercisePerformed);
        assertEquals(519, exercisePerformedRepository.count());

        // Test a delete
        exercisePerformedRepository.delete(newExercisePerformed);
        assertEquals(518, exercisePerformedRepository.count());
    }

    @Test
    public void testReportDataRepository() {
        final User user = userRepository.findAll().iterator().next();

        // Test an insert, update, find, and delete
        final ReportData todayData = new ReportData(UUID.randomUUID(), user, new Date(System.currentTimeMillis()), 200.0, 2000, 30);
        reportDataRepository.save(todayData);
        assertEquals(1, reportDataRepository.findByUserOrderByDateAsc(user).size());

        todayData.setNetPoints(35.0);
        reportDataRepository.save(todayData);
        assertEquals(1, reportDataRepository.findByUserOrderByDateAsc(user).size());

        reportDataRepository.delete(todayData);
        assertEquals(0, reportDataRepository.findByUserOrderByDateAsc(user).size());

        // Create records across a couple of weeks, and test retrieving on a specific date and across a date range
        final Calendar dateCursor = new GregorianCalendar();
        dateCursor.add(Calendar.DATE, -14);
        while (new Date(dateCursor.getTimeInMillis()).compareTo(new Date(System.currentTimeMillis())) < 0) { //NOPMD
            final ReportData reportData = new ReportData(UUID.randomUUID(), user, new Date(dateCursor.getTimeInMillis()), 200.0, 2000, 30); //NOPMD
            reportDataRepository.save(reportData);
            dateCursor.add(Calendar.DATE, 1);
        }

        final Calendar twoWeeksAgo = new GregorianCalendar();
        twoWeeksAgo.add(Calendar.DATE, -14);
        final List<ReportData> specificDateReportData = reportDataRepository.findByUserAndDateOrderByDateAsc(
                user,
                new Date(twoWeeksAgo.getTimeInMillis())
        );
        assertEquals(1, specificDateReportData.size());

        final Calendar oneWeekFromStart = (Calendar) twoWeeksAgo.clone();
        oneWeekFromStart.add(Calendar.DATE, 6);  // "Between" clauses are inclusive, so effectively a date range is zero-indexed.  A 7-day range is Mon-Sun... *not* Mon-Mon.
        final List<ReportData> dateRangeReportData = reportDataRepository.findByUserAndDateBetweenOrderByDateAsc(
                user,
                new Date(twoWeeksAgo.getTimeInMillis()),
                new Date(oneWeekFromStart.getTimeInMillis())
        );
        assertEquals(7, dateRangeReportData.size());
    }

}
