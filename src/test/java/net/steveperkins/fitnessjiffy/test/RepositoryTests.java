package net.steveperkins.fitnessjiffy.test;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;

import static junit.framework.TestCase.*;

import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryTests extends AbstractTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private FoodEatenRepository foodEatenRepository;

	@Test
	public void testUserRepository() {
        // Test that the database is already populated with one user
        User existingUser = userRepository.findAll().iterator().next();
        assertNotNull(existingUser);

        // Test creation of a new user
        UUID userId = UUID.randomUUID();
        User newUser = new User(
                userId,
                User.Gender.MALE,
                30,
                70,
                User.ActivityLevel.SEDENTARY,
                "username",
                "password",
                "Jane",
                "Doe",
                true
        );
        userRepository.save(newUser);
        User retrievedNewUser = userRepository.findOne(userId);
        assertEquals(newUser, retrievedNewUser);

        // Test update of a user
        newUser.setLastName("Married");
        userRepository.save(newUser);
        User retrievedUpdatedUser = userRepository.findOne(userId);
        assertEquals(newUser, retrievedUpdatedUser);

        // Test removal of a user
        userRepository.delete(newUser);
        assertNull(userRepository.findOne(userId));
        assertEquals(1, userRepository.count());
	}

    @Test
    public void testFoodRepository() {
        // Test that the database is already populated with global foods
        List<Food> globalFoods = foodRepository.findByOwnerIsNull();
        assertEquals(419, globalFoods.size());

        // Test that the database has at least one user, but no user-owned foods
        User existingUser = userRepository.findAll().iterator().next();
        assertNotNull(existingUser);
        List<Food> userFoods = foodRepository.findByOwner(existingUser);
        assertEquals(0, userFoods.size());

        // Test creating a global food
        Food globalFood = globalFoods.get(0);
        Food globalCopyFood = new Food(
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
                globalFood.getSodium()
        );
        foodRepository.save(globalCopyFood);
        globalFoods = foodRepository.findByOwnerIsNull();
        assertEquals(420, globalFoods.size());

        // Test creating user-owned food, with and without names that match a global food name
        Food userCopyFood = new Food(
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
                globalFood.getSodium()
        );
        Food userNewFood = new Food(
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
                globalFood.getSodium()
        );
        foodRepository.save(userCopyFood);
        foodRepository.save(userNewFood);
        List<Food> userOnlyFoods = foodRepository.findByOwner(existingUser);
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
        List<Food> foodsWithNameCollision = foodRepository.findByOwnerEqualsAndNameEquals(existingUser, "Test User-Owned Food");
        assertEquals(1, foodsWithNameCollision.size());

        // Test delete food
        foodRepository.delete(globalCopyFood);
        foodRepository.delete(userCopyFood);
        assertEquals(420, foodRepository.count());
    }
	
    @Test
    public void testFoodEatenRepository() throws ParseException {
        // Grab the first test user, and confirm that they have foods eaten
        User existingUser = userRepository.findAll().iterator().next();
        assertNotNull(existingUser);
        assertEquals(19307, foodEatenRepository.count());

        // Test "recently eaten foods" query
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date(simpleDateFormat.parse("2013-12-01").getTime());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -14);
        Date twoWeeksAgo = new Date(calendar.getTime().getTime());
        List<Food> recentFoods = foodEatenRepository.findByUserEatenWithinRange(
                existingUser,
                new java.sql.Date(twoWeeksAgo.getTime()),
                new java.sql.Date(currentDate.getTime())
        );
        TestCase.assertEquals(72, recentFoods.size());

        // Test a save
        FoodEaten foodEaten = foodEatenRepository.findAll().iterator().next();
        FoodEaten copyFoodEaten = new FoodEaten(
                UUID.randomUUID(),
                existingUser,
                foodEaten.getFood(),
                new java.sql.Date(new java.util.Date().getTime()),
                foodEaten.getServingType(),
                foodEaten.getServingQty()
        );
        foodEatenRepository.save(copyFoodEaten);
        assertEquals(19308, foodEatenRepository.count());

        // Test delete
        foodEatenRepository.delete(copyFoodEaten);
        assertEquals(19307, foodEatenRepository.count());
    }
	
}
