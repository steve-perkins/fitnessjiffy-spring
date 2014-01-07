package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Exercise;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ExerciseRepository extends CrudRepository<Exercise, UUID> {
}
