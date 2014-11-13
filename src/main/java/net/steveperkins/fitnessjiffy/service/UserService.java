package net.steveperkins.fitnessjiffy.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.steveperkins.fitnessjiffy.config.SecurityConfig;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import net.steveperkins.fitnessjiffy.dto.converter.UserToUserDTO;
import net.steveperkins.fitnessjiffy.dto.converter.WeightToWeightDTO;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public final class UserService {

    @Autowired
    private ReportDataService reportDataService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private UserToUserDTO userDTOConverter;

    @Autowired
    private WeightToWeightDTO weightDTOConverter;

    @Nullable
    public UserDTO findUser(@Nonnull final UUID userId) {
        final User user = userRepository.findOne(userId);
        return userDTOConverter.convert(user);
    }

    @Nonnull
    public List<UserDTO> findAllUsers() {
        return Lists.transform(Lists.newLinkedList(userRepository.findAll()), new Function<User, UserDTO>() {
            @Nullable
            @Override
            public UserDTO apply(@Nullable final User user) {
                return userDTOConverter.convert(user);
            }
        });
    }

    public void createUser(
            @Nonnull final UserDTO userDTO,
            @Nonnull final String password
    ) {
        final User user = new User(
                userDTO.getId(),
                userDTO.getGender(),
                userDTO.getBirthdate(),
                userDTO.getHeightInInches(),
                userDTO.getActivityLevel(),
                userDTO.getEmail(),
                encryptPassword(password),
                userDTO.getFirstName(),
                userDTO.getLastName(),
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(new java.util.Date().getTime())
        );
        userRepository.save(user);
        reportDataService.updateUserFromDate(userDTO.getId(), new Date(System.currentTimeMillis()));
    }

    public void updateUser(
            @Nonnull final UserDTO userDTO,
            @Nullable final String newPassword
    ) {
        final User user = userRepository.findOne(userDTO.getId());
        user.setGender(userDTO.getGender());
        user.setBirthdate(userDTO.getBirthdate());
        user.setHeightInInches(userDTO.getHeightInInches());
        user.setActivityLevel(userDTO.getActivityLevel());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setLastUpdatedTime(new Timestamp(new java.util.Date().getTime()));
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPasswordHash(encryptPassword(newPassword));
        }
        userRepository.save(user);
        reportDataService.updateUserFromDate(userDTO.getId(), new Date(System.currentTimeMillis()));
        refreshAuthenticatedUser();
    }

    public void refreshAuthenticatedUser() {
        final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuthentication.getPrincipal() instanceof SecurityConfig.SpringUserDetails) {
            final SecurityConfig.SpringUserDetails currentPrincipal = (SecurityConfig.SpringUserDetails) currentAuthentication.getPrincipal();
            final User refreshedUser = userRepository.findOne(currentPrincipal.getUserDTO().getId());
            final SecurityConfig.SpringUserDetails refreshedPrincipal = new SecurityConfig.SpringUserDetails(userDTOConverter.convert(refreshedUser), refreshedUser.getPasswordHash());
            final Authentication newAuthentication = new UsernamePasswordAuthenticationToken(refreshedPrincipal, refreshedUser.getPasswordHash(), currentPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        } else {
            System.out.println("The currently-authenticated principal is not an instance of type SecurityConfig.SpringUserDetails");
        }
    }

    @Nullable
    public WeightDTO findWeightOnDate(
            @Nonnull final UserDTO userDTO,
            @Nonnull final Date date
    ) {

        // TODO:  This logic should account for days on which no weight entry exists in the database, search backwards for the weight entry most recent to that date instead.

        final User user = userRepository.findOne(userDTO.getId());
        final Weight weight = weightRepository.findByUserMostRecentOnDate(user, date);
        return weightDTOConverter.convert(weight);
    }

    public void updateWeight(
            @Nonnull final UserDTO userDTO,
            @Nonnull final Date date,
            final double pounds
    ) {
        final User user = userRepository.findOne(userDTO.getId());
        Weight weight = weightRepository.findByUserAndDate(user, date);
        if (weight == null) {
            weight = new Weight(
                    UUID.randomUUID(),
                    user,
                    date,
                    pounds
            );
        } else {
            weight.setPounds(pounds);
        }
        weightRepository.save(weight);
        reportDataService.updateUserFromDate(userDTO.getId(), date);
        refreshAuthenticatedUser();
    }

    public boolean verifyPassword(
            @Nonnull final UserDTO userDTO,
            @Nonnull final String password
    ) {
        final User user = userRepository.findOne(userDTO.getId());
        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    @Nonnull
    public String encryptPassword(@Nonnull final String rawPassword) {
        final String salt = BCrypt.gensalt(10, new SecureRandom());
        return BCrypt.hashpw(rawPassword, salt);
    }

    @Nullable
    public String getPasswordHash(@Nonnull final UserDTO userDTO) {
        final User user = userRepository.findOne(userDTO.getId());
        return user.getPasswordHash();
    }

}
