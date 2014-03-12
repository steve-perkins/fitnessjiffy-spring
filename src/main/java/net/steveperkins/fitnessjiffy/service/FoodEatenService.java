package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class FoodEatenService {

    @Autowired
    FoodEatenRepository foodEatenRepository;

    @Autowired
    UserRepository userRepository;

    public List<Food> findEatenRecently(UUID userId) {
        User user = userRepository.findOne(userId);
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -14);
        Date twoWeeksAgo = calendar.getTime();

        List<Food> foods = new ArrayList<>();
        for(FoodEaten foodEaten : foodEatenRepository.findByUserEqualsAndDateAfter(user, twoWeeksAgo)) {
            foods.add(foodEaten.getFood());
        }
        return foods;
    }

}
