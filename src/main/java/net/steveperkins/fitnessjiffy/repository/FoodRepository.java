package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FoodRepository extends CrudRepository<Food, UUID> {

    /** Returns all "global" foods (i.e. where ownerId is null). */
    List<Food> findByOwnerIsNull();

    /** Returns all foods owned by a particular user. */
    List<Food> findByOwner(User owner);

    /**
     * Returns all foods visible to a particular user.  This includes the foods that they own, as well as all global
     * foods.  When a user customizes a global food, an owned copy of that food is made for the user, rather than
     * actually modifying the global food itself.  Therefore, any global foods matching the name of an owned food
     * is filtered out of these results.
     */
    @Query(
            "SELECT food FROM Food food WHERE food.owner = :owner "
            + "OR ("
            + "food.owner IS NULL "
            + "AND NOT EXISTS (SELECT subFood FROM Food subFood WHERE subFood.owner = :owner AND subFood.name = food.name)"
            + ") ORDER BY food.name ASC"
    )
    List<Food> findVisibleByOwner(@Param("owner") User owner);

    @Query(
            "SELECT food FROM Food food "
            + "WHERE ("
            + "   food.owner = :owner "
            + "   OR ("
            + "      food.owner IS NULL "
            + "      AND NOT EXISTS (SELECT subFood FROM Food subFood WHERE subFood.owner = :owner AND subFood.name = food.name)"
            + "   )"
            + ") AND LOWER(food.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "ORDER BY food.name ASC")
    List<Food> findByNameLike(@Param("owner") User owner, @Param("name") String name);

    List<Food> findByOwnerEqualsAndNameEquals(User owner, String name);

}
