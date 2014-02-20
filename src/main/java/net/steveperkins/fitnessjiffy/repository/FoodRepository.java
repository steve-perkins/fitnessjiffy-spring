package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Food;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FoodRepository extends CrudRepository<Food, UUID> {

    List<Food> findByOwnerIsNull();

    @Query("SELECT f FROM Food f WHERE f.owner.id = :ownerId OR f.owner IS NULL")
    List<Food> findByOwner(@Param("ownerId") UUID ownerId);

}
