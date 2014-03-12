package net.steveperkins.fitnessjiffy.test;

import net.steveperkins.fitnessjiffy.Application;
import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {Application.class})
public class ServiceTests extends AbstractTests {

    @Autowired
    UserService userService;

    @Autowired
    FoodService foodService;

    @Test
    public void testUserService() {
        // Test get all users
        List<User> allUsers = userService.getAllUsers();
        assertEquals(1, allUsers.size());

        // Test get a single user by ID
        User user = userService.getUser(allUsers.get(0).getId());
        assertNotNull(user);
    }

    @Test
    public void testFoodService() {
        // Test get recently-eaten foods
        // NOTE:  Currently always returns 0, because the static test data set ages.  Need
        //        a way to populate some temporary test data dynamically.
        User user = userService.getAllUsers().get(0);
        List<Food> recentFoods = foodService.findEatenRecently(user.getId());
        assertEquals(0, recentFoods.size());
    }

}
