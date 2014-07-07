package net.steveperkins.fitnessjiffy.config;

import net.steveperkins.fitnessjiffy.controller.AbstractController;
import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodEatenToFoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodToFoodDTO;
import net.steveperkins.fitnessjiffy.dto.converter.UserToUserDTO;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Nonnull;
import javax.servlet.MultipartConfigElement;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Bean
    @Nonnull
    UserService userService() {
        return new UserService();
    }

    @Bean
    @Nonnull
    FoodService foodService() {
        return new FoodService();
    }

    @Bean
    @Nonnull
    Converter<User, UserDTO> userDTOConverter() {
        return new UserToUserDTO();
    }

    @Bean
    @Nonnull
    Converter<Food, FoodDTO> foodDTOConverter() {
        return new FoodToFoodDTO();
    }

    @Bean
    @Nonnull
    Converter<FoodEaten, FoodEatenDTO> foodEatenDTOConverter() {
        return new FoodEatenToFoodEatenDTO();
    }

    /** Needed to support file uploads. */
    @Bean
    @Nonnull
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        super.addViewControllers(registry);
        registry.addViewController("/login").setViewName(AbstractController.LOGIN_TEMPLATE);
    }

}
