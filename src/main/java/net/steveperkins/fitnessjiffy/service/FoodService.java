package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

//    public Food getFood(UUID foodId) {
//        return foodRepository.findOne(foodId);
//    }

}
