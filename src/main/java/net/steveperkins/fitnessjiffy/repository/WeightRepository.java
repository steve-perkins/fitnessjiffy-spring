package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Weight;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface WeightRepository extends CrudRepository<Weight, UUID> {
}
