package com.steveperkins.fitnessjiffy.controller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.steveperkins.fitnessjiffy.domain.Food;

import com.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import com.steveperkins.fitnessjiffy.dto.FoodDTO;
import com.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import com.steveperkins.fitnessjiffy.dto.UserDTO;
import com.steveperkins.fitnessjiffy.service.ExerciseService;
import com.steveperkins.fitnessjiffy.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
final class FoodController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoodController.class);

    private final FoodService foodService;
    private final ExerciseService exerciseService;

    @Autowired
    public FoodController(
            @Nonnull final FoodService foodService,
            @Nonnull final ExerciseService exerciseService
    ) {
        this.foodService = foodService;
        this.exerciseService = exerciseService;
    }

    @RequestMapping(value = {"/food"}, method = RequestMethod.GET)
    @Nonnull
    public final String viewMainFoodPage(
            @Nullable
            @RequestParam(value = "date", required = false)
            final String dateString,

            @Nonnull
            final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        final Date date = dateString == null ? todaySqlDateForUser(userDTO) : stringToSqlDate(dateString);

        final List<FoodDTO> foodsEatenRecently = foodService.findEatenRecently(userDTO.getId(), date);
        final List<FoodEatenDTO> foodsEatenThisDate = foodService.findEatenOnDate(userDTO.getId(), date);
        int caloriesForDay, fatForDay, saturatedFatForDay, sodiumForDay, carbsForDay, fiberForDay, sugarForDay, proteinForDay;
        caloriesForDay = fatForDay = saturatedFatForDay = sodiumForDay = carbsForDay = fiberForDay = sugarForDay = proteinForDay = 0;
        double pointsForDay = 0.0;
        for (final FoodEatenDTO foodEaten : foodsEatenThisDate) {
            caloriesForDay += foodEaten.getCalories();
            fatForDay += foodEaten.getFat();
            saturatedFatForDay += foodEaten.getSaturatedFat();
            sodiumForDay += foodEaten.getSodium();
            carbsForDay += foodEaten.getCarbs();
            fiberForDay += foodEaten.getFiber();
            sugarForDay += foodEaten.getSugar();
            proteinForDay += foodEaten.getProtein();
            pointsForDay += foodEaten.getPoints();
        }
        int netCaloriesForDay = caloriesForDay;
        double netPointsForDay = pointsForDay;
        for (final ExercisePerformedDTO exercisePerformed : exerciseService.findPerformedOnDate(userDTO.getId(), date)) {
            netCaloriesForDay -= exercisePerformed.getCaloriesBurned();
            netPointsForDay -= exercisePerformed.getPointsBurned();
        }

        model.addAttribute("user", userDTO);
        model.addAttribute("dateString", dateString);
        model.addAttribute("foodsEatenRecently", foodsEatenRecently);
        model.addAttribute("foodsEatenThisDate", foodsEatenThisDate);
        model.addAttribute("caloriesForDay", caloriesForDay);
        model.addAttribute("fatForDay", fatForDay);
        model.addAttribute("saturatedFatForDay", saturatedFatForDay);
        model.addAttribute("sodiumForDay", sodiumForDay);
        model.addAttribute("carbsForDay", carbsForDay);
        model.addAttribute("fiberForDay", fiberForDay);
        model.addAttribute("sugarForDay", sugarForDay);
        model.addAttribute("proteinForDay", proteinForDay);
        model.addAttribute("pointsForDay", pointsForDay);
        model.addAttribute("netCalories", netCaloriesForDay);
        model.addAttribute("netPoints", netPointsForDay);

        return FOOD_TEMPLATE;
    }

    @RequestMapping(value = "/food/eaten/add")
    @Nonnull
    public final String addFoodEaten(
            @Nonnull @RequestParam(value = "foodId", required = true) final String foodIdString,
            @Nonnull @RequestParam(value = "date", required = false) final String dateString,
            @Nonnull final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        final Date date = dateString == null ? todaySqlDateForUser(userDTO) : stringToSqlDate(dateString);
        final UUID foodId = UUID.fromString(foodIdString);
        foodService.addFoodEaten(userDTO.getId(), foodId, date);
        return viewMainFoodPage(dateString, model);
    }

    @RequestMapping(value = "/food/eaten/update")
    @Nonnull
    public final String updateFoodEaten(
            @Nonnull @RequestParam(value = "foodEatenId", required = true) final String foodEatenId,
            @Nonnull @RequestParam(value = "foodEatenQty", required = true) final double foodEatenQty,
            @Nonnull @RequestParam(value = "foodEatenServing", required = true) final String foodEatenServing,
            @Nonnull @RequestParam(value = "action", required = true) final String action,
            @Nonnull final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        final UUID foodEatenUUID = UUID.fromString(foodEatenId);
        final FoodEatenDTO foodEatenDTO = foodService.findFoodEatenById(foodEatenUUID);
        final String dateString = dateFormat.format(foodEatenDTO.getDate());
        if (!userDTO.getId().equals(foodEatenDTO.getUserId())) {
            LOGGER.info("This user is unable to update this food eaten");
        } else if (action.equalsIgnoreCase("update")) {
            final Food.ServingType servingType = Food.ServingType.fromString(foodEatenServing);
            foodService.updateFoodEaten(foodEatenUUID, foodEatenQty, servingType);
        } else if (action.equalsIgnoreCase("delete")) {
            foodService.deleteFoodEaten(foodEatenUUID);
        }
        return viewMainFoodPage(dateString, model);
    }

    @RequestMapping(value = "/food/search/{searchString}")
    @Nonnull
    public final
    @ResponseBody
    List<FoodDTO> searchFoods(@Nonnull @PathVariable final String searchString) {
        final UserDTO userDTO = currentAuthenticatedUser();
        return foodService.searchFoods(userDTO.getId(), searchString);
    }

    @RequestMapping(value = "/food/get/{foodId}")
    @Nullable
    public final
    @ResponseBody
    FoodDTO getFood(@Nonnull @PathVariable final String foodId) {
        final UserDTO userDTO = currentAuthenticatedUser();
        FoodDTO foodDTO = foodService.getFoodById(UUID.fromString(foodId));
        // Only return foods that are visible to the requesting user
        if (foodDTO.getOwnerId() != null && !foodDTO.getOwnerId().equals(userDTO.getId())) {
            foodDTO = null;
        }
        return foodDTO;
    }

    @RequestMapping(value = "/food/update")
    @Nonnull
    public final
    @ResponseBody
    String createOrUpdateFood(
            @Nonnull @ModelAttribute final FoodDTO foodDTO,
            @Nonnull final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        String resultMessage;
        if (foodDTO.getId() == null) {
            resultMessage = foodService.createFood(foodDTO, userDTO);
        } else {
            resultMessage = foodService.updateFood(foodDTO, userDTO);
        }
        return resultMessage;
    }

}
