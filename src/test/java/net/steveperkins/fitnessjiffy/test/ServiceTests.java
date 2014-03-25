package net.steveperkins.fitnessjiffy.test;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
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
    UserRepository userRepository;

    @Autowired
    FoodRepository foodRepository;

    @Test
    public void testUserService() {
        // Test get all users
        List<UserDTO> allUsers = userService.getAllUsers();
        assertEquals(1, allUsers.size());

        // Test get a single user by ID
        UserDTO user = userService.getUser(allUsers.get(0).getId());
        assertNotNull(user);
    }

    @Test
    public void testFoodService() {
        // Test get recently-eaten foods (NOTE: the most recent date in the test data set is 2013-12-12).
        UserDTO user = userService.getAllUsers().get(0);
        Calendar december12 = new GregorianCalendar(2013, 11, 12);
        Date currentDate = new Date(december12.getTimeInMillis());
        List<FoodDTO> recentFoods = foodService.findEatenRecently(user.getId(), currentDate);
        assertEquals(69, recentFoods.size());

        // Test retrieving foods eaten on a specific date
        List<FoodEatenDTO> eatenOnDecember12 = foodService.findEatenOnDate(user.getId(), currentDate);
        assertEquals(2, eatenOnDecember12.size());

        // Test retrieving a specific food eaten by ID
        FoodEatenDTO knownFoodEaten = eatenOnDecember12.get(0);
        FoodEatenDTO copyOfFoodEaten = foodService.findFoodEatenById(knownFoodEaten.getId());
        assertEquals(knownFoodEaten, copyOfFoodEaten);

        // Attempt to add a duplicate food eaten (should not be allowed)
        foodService.addFoodEaten(user.getId(), knownFoodEaten.getFood().getId(), currentDate);
        eatenOnDecember12 = foodService.findEatenOnDate(user.getId(), currentDate);
        assertEquals(2, eatenOnDecember12.size());

        // Add a non-duplicate food eaten
        Calendar december13 = (Calendar) december12.clone();
        december13.add(Calendar.DATE, 1);
        foodService.addFoodEaten(user.getId(), knownFoodEaten.getFood().getId(), new Date(december13.getTimeInMillis()));
        List<FoodEatenDTO> eatenOnDecember13 = foodService.findEatenOnDate(user.getId(), new Date(december13.getTimeInMillis()));
        assertEquals(1, eatenOnDecember13.size());

        // Update food eaten
        FoodEatenDTO addedFoodEaten = eatenOnDecember13.get(0);
        double oldServingQty = addedFoodEaten.getServingQty();
        foodService.updateFoodEaten(addedFoodEaten.getId(), oldServingQty * 2, addedFoodEaten.getServingType());
        eatenOnDecember13 = foodService.findEatenOnDate(user.getId(), new Date(december13.getTimeInMillis()));
        FoodEatenDTO copyOfAddedFoodEaten = foodService.findFoodEatenById(addedFoodEaten.getId());
        assertEquals(oldServingQty * 2, copyOfAddedFoodEaten.getServingQty());

        // Delete a food eaten
        foodService.deleteFoodEaten(copyOfAddedFoodEaten.getId());
        eatenOnDecember13 = foodService.findEatenOnDate(user.getId(), new Date(december13.getTimeInMillis()));
        assertEquals(0, eatenOnDecember13.size());

        // Search for foods by partial name
        List<FoodDTO> foodsContainingChicken = foodService.searchFoods(user.getId(), "chicken");
        assertEquals(44, foodsContainingChicken.size());

        // Test creating a user-owned food with the same name as a global food (should be allowed)
        FoodDTO globalFood = knownFoodEaten.getFood();
        FoodDTO userOwnedFood = new FoodDTO();
        BeanUtils.copyProperties(globalFood, userOwnedFood);
        userOwnedFood.setId(UUID.randomUUID());
        userOwnedFood.setOwnerId(user.getId());
        String result = foodService.createFood(userOwnedFood, user);
        assertEquals("Success!", result);

        // Test creating a user-owned food with the same name as an existing food owned by that user (should NOT be allowed)
        FoodDTO userOwnedDuplicate = new FoodDTO();
        BeanUtils.copyProperties(userOwnedFood, userOwnedDuplicate);
        userOwnedDuplicate.setId(UUID.randomUUID());
        result = foodService.createFood(userOwnedDuplicate, user);
        assertEquals("Error:  You already have another customized food with this name.", result);

        // Test updating a food that belongs to a different user (should NOT be allowed)
        UserDTO additionalUser = new UserDTO();
        BeanUtils.copyProperties(user, additionalUser);
        additionalUser.setId(UUID.randomUUID());
        additionalUser.setUsername("fake");
        userService.createUser(additionalUser);
        result = foodService.updateFood(userOwnedFood, additionalUser);
        assertEquals("Error:  You are attempting to modify another user's customized food.", result);
        User additionalUserEntity = userRepository.findOne(additionalUser.getId());
        userRepository.delete(additionalUserEntity);

        // Test updating a food that belongs to this user
        userOwnedFood.setName("Yet Another Non-Duplicate Food Name");
        result = foodService.updateFood(userOwnedFood, user);
        assertEquals("Success!", result);

        // Test updating a global food (should create a copy owned by this user)
        User userEntity = userRepository.findOne(user.getId());
        int globalFoodsBefore = foodRepository.findByOwnerIsNull().size();
        int userOwnedFoodsBefore = foodRepository.findByOwner(userEntity).size();
        FoodDTO otherGlobalFood = eatenOnDecember12.get(1).getFood();
        result = foodService.updateFood(otherGlobalFood, user);
        assertEquals("Success!", result);
        int globalFoodsAfter = foodRepository.findByOwnerIsNull().size();
        int userOwnedFoodsAfter = foodRepository.findByOwner(userEntity).size();
        assertEquals(globalFoodsBefore, globalFoodsAfter);
        assertEquals(userOwnedFoodsBefore + 1, userOwnedFoodsAfter);
    }

}
