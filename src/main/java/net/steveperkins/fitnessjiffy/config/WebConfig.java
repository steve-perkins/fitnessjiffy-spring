package net.steveperkins.fitnessjiffy.config;

import net.steveperkins.fitnessjiffy.controller.AbstractController;
import net.steveperkins.fitnessjiffy.dto.converter.ExercisePerformedToExercisePerformedDTO;
import net.steveperkins.fitnessjiffy.dto.converter.ExerciseToExerciseDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodEatenToFoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodToFoodDTO;
import net.steveperkins.fitnessjiffy.dto.converter.ReportDataToReportDataDTO;
import net.steveperkins.fitnessjiffy.dto.converter.UserToUserDTO;
import net.steveperkins.fitnessjiffy.dto.converter.WeightToWeightDTO;
import net.steveperkins.fitnessjiffy.service.ExerciseService;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.ReportDataService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    ExerciseService exerciseService() {
        return new ExerciseService();
    }

    @Bean
    @Nonnull
    ReportDataService reportDataService() {
        return new ReportDataService();
    }

    @Bean
    @Nonnull
    UserToUserDTO userDTOConverter() {
        return new UserToUserDTO();
    }

    @Bean
    @Nonnull
    WeightToWeightDTO weightDTOConverter() {
        return new WeightToWeightDTO();
    }

    @Bean
    @Nonnull
    FoodToFoodDTO foodDTOConverter() {
        return new FoodToFoodDTO();
    }

    @Bean
    @Nonnull
    FoodEatenToFoodEatenDTO foodEatenDTOConverter() {
        return new FoodEatenToFoodEatenDTO();
    }

    @Bean
    @Nonnull
    ExerciseToExerciseDTO exerciseDTOConverter() {
        return new ExerciseToExerciseDTO();
    }

    @Bean
    @Nonnull
    ReportDataToReportDataDTO reportDataDTOConverter() {
        return new ReportDataToReportDataDTO();
    }

    @Bean
    @Nonnull
    ExercisePerformedToExercisePerformedDTO exercisePerformedDTOConverter() {
        return new ExercisePerformedToExercisePerformedDTO();
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
