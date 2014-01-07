package net.steveperkins.fitnessjiffy.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.steveperkins.fitnessjiffy.Application;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.User.ActivityLevel;
import net.steveperkins.fitnessjiffy.domain.User.Gender;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import net.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import net.steveperkins.fitnessjiffy.repository.ExerciseRepository;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes={Application.class})
public class BasicSmokeTests {

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

//	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);

	@Test
	public void testUserRepository() {
        UUID userId = UUID.randomUUID();
        userRepository.save(new User(
                userId,
                User.Gender.MALE,
                30,
                70,
                User.ActivityLevel.SEDENTARY,
                "username",
                "password",
                "John",
                "Doe",
                true
        ));

        List<User> users = new ArrayList<>();
        for(User user : userRepository.findAll()) users.add(user);
		assertTrue(users.size() == 1);

		User user = userRepository.findOne(userId);
		assertNotNull(user);
		assertEquals(ActivityLevel.SEDENTARY, user.getActivityLevel());
		assertEquals(Gender.MALE, user.getGender());
//		assertEquals(String.format("%.2f", user.getCurrentWeight()), "300");
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
