package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ExercisePerformedRepository extends CrudRepository<ExercisePerformed, UUID> {
}
