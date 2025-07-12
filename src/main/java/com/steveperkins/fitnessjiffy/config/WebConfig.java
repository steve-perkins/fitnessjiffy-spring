package com.steveperkins.fitnessjiffy.config;

import com.steveperkins.fitnessjiffy.controller.AbstractController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;
import jakarta.servlet.MultipartConfigElement;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Needed to support file uploads. */
    @Bean
    @Nonnull
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName(AbstractController.LOGIN_TEMPLATE);
    }
}
