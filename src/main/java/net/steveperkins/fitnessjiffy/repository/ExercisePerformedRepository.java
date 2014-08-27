package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Exercise;
import net.steveperkins.fitnessjiffy.domain.ExercisePerformed;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface ExercisePerformedRepository extends CrudRepository<ExercisePerformed, UUID> {

    @Query(
            "SELECT exercisePerformed FROM ExercisePerformed exercisePerformed, Exercise exercise "
                + "WHERE exercisePerformed.exercise = exercise "
                + "AND exercisePerformed.user = :user "
                + "AND exercisePerformed.date = :date "
                + "ORDER BY exercise.description ASC"
    )
    List<ExercisePerformed> findByUserEqualsAndDateEquals(
            @Nonnull @Param("user") User user,
            @Nonnull @Param("date") Date date
    );

    @Query(
            "SELECT DISTINCT exercise FROM Exercise exercise, ExercisePerformed exercisePerformed "
                + "WHERE exercise = exercisePerformed.exercise "
                + "AND exercisePerformed.user = :user "
                + "AND exercisePerformed.date BETWEEN :startDate AND :endDate "
                + "ORDER BY exercise.description ASC"
    )
    List<Exercise> findByUserPerformedWithinRange(
            @Nonnull @Param("user") User user,
            @Nonnull @Param("startDate") Date startDate,
            @Nonnull @Param("endDate") Date endDate
    );

}
