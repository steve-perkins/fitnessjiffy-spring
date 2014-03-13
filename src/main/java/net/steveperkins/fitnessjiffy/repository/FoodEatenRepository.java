package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface FoodEatenRepository extends CrudRepository<FoodEaten, UUID> {

    List<FoodEaten> findByUserEqualsAndDateEquals(User user, @Temporal(TemporalType.DATE) Date date);

    List<FoodEaten> findByUserEqualsAndDateAfter(User user, Date date);

}
