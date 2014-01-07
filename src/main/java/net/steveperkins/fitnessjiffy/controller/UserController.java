package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {
	
//	Log log = LogFactory.getLog(UserController.class);

    @Autowired
	private UserRepository userRepository;
	
	@RequestMapping(value={"/", "/user"}, method= RequestMethod.GET)
	public ModelAndView viewUser(@RequestParam(value="userId", required=false) UUID userId, HttpSession session) {
        ModelAndView view = new ModelAndView();

        List<User> users = new ArrayList<>();
        for(User user : userRepository.findAll()) {
            users.add(user);
        }
        view.addObject("users", users);

        User user = null;
		if(userId != null) {
			// A user has been selected
			user = userRepository.findOne(userId);
			user = (user != null) ? user : new User();
		} else if(session.getAttribute("user") != null && session.getAttribute("user") instanceof User) {
			// A previously-selected user exists in the session
			user = (User) session.getAttribute("user");
		} else {
			// No user selected
			user = new User();
		}
		session.setAttribute("user", user);
        view.addObject("user", user);
        view.setViewName(Views.USER_TEMPLATE);
        return view;
	}

	@RequestMapping(value={"/user/save"}, method=RequestMethod.POST)
	public ModelAndView createOrUpdateUser(@ModelAttribute("user") User user, BindingResult result, HttpSession session) {
        user = userRepository.save(user);
		return viewUser(user.getId(), session);
	}

	@RequestMapping(value={"/user/delete/{id}"}, method=RequestMethod.GET)
	public ModelAndView deleteUser(@PathVariable UUID id, HttpSession session) {
		if(id != null) {
			User user = userRepository.findOne(id);
			if(user != null) {
                userRepository.delete(user);
			}
		}
		session.removeAttribute("user");
		return viewUser(null, session);
	}
	
}
