package net.steveperkins.fitnessjiffy.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodEatenToFoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodToFoodDTO;
import net.steveperkins.fitnessjiffy.repository.FoodEatenRepository;
import net.steveperkins.fitnessjiffy.repository.FoodRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public final class FoodService {

    @Autowired
    private ReportDataService reportDataService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private FoodEatenRepository foodEatenRepository;

    @Autowired
    private FoodToFoodDTO foodDTOConverter;

    @Autowired
    private FoodEatenToFoodEatenDTO foodEatenDTOConverter;

    private final Function<Food, FoodDTO> foodToDTOConversionFunction =
            new Function<Food, FoodDTO>() {
                @Nullable
                @Override
                public FoodDTO apply(@Nullable final Food food) {
                    return foodDTOConverter.convert(food);
                }
            };

    private final Function<FoodEaten, FoodEatenDTO> foodEatenToDTOConversionFunction =
            new Function<FoodEaten, FoodEatenDTO>() {
                @Nullable
                @Override
                public FoodEatenDTO apply(@Nullable final FoodEaten foodEaten) {
                    return foodEatenDTOConverter.convert(foodEaten);
                }
            };

    @Nonnull
    public List<FoodEatenDTO> findEatenOnDate(
            @Nonnull final UUID userId,
            @Nonnull final Date date
    ) {
        final User user = userRepository.findOne(userId);
        final List<FoodEaten> foodEatens = foodEatenRepository.findByUserEqualsAndDateEquals(user, date);
        return Lists.transform(foodEatens, foodEatenToDTOConversionFunction);
    }

    @Nonnull
    public List<FoodDTO> findEatenRecently(
            @Nonnull final UUID userId,
            @Nonnull final Date currentDate
    ) {
        final User user = userRepository.findOne(userId);
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -14);
        final Date twoWeeksAgo = new Date(calendar.getTime().getTime());
        final List<Food> recentFoods = foodEatenRepository.findByUserEatenWithinRange(
                user,
                new Date(twoWeeksAgo.getTime()),
                new Date(currentDate.getTime())
        );
        return Lists.transform(recentFoods, foodToDTOConversionFunction);
    }

    @Nullable
    public FoodEatenDTO findFoodEatenById(@Nonnull final UUID foodEatenId) {
        final FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        return foodEatenDTOConverter.convert(foodEaten);
    }

    public void addFoodEaten(
            @Nonnull final UUID userId,
            @Nonnull final UUID foodId,
            @Nonnull final Date date
    ) {
        final boolean duplicate = Iterables.any(findEatenOnDate(userId, date), new Predicate<FoodEatenDTO>() {
            @Override
            public boolean apply(@Nonnull final FoodEatenDTO foodAlreadyEaten) {
                return foodAlreadyEaten.getFood().getId().equals(foodId);
            }
        });
        if (!duplicate) {
            final User user = userRepository.findOne(userId);
            final Food food = foodRepository.findOne(foodId);
            final FoodEaten foodEaten = new FoodEaten(
                    UUID.randomUUID(),
                    user,
                    food,
                    date,
                    food.getDefaultServingType(),
                    food.getServingTypeQty()
            );
            foodEatenRepository.save(foodEaten);
            reportDataService.updateUserFromDate(userId, date);
        }
    }

    public void updateFoodEaten(
            @Nonnull final UUID foodEatenId,
            final double servingQty,
            @Nonnull final Food.ServingType servingType
    ) {
        final FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        foodEaten.setServingQty(servingQty);
        foodEaten.setServingType(servingType);
        foodEatenRepository.save(foodEaten);
        reportDataService.updateUserFromDate(foodEaten.getUser().getId(), foodEaten.getDate());
    }

    public void deleteFoodEaten(@Nonnull final UUID foodEatenId) {
        final FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        reportDataService.updateUserFromDate(foodEaten.getUser().getId(), foodEaten.getDate());
        foodEatenRepository.delete(foodEaten);
    }

    @Nonnull
    public List<FoodDTO> searchFoods(
            @Nonnull final UUID userId,
            @Nonnull final String searchString
    ) {
        final User user = userRepository.findOne(userId);
        final List<Food> foods = foodRepository.findByNameLike(user, searchString);
        return Lists.transform(foods, foodToDTOConversionFunction);
    }

    @Nullable
    public FoodDTO getFoodById(@Nonnull final UUID foodId) {
        final Food food = foodRepository.findOne(foodId);
        return foodDTOConverter.convert(food);
    }

    @Nonnull
    public String updateFood(
            @Nonnull final FoodDTO foodDTO,
            @Nonnull final UserDTO userDTO
    ) {

        // TODO: Maybe this method should return some sort of ID, which maps to a message string elsewhere... rather than directly returning hardcoded strings meant for display.

        String resultMessage = "";
        // Halt if this operation is not allowed
        if (foodDTO.getOwnerId() == null || foodDTO.getOwnerId().equals(userDTO.getId())) {

            // Halt if this update would create two foods with duplicate names owned by the same user.
            final User user = userRepository.findOne(userDTO.getId());
            final List<Food> foodsWithSameNameOwnedByThisUser = foodRepository.findByOwnerEqualsAndNameEquals(user, foodDTO.getName());
            final boolean noConflictsFound = Iterables.all(foodsWithSameNameOwnedByThisUser, new Predicate<Food>() {
                @Override
                public boolean apply(@Nonnull final Food food) {
                    return foodDTO.getId().equals(food.getId());
                }
            });
            if (!noConflictsFound) {
                resultMessage = "Error:  You already have another customized food with this name.";

            } else {

                // If this is already a user-owned food, then simply update it.  Otherwise, if it's a global food then create a
                // user-owned copy for this user.
                Food food = null;
                Date dateFirstEaten = null;
                if (foodDTO.getOwnerId() == null) {
                    food = new Food();
                    food.setId(UUID.randomUUID());
                    food.setOwner(user);
                    dateFirstEaten = new Date(System.currentTimeMillis());
                } else {
                    food = foodRepository.findOne(foodDTO.getId());
                    final List<FoodEaten> foodsEatenSortedByDate = foodEatenRepository.findByUserEqualsAndFoodEqualsOrderByDateAsc(user, food);
                    dateFirstEaten = (foodsEatenSortedByDate != null && !foodsEatenSortedByDate.isEmpty())
                            ? foodsEatenSortedByDate.get(0).getDate() : new Date(System.currentTimeMillis());
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
                resultMessage = "Success!";
                reportDataService.updateUserFromDate(userDTO.getId(), dateFirstEaten);
            }

        } else {
            resultMessage = "Error:  You are attempting to modify another user's customized food.";
        }
        return resultMessage;
    }

    @Nonnull
    public String createFood(
            @Nonnull final FoodDTO foodDTO,
            @Nonnull final UserDTO userDTO
    ) {

        // TODO: Maybe this method should return some sort of ID, which maps to a message string elsewhere... rather than directly returning hardcoded strings meant for display.

        String resultMessage = "";

        // Halt if this update would create two foods with duplicate names owned by the same user.
        final User user = userRepository.findOne(userDTO.getId());
        final List<Food> foodsWithSameNameOwnedByThisUser = foodRepository.findByOwnerEqualsAndNameEquals(user, foodDTO.getName());

        if (foodsWithSameNameOwnedByThisUser.isEmpty()) {
            final Food food = new Food();
            if (foodDTO.getId() == null) {
                food.setId(UUID.randomUUID());
            } else {
                food.setId(foodDTO.getId());
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
            resultMessage = "Success!";
            reportDataService.updateUserFromDate(userDTO.getId(), new Date(System.currentTimeMillis()));
        } else {
            resultMessage = "Error:  You already have another customized food with this name.";
        }
        return resultMessage;
    }

}
