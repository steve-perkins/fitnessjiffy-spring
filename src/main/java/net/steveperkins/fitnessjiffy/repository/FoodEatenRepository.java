package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface FoodEatenRepository extends CrudRepository<FoodEaten, UUID> {

    List<FoodEaten> findByUserEqualsAndDateAfter(User user, Date date);

}
