package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nullable;

public class FoodEatenToFoodEatenDTO implements Converter<FoodEaten, FoodEatenDTO> {

    @Autowired
    Converter<Food, FoodDTO> foodDTOConverter;

    @Override
    @Nullable
    public FoodEatenDTO convert(@Nullable FoodEaten foodEaten) {
        if(foodEaten == null) {
            return null;
        }
        return new FoodEatenDTO(
                foodEaten.getId(),
                foodEaten.getUser().getId(),
                foodDTOConverter.convert(foodEaten.getFood()),
                foodEaten.getDate(),
                foodEaten.getServingType(),
                foodEaten.getServingQty(),
                foodEaten.getCalories(),
                foodEaten.getFat(),
                foodEaten.getSaturatedFat(),
                foodEaten.getSodium(),
                foodEaten.getCarbs(),
                foodEaten.getFiber(),
                foodEaten.getSugar(),
                foodEaten.getProtein(),
                foodEaten.getPoints()
        );
    }

}
