package net.steveperkins.fitnessjiffy.controller;

import javax.servlet.http.HttpSession;

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
	public ModelAndView viewUser(@RequestParam(value="userId", required=false) Integer userId, HttpSession session) {
		ModelAndView view = new ModelAndView();
		view.addObject("users", userDao.findAll());
		User user = null;
		if(userId != null && userId.intValue() != 0) {
			// A user has been selected
			user = userDao.findById(userId.intValue());
			user = (user != null) ? user : new User();
		} else if(session.getAttribute("user") != null && session.getAttribute("user") instanceof User && ((User) session.getAttribute("user")).getId() != 0) {
			// A previously-selected user exists in the session 
			user = (User) session.getAttribute("user");
		} else {
			// No user selected 
			user = new User();
		}
		session.setAttribute("user", user);
		view.addObject("user", user);
		view.setViewName("user");
		return view;
	}
	
	@RequestMapping(value={"/user/save"}, method=RequestMethod.POST)
	public ModelAndView createOrUpdateUser(@ModelAttribute("user") User user, BindingResult result, HttpSession session) {
		ModelAndView view = new ModelAndView();
		view.addObject("users", userDao.findAll());
		user = userDao.save(user);
		return viewUser(user.getId(), session);
	}
	
	@RequestMapping(value={"/user/delete/{id}"}, method=RequestMethod.GET)
	public ModelAndView deleteUser(@PathVariable Integer id, HttpSession session) {
		if(id != null && id.intValue() != 0) {
			User user = userDao.findById(id.intValue());
			if(user != null) {
				userDao.delete(user);
			}
		}
		session.removeAttribute("user");
		return viewUser(null, session);
	}
	
}
