package net.steveperkins.fitnessjiffy.test;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.*;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.service.ExerciseService;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.*;

public class ServiceTests extends AbstractTests {

    @Autowired
    UserService userService;

    @Autowired
    FoodService foodService;

    @Autowired
    ExerciseService exerciseService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FoodRepository foodRepository;

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
        final User additionalUserEntity = userRepository.findOne(additionalUser.getId());
        userRepository.delete(additionalUserEntity);

        // Test updating a food that belongs to this user
        userOwnedFood.setName("Yet Another Non-Duplicate Food Name");
        result = foodService.updateFood(userOwnedFood, user);
        assertEquals("Success!", result);

        // Test updating a global food (should create a copy owned by this user)
        final User userEntity = userRepository.findOne(user.getId());
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

}
