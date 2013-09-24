package net.steveperkins.fitnessjiffy.test;

import java.util.List;

import net.steveperkins.fitnessjiffy.config.WebConfig;
import net.steveperkins.fitnessjiffy.dao.UserDao;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.User.ActivityLevel;
import net.steveperkins.fitnessjiffy.domain.User.Gender;
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
		assertEquals(male.toString(), "male");
		
		Gender female = Gender.FEMALE;
		assertEquals(female.toString(), "female");
	}
	
	@Test
	public void testActivityLevel() {
		ActivityLevel sedentary = ActivityLevel.SEDENTARY;
		assertEquals(sedentary.toString(), "Sedentary");
		assertTrue(sedentary.getValue() == 1.25f);
		
		ActivityLevel lightlyActive = ActivityLevel.LIGHTLY_ACTIVE;
		assertEquals(lightlyActive.toString(), "Lightly Active");
		assertTrue(lightlyActive.getValue() == 1.3f);

		ActivityLevel moderatelyActive = ActivityLevel.MODERATELY_ACTIVE;
		assertEquals(moderatelyActive.toString(), "Moderately Active");
		assertTrue(moderatelyActive.getValue() == 1.5f);
		
		ActivityLevel veryActive = ActivityLevel.VERY_ACTIVE;
		assertEquals(veryActive.toString(), "Very Active");
		assertTrue(veryActive.getValue() == 1.7f);
		
		ActivityLevel extremelyActive = ActivityLevel.EXTREMELY_ACTIVE;
		assertEquals(extremelyActive.toString(), "Extremely Active");
		assertTrue(extremelyActive.getValue() == 2.0f);
	}
	
	@Test
	public void testUserDao() {
		UserDao userDao = applicationContext.getBean(UserDao.class);
		
		List<User> users = userDao.findAll();
		assertTrue(users.size() == 1);

		User user = userDao.findById(1);
		assertNotNull(user);
		assertEquals(user.getActivityLevel(), ActivityLevel.SEDENTARY);
		assertEquals(user.getGender(), Gender.MALE);
	}
	
}
