package net.steveperkins.fitnessjiffy.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.steveperkins.fitnessjiffy.domain.User;

import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Controller
public class FoodController {

    @Autowired
    FoodService foodService;

    @RequestMapping(value={"/diet"}, method=RequestMethod.GET)
	public String viewDiet(
            @RequestParam(value="date", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) Date date,
            HttpSession session,
            Model model
    ) {
		UserDTO user = (UserDTO) session.getAttribute("user");
        if(date == null) {
            date = new Date();
        }

        List<FoodDTO> foodsEatenRecently = foodService.foodToDTO(foodService.findEatenRecently(user.getId()));
        List<FoodEatenDTO> foodsEatenThisDate = foodService.foodEatenToDTO(foodService.findEatenOnDate(user.getId(), date));
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
        model.addAttribute("date", date);
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

		return Views.DIET_TEMPLATE;
	}

    @RequestMapping(value={"/food/search"})
    public String searchFoods(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        String searchString = request.getParameter("searchString");
//        List<Food> foods = foodDao.findByNameLike(user.getId(), searchString);
//        model.addAttribute("foods", foods);
        return Views.SEARCH_FOODS_TEMPLATE;
    }
	
}
