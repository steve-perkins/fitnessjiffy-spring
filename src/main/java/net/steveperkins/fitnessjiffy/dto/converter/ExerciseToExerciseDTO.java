package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.Exercise;
import net.steveperkins.fitnessjiffy.dto.ExerciseDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public final class ExerciseToExerciseDTO implements Converter<Exercise, ExerciseDTO> {

    @Override
    @Nullable
    public ExerciseDTO convert(@Nullable final Exercise exercise) {
        ExerciseDTO dto = null;
        if (exercise != null) {
            dto = new ExerciseDTO(
                    exercise.getId(),
                    exercise.getCode(),
                    exercise.getMetabolicEquivalent(),
                    exercise.getCategory(),
                    exercise.getDescription()
            );
        }
        return dto;
    }

}
