package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import net.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import org.springframework.core.convert.converter.Converter;

public class ExercisePerformedToExercisePerformedDTO implements Converter<ExercisePerformed, ExercisePerformedDTO> {

    @Override
    public ExercisePerformedDTO convert(final ExercisePerformed exercisePerformed) {
        ExercisePerformedDTO dto = null;
        if (exercisePerformed != null) {
            dto = new ExercisePerformedDTO();
            dto.setId(exercisePerformed.getId());
            dto.setUserId(exercisePerformed.getUser().getId());
            dto.setExerciseId(exercisePerformed.getExercise().getId());
            dto.setDate(exercisePerformed.getDate());
            dto.setMinutes(exercisePerformed.getMinutes());
        }
        return dto;
    }

}
