package com.steveperkins.fitnessjiffy.repository;

import com.steveperkins.fitnessjiffy.domain.ReportData;
import com.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface ReportDataRepository extends CrudRepository<ReportData, UUID> {

    @Nonnull
    public List<ReportData> findByUserOrderByDateAsc(@Nonnull User user);

    @Nonnull
    public List<ReportData> findByUserAndDateOrderByDateAsc(@Nonnull User user, @Nonnull Date date);

    @Nonnull
    public List<ReportData> findByUserAndDateBetweenOrderByDateAsc(@Nonnull User user, @Nonnull Date startDate, @Nonnull Date endDate);

}
