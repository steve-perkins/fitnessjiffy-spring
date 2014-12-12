package net.steveperkins.fitnessjiffy.service;

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
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public final class FoodService {

    private final ReportDataService reportDataService;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final FoodEatenRepository foodEatenRepository;
    private final FoodToFoodDTO foodDTOConverter;
    private final FoodEatenToFoodEatenDTO foodEatenDTOConverter;

    @Autowired
    public FoodService(
            @Nonnull final ReportDataService reportDataService,
            @Nonnull final UserRepository userRepository,
            @Nonnull final FoodRepository foodRepository,
            @Nonnull final FoodEatenRepository foodEatenRepository,
            @Nonnull final FoodToFoodDTO foodDTOConverter,
            @Nonnull final FoodEatenToFoodEatenDTO foodEatenDTOConverter
    ) {
        this.reportDataService = reportDataService;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.foodEatenRepository = foodEatenRepository;
        this.foodDTOConverter = foodDTOConverter;
        this.foodEatenDTOConverter = foodEatenDTOConverter;
    }

    @Nonnull
    public final List<FoodEatenDTO> findEatenOnDate(
            @Nonnull final UUID userId,
            @Nonnull final Date date
    ) {
        final User user = userRepository.findOne(userId);
        return foodEatenRepository.findByUserEqualsAndDateEquals(user, date)
                .stream()
                .map( (FoodEaten foodEaten) -> foodEatenDTOConverter.convert(foodEaten))
                .collect(toList());
    }

    @Nonnull
    public final List<FoodDTO> findEatenRecently(
            @Nonnull final UUID userId,
            @Nonnull final Date currentDate
    ) {
        final User user = userRepository.findOne(userId);
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -14);
        final Date twoWeeksAgo = new Date(calendar.getTime().getTime());
        return foodEatenRepository.findByUserEatenWithinRange(user, new Date(twoWeeksAgo.getTime()), new Date(currentDate.getTime()) )
                .stream()
                .map( (Food food) -> foodDTOConverter.convert(food) )
                .collect(toList());
    }

    @Nullable
    public final FoodEatenDTO findFoodEatenById(@Nonnull final UUID foodEatenId) {
        final FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        return foodEatenDTOConverter.convert(foodEaten);
    }

    public final void addFoodEaten(
            @Nonnull final UUID userId,
            @Nonnull final UUID foodId,
            @Nonnull final Date date
    ) {
        final boolean duplicate = findEatenOnDate(userId, date).stream()
                .anyMatch( (FoodEatenDTO foodAlreadyEaten) -> foodAlreadyEaten.getFood().getId().equals(foodId) );
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
            reportDataService.updateUserFromDate(user, date);
        }
    }

    public final void updateFoodEaten(
            @Nonnull final UUID foodEatenId,
            final double servingQty,
            @Nonnull final Food.ServingType servingType
    ) {
        final FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        foodEaten.setServingQty(servingQty);
        foodEaten.setServingType(servingType);
        foodEatenRepository.save(foodEaten);
        reportDataService.updateUserFromDate(foodEaten.getUser(), foodEaten.getDate());
    }

    public final void deleteFoodEaten(@Nonnull final UUID foodEatenId) {
        final FoodEaten foodEaten = foodEatenRepository.findOne(foodEatenId);
        reportDataService.updateUserFromDate(foodEaten.getUser(), foodEaten.getDate());
        foodEatenRepository.delete(foodEaten);
    }

    @Nonnull
    public final List<FoodDTO> searchFoods(
            @Nonnull final UUID userId,
            @Nonnull final String searchString
    ) {
        final User user = userRepository.findOne(userId);
        final List<Food> foods = foodRepository.findByNameLike(user, searchString);
        return foods.stream().map( (Food food) -> foodDTOConverter.convert(food) ).collect(toList());
    }

    @Nullable
    public final FoodDTO getFoodById(@Nonnull final UUID foodId) {
        final Food food = foodRepository.findOne(foodId);
        return foodDTOConverter.convert(food);
    }

    /** @return A message, suitable for UI display, indicating the result of the save operation. */
    @Nonnull
    public final String updateFood(
            @Nonnull final FoodDTO foodDTO,
            @Nonnull final UserDTO userDTO
    ) {
        String resultMessage = "";
        // Halt if this operation is not allowed
        if (foodDTO.getOwnerId() == null || foodDTO.getOwnerId().equals(userDTO.getId())) {

            // Halt if this update would create two foods with duplicate names owned by the same user.
            final User user = userRepository.findOne(userDTO.getId());
            final List<Food> foodsWithSameNameOwnedByThisUser = foodRepository.findByOwnerEqualsAndNameEquals(user, foodDTO.getName());
            final boolean noConflictsFound = foodsWithSameNameOwnedByThisUser
                    .stream()
                    .allMatch( (Food food) -> foodDTO.getId().equals(food.getId()) ); // Should be only one item in this stream anyway
            if (noConflictsFound) {
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
                reportDataService.updateUserFromDate(user, dateFirstEaten);
            } else {
                resultMessage = "Error:  You already have another customized food with this name.";
            }

        } else {
            resultMessage = "Error:  You are attempting to modify another user's customized food.";
        }
        return resultMessage;
    }

    /** @return A message, suitable for UI display, indicating the result of the save operation. */
    @Nonnull
    public final String createFood(
            @Nonnull final FoodDTO foodDTO,
            @Nonnull final UserDTO userDTO
    ) {
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
            reportDataService.updateUserFromDate(user, new Date(System.currentTimeMillis()));
        } else {
            resultMessage = "Error:  You already have another customized food with this name.";
        }
        return resultMessage;
    }

}
