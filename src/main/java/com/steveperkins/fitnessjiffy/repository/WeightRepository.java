package com.steveperkins.fitnessjiffy.repository;

import com.steveperkins.fitnessjiffy.domain.User;
import com.steveperkins.fitnessjiffy.domain.Weight;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface WeightRepository extends CrudRepository<Weight, UUID> {

    @Nonnull
    public List<Weight> findByUserOrderByDateDesc(@Nonnull User user);

    /**
     * Unfortunately, this method is using a native query because JPQL does not support the "LIMIT" keyword.
     * Alternatives would include using a JPQL query built with a subselect, or using Spring Data JPA pagination...
     * but a native query is perhaps the least ugly of all evils.  Also, this is meant to be a demo and teaching
     * application anyway, so why not show a native query example somewhere in the mix?
     */
    @Query(
            value = "SELECT weight.* FROM weight, fitnessjiffy_user "
                    + "WHERE weight.user_id = fitnessjiffy_user.id "
                    + "AND fitnessjiffy_user.id = ?1 "
                    + "AND weight.date <= ?2 "
                    + "ORDER BY weight.date DESC LIMIT 1",
            nativeQuery = true
    )
    @Nullable
    public Weight findByUserMostRecentOnDate(
            @Nonnull User user,
            @Nonnull Date date
    );

    /**
     * "findByUserMostRecentOnDate" is used for purposes of display, and for report generation, to account for days
     * on which weight entry might have been skipped.  "findByUserAndDate", however, looks only on the specified
     * date with no adjustment... for purposes of updating a particular weight entry correctly.
     */
    @Nullable
    public Weight findByUserAndDate(
            @Nonnull User user,
            @Nonnull Date date
    );

}
