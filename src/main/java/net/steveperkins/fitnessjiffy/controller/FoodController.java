package net.steveperkins.fitnessjiffy.controller;

import javax.servlet.http.HttpSession;

import net.steveperkins.fitnessjiffy.domain.Food;

import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@Controller
public class FoodController {

    @Autowired
    FoodService foodService;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = {"/food"}, method=RequestMethod.GET)
	public String viewMainFoodPage(
            @RequestParam(value = "date", required = false) String dateString,
            HttpSession session,
            Model model
    ) {
		UserDTO user = (UserDTO) session.getAttribute("user");
        Date date = null;
        if(dateString != null) {
            try {
                date = new Date(simpleDateFormat.parse(dateString).getTime());
            } catch (ParseException e) {
                date = new Date(new java.util.Date().getTime());
            }
        } else {
            date = new Date(new java.util.Date().getTime());
            dateString = simpleDateFormat.format(date);
        }

        List<FoodDTO> foodsEatenRecently = foodService.findEatenRecently(user.getId(), date);
        List<FoodEatenDTO> foodsEatenThisDate = foodService.findEatenOnDate(user.getId(), date);
        int caloriesForDay, fatForDay, saturatedFatForDay, sodiumForDay, carbsForDay, fiberForDay, sugarForDay, proteinForDay, pointsForDay;
        caloriesForDay = fatForDay = saturatedFatForDay = sodiumForDay = carbsForDay = fiberForDay = sugarForDay = proteinForDay = pointsForDay = 0;
        for(FoodEatenDTO foodEaten : foodsEatenThisDate) {
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

        model.addAttribute("user", user);
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
        // TODO: Adjust the two values below to account for calories burned through exercise
        model.addAttribute("netCalories", caloriesForDay);
        model.addAttribute("netPoints", pointsForDay);

		return Views.FOOD_TEMPLATE;
	}

    @RequestMapping(value = "/food/eaten/add")
    public String addFoodEaten(
            @RequestParam(value = "foodId", required = true) String foodIdString,
            @RequestParam(value = "date", required = true) String dateString,
            HttpSession session,
            Model model
    ) {
        UserDTO userDTO = (UserDTO) session.getAttribute("user");
        UUID foodId = UUID.fromString(foodIdString);
        Date date = null;
        try {
            date = new Date(simpleDateFormat.parse(dateString).getTime());
        } catch (ParseException e) {
            date = new Date(new java.util.Date().getTime());
        }
        foodService.addFoodEaten(userDTO.getId(), foodId, date);

        return viewMainFoodPage(dateString, session, model);
    }

    @RequestMapping(value = "/food/eaten/update")
    public String updateFoodEaten(
            @RequestParam(value = "foodEatenId", required = true) String foodEatenId,
            @RequestParam(value = "foodEatenQty", required = true) double foodEatenQty,
            @RequestParam(value = "foodEatenServing", required = true) String foodEatenServing,
            @RequestParam(value = "action", required = true) String action,
            HttpSession session,
            Model model
    ) {
        UserDTO userDTO = (UserDTO) session.getAttribute("user");
        UUID foodEatenUUID = UUID.fromString(foodEatenId);
        FoodEatenDTO foodEatenDTO = foodService.findFoodEatenById(foodEatenUUID);
        String dateString = simpleDateFormat.format(foodEatenDTO.getDate());
        if(!userDTO.getId().equals(foodEatenDTO.getUserId())) {
            // TODO: Add logging, and flash message on view template
            System.out.println("\n\nThis user is unable to update this food eaten\n");
        } else if(action.equalsIgnoreCase("update")) {
            Food.ServingType servingType = Food.ServingType.fromString(foodEatenServing);
            foodService.updateFoodEaten(foodEatenUUID, foodEatenQty, servingType);
        } else if(action.equalsIgnoreCase("delete")) {
            foodService.deleteFoodEaten(foodEatenUUID);
        }
        return viewMainFoodPage(dateString, session, model);
    }

    @RequestMapping(value = "/food/search/{searchString}")
    public @ResponseBody List<FoodDTO> searchFoods(
            @PathVariable String searchString,
            HttpSession session,
            Model model
    ) {
        UserDTO userDTO = (UserDTO) session.getAttribute("user");
        List<FoodDTO> foods = foodService.searchFoods(userDTO.getId(), searchString);
        return foods;
    }
	
}
