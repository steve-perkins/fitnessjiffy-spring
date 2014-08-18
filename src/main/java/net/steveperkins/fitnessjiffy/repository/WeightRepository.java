package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface WeightRepository extends CrudRepository<Weight, UUID> {

    @Nullable
    List<Weight> findByUserOrderByDateDesc(@Nonnull User user);

    @Nullable
    Weight findByUserAndDate(
            @Nonnull User user,
            @Nonnull Date date
    );

}
