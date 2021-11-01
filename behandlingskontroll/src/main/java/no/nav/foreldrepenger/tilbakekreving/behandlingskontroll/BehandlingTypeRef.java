package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

/**
 * Marker type som implementerer interface {@link BehandlingSteg} for å skille ulike implementasjoner av samme steg for ulike behandlingtyper.<br>
 *
 * NB: Settes kun dersom det er flere implementasjoner av med samme {@link BehandlingStegRef}.
 */
@Repeatable(BehandlingTypeRef.ContainerOfBehandlingTypeRef.class)
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Documented
public @interface BehandlingTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike behandling typer.
     * <p>
     * Må matche ett innslag i <code>BEHANDling_TYPE</code> tabell for å kunne
     * kjøres.
     *
     * @see BehandlingType
     */
    String value() default "*";

    /** AnnotationLiteral som kan brukes ved CDI søk. */
    public static class BehandlingTypeRefLiteral extends AnnotationLiteral<BehandlingTypeRef> implements BehandlingTypeRef {

        private String navn;

        public BehandlingTypeRefLiteral() {
            this.navn = "*";
        }

        public BehandlingTypeRefLiteral(String navn) {
            this.navn = navn;
        }

        public BehandlingTypeRefLiteral(BehandlingType ytelseType) {
            this.navn = (ytelseType == null ? "*" : ytelseType.getKode());
        }

        @Override
        public String value() {
            return navn;
        }
    }

    @SuppressWarnings("unchecked")
    public static final class Lookup {

        private Lookup() {
        }

        public static <I> Optional<I> find(Class<I> cls, String behandlingType) {
            return find(cls, (CDI<I>) CDI.current(), behandlingType);
        }

        public static <I> Optional<I> find(Class<I> cls, BehandlingType behandlingType) {
            return find(cls, (CDI<I>) CDI.current(), behandlingType);
        }

        public static <I> Optional<I> find(Class<I> cls, Instance<I> instances, BehandlingType behandlingType) {
            return find(cls, instances,
                behandlingType == null ? null : behandlingType.getKode());
        }

        public static <I> Optional<I> find(Class<I> cls, Instance<I> instances, String behandlingType) { // NOSONAR
            Objects.requireNonNull(instances, "instances");

            for (var behandlingLiteral : coalesce(behandlingType, "*")) {
                var binst = select(cls, instances, new BehandlingTypeRefLiteral(behandlingLiteral));
                if (binst.isResolvable()) {
                    return Optional.of(getInstance(binst));
                }
                if (binst.isAmbiguous()) {
                    throw new IllegalStateException("Har flere matchende instanser for klasse : " + cls.getName() + ", behandlingType=" + behandlingLiteral);
                }
            }
            return Optional.empty();
        }

        private static <I> I getInstance(Instance<I> inst) {
            var i = inst.get();
            if (i.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                    "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + i.getClass());
            }
            return i;
        }

        private static List<String> coalesce(String... vals) {
            return Arrays.stream(vals).filter(Objects::nonNull).distinct().toList();
        }

        private static <I> Instance<I> select(Class<I> cls, Instance<I> instances, Annotation anno) {
            return cls != null
                ? instances.select(cls, anno)
                : instances.select(anno);
        }

    }

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
    @Documented
    public @interface ContainerOfBehandlingTypeRef {
        BehandlingTypeRef[] value();
    }
}
