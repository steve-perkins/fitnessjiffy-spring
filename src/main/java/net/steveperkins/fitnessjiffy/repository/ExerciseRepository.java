package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Exercise;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface ExerciseRepository extends CrudRepository<Exercise, UUID> {

    @Query("SELECT DISTINCT(exercise.category) FROM Exercise exercise ORDER BY exercise.category")
    @Nonnull
    public List<String> findAllCategories();

    @Nonnull
    public List<Exercise> findByCategoryOrderByDescriptionAsc(@Nonnull String category);

    @Query(
            "SELECT exercise FROM Exercise exercise "
                + "WHERE LOWER(exercise.description) LIKE LOWER(CONCAT('%', :description, '%')) "
                + "ORDER BY exercise.description ASC"
    )
    @Nonnull
    public List<Exercise> findByDescriptionLike(@Nonnull @Param("description") String description);

}
