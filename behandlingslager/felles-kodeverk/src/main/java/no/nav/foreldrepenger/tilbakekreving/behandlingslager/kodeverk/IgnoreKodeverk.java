package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Payload;

@Target({ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreKodeverk {

    String message() default "kodeverk kan være ignorert";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
