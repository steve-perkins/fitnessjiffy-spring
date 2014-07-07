package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Controller
public final class UserController extends AbstractController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/", "/user"}, method = RequestMethod.GET)
    @Nonnull
    public String viewMainUserPage(
            @Nullable @RequestParam(value = "userId", required = false) final UUID userId,
            @Nonnull final Model model
    ) {
        model.addAttribute("allActivityLevels", User.ActivityLevel.values());
        model.addAttribute("allGenders", User.Gender.values());
        model.addAttribute("user", currentAuthenticatedUser());
        return USER_TEMPLATE;
    }

    @RequestMapping(value = {"/user/save"}, method = RequestMethod.POST)
    @Nonnull
    public String createOrUpdateUser(
            @Nonnull @ModelAttribute("user") final UserDTO user,
            @Nonnull final BindingResult result,
            @Nonnull final Model model
    ) {

        // TODO: implement

        return viewMainUserPage(user.getId(), model);
    }

}
