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
        return foodEatenToDTO(foodEatens);
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
        return foodToDTO(recentFoods);
    }

    private FoodDTO foodToDTO(Food food) {
        return foodDTOConverter.convert(food);
    }

    private List<FoodDTO> foodToDTO(List<Food> foods) {
        List<FoodDTO> dtos = new ArrayList<>();
        for(Food food : foods) {
            dtos.add(foodDTOConverter.convert(food));
        }
        return dtos;
    }

    private FoodEatenDTO foodEatenToDTO(FoodEaten foodEaten) {
        return foodEatenDTOConverter.convert(foodEaten);
    }

    private List<FoodEatenDTO> foodEatenToDTO(List<FoodEaten> foodsEaten) {
        List<FoodEatenDTO> dtos = new ArrayList<>();
        for(FoodEaten foodEaten : foodsEaten) {
            dtos.add(foodEatenDTOConverter.convert(foodEaten));
        }
        return dtos;
    }

}
