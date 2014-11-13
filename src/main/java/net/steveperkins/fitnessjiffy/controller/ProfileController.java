package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.config.SecurityConfig;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import java.sql.Date;

@Controller
public final class ProfileController extends AbstractController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/", "/profile"}, method = RequestMethod.GET)
    @Nonnull
    public final String viewMainProfilePage(
            @Nonnull
            @RequestParam(value = "date", defaultValue = TODAY)
            final String dateString,

            @Nonnull final Model model
    ) {

        // TODO: Add Datepicker widget to "Birthdate" field in template, and also change "Height in Inches" to more user-friendly pulldown selectors for feet and inches.

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
    public final String updateProfile(
            @Nonnull
            @RequestParam(value = "date", defaultValue = TODAY)
            final String dateString,

            @Nonnull
            @RequestParam(value = "currentPassword")
            final String currentPassword,

            @Nonnull
            @RequestParam(value = "newPassword")
            final String newPassword,

            @Nonnull
            @RequestParam(value = "reenterNewPassword")
            final String reenterNewPassword,

            @Nonnull
            @ModelAttribute("user")
            final UserDTO user,

            @Nonnull
            final BindingResult result,

            @Nonnull
            final Model model
    ) {

        if (currentPassword == null || currentPassword.isEmpty()) {
            model.addAttribute("profileSaveError", "You must verify the current password in order to make any changes to this profile.");
        } else if (!userService.verifyPassword(user, currentPassword)) {
            model.addAttribute("profileSaveError", "The password entered does not match the current password.");
        } else if (newPassword != null && !newPassword.isEmpty() && reenterNewPassword != null && !reenterNewPassword.equals(newPassword)) {
            model.addAttribute("profileSaveError", "The 'New Password' and 'Re-enter New Password' fields did not match.");
        } else {
            // Update user in the database
            userService.updateUser(user, newPassword);

            // Update the user in the active Spring Security session
            final UserDTO updatedUser = userService.findUser(user.getId());  // re-calc BMI, daily calorie needs, etc
            final String passwordHash = userService.getPasswordHash(user);
            final SecurityConfig.SpringUserDetails newUserDetails = new SecurityConfig.SpringUserDetails(updatedUser, passwordHash);
            final UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(newUserDetails, passwordHash, null);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
        return viewMainProfilePage(dateString, model);
    }

    @RequestMapping(value = {"/profile/weight/save"}, method = RequestMethod.POST)
    @Nonnull
    public final String createOrUpdateWeight(
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
