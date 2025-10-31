package com.socialmedia.app.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TrimNotEmptyValidator implements ConstraintValidator<TrimmedNotEmpty, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // use @NotNull separately
        return !value.trim().isEmpty();
    }
}
