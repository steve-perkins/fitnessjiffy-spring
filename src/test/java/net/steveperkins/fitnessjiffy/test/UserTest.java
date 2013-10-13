package net.steveperkins.fitnessjiffy.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import net.steveperkins.fitnessjiffy.config.WebConfig;
import net.steveperkins.fitnessjiffy.dao.UserDao;
import net.steveperkins.fitnessjiffy.dao.WeightDao;
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
public class UserTest {

	@Autowired
    private ApplicationContext applicationContext;
	
	@Test
	public void testGender() {
		Gender male = Gender.MALE;
		assertEquals("male", male.toString());
		
		Gender female = Gender.FEMALE;
		assertEquals("female", female.toString());
	}
	
	@Test
	public void testActivityLevel() {
		ActivityLevel sedentary = ActivityLevel.SEDENTARY;
		assertEquals("Sedentary", sedentary.toString());
		assertTrue(sedentary.getValue() == 1.25f);
		
		ActivityLevel lightlyActive = ActivityLevel.LIGHTLY_ACTIVE;
		assertEquals("Lightly Active", lightlyActive.toString());
		assertTrue(lightlyActive.getValue() == 1.3f);

		ActivityLevel moderatelyActive = ActivityLevel.MODERATELY_ACTIVE;
		assertEquals("Moderately Active", moderatelyActive.toString());
		assertTrue(moderatelyActive.getValue() == 1.5f);
		
		ActivityLevel veryActive = ActivityLevel.VERY_ACTIVE;
		assertEquals("Very Active", veryActive.toString());
		assertTrue(veryActive.getValue() == 1.7f);
		
		ActivityLevel extremelyActive = ActivityLevel.EXTREMELY_ACTIVE;
		assertEquals("Extremely Active", extremelyActive.toString());
		assertTrue(extremelyActive.getValue() == 2.0f);
	}
	
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
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
		List<Weight> weights = weightDao.findAllForUser(1, dateFormatter.parse("2007-11-22"), dateFormatter.parse("2013-10-12"));
		assertEquals(weights.size(), 2061);
	}
	
}
