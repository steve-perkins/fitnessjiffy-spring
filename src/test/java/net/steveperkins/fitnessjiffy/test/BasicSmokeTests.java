package net.steveperkins.fitnessjiffy.test;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import net.steveperkins.fitnessjiffy.Application;
import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.User;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertNotNull;

import net.steveperkins.fitnessjiffy.etl.model.Datastore;
import net.steveperkins.fitnessjiffy.etl.writer.H2Writer;
import net.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import net.steveperkins.fitnessjiffy.repository.ExerciseRepository;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {Application.class})
public class BasicSmokeTests {

    /**
     * The database setup needs to happen only once, so normally we would use a JUnit method annotated with @BeforeClass
     * rather than @Before.  However, @BeforeClass methods must be static, and Spring can only inject the necessary DataSource
     * object into an instance variable.  So we use this static "flag" variable here, to ensure that the @Before method
     * only populates the database once.
     */
    private static boolean databasePopulated = false;

    @Autowired
    DataSource dataSource;

    @Autowired
    private ExercisePerformedRepository exercisePerformedRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private FoodEatenRepository foodEatenRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightRepository weightRepository;

    private final String CURRENT_WORKING_DIRECTORY = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

    @Before
    public void before() throws Exception {
        if(!databasePopulated) {
            Datastore datastore = Datastore.fromJSONFile( new File(CURRENT_WORKING_DIRECTORY + "testdata.json") );
            try ( Connection connection = dataSource.getConnection() ) {
                new H2Writer(connection, datastore).write();
                databasePopulated = true;
            }
        }
    }

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
        int count = 0;
        for(User user : userRepository.findAll()) count++;
        assertEquals(count, 1);
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

        // Test creating a user-owned food
        Food globalFood = globalFoods.get(0);
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
        foodRepository.save(userCopyFood);
        List<Food> userOnlyFoods = foodRepository.findByOwner(existingUser);
        assertEquals(1, userOnlyFoods.size());

        // Test that user-owned foods do not show up as global...
        globalFoods = foodRepository.findByOwnerIsNull();
        assertEquals(419, globalFoods.size());

        // ... and that global foods with the same name as user-owned food are excluded from the list of foods visible
        // to that user
        userFoods = foodRepository.findVisibleByOwner(existingUser);
        assertEquals(419, userFoods.size());
        int allFoodsCount = 0;
        for(Food food : foodRepository.findAll()) {
            allFoodsCount++;
        }
        assertEquals(420, allFoodsCount);
    }
	
//	@Test
//	public void testWeightDao() throws ParseException {
//		WeightDao weightDao = applicationContext.getBean(WeightDao.class);
//		List<Weight> weights = weightDao.findAllForUser(1, dateFormatter.parse("2007-11-22"), dateFormatter.parse("2013-10-12"));
//		assertEquals(weights.size(), 2061);
//	}
//
//	@Test
//	public void testFoodDao() throws ParseException {
//		FoodDao foodDao = applicationContext.getBean(FoodDao.class);
//		List<Food> foods = foodDao.findByUser(1);
//		assertEquals(foods.size(), 418);
//	}
//
//	@Test
//	public void testFoodEatenDao() throws ParseException {
//		FoodEatenDao foodEatenDao = applicationContext.getBean(FoodEatenDao.class);
//		List<FoodEaten> foodsEaten = foodEatenDao.findEatenOnDate(1, dateFormatter.parse("2013-10-13"));
//		assertEquals(foodsEaten.size(), 8);
//	}
	
}
