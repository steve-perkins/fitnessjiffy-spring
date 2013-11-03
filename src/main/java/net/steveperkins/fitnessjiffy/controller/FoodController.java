package net.steveperkins.fitnessjiffy.controller;

import javax.servlet.http.HttpSession;

import net.steveperkins.fitnessjiffy.dao.FoodEatenDao;
import net.steveperkins.fitnessjiffy.dao.UserDao;
import net.steveperkins.fitnessjiffy.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FoodController {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private FoodEatenDao foodEatenDao;
	
	@RequestMapping(value={"/diet"}, method=RequestMethod.GET)
	public ModelAndView viewDiet(HttpSession session) {
		ModelAndView view = new ModelAndView();
		User user = (User) session.getAttribute("user");

		view.addObject("user", user);
		view.addObject("foodsRecentlyEaten", foodEatenDao.findEatenRecently(user.getId()));
		view.setViewName("diet");
		return view;
	}
	
}
