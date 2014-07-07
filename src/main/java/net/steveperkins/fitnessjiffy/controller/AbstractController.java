package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.config.SecurityConfig;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import sun.beans.editors.StringEditor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractController {

    public static final String LOGIN_TEMPLATE = "login";
    public static final String PROFILE_TEMPLATE = "profile";
    public static final String FOOD_TEMPLATE = "food";
    public static final String ADMIN_TEMPLATE = "admin";

    public static final String TODAY = "today";

    protected final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * When a controller method parameter has a default value of TODAY, the binder registration here
     * will cause the default value to be the current date in "yyyy-MM-dd" format.
     */
    @InitBinder
    public void initBinder(@Nonnull final WebDataBinder binder) throws Exception {
        final StringEditor stringEditor = new StringEditor() {
            @Override
            public void setAsText(final String text) {
                if (TODAY.equals(text)) {
                    setValue(dateFormat.format(new Date()));
                } else {
                    super.setAsText(text);
                }
            }
        };
        binder.registerCustomEditor(String.class, stringEditor);
    }

    /**
     * Used by child class controllers to obtain the currently authenticated user from Spring Security.
     */
    @Nullable
    protected final UserDTO currentAuthenticatedUser() {
        UserDTO userDTO = null;
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityConfig.SpringUserDetails) {
            final SecurityConfig.SpringUserDetails userDetails = (SecurityConfig.SpringUserDetails) auth.getPrincipal();
            userDTO = userDetails.getUserDTO();
        }
        return userDTO;
    }

    @Nullable
    protected final java.sql.Date stringToSqlDate(@Nonnull final String dateString) {
        java.sql.Date date = null;
        try {
            final Date utilDate = dateFormat.parse(dateString);
            date = new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
        }
        return date;
    }

}
