package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Controller
public final class ProfileController extends AbstractController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/", "/profile"}, method = RequestMethod.GET)
    @Nonnull
    public String viewMainProfilePage(
            @Nullable
            @RequestParam(value = "userId", required = false)
            final UUID userId,

            @Nonnull
            @RequestParam(value = "date", defaultValue = TODAY)
            final String dateString,

            @Nonnull final Model model
    ) {
//        final java.sql.Date date = stringToSqlDate(dateString);

        model.addAttribute("allActivityLevels", User.ActivityLevel.values());
        model.addAttribute("allGenders", User.Gender.values());
        model.addAttribute("user", currentAuthenticatedUser());
        model.addAttribute("dateString", dateString);
        return PROFILE_TEMPLATE;
    }

    @RequestMapping(value = {"/profile/save"}, method = RequestMethod.POST)
    @Nonnull
    public String createOrUpdateProfile(
            @Nonnull
            @ModelAttribute("user")
            final UserDTO user,

            @Nonnull
            @RequestParam(value = "date", defaultValue = TODAY)
            final String dateString,

            @Nonnull
            final BindingResult result,

            @Nonnull
            final Model model
    ) {

        // TODO: implement

        return viewMainProfilePage(user.getId(), dateString, model);
    }

}
