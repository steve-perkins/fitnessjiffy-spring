package net.steveperkins.fitnessjiffy;

import java.util.Arrays;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.FoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.FoodDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodEatenToFoodEatenDTO;
import net.steveperkins.fitnessjiffy.dto.converter.FoodToFoodDTO;
import net.steveperkins.fitnessjiffy.dto.converter.UserToUserDTO;
import net.steveperkins.fitnessjiffy.interceptor.AuthenticationInterceptor;
import net.steveperkins.fitnessjiffy.service.AuthenticationService;
import net.steveperkins.fitnessjiffy.service.FoodService;
import net.steveperkins.fitnessjiffy.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Nonnull;
import javax.servlet.MultipartConfigElement;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories
@PropertySource("classpath:application.properties")
public class Application extends WebMvcConfigurerAdapter {

    @Bean
    @Nonnull
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    @Bean
    @Nonnull
    AuthenticationService authenticationService() {
        return new AuthenticationService();
    }

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

    @Override
    public void addInterceptors(@Nonnull final InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login");
    }

    public static void main(final String[] args) {
        final ApplicationContext ctx = SpringApplication.run(Application.class, args);

        System.out.println("Let's inspect the beans provided by Spring Boot:");

        final String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (final String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

}
