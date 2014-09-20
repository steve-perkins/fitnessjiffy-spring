package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.ReportData;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ReportDataRepository extends CrudRepository<ReportData, UUID> {
}
