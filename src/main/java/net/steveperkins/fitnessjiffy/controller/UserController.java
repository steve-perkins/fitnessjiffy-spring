package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Converter<User, UserDTO> userDTOConverter;
	
	@RequestMapping(value = {"/", "/user"}, method = RequestMethod.GET)
	public String viewUser(@RequestParam(value="userId", required=false) UUID userId, HttpSession session, Map<String, Object> model) {
        List<UserDTO> users = new ArrayList<>();
        for(User user : userService.getAllUsers()) {
            users.add(userDTOConverter.convert(user));
        }
        model.put("users", users);

        UserDTO user = null;
		if(userId != null) {
			// A user has been selected
			user = userDTOConverter.convert(userService.getUser(userId));
			user = (user != null) ? user : new UserDTO();
		} else if(session.getAttribute("user") != null && session.getAttribute("user") instanceof UserDTO) {
			// A previously-selected user exists in the session
			user = (UserDTO) session.getAttribute("user");
		} else {
			// No user selected
			user = new UserDTO();
		}
		session.setAttribute("user", user);
        model.put("user", user);
        return Views.USER_TEMPLATE;
	}

	@RequestMapping(value = {"/user/save"}, method = RequestMethod.POST)
	public String createOrUpdateUser(@ModelAttribute("user") UserDTO user, BindingResult result, HttpSession session, Map<String, Object> model) {
//        user = userRepository.save(user);
		return viewUser(user.getId(), session, model);
	}

	@RequestMapping(value = {"/user/delete/{id}"}, method = RequestMethod.GET)
	public String deleteUser(@PathVariable UUID id, HttpSession session, Map<String, Object> model) {
//		if(id != null) {
//			UserDTO user = userRepository.findOne(id);
//			if(user != null) {
//                userRepository.delete(user);
//			}
//		}
		session.removeAttribute("user");
		return viewUser(null, session, model);
    }
	
}
