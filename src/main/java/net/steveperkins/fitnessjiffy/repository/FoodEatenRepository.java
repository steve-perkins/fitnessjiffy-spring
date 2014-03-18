package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface FoodEatenRepository extends CrudRepository<FoodEaten, UUID> {

    @Query(
            "SELECT foodEaten FROM FoodEaten foodEaten, Food food "
                    + "WHERE foodEaten.food = food "
                    + "AND foodEaten.user = :user "
                    + "AND foodEaten.date = :date "
                    + "ORDER BY food.name ASC")
    List<FoodEaten> findByUserEqualsAndDateEquals(@Param("user") User user, @Param("date") Date date);

    @Query(
            "SELECT DISTINCT food FROM Food food, FoodEaten foodEaten "
            + "WHERE food = foodEaten.food "
            + "AND foodEaten.user = :user "
            + "AND foodEaten.date BETWEEN :startDate AND :endDate "
            + "ORDER BY food.name ASC")
    List<Food> findByUserEatenWithinRange(@Param("user") User user, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
