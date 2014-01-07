package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Food;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FoodRepository extends CrudRepository<Food, UUID> {
}
