package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

public final class UserToUserDTO implements Converter<User, UserDTO> {

    @Autowired
    private WeightRepository weightRepository;

    @Override
    public UserDTO convert(User user) {
        if(user == null) {
            return null;
        }
        double currentWeight = getCurrentWeight(user);
        return new UserDTO(
                user.getId(),
                user.getGender(),
                user.getAge(),
                user.getHeightInInches(),
                user.getActivityLevel(),
                user.getUsername(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                currentWeight,
                getBmi(user, currentWeight),
                getMaintenanceCalories(user, currentWeight),
                getDailyPoints(user, currentWeight)
        );
    }

    private double getCurrentWeight(User user) {
        List<Weight> weights = weightRepository.findByUserOrderByDateDesc(user);
        if(weights == null || weights.isEmpty()) {
            return 0;
        } else {
            return weights.get(0).getPounds();
        }
    }

    private double getBmi(User user, double currentWeight) {
        if(currentWeight == 0 || user.getHeightInInches() == 0) {
            return 0;
        } else {
            return (currentWeight * 703) / (user.getHeightInInches() * user.getHeightInInches());
        }
    }

    private int getMaintenanceCalories(User user, double currentWeight) {
        if(user.getGender() == null || currentWeight == 0 || user.getHeightInInches() == 0 || user.getAge() == 0 || user.getActivityLevel() == null) {
            return 0;
        } else {
            double centimeters = user.getHeightInInches() * 2.54f;
            double kilograms   = currentWeight / 2.2f;
            double adjustedWeight = user.getGender().equals(User.Gender.FEMALE) ? 655f + (9.6f * kilograms) : 66f + (13.7f * kilograms);
            double adjustedHeight = user.getGender().equals(User.Gender.FEMALE) ? 1.7f * centimeters : 5f * centimeters;
            float adjustedAge = user.getGender().equals(User.Gender.FEMALE) ? 4.7f * user.getAge() : 6.8f * user.getAge();
            return (int) ((adjustedWeight + adjustedHeight - adjustedAge) * user.getActivityLevel().getValue());
        }
    }

    public int getDailyPoints(User user, double currentWeight) {
        if(user.getGender() == null || user.getAge() == 0 || currentWeight == 0 || user.getHeightInInches() == 0 || user.getActivityLevel() == null) {
            return 0;
        } else {
            // Factor in gender
            int dailyPoints = user.getGender().equals(User.Gender.FEMALE) ? 2 : 8;
            // Factor in age
            if(user.getAge() <= 26) {
                dailyPoints += 4;
            } else if(user.getAge() <= 37) {
                dailyPoints += 3;
            } else if(user.getAge() <= 47) {
                dailyPoints += 2;
            } else if(user.getAge() <= 58) {
                dailyPoints += 1;
            }
            // Factor in weight
            dailyPoints += (currentWeight / 10f);
            // Factor in height
            if(user.getHeightInInches() >= 61 && user.getHeightInInches() <= 70) {
                dailyPoints += 1;
            } else if(user.getHeightInInches() > 70) {
                dailyPoints += 2;
            }
            // Factor in activity level
            if(user.getActivityLevel().equals(User.ActivityLevel.EXTREMELY_ACTIVE) || user.getActivityLevel().equals(User.ActivityLevel.VERY_ACTIVE)) {
                dailyPoints += 6;
            } else if(user.getActivityLevel().equals(User.ActivityLevel.MODERATELY_ACTIVE)) {
                dailyPoints += 4;
            } else if(user.getActivityLevel().equals(User.ActivityLevel.LIGHTLY_ACTIVE)) {
                dailyPoints += 2;
            }
            // Factor in daily "flex" points quota
            dailyPoints += 5;
            return dailyPoints;
        }
    }

}
