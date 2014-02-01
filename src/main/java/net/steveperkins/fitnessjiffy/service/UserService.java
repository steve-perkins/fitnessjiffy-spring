package net.steveperkins.fitnessjiffy.service;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUser(UUID userId) {
        return userRepository.findOne(userId);
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

}
