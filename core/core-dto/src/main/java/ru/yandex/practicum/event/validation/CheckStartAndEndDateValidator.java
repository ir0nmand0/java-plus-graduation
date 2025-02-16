package ru.yandex.practicum.event.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ObjectUtils;
import ru.yandex.practicum.event.model.dto.PublicParameterDto;

public class CheckStartAndEndDateValidator implements ConstraintValidator<EventStartDateBeforeEndDate, PublicParameterDto> {
    @Override
    public boolean isValid(final PublicParameterDto publicParameterDto, final ConstraintValidatorContext context) {
        if (ObjectUtils.isEmpty(publicParameterDto.getRangeStart()) || ObjectUtils.isEmpty(publicParameterDto.getRangeEnd())) {
            return true;
        }

        return publicParameterDto.getRangeStart().isBefore(publicParameterDto.getRangeEnd());
    }
}
