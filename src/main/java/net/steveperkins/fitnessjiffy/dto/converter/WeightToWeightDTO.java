package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.Weight;
import net.steveperkins.fitnessjiffy.dto.WeightDTO;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nullable;

public final class WeightToWeightDTO implements Converter<Weight, WeightDTO> {

    @Override
    @Nullable
    public WeightDTO convert(final Weight weight) {
        WeightDTO dto = null;
        if (weight != null) {
            dto = new WeightDTO(
                    weight.getId(),
                    weight.getUser().getId(),
                    weight.getDate(),
                    weight.getPounds()
            );
        }
        return dto;
    }

}
