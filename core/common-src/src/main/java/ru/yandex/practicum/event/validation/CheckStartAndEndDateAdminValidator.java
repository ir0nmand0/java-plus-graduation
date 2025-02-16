package ru.yandex.practicum.event.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ObjectUtils;
import ru.yandex.practicum.event.model.dto.AdminParameterDto;

public class CheckStartAndEndDateAdminValidator implements ConstraintValidator<EventStartDateBeforeEndDate, AdminParameterDto> {
    @Override
    public boolean isValid(final AdminParameterDto adminParameterDto, final ConstraintValidatorContext context) {
        if (ObjectUtils.isEmpty(adminParameterDto.getRangeStart()) || ObjectUtils.isEmpty(adminParameterDto.getRangeEnd())) {
            return true;
        }

        return adminParameterDto.getRangeStart().isBefore(adminParameterDto.getRangeEnd());
    }
}
