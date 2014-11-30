package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.ReportData;
import net.steveperkins.fitnessjiffy.dto.ReportDataDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class ReportDataToReportDataDTO implements Converter<ReportData, ReportDataDTO> {

    @Override
    @Nullable
    public ReportDataDTO convert(@Nullable final ReportData reportData) {
        ReportDataDTO dto = null;
        if (reportData != null) {
            dto = new ReportDataDTO();
            dto.setId(reportData.getId());
            dto.setUserId(reportData.getUser().getId());
            dto.setDate(reportData.getDate());
            dto.setPounds(reportData.getPounds());
            dto.setNetCalories(reportData.getNetCalories());
            dto.setNetPoints(reportData.getNetPoints());
        }
        return dto;
    }

}
