package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class FoodService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    FoodEatenRepository foodEatenRepository;

    @Autowired
    Converter<Food, FoodDTO> foodDTOConverter;

    @Autowired
    Converter<FoodEaten, FoodEatenDTO> foodEatenDTOConverter;

    public List<FoodEatenDTO> findEatenOnDate(UUID userId, Date date) {
        User user = userRepository.findOne(userId);
        List<FoodEaten> foodEatens = foodEatenRepository.findByUserEqualsAndDateEquals(user, date);
        return foodsEatenToDTO(foodEatens);
    }

    public List<FoodDTO> findEatenRecently(UUID userId, Date currentDate) {
        User user = userRepository.findOne(userId);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -14);
        Date twoWeeksAgo = new Date(calendar.getTime().getTime());
        List<Food> recentFoods = foodEatenRepository.findByUserEatenWithinRange(
                user,
                new java.sql.Date(twoWeeksAgo.getTime()),
                new java.sql.Date(currentDate.getTime())
        );
        return foodsToDTO(recentFoods);
    }

    public FoodEatenDTO findFoodEatenById(UUID foodEatenId) {
        FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        return foodEatenToDTO(foodEaten);
    }

    public void addFoodEaten(UUID userId, UUID foodId, Date date) {
        boolean duplicate = false;
        for(FoodEatenDTO foodAlreadyEaten : findEatenOnDate(userId, date)) {
            if(foodAlreadyEaten.getFood().getId().equals(foodId)) {
                duplicate = true;
                break;
            }
        }
        if(!duplicate) {
            User user = userRepository.findOne(userId);
            Food food = foodRepository.findOne(foodId);
            FoodEaten foodEaten = new FoodEaten(
                    UUID.randomUUID(),
                    user,
                    food,
                    date,
                    food.getDefaultServingType(),
                    food.getServingTypeQty()
            );
            foodEatenRepository.save(foodEaten);
        }
    }

    public void updateFoodEaten(UUID foodEatenId, double servingQty, Food.ServingType servingType) {
        FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        foodEaten.setServingQty(servingQty);
        foodEaten.setServingType(servingType);
        foodEatenRepository.save(foodEaten);
    }

    public void deleteFoodEaten(UUID foodEatenId) {
        FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        foodEatenRepository.delete(foodEaten);
    }

    public List<FoodDTO> searchFoods(UUID userId, String searchString) {
        User user = userRepository.findOne(userId);
        List<Food> foods = foodRepository.findByNameLike(user, searchString);
        return foodsToDTO(foods);
    }

    public FoodDTO getFoodById(UUID foodId) {
        Food food = foodRepository.findOne(foodId);
        return foodToDTO(food);
    }



    private FoodDTO foodToDTO(Food food) {
        return foodDTOConverter.convert(food);
    }

    private List<FoodDTO> foodsToDTO(List<Food> foods) {
        List<FoodDTO> dtos = new ArrayList<>();
        for(Food food : foods) {
            dtos.add(foodDTOConverter.convert(food));
        }
        return dtos;
    }

    private FoodEatenDTO foodEatenToDTO(FoodEaten foodEaten) {
        return foodEatenDTOConverter.convert(foodEaten);
    }

    private List<FoodEatenDTO> foodsEatenToDTO(List<FoodEaten> foodsEaten) {
        List<FoodEatenDTO> dtos = new ArrayList<>();
        for(FoodEaten foodEaten : foodsEaten) {
            dtos.add(foodEatenDTOConverter.convert(foodEaten));
        }
        return dtos;
    }

}
