package net.steveperkins.fitnessjiffy.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.steveperkins.fitnessjiffy.domain.User;

import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
public class FoodController {

    @Autowired
    FoodService foodService;

    @RequestMapping(value={"/diet"}, method=RequestMethod.GET)
	public String viewDiet(
            @RequestParam(value="date", required=false) String dateString,
            HttpSession session,
            Model model
    ) {
		UserDTO user = (UserDTO) session.getAttribute("user");
        Date date = null;
        if(dateString != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                date = new Date(simpleDateFormat.parse(dateString).getTime());
            } catch (ParseException e) {
                date = new Date(new java.util.Date().getTime());
            }
        } else {
            date = new Date(new java.util.Date().getTime());
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
