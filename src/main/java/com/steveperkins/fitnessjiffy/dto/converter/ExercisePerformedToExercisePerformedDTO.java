package com.steveperkins.fitnessjiffy.dto.converter;

import com.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import com.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public class ExercisePerformedToExercisePerformedDTO implements Converter<ExercisePerformed, ExercisePerformedDTO> {

    private final ExerciseToExerciseDTO exerciseDTOConverter;

    @Autowired
    public ExercisePerformedToExercisePerformedDTO(@Nonnull final ExerciseToExerciseDTO exerciseDTOConverter) {
        this.exerciseDTOConverter = exerciseDTOConverter;
    }

    @Override
    @Nullable
    public ExercisePerformedDTO convert(@Nullable final ExercisePerformed exercisePerformed) {
        ExercisePerformedDTO dto = null;
        if (exercisePerformed != null) {
            dto = new ExercisePerformedDTO();
            dto.setId(exercisePerformed.getId());
            dto.setUserId(exercisePerformed.getUser().getId());
            dto.setExercise(exerciseDTOConverter.convert(exercisePerformed.getExercise()));
            dto.setDate(exercisePerformed.getDate());
            dto.setMinutes(exercisePerformed.getMinutes());
        }
        return dto;
    }

}
