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
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

/**
 * Marker type som implementerer interface {@link BehandlingSteg}.<br>
 */
@Repeatable(BehandlingStegRef.ContainerOfBehandlingStegRef.class)
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Documented
public @interface BehandlingStegRef {

    /**
     * Kode-verdi som identifiserer behandlingsteget.
     * <p>
     * Må matche ett innslag i <code>BEHANDLING_STEG_TYPE</code> tabell for å kunne
     * kjøres.
     *
     * @see BehandlingStegType
     */
    String kode();

    /**
     * AnnotationLiteral som kan brukes i CDI søk.
     * <p>
     * Eks. for bruk i:<br>
     * {@link CDI#current#select(javax.enterprise.util.TypeLiteral,
     * Annotation...)}.
     */
    public static class BehandlingStegRefLiteral extends AnnotationLiteral<BehandlingStegRef> implements BehandlingStegRef {

        private String stegKode;

        public BehandlingStegRefLiteral() {
            this("*");
        }

        public BehandlingStegRefLiteral(String stegKode) {
            this.stegKode = stegKode;
        }

        public BehandlingStegRefLiteral(BehandlingStegType stegType) {
            this(stegType == null ? "*" : stegType.getKode());
        }

        @Override
        public String kode() {
            return stegKode;
        }

    }

    @SuppressWarnings("unchecked")
    public final static class Lookup {

        private Lookup() {
        }

        public static <I> Optional<I> find(Class<I> cls, String behandlingType, String behandlingStegRef) {
            return find(cls, (CDI<I>) CDI.current(), behandlingType, behandlingStegRef);
        }

        public static <I> Optional<I> find(Class<I> cls, BehandlingType behandlingType,
                                           BehandlingStegType behandlingStegRef) {
            return find(cls, (CDI<I>) CDI.current(), behandlingType, behandlingStegRef);
        }

        public static <I> Optional<I> find(Class<I> cls, Instance<I> instances, BehandlingType behandlingType,
                                           BehandlingStegType behandlingStegRef) {
            return find(cls, instances,
                behandlingType == null ? null : behandlingType.getKode(),
                behandlingStegRef == null ? null : behandlingStegRef.getKode());
        }

        public static <I> Optional<I> find(Class<I> cls, Instance<I> instances, String behandlingType,
                                           String behandlingStegRef) { // NOSONAR
            Objects.requireNonNull(instances, "instances");

            for (var behandlingLiteral : coalesce(behandlingType, "*")) {
                var binst = select(cls, instances, new BehandlingTypeRef.BehandlingTypeRefLiteral(behandlingLiteral));
                if (binst.isUnsatisfied()) {
                    continue;
                }
                for (var stegRef : coalesce(behandlingStegRef, "*")) {
                    var cinst = select(cls, binst, new BehandlingStegRefLiteral(stegRef));
                    if (cinst.isResolvable()) {
                        return Optional.of(getInstance(cinst));
                    }
                    if (cinst.isAmbiguous()) {
                        throw new IllegalStateException("Har flere matchende instanser for klasse : " + cls.getName() +
                            ", behandlingType=" + behandlingLiteral + ", behandlingStegRef=" + stegRef);
                    }
                }
            }

            return Optional.empty();
        }

        private static <I> Instance<I> select(Class<I> cls, Instance<I> instances, Annotation anno) {
            return cls != null
                ? instances.select(cls, anno)
                : instances.select(anno);
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
            return Arrays.asList(vals).stream().filter(v -> v != null).distinct().collect(Collectors.toList());
        }

    }

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Documented
    public @interface ContainerOfBehandlingStegRef {
        BehandlingStegRef[] value();
    }

}
