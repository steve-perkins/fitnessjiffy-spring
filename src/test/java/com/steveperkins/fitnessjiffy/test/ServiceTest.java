package com.steveperkins.fitnessjiffy.test;

import com.steveperkins.fitnessjiffy.domain.ReportData;
import com.steveperkins.fitnessjiffy.domain.User;
import com.steveperkins.fitnessjiffy.dto.ExerciseDTO;
import com.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import com.steveperkins.fitnessjiffy.dto.FoodDTO;
import com.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import com.steveperkins.fitnessjiffy.dto.ReportDataDTO;
import com.steveperkins.fitnessjiffy.dto.UserDTO;
import com.steveperkins.fitnessjiffy.repository.FoodRepository;
import com.steveperkins.fitnessjiffy.repository.ReportDataRepository;
import com.steveperkins.fitnessjiffy.repository.UserRepository;
import com.steveperkins.fitnessjiffy.service.ExerciseService;
import com.steveperkins.fitnessjiffy.service.FoodService;
import com.steveperkins.fitnessjiffy.service.ReportDataService;
import com.steveperkins.fitnessjiffy.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest extends AbstractTest {

    @Autowired
    UserService userService;

    @Autowired
    FoodService foodService;

    @Autowired
    ExerciseService exerciseService;

    @Autowired
    ReportDataService reportDataService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    ReportDataRepository reportDataRepository;

    @Test
    public void testUserService() {
        // Test get all users
        final List<UserDTO> allUsers = userService.findAllUsers();
        assertEquals(1, allUsers.size());

        // Test get a single user by ID
        final UserDTO user = userService.findUser(allUsers.get(0).getId());
        assertNotNull(user);

        // Test create user
        final UserDTO newUser = new UserDTO(
                UUID.randomUUID(),
                User.Gender.MALE,
                new java.sql.Date(new java.util.Date().getTime()),
                70.0,
                User.ActivityLevel.MODERATELY_ACTIVE,
                "john.doe@fake.com",
                "John",
                "Doe",
                "America/New_York",
                200,
                30,
                2000,
                30
        );
        userService.createUser(newUser, "password");
        assertEquals(2, userService.findAllUsers().size());

        // Test password verification
        assertTrue(userService.verifyPassword(newUser, "password"));
        assertFalse(userService.verifyPassword(newUser, "wrongPassword"));
    }

    @Test
    public void testFoodService() {
        // Test get recently-eaten foods (NOTE: the most recent date in the test data set is 2013-12-12).
        final UserDTO user = userService.findAllUsers().get(0);
        final Calendar december11 = new GregorianCalendar(2013, Calendar.DECEMBER, 11);
        final Date currentDate = new Date(december11.getTimeInMillis());
        final List<FoodDTO> recentFoods = foodService.findEatenRecently(user.getId(), currentDate);
        assertEquals(69, recentFoods.size());

        // Test retrieving foods eaten on a specific date
        List<FoodEatenDTO> eatenOnDecember11 = foodService.findEatenOnDate(user.getId(), currentDate);
        assertEquals(2, eatenOnDecember11.size());

        // Test retrieving a specific food eaten by ID
        final FoodEatenDTO knownFoodEaten = eatenOnDecember11.get(0);
        final FoodEatenDTO copyOfFoodEaten = foodService.findFoodEatenById(knownFoodEaten.getId());
        assertEquals(knownFoodEaten, copyOfFoodEaten);

        // Attempt to add a duplicate food eaten (should not be allowed)
        foodService.addFoodEaten(user.getId(), knownFoodEaten.getFood().getId(), currentDate);
        eatenOnDecember11 = foodService.findEatenOnDate(user.getId(), currentDate);
        assertEquals(2, eatenOnDecember11.size());

        // Add a non-duplicate food eaten
        final Calendar december13 = (Calendar) december11.clone();
        december13.add(Calendar.DATE, 1);
        foodService.addFoodEaten(user.getId(), knownFoodEaten.getFood().getId(), new Date(december13.getTimeInMillis()));
        List<FoodEatenDTO> eatenOnDecember13 = foodService.findEatenOnDate(user.getId(), new Date(december13.getTimeInMillis()));
        assertEquals(1, eatenOnDecember13.size());

        // Update food eaten
        final FoodEatenDTO addedFoodEaten = eatenOnDecember13.get(0);
        final double oldServingQty = addedFoodEaten.getServingQty();
        foodService.updateFoodEaten(addedFoodEaten.getId(), oldServingQty * 2, addedFoodEaten.getServingType());
        eatenOnDecember13 = foodService.findEatenOnDate(user.getId(), new Date(december13.getTimeInMillis()));
        final FoodEatenDTO copyOfAddedFoodEaten = foodService.findFoodEatenById(addedFoodEaten.getId());
        assertEquals(oldServingQty * 2, copyOfAddedFoodEaten.getServingQty());

        // Delete a food eaten
        foodService.deleteFoodEaten(copyOfAddedFoodEaten.getId());
        eatenOnDecember13 = foodService.findEatenOnDate(user.getId(), new Date(december13.getTimeInMillis()));
        assertEquals(0, eatenOnDecember13.size());

        // Search for foods by partial name
        final List<FoodDTO> foodsContainingChicken = foodService.searchFoods(user.getId(), "chicken");
        assertEquals(44, foodsContainingChicken.size());

        // Test creating a user-owned food with the same name as a global food (should be allowed)
        final FoodDTO globalFood = knownFoodEaten.getFood();
        final FoodDTO userOwnedFood = new FoodDTO();
        BeanUtils.copyProperties(globalFood, userOwnedFood);
        userOwnedFood.setId(UUID.randomUUID());
        userOwnedFood.setOwnerId(user.getId());
        String result = foodService.createFood(userOwnedFood, user);
        assertEquals("Success!", result);

        // Test creating a user-owned food with the same name as an existing food owned by that user (should NOT be allowed)
        final FoodDTO userOwnedDuplicate = new FoodDTO();
        BeanUtils.copyProperties(userOwnedFood, userOwnedDuplicate);
        userOwnedDuplicate.setId(UUID.randomUUID());
        result = foodService.createFood(userOwnedDuplicate, user);
        assertEquals("Error:  You already have another customized food with this name.", result);

        // Test updating a food that belongs to a different user (should NOT be allowed)
        final UserDTO additionalUser = new UserDTO();
        BeanUtils.copyProperties(user, additionalUser);
        additionalUser.setId(UUID.randomUUID());
        additionalUser.setEmail("fake@address.com");
        userService.createUser(additionalUser, "password");
        result = foodService.updateFood(userOwnedFood, additionalUser);
        assertEquals("Error:  You are attempting to modify another user's customized food.", result);
        final User additionalUserEntity = userRepository.findById(additionalUser.getId()).orElse(null);
        userRepository.delete(additionalUserEntity);

        // Test updating a food that belongs to this user
        userOwnedFood.setName("Yet Another Non-Duplicate Food Name");
        result = foodService.updateFood(userOwnedFood, user);
        assertEquals("Success!", result);

        // Test updating a global food (should create a copy owned by this user)
        final User userEntity = userRepository.findById(user.getId()).orElse(null);
        final int globalFoodsBefore = foodRepository.findByOwnerIsNull().size();
        final int userOwnedFoodsBefore = foodRepository.findByOwner(userEntity).size();
        final FoodDTO otherGlobalFood = eatenOnDecember11.get(1).getFood();
        result = foodService.updateFood(otherGlobalFood, user);
        assertEquals("Success!", result);
        final int globalFoodsAfter = foodRepository.findByOwnerIsNull().size();
        final int userOwnedFoodsAfter = foodRepository.findByOwner(userEntity).size();
        assertEquals(globalFoodsBefore, globalFoodsAfter);
        assertEquals(userOwnedFoodsBefore + 1, userOwnedFoodsAfter);
    }

    @Test
    public void testExerciseService() throws ParseException {
        // Test retrieving exercises performed on a specific date
        final UserDTO user = userService.findAllUsers().get(0);
        final Date exercisePerformedDate = new Date(simpleDateFormat.parse("2012-06-30").getTime());
        final List<ExercisePerformedDTO> exercisePerformedDTOs = exerciseService.findPerformedOnDate(user.getId(), exercisePerformedDate);
        assertEquals(1, exercisePerformedDTOs.size());

        // Test retrieving exercises performed during a date range
        final List<ExerciseDTO> exerciseRangeList = exerciseService.findPerformedRecently(user.getId(), exercisePerformedDate);
        assertEquals(4, exerciseRangeList.size());
    }

    @Test
    public void testReportDataService() throws ParseException, ExecutionException, InterruptedException {
        // NOTE:  In the test dataset, the earliest date on which the test user has raw data is '2007-11-21', and the most recent date is '2013-12-11'.

        // Test generating report data for the past week.
        final User user = userRepository.findAll().iterator().next();
        final Calendar oneWeekAgo = new GregorianCalendar();
        oneWeekAgo.add(Calendar.DATE, -6);
        final Future lastWeekUpdate = reportDataService.updateUserFromDate(user, new Date(oneWeekAgo.getTime().getTime()));
        lastWeekUpdate.get();

        // Test retrieving all data for this user.
        final List<ReportDataDTO> lastWeekReportData = reportDataService.findByUser(user.getId());
        assertEquals(7, lastWeekReportData.size());

        // Test retrieving a single date's data.
        final List<ReportData> firstDayOfLastWeekReportData = reportDataRepository.findByUserAndDateOrderByDateAsc(user, new Date(oneWeekAgo.getTime().getTime()));
        assertEquals(1, firstDayOfLastWeekReportData.size());

        // Test retrieving report data between a specified date range
        final Calendar thirdDayOfLastWeek = (Calendar) oneWeekAgo.clone();
        thirdDayOfLastWeek.add(Calendar.DATE, 2);
        final List<ReportData> firstThreeDaysOfLastWeekReportData = reportDataRepository.findByUserAndDateBetweenOrderByDateAsc(
                user,
                new Date(oneWeekAgo.getTime().getTime()),
                new Date(thirdDayOfLastWeek.getTime().getTime())
        );
        assertEquals(3, firstThreeDaysOfLastWeekReportData.size());

        // Schedule an update starting from a month ago, but immediately move on without blocking.  This scheduled update
        // should be superseded and canceled by the one that follows next.
        final Calendar oneMonthAgo = new GregorianCalendar();
        oneMonthAgo.add(Calendar.MONTH, -1);
        final Future shouldGetCanceledUpdate = reportDataService.updateUserFromDate(user, new Date(oneMonthAgo.getTimeInMillis()));

        // Generate report data across all good dates for the test user.  This tests the actual number-crunching (because
        // the data entries generated above were for dates that didn't actually have any data), and well as the update
        // operation (because the "last week" entries that were just created will now be re-written).
        final Date firstDateWithGoodData = new Date(simpleDateFormat.parse("2007-11-21").getTime());
        final Date lastDateWithGoodData = new Date(simpleDateFormat.parse("2013-12-11").getTime());
        final Future allTimeUpdate = reportDataService.updateUserFromDate(user, firstDateWithGoodData);
        allTimeUpdate.get();

        assertTrue(shouldGetCanceledUpdate.isCancelled());

        final List<ReportData> allGoodReportData = reportDataRepository.findByUserAndDateBetweenOrderByDateAsc(user, firstDateWithGoodData, lastDateWithGoodData);
        assertEquals(2213, allGoodReportData.size());

        final List<ReportDataDTO> allReportData = reportDataService.findByUser(user.getId());
        assertTrue(allReportData.size() > 2213);
    }

}
