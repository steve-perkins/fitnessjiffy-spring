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

    public User getUser(UUID userId) {
        return userRepository.findOne(userId);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for(User user : userRepository.findAll()) {
            users.add(user);
        }
        return users;
    }

    // TODO: Maybe the Service layer should not return JPA entity objects AT ALL... refactoring these converter methods as private, and changing the signatures of the methods currently returning entities.  We'll see as development progresses whether anything above the Server layer truly needs a JPA entity object.
    public UserDTO userToDTO(User user) {
        return userDTOConverter.convert(user);
    }

    public List<UserDTO> userToDTO(List<User> users) {
        List<UserDTO> dtos = new ArrayList<>();
        for(User user : users) {
            dtos.add(userDTOConverter.convert(user));
        }
        return dtos;
    }

}
