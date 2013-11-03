package net.steveperkins.fitnessjiffy.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import net.steveperkins.fitnessjiffy.config.WebConfig;
import net.steveperkins.fitnessjiffy.dao.FoodDao;
import net.steveperkins.fitnessjiffy.dao.FoodEatenDao;
import net.steveperkins.fitnessjiffy.dao.UserDao;
import net.steveperkins.fitnessjiffy.dao.WeightDao;
import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.User.ActivityLevel;
import net.steveperkins.fitnessjiffy.domain.User.Gender;
import net.steveperkins.fitnessjiffy.domain.Weight;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes={WebConfig.class})
public class BasicSmokeTests {

	@Autowired
    private ApplicationContext applicationContext;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);

	@Test
	public void testUserDao() {
		UserDao userDao = applicationContext.getBean(UserDao.class);
		
		List<User> users = userDao.findAll();
		assertTrue(users.size() == 1);

		User user = userDao.findById(1);
		assertNotNull(user);
		assertEquals(ActivityLevel.SEDENTARY, user.getActivityLevel());
		assertEquals(Gender.MALE, user.getGender());
//		assertEquals(String.format("%.2f", user.getCurrentWeight()), "300");
	}
	
	@Test
	public void testWeightDao() throws ParseException {
		WeightDao weightDao = applicationContext.getBean(WeightDao.class);
		List<Weight> weights = weightDao.findAllForUser(1, dateFormatter.parse("2007-11-22"), dateFormatter.parse("2013-10-12"));
		assertEquals(weights.size(), 2061);
	}
	
	@Test
	public void testFoodDao() throws ParseException {
		FoodDao foodDao = applicationContext.getBean(FoodDao.class);
		List<Food> foods = foodDao.findByUser(1);
		assertEquals(foods.size(), 418);
	}
	
	@Test
	public void testFoodEatenDao() throws ParseException {
		FoodEatenDao foodEatenDao = applicationContext.getBean(FoodEatenDao.class);
		List<FoodEaten> foodsEaten = foodEatenDao.findEatenByDate(1, dateFormatter.parse("2013-10-13"));
		assertEquals(foodsEaten.size(), 8);
	}
	
}
