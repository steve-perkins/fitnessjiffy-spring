package com.steveperkins.fitnessjiffy.repository;

import com.steveperkins.fitnessjiffy.domain.Food;
import com.steveperkins.fitnessjiffy.domain.FoodEaten;
import com.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface FoodEatenRepository extends CrudRepository<FoodEaten, UUID> {

    @Nonnull
    public List<FoodEaten> findByUserEqualsOrderByDateAsc(@Nonnull User user);

    @Nonnull
    public List<FoodEaten> findByUserEqualsAndFoodEqualsOrderByDateAsc(
            @Nonnull User user,
            @Nonnull Food food
    );

    @Query(
            "SELECT foodEaten FROM FoodEaten foodEaten, Food food "
                    + "WHERE foodEaten.food = food "
                    + "AND foodEaten.user = :user "
                    + "AND foodEaten.date = :date "
                    + "ORDER BY food.name ASC")
    @Nonnull
    public List<FoodEaten> findByUserEqualsAndDateEquals(
            @Nonnull @Param("user") User user,
            @Nonnull @Param("date") Date date
    );

    @Query(
            "SELECT DISTINCT food FROM Food food, FoodEaten foodEaten "
                    + "WHERE food = foodEaten.food "
                    + "AND foodEaten.user = :user "
                    + "AND foodEaten.date BETWEEN :startDate AND :endDate "
                    + "ORDER BY food.name ASC")
    @Nonnull
    public List<Food> findByUserEatenWithinRange(
            @Nonnull @Param("user") User user,
            @Nonnull @Param("startDate") Date startDate,
            @Nonnull @Param("endDate") Date endDate
    );

}
