package com.uth.confms.auth.dto;

import com.uth.confms.common.util.ValidationUtil;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation để check password strength
 *
 * <p>Requirements:
 * <ul>
 *   <li>At least 8 characters
 *   <li>At least 1 uppercase letter
 *   <li>At least 1 lowercase letter
 *   <li>At least 1 digit
 *   <li>At least 1 special character (@$!%*?&)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = PasswordConstraint.PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordConstraint {
  String message() default "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
    @Override
    public void initialize(PasswordConstraint constraintAnnotation) {
      // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
      if (password == null || password.isEmpty()) {
        return false;
      }
      return ValidationUtil.isStrongPassword(password);
    }
  }
}
