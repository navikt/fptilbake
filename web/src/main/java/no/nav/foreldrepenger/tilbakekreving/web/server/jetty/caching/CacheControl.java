package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denne annotasjon kan brukast på ein rest endepunkt metode for å få satt Cache-Control header på http responsen frå metoden.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheControl {
    int maxAge() default 0;
    boolean noStore() default false;
    boolean mustRevalidate() default true;
    boolean isPrivate() default true;
    boolean noTransform() default true;
    boolean immutable() default false;
    int staleWhileRevalidate() default 0;
    int staleIfError() default 0;
}
