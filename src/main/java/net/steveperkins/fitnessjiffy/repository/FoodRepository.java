package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface FoodRepository extends CrudRepository<Food, UUID> {

    /**
     * Returns all "global" foods (i.e. where ownerId is null).
     */
    @Nonnull
    public List<Food> findByOwnerIsNull();

    /**
     * Returns all foods owned by a particular user.
     */
    @Nonnull
    public List<Food> findByOwner(@Nonnull User owner);

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
    @Nonnull
    public List<Food> findVisibleByOwner(@Nonnull @Param("owner") User owner);

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
    @Nonnull
    public List<Food> findByNameLike(
            @Nonnull @Param("owner") User owner,
            @Nonnull @Param("name") String name
    );

    @Nonnull
    public List<Food> findByOwnerEqualsAndNameEquals(
            @Nonnull User owner,
            @Nonnull String name
    );

}
