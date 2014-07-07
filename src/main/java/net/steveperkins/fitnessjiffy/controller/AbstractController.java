package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.config.SecurityConfig;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Nullable;

public abstract class AbstractController {

    public static final String LOGIN_TEMPLATE = "login";

    public static final String USER_TEMPLATE = "user";

    public static final String FOOD_TEMPLATE = "food";

    public static final String ADMIN_TEMPLATE = "admin";

    @Nullable
    protected UserDTO currentAuthenticatedUser() {
        UserDTO userDTO = null;
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityConfig.SpringUserDetails) {
            final SecurityConfig.SpringUserDetails userDetails = (SecurityConfig.SpringUserDetails) auth.getPrincipal();
            userDTO = userDetails.getUserDTO();
        }
        return userDTO;
    }

}
