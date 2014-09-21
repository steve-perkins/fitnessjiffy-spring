package net.steveperkins.fitnessjiffy.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.steveperkins.fitnessjiffy.domain.Exercise;
import net.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.dto.ExerciseDTO;
import net.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import net.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import net.steveperkins.fitnessjiffy.repository.ExerciseRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import net.steveperkins.fitnessjiffy.repository.WeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public final class ExerciseService {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WeightRepository weightRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    ExercisePerformedRepository exercisePerformedRepository;

    @Autowired
    Converter<User, UserDTO> userDTOConverter;

    @Autowired
    Converter<Exercise, ExerciseDTO> exerciseDTOConverter;

    @Autowired
    Converter<ExercisePerformed, ExercisePerformedDTO> exercisePerformedDTOConverter;

    private final Function<Exercise, ExerciseDTO> exerciseToDTOConversionFunction =
            new Function<Exercise, ExerciseDTO>() {
                @Nullable
                @Override
                public ExerciseDTO apply(final @Nullable Exercise exercise) {
                    return exerciseDTOConverter.convert(exercise);
                }
            };

    @Nonnull
    public List<ExercisePerformedDTO> findPerformedOnDate(
            @Nonnull final UUID userId,
            @Nonnull final Date date
    ) {
        final User user = userRepository.findOne(userId);
        final WeightDTO weight = userService.findWeightOnDate(userDTOConverter.convert(user), date);

        final List<ExercisePerformed> exercisesPerformed = exercisePerformedRepository.findByUserEqualsAndDateEquals(user, date);
        return Lists.transform(exercisesPerformed, new Function<ExercisePerformed, ExercisePerformedDTO>() {
            @Nullable
            @Override
            public ExercisePerformedDTO apply(@Nullable final ExercisePerformed exercisePerformed) {
                final ExercisePerformedDTO dto = exercisePerformedDTOConverter.convert(exercisePerformed);
                if (dto != null) {
                    final int caloriesBurned = calculateCaloriesBurned(
                            exercisePerformed.getExercise().getMetabolicEquivalent(),
                            exercisePerformed.getMinutes(),
                            weight.getPounds()
                    );
                    dto.setCaloriesBurned(caloriesBurned);
                    final double pointsBurned = calculatePointsBurned(
                            exercisePerformed.getExercise().getMetabolicEquivalent(),
                            exercisePerformed.getMinutes(),
                            weight.getPounds()
                    );
                    dto.setPointsBurned(pointsBurned);
                }
                return dto;
            }
        });
    }

    @Nonnull
    public List<ExerciseDTO> findPerformedRecently(
            @Nonnull final UUID userId,
            @Nonnull final Date currentDate
    ) {
        final User user = userRepository.findOne(userId);
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -14);
        final Date twoWeeksAgo = new Date(calendar.getTime().getTime());
        final List<Exercise> recentExercises = exercisePerformedRepository.findByUserPerformedWithinRange(
                user,
                new Date(twoWeeksAgo.getTime()),
                new Date(currentDate.getTime())
        );
        return Lists.transform(recentExercises, exerciseToDTOConversionFunction);
    }

    public void addExercisePerformed(
            @Nonnull final UUID userId,
            @Nonnull final UUID exerciseId,
            @Nonnull final Date date
    ) {
        final boolean duplicate = Iterables.any(findPerformedOnDate(userId, date), new Predicate<ExercisePerformedDTO>() {
            @Override
            public boolean apply(@Nonnull final ExercisePerformedDTO exerciseAlreadyPerformed) {
                return exerciseAlreadyPerformed.getExercise().getId().equals(exerciseId);
            }
        });
        if (!duplicate) {
            final User user = userRepository.findOne(userId);
            final Exercise exercise = exerciseRepository.findOne(exerciseId);
            final ExercisePerformed exercisePerformed = new ExercisePerformed(
                    UUID.randomUUID(),
                    user,
                    exercise,
                    date,
                    1
            );
            exercisePerformedRepository.save(exercisePerformed);
        }
    }

    public void updateExercisePerformed(
            @Nonnull final UUID exercisePerformedId,
            @Nonnull final int minutes
    ) {
        final ExercisePerformed exercisePerformed = exercisePerformedRepository.findOne(exercisePerformedId);
        exercisePerformed.setMinutes(minutes);
        exercisePerformedRepository.save(exercisePerformed);
    }

    public void deleteExercisePerformed(@Nonnull final UUID exercisePerformedId) {
        final ExercisePerformed exercisePerformed = exercisePerformedRepository.findOne(exercisePerformedId);
        exercisePerformedRepository.delete(exercisePerformed);
    }

    @Nullable
    public ExercisePerformedDTO findExercisePerformedById(@Nonnull final UUID exercisePerformedId) {
        final ExercisePerformed exercisePerformed = exercisePerformedRepository.findOne(exercisePerformedId);
        return exercisePerformedDTOConverter.convert(exercisePerformed);
    }

    @Nonnull
    public List<String> findAllCategories() {
        return exerciseRepository.findAllCategories();
    }

    @Nonnull
    public List<ExerciseDTO> findExercisesInCategory(@Nonnull final String category) {
        final List<Exercise> exercises = exerciseRepository.findByCategoryOrderByDescriptionAsc(category);
        return Lists.transform(exercises, exerciseToDTOConversionFunction);
    }

    @Nonnull
    public List<ExerciseDTO> searchExercises(@Nonnull final String searchString) {
        final List<Exercise> exercises = exerciseRepository.findByDescriptionLike(searchString);
        return Lists.transform(exercises, exerciseToDTOConversionFunction);
    }

    private int calculateCaloriesBurned(
            final double metabolicEquivalent,
            final int minutes,
            final double weightInPounds
    ) {
        final double weightInKilograms = weightInPounds / 2.2;
        return (int) (metabolicEquivalent * 3.5 * weightInKilograms / 200 * minutes);
    }

    private double calculatePointsBurned(
            final double metabolicEquivalent,
            final int minutes,
            final double weightInPounds
    ) {
        final double caloriesBurnedPerHour = calculateCaloriesBurned(metabolicEquivalent, 60, weightInPounds);
        double pointsBurned;
        if (caloriesBurnedPerHour < 400) {
            pointsBurned = weightInPounds * minutes * 0.000232;
        } else if (caloriesBurnedPerHour < 900) {
            pointsBurned = weightInPounds * minutes * 0.000327;
        } else {
            pointsBurned = weightInPounds * minutes * 0.0008077;
        }
        return pointsBurned;
    }

}
