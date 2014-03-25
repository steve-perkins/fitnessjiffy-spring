package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Converter<User, UserDTO> userDTOConverter;

    public UserDTO getUser(UUID userId) {
        User user = userRepository.findOne(userId);
        return userToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        for(User user : userRepository.findAll()) {
            users.add(userToDTO(user));
        }
        return users;
    }

    public void createUser(UserDTO userDTO) {
        User user = new User(
                userDTO.getId(),
                userDTO.getGender(),
                userDTO.getAge(),
                userDTO.getHeightInInches(),
                userDTO.getActivityLevel(),
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.isActive()
        );
        userRepository.save(user);
    }


    private UserDTO userToDTO(User user) {
        return userDTOConverter.convert(user);
    }

}
