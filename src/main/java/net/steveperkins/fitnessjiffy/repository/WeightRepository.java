package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.Weight;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface WeightRepository extends CrudRepository<Weight, UUID> {

    @Nullable
    public List<Weight> findByUserOrderByDateDesc(@Nonnull User user);

    /**
     * Unfortunately, this method is using a native query because JPQL does
     * not support the "LIMIT" keyword.  Alternatives would include using
     * a JPQL query built with a subselect, or using Spring Data JPA
     * pagination... but a native query is perhaps the least ugly of all
     * evils.  Also, this is meant to be a demo and teaching application
     * anyway, so why not show a native query example somewhere in the mix?
     */
    @Query(
            value = "SELECT WEIGHT.* FROM WEIGHT, FITNESSJIFFY_USER "
                    + "WHERE WEIGHT.USER_ID = FITNESSJIFFY_USER.ID "
                    + "AND FITNESSJIFFY_USER.ID = ?1 "
                    + "AND WEIGHT.DATE <= ?2 "
                    + "ORDER BY WEIGHT.DATE DESC LIMIT 1",
            nativeQuery = true
    )
    @Nullable
    public Weight findByUserMostRecentOnDate(
            @Nonnull User user,
            @Nonnull Date date
    );

}
