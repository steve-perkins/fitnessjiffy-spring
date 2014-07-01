package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

	@RequestMapping(value = {"/", "/user"}, method = RequestMethod.GET)
    @Nonnull
    public String viewMainUserPage(
            @Nullable @RequestParam(value = "userId", required = false) UUID userId,
            @Nonnull HttpSession session,
            @Nonnull Model model
    ) {
        model.addAttribute("allActivityLevels", User.ActivityLevel.values());
        model.addAttribute("allGenders", User.Gender.values());

        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);

        UserDTO user = null;
		if(userId != null) {
			// A user has been selected
            user = userService.getUser(userId);
			user = (user != null) ? user : new UserDTO();
		} else if(session.getAttribute("user") != null && session.getAttribute("user") instanceof UserDTO) {
			// A previously-selected user exists in the session
			user = (UserDTO) session.getAttribute("user");
		} else {
			// No user selected
			user = new UserDTO();
		}
		session.setAttribute("user", user);
        model.addAttribute("user", user);
        return Views.USER_TEMPLATE;
	}

	@RequestMapping(value = {"/user/save"}, method = RequestMethod.POST)
    @Nonnull
	public String createOrUpdateUser(
            @Nonnull @ModelAttribute("user") UserDTO user,
            @Nonnull BindingResult result,
            @Nonnull HttpSession session,
            @Nonnull Model model
    ) {
        System.out.println("Inside createOrUpdateUser() with user == " + user.toString());

//        user = userRepository.save(user);
		return viewMainUserPage(user.getId(), session, model);
	}

	@RequestMapping(value = {"/user/delete/{id}"}, method = RequestMethod.GET)
    @Nonnull
    public String deleteUser(
            @Nonnull @PathVariable UUID id,
            @Nonnull HttpSession session,
            @Nonnull Model model
    ) {
        System.out.println("Inside deleteUser() with id == " + id.toString());

//		if(id != null) {
//			UserDTO user = userRepository.findOne(id);
//			if(user != null) {
//                userRepository.delete(user);
//			}
//		}
		session.removeAttribute("user");
		return viewMainUserPage(null, session, model);
    }
	
}
