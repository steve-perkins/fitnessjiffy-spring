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
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import net.steveperkins.fitnessjiffy.dto.converter.ExercisePerformedToExercisePerformedDTO;
import net.steveperkins.fitnessjiffy.dto.converter.ExerciseToExerciseDTO;
import net.steveperkins.fitnessjiffy.dto.converter.UserToUserDTO;
import net.steveperkins.fitnessjiffy.repository.ExercisePerformedRepository;
import net.steveperkins.fitnessjiffy.repository.ExerciseRepository;
import net.steveperkins.fitnessjiffy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

@Service
public final class ExerciseService {

    private final UserService userService;
    private final ReportDataService reportDataService;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExercisePerformedRepository exercisePerformedRepository;
    private final UserToUserDTO userDTOConverter;
    private final ExerciseToExerciseDTO exerciseDTOConverter;
    private final ExercisePerformedToExercisePerformedDTO exercisePerformedDTOConverter;

    private final Function<Exercise, ExerciseDTO> exerciseToDTOConversionFunction =
            new Function<Exercise, ExerciseDTO>() {
                @Nullable
                @Override
                public ExerciseDTO apply(final @Nullable Exercise exercise) {
                    return exerciseDTOConverter.convert(exercise);
                }
            };

    @Autowired
    public ExerciseService(
            @Nonnull final UserService userService,
            @Nonnull final ReportDataService reportDataService,
            @Nonnull final UserRepository userRepository,
            @Nonnull final ExerciseRepository exerciseRepository,
            @Nonnull final ExercisePerformedRepository exercisePerformedRepository,
            @Nonnull final UserToUserDTO userDTOConverter,
            @Nonnull final ExerciseToExerciseDTO exerciseDTOConverter,
            @Nonnull final ExercisePerformedToExercisePerformedDTO exercisePerformedDTOConverter
    ) {
        this.userService = userService;
        this.reportDataService = reportDataService;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.exercisePerformedRepository = exercisePerformedRepository;
        this.userDTOConverter = userDTOConverter;
        this.exerciseDTOConverter = exerciseDTOConverter;
        this.exercisePerformedDTOConverter = exercisePerformedDTOConverter;
    }

    @Nonnull
    public final List<ExercisePerformedDTO> findPerformedOnDate(
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
    public final List<ExerciseDTO> findPerformedRecently(
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

    public final void addExercisePerformed(
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
            reportDataService.updateUserFromDate(userId, date);
        }
    }

    public final void updateExercisePerformed(
            @Nonnull final UUID exercisePerformedId,
            final int minutes
    ) {
        final ExercisePerformed exercisePerformed = exercisePerformedRepository.findOne(exercisePerformedId);
        exercisePerformed.setMinutes(minutes);
        exercisePerformedRepository.save(exercisePerformed);
        reportDataService.updateUserFromDate(exercisePerformed.getUser().getId(), exercisePerformed.getDate());
    }

    public final void deleteExercisePerformed(@Nonnull final UUID exercisePerformedId) {
        final ExercisePerformed exercisePerformed = exercisePerformedRepository.findOne(exercisePerformedId);
        exercisePerformedRepository.delete(exercisePerformed);
        reportDataService.updateUserFromDate(exercisePerformed.getUser().getId(), exercisePerformed.getDate());
    }

    @Nullable
    public final ExercisePerformedDTO findExercisePerformedById(@Nonnull final UUID exercisePerformedId) {
        final ExercisePerformed exercisePerformed = exercisePerformedRepository.findOne(exercisePerformedId);
        return exercisePerformedDTOConverter.convert(exercisePerformed);
    }

    @Nonnull
    public final List<String> findAllCategories() {
        return exerciseRepository.findAllCategories();
    }

    @Nonnull
    public final List<ExerciseDTO> findExercisesInCategory(@Nonnull final String category) {
        final List<Exercise> exercises = exerciseRepository.findByCategoryOrderByDescriptionAsc(category);
        return Lists.transform(exercises, exerciseToDTOConversionFunction);
    }

    @Nonnull
    public final List<ExerciseDTO> searchExercises(@Nonnull final String searchString) {
        final List<Exercise> exercises = exerciseRepository.findByDescriptionLike(searchString);
        return Lists.transform(exercises, exerciseToDTOConversionFunction);
    }

    public static int calculateCaloriesBurned(
            final double metabolicEquivalent,
            final int minutes,
            final double weightInPounds
    ) {
        final double weightInKilograms = weightInPounds / 2.2;
        return (int) (metabolicEquivalent * 3.5 * weightInKilograms / 200 * minutes);
    }

    public static double calculatePointsBurned(
            final double metabolicEquivalent,
            final int minutes,
            final double weightInPounds
    ) {
        final int caloriesBurnedPerHour = calculateCaloriesBurned(metabolicEquivalent, 60, weightInPounds);
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
