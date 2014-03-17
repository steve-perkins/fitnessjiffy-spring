package net.steveperkins.fitnessjiffy.test;

import net.steveperkins.fitnessjiffy.Application;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Date;
import java.util.List;

import static junit.framework.TestCase.*;

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
        List<UserDTO> allUsers = userService.getAllUsers();
        assertEquals(1, allUsers.size());

        // Test get a single user by ID
        UserDTO user = userService.getUser(allUsers.get(0).getId());
        assertNotNull(user);
    }

    @Test
    public void testFoodService() {
        // Test get recently-eaten foods
        // NOTE:  Currently always returns 0, because the static test data set ages.  Need
        //        a way to populate some temporary test data dynamically.
        UserDTO user = userService.getAllUsers().get(0);
        Date currentDate = new Date(new java.util.Date().getTime());
        List<FoodDTO> recentFoods = foodService.findEatenRecently(user.getId(), currentDate);
        assertEquals(0, recentFoods.size());
    }

}
