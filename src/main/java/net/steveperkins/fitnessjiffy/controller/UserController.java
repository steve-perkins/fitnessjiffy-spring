package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.dao.UserDao;
import net.steveperkins.fitnessjiffy.domain.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
	
	Log log = LogFactory.getLog(UserController.class);

	@Autowired
	private UserDao userDao;
	
	@RequestMapping(value={"/", "/user"}, method=RequestMethod.GET)
	public ModelAndView viewUser(@RequestParam(value="userId", required=false) Integer userId) {
		ModelAndView view = new ModelAndView();
		view.addObject("users", userDao.findAll());
		if(userId == null || userId.intValue() == 0) {
			// No user selected 
			view.addObject("user", new User());
		} else {
			// A user has been selected
			User user = userDao.findById(userId.intValue());
			view.addObject("user", (user != null) ? user : new User());
		}
		view.setViewName("user");
		return view;
	}
	
	@RequestMapping(value={"/user/save"}, method=RequestMethod.POST)
	public ModelAndView createOrUpdateUser(@ModelAttribute("user") User user, BindingResult result) {
		ModelAndView view = new ModelAndView();
		view.addObject("users", userDao.findAll());
		user = userDao.save(user);
		return viewUser(user.getId());
	}
	
	@RequestMapping(value={"/user/delete/{id}"}, method=RequestMethod.GET)
	public ModelAndView deleteUser(@PathVariable Integer id) {
		if(id != null && id.intValue() != 0) {
			User user = userDao.findById(id.intValue());
			if(user != null) {
				userDao.delete(user);
			}
		}
		return viewUser(null);
	}
	
}
