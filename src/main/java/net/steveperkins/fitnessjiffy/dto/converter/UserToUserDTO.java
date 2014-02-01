package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import org.springframework.core.convert.converter.Converter;

public final class UserToUserDTO implements Converter<User, UserDTO> {

    @Override
    public UserDTO convert(User user) {
        if(user == null) {
            return null;
        }
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
                user.getCurrentWeight(),
                user.getBmi(),
                user.getMaintenanceCalories(),
                user.getDailyPoints()
        );
    }

}
