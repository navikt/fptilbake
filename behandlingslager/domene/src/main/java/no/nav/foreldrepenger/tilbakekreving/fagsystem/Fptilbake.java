package no.nav.foreldrepenger.tilbakekreving.fagsystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Fptilbake {

    class FptilbakeAnnotationLiteral extends AnnotationLiteral<Fptilbake> implements Fptilbake {
    }
}
