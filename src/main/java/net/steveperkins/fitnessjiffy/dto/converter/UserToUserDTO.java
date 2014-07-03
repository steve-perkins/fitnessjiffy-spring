package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.joda.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class UserToUserDTO implements Converter<User, UserDTO> {

    @Autowired
    private WeightRepository weightRepository;

    @Override
    @Nullable
    public UserDTO convert(@Nullable final User user) {
        UserDTO dto = null;
        if (user != null) {
            final double currentWeight = getCurrentWeight(user);
            dto = new UserDTO(
                    user.getId(),
                    user.getGender(),
                    user.getBirthdate(),
                    user.getHeightInInches(),
                    user.getActivityLevel(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    currentWeight,
                    getBmi(user, currentWeight),
                    getMaintenanceCalories(user, currentWeight),
                    getDailyPoints(user, currentWeight)
            );
        }
        return dto;
    }

    private double getCurrentWeight(@Nonnull final User user) {
        final List<Weight> weights = weightRepository.findByUserOrderByDateDesc(user);
        double currentWeight = 0;
        if (weights != null && !weights.isEmpty()) {
            currentWeight = weights.get(0).getPounds();
        }
        return currentWeight;
    }

    private double getBmi(
            @Nonnull final User user,
            final double currentWeight
    ) {
        double bmi = 0;
        if (currentWeight != 0 && user.getHeightInInches() != 0) {
            bmi = (currentWeight * 703) / (user.getHeightInInches() * user.getHeightInInches());
        }
        return bmi;
    }

    private int getMaintenanceCalories(
            @Nonnull final User user,
            final double currentWeight
    ) {
        int maintenanceCalories = 0;
        final int age = Years.yearsBetween(new DateTime(user.getBirthdate().getTime()), new DateTime()).getYears();
        if (user.getGender() != null
                && currentWeight != 0
                && user.getHeightInInches() != 0
                && age != 0
                && user.getActivityLevel() != null
                ) {
            final double centimeters = user.getHeightInInches() * 2.54f;
            final double kilograms = currentWeight / 2.2f;
            final double adjustedWeight = user.getGender().equals(User.Gender.FEMALE) ? 655f + (9.6f * kilograms) : 66f + (13.7f * kilograms);
            final double adjustedHeight = user.getGender().equals(User.Gender.FEMALE) ? 1.7f * centimeters : 5f * centimeters;
            final float adjustedAge = user.getGender().equals(User.Gender.FEMALE) ? 4.7f * age : 6.8f * age;
            maintenanceCalories = (int) ((adjustedWeight + adjustedHeight - adjustedAge) * user.getActivityLevel().getValue());
        }
        return maintenanceCalories;
    }

    public int getDailyPoints(
            @Nonnull final User user,
            final double currentWeight
    ) {
        int dailyPoints = 0;
        final int age = Years.yearsBetween(new DateTime(user.getBirthdate().getTime()), new DateTime()).getYears();
        if (user.getGender() != null
                && age != 0
                && currentWeight != 0
                && user.getHeightInInches() != 0
                && user.getActivityLevel() != null
                ) {
            // Factor in gender
            dailyPoints = user.getGender().equals(User.Gender.FEMALE) ? 2 : 8;
            // Factor in age
            if (age <= 26) {
                dailyPoints += 4;
            } else if (age <= 37) {
                dailyPoints += 3;
            } else if (age <= 47) {
                dailyPoints += 2;
            } else if (age <= 58) {
                dailyPoints += 1;
            }
            // Factor in weight
            dailyPoints += (currentWeight / 10f);
            // Factor in height
            if (user.getHeightInInches() >= 61 && user.getHeightInInches() <= 70) {
                dailyPoints += 1;
            } else if (user.getHeightInInches() > 70) {
                dailyPoints += 2;
            }
            // Factor in activity level
            if (user.getActivityLevel().equals(User.ActivityLevel.EXTREMELY_ACTIVE) || user.getActivityLevel().equals(User.ActivityLevel.VERY_ACTIVE)) {
                dailyPoints += 6;
            } else if (user.getActivityLevel().equals(User.ActivityLevel.MODERATELY_ACTIVE)) {
                dailyPoints += 4;
            } else if (user.getActivityLevel().equals(User.ActivityLevel.LIGHTLY_ACTIVE)) {
                dailyPoints += 2;
            }
            // Factor in daily "flex" points quota
            dailyPoints += 5;
        }
        return dailyPoints;
    }

}
