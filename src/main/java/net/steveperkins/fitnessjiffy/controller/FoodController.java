package net.steveperkins.fitnessjiffy.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.User;

import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.FoodEatenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Controller
public class FoodController {

    @Autowired
    FoodEatenService foodEatenService;

    @RequestMapping(value={"/diet"}, method=RequestMethod.GET)
	public ModelAndView viewDiet(HttpSession session) {
		ModelAndView view = new ModelAndView();

		UserDTO user = (UserDTO) session.getAttribute("user");
        Date date = new Date();  // TODO: Look for dynamic value passed as request parameter
        List<Food> foodsEatenRecently = foodEatenService.findEatenRecently(user.getId());  // TODO: Convert to DTO before putting on the Model
//        List<FoodEaten> foodsEatenThisDate = foodEatenDao.findEatenOnDate(user.getId(), new Date());
        int caloriesForDay, fatForDay, saturatedFatForDay, sodiumForDay, carbsForDay, fiberForDay, sugarForDay, proteinForDay, pointsForDay;
        caloriesForDay = fatForDay = saturatedFatForDay = sodiumForDay = carbsForDay = fiberForDay = sugarForDay = proteinForDay = pointsForDay = 0;
//        for(FoodEaten foodEaten : foodsEatenThisDate) {
//            caloriesForDay += foodEaten.getCalories();
//            fatForDay += foodEaten.getFat();
//            saturatedFatForDay += foodEaten.getSaturatedFat();
//            sodiumForDay += foodEaten.getSodium();
//            carbsForDay += foodEaten.getCarbs();
//            fiberForDay += foodEaten.getFiber();
//            sugarForDay += foodEaten.getSugar();
//            proteinForDay += foodEaten.getProtein();
//            pointsForDay += foodEaten.getPoints();
//        }

		view.addObject("user", user);
        view.addObject("date", date);
        view.addObject("foodsEatenRecently", foodsEatenRecently);
//        view.addObject("foodsEatenThisDate", foodsEatenThisDate);
        view.addObject("caloriesForDay", caloriesForDay);
        view.addObject("fatForDay", fatForDay);
        view.addObject("saturatedFatForDay", saturatedFatForDay);
        view.addObject("sodiumForDay", sodiumForDay);
        view.addObject("carbsForDay", carbsForDay);
        view.addObject("fiberForDay", fiberForDay);
        view.addObject("sugarForDay", sugarForDay);
        view.addObject("proteinForDay", proteinForDay);
        view.addObject("pointsForDay", pointsForDay);

		view.setViewName("diet");
		return view;
	}

    @RequestMapping(value={"/food/search"})
    public ModelAndView searchFoods(HttpServletRequest request) {
        ModelAndView view = new ModelAndView();

        User user = (User) request.getSession().getAttribute("user");
        String searchString = request.getParameter("searchString");
//        List<Food> foods = foodDao.findByNameLike(user.getId(), searchString);

//        view.addObject("foods", foods);
        view.setViewName("searchFoods.jsp");
        return view;
    }
	
}
