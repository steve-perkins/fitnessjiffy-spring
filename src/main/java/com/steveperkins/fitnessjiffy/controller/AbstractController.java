package com.steveperkins.fitnessjiffy.controller;

import com.steveperkins.fitnessjiffy.config.SecurityConfig;
import com.steveperkins.fitnessjiffy.dto.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class AbstractController {

    private class StringEditor implements PropertyEditor {

        private String text;

        @Override
        public void setValue(final Object value) {
            this.text = String.valueOf(value);
        }

        @Override
        public Object getValue() {
            return this.text;
        }

        @Override
        public boolean isPaintable() {
            return false;
        }

        @Override
        public void paintValue(final Graphics gfx, final Rectangle box) {

        }

        @Override
        public String getJavaInitializationString() {
            return null;
        }

        @Override
        public String getAsText() {
            return this.text;
        }

        @Override
        public void setAsText(final String text) throws IllegalArgumentException {
            if (TODAY.equals(text)) {
                setValue(dateFormat.format(new Date()));
            } else {
                setValue(text);
            }
        }

        @Override
        public String[] getTags() {
            return new String[0];
        }

        @Override
        public Component getCustomEditor() {
            return null;
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {

        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {

        }
    }

    public static final String LOGIN_TEMPLATE = "login";
    public static final String PROFILE_TEMPLATE = "profile";
    public static final String FOOD_TEMPLATE = "food";
    public static final String EXERCISE_TEMPLATE = "exercise";
    public static final String REPORT_TEMPLATE = "report";

    public static final String TODAY = "today";

    protected final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * When a controller method parameter has a default value of TODAY, the binder registration here
     * will cause the default value to be the current date in "yyyy-MM-dd" format.
     */
    @InitBinder
    public void initBinder(@Nonnull final WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(String.class, new StringEditor());
    }

    /**
     * Used by child class controllers to obtain the currently authenticated user from Spring Security.
     */
    @Nullable
    protected final UserDTO currentAuthenticatedUser() {
        UserDTO userDTO = null;
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityConfig.SpringUserDetails) {
            final SecurityConfig.SpringUserDetails userDetails = (SecurityConfig.SpringUserDetails) authentication.getPrincipal();
            userDTO = userDetails.getUserDTO();
        }
        return userDTO;
    }

    @Nullable
    protected final java.sql.Date stringToSqlDate(@Nonnull final String dateString) {
        java.sql.Date date = null;
        try {
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            final Date utilDate = dateFormat.parse(dateString);
            date = new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
        }
        return date;
    }

}
