package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public final class FoodEatenToFoodEatenDTO implements Converter<FoodEaten, FoodEatenDTO> {

    private final FoodToFoodDTO foodDTOConverter;

    @Autowired
    public FoodEatenToFoodEatenDTO(@Nonnull final FoodToFoodDTO foodDTOConverter) {
        this.foodDTOConverter = foodDTOConverter;
    }

    @Override
    @Nullable
    public FoodEatenDTO convert(@Nullable final FoodEaten foodEaten) {
        FoodEatenDTO dto = null;
        if (foodEaten != null) {
            dto = new FoodEatenDTO(
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
        return dto;
    }

}
