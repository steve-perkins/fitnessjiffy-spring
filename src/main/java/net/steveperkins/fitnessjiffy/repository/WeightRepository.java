package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface WeightRepository extends CrudRepository<Weight, UUID> {

    @Nonnull
    public List<Weight> findByUserOrderByDateDesc(@Nonnull User user);

}
