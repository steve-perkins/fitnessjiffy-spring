package net.steveperkins.fitnessjiffy.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public final class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private Converter<User, UserDTO> userDTOConverter;

    @Autowired
    private Converter<Weight, WeightDTO> weightDTOConverter;

    @Nullable
    public UserDTO findUser(@Nonnull final UUID userId) {
        final User user = userRepository.findOne(userId);
        return userToDTO(user);
    }

    @Nonnull
    public List<UserDTO> findAllUsers() {
        return Lists.transform(Lists.newLinkedList(userRepository.findAll()), new Function<User, UserDTO>() {
            @Nullable
            @Override
            public UserDTO apply(@Nullable final User user) {
                return userToDTO(user);
            }
        });
    }

    public void createUser(@Nonnull final UserDTO userDTO) {
        final User user = new User(
                userDTO.getId(),
                userDTO.getGender(),
                userDTO.getBirthdate(),
                userDTO.getHeightInInches(),
                userDTO.getActivityLevel(),
                userDTO.getEmail(),
                // TODO: How to set password, without necessarily making it part of the DTO?  Probably add an extra parameter to this service method for the plain-text password, and perform the hashing here...
                null,
                userDTO.getFirstName(),
                userDTO.getLastName(),
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(new java.util.Date().getTime())
        );
        userRepository.save(user);
    }

    @Nullable
    public WeightDTO findWeightOnDate(
            @Nonnull final UserDTO userDTO,
            @Nonnull final Date date
    ) {
        final User user = userRepository.findOne(userDTO.getId());
        final Weight weight = weightRepository.findByUserAndDate(user, date);
        return weightDTOConverter.convert(weight);
    }

    public void updateWeight(
            @Nonnull final UserDTO userDTO,
            @Nonnull final Date date,
            @Nonnull final double pounds
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
    }

    @Nullable
    private UserDTO userToDTO(@Nullable final User user) {
        return userDTOConverter.convert(user);
    }

}
