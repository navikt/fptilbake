package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Valid
@Target(ElementType.FIELD)
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
@Size(min = 32, max = 36)
@Pattern(regexp = "^" + IsUUID.UUID_REGEXP + "$", message = "${validatedValue} is not valid UUID ({regexp})")
public @interface IsUUID {
    String UUID_REGEXP = "[\\p{XDigit}]{8}-[\\p{XDigit}]{4}-[34][\\p{XDigit}]{3}-[89ab][\\p{XDigit}]{3}-[\\p{XDigit}]{12}";

    String message() default "{invalid.uuid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
