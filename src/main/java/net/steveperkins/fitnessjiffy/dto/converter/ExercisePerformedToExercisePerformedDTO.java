package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import net.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nullable;

public class ExercisePerformedToExercisePerformedDTO implements Converter<ExercisePerformed, ExercisePerformedDTO> {

    @Autowired
    ExerciseToExerciseDTO exerciseDTOCoverter;

    @Override
    @Nullable
    public ExercisePerformedDTO convert(@Nullable final ExercisePerformed exercisePerformed) {
        ExercisePerformedDTO dto = null;
        if (exercisePerformed != null) {
            dto = new ExercisePerformedDTO();
            dto.setId(exercisePerformed.getId());
            dto.setUserId(exercisePerformed.getUser().getId());
            dto.setExercise(exerciseDTOCoverter.convert(exercisePerformed.getExercise()));
            dto.setDate(exercisePerformed.getDate());
            dto.setMinutes(exercisePerformed.getMinutes());
        }
        return dto;
    }

}
