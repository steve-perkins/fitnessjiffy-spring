package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FoodEatenRepository extends CrudRepository<FoodEaten, UUID> {
}
