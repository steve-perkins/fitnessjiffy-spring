package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.sql.Date;

@Controller
public final class ProfileController extends AbstractController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/", "/profile"}, method = RequestMethod.GET)
    @Nonnull
    public String viewMainProfilePage(
            @Nonnull
            @RequestParam(value = "date", defaultValue = TODAY)
            final String dateString,

            @Nonnull final Model model
    ) {
        final UserDTO user = currentAuthenticatedUser();
        final Date date = stringToSqlDate(dateString);
        final WeightDTO weight = userService.findWeightOnDate(user, date);
        final String weightEntry = (weight == null) ? "" : String.valueOf(weight.getPounds());

        model.addAttribute("allActivityLevels", User.ActivityLevel.values());
        model.addAttribute("allGenders", User.Gender.values());
        model.addAttribute("user", user);
        model.addAttribute("dateString", dateString);
        model.addAttribute("weightEntry", weightEntry);
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

        return viewMainProfilePage(dateString, model);
    }

    @RequestMapping(value = {"/profile/weight/save"}, method = RequestMethod.POST)
    @Nonnull
    public String createOrUpdateWeight(
        @Nonnull
        @RequestParam(value = "weightEntry", defaultValue = "0")
        final double weightEntry,

        @Nonnull
        @RequestParam(value = "dateString", defaultValue = TODAY)
        final String dateString,

        @Nonnull final Model model
    ) {
        final UserDTO user = currentAuthenticatedUser();
        final Date date = stringToSqlDate(dateString);
        userService.updateWeight(user, date, weightEntry);

        return viewMainProfilePage(dateString, model);
    }

}
