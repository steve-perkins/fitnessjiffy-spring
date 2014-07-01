package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    public List<FoodEatenDTO> findEatenOnDate(
            @Nonnull UUID userId,
            @Nonnull Date date
    ) {
        User user = userRepository.findOne(userId);
        List<FoodEaten> foodEatens = foodEatenRepository.findByUserEqualsAndDateEquals(user, date);
        return foodsEatenToDTO(foodEatens);
    }

    @Nonnull
    public List<FoodDTO> findEatenRecently(
            @Nonnull UUID userId,
            @Nonnull Date currentDate
    ) {
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

    @Nullable
    public FoodEatenDTO findFoodEatenById(@Nonnull UUID foodEatenId) {
        FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        return foodEatenToDTO(foodEaten);
    }

    public void addFoodEaten(
            @Nonnull UUID userId,
            @Nonnull UUID foodId,
            @Nonnull Date date
    ) {
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

    public void updateFoodEaten(
            @Nonnull UUID foodEatenId,
            double servingQty,
            @Nonnull Food.ServingType servingType
    ) {
        FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        foodEaten.setServingQty(servingQty);
        foodEaten.setServingType(servingType);
        foodEatenRepository.save(foodEaten);
    }

    public void deleteFoodEaten(@Nonnull UUID foodEatenId) {
        FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        foodEatenRepository.delete(foodEaten);
    }

    @Nonnull
    public List<FoodDTO> searchFoods(
            @Nonnull UUID userId,
            @Nonnull String searchString
    ) {
        User user = userRepository.findOne(userId);
        List<Food> foods = foodRepository.findByNameLike(user, searchString);
        return foodsToDTO(foods);
    }

    @Nullable
    public FoodDTO getFoodById(@Nonnull UUID foodId) {
        Food food = foodRepository.findOne(foodId);
        return foodToDTO(food);
    }

    @Nonnull
    public String updateFood(
            @Nonnull FoodDTO foodDTO,
            @Nonnull UserDTO userDTO
    ) {

        // TODO: Maybe this method should return some sort of ID, which maps to a message string elsewhere... rather than directly returning hardcoded strings meant for display.

        // Halt if this operation is not allowed
        if(foodDTO.getOwnerId() != null && !foodDTO.getOwnerId().equals(userDTO.getId())) {
            return "Error:  You are attempting to modify another user's customized food.";
        }

        // Halt if this update would create two foods with duplicate names owned by the same user.
        User user = userRepository.findOne(userDTO.getId());
        List<Food> foodsWithSameNameOwnedByThisUser = foodRepository.findByOwnerEqualsAndNameEquals(user, foodDTO.getName());
        for(Food possibleCollision : foodsWithSameNameOwnedByThisUser) {
            if(!foodDTO.getId().equals(possibleCollision.getId())) {
                return "Error:  You already have another customized food with this name.";
            }
        }

        // If this is already a user-owned food, then simply update it.  Otherwise, if it's a global food then create a
        // user-owned copy for this user.
        Food food = null;
        if(foodDTO.getOwnerId() != null) {
            food = foodRepository.findOne(foodDTO.getId());
        } else {
            food = new Food();
            food.setId(UUID.randomUUID());
            food.setOwner(user);
        }
        food.setName(foodDTO.getName());
        food.setDefaultServingType(foodDTO.getDefaultServingType());
        food.setServingTypeQty(foodDTO.getServingTypeQty());
        food.setCalories(foodDTO.getCalories());
        food.setFat(foodDTO.getFat());
        food.setSaturatedFat(foodDTO.getSaturatedFat());
        food.setCarbs(foodDTO.getCarbs());
        food.setFiber(foodDTO.getFiber());
        food.setSugar(foodDTO.getSugar());
        food.setProtein(foodDTO.getProtein());
        food.setSodium(foodDTO.getSodium());
        foodRepository.save(food);
        return "Success!";
    }

    @Nonnull
    public String createFood(
            @Nonnull FoodDTO foodDTO,
            @Nonnull UserDTO userDTO
    ) {

        // TODO: Maybe this method should return some sort of ID, which maps to a message string elsewhere... rather than directly returning hardcoded strings meant for display.

        // Halt if this update would create two foods with duplicate names owned by the same user.
        User user = userRepository.findOne(userDTO.getId());
        List<Food> foodsWithSameNameOwnedByThisUser = foodRepository.findByOwnerEqualsAndNameEquals(user, foodDTO.getName());
        if(foodsWithSameNameOwnedByThisUser.size() > 0) {
            return "Error:  You already have another customized food with this name.";
        }

        Food food = new Food();
        if(foodDTO.getId() != null) {
            food.setId(foodDTO.getId());
        } else {
            food.setId(UUID.randomUUID());
        }
        food.setOwner(user);
        food.setName(foodDTO.getName());
        food.setDefaultServingType(foodDTO.getDefaultServingType());
        food.setServingTypeQty(foodDTO.getServingTypeQty());
        food.setCalories(foodDTO.getCalories());
        food.setFat(foodDTO.getFat());
        food.setSaturatedFat(foodDTO.getSaturatedFat());
        food.setCarbs(foodDTO.getCarbs());
        food.setFiber(foodDTO.getFiber());
        food.setSugar(foodDTO.getSugar());
        food.setProtein(foodDTO.getProtein());
        food.setSodium(foodDTO.getSodium());
        foodRepository.save(food);
        return "Success!";
    }

    @Nullable
    private FoodDTO foodToDTO(@Nullable Food food) {
        return foodDTOConverter.convert(food);
    }

    @Nonnull
    private List<FoodDTO> foodsToDTO(@Nonnull List<Food> foods) {
        List<FoodDTO> dtos = new ArrayList<>();
        for(Food food : foods) {
            dtos.add(foodDTOConverter.convert(food));
        }
        return dtos;
    }

    @Nullable
    private FoodEatenDTO foodEatenToDTO(@Nullable FoodEaten foodEaten) {
        return foodEatenDTOConverter.convert(foodEaten);
    }

    @Nonnull
    private List<FoodEatenDTO> foodsEatenToDTO(@Nonnull List<FoodEaten> foodsEaten) {
        List<FoodEatenDTO> dtos = new ArrayList<>();
        for(FoodEaten foodEaten : foodsEaten) {
            dtos.add(foodEatenDTOConverter.convert(foodEaten));
        }
        return dtos;
    }

}
