package com.steveperkins.fitnessjiffy.dto.converter;

import com.steveperkins.fitnessjiffy.domain.Food;
import com.steveperkins.fitnessjiffy.dto.FoodDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public final class FoodToFoodDTO implements Converter<Food, FoodDTO> {

    @Override
    @Nullable
    public FoodDTO convert(@Nullable final Food food) {
        FoodDTO dto = null;
        if (food != null) {
            dto = new FoodDTO(
                    food.getId(),
                    (food.getOwner() == null) ? null : food.getOwner().getId(),
                    food.getName(),
                    food.getDefaultServingType(),
                    food.getServingTypeQty(),
                    food.getCalories(),
                    food.getFat(),
                    food.getSaturatedFat(),
                    food.getCarbs(),
                    food.getFiber(),
                    food.getSugar(),
                    food.getProtein(),
                    food.getSodium()
            );
        }
        return dto;
    }

}
