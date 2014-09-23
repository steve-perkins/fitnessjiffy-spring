package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.ReportData;
import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface ReportDataRepository extends CrudRepository<ReportData, UUID> {

    @Nonnull
    public List<ReportData> findByUserAndDate(@Nonnull User user, @Nonnull Date date);

}
