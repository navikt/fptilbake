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

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

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
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
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
    BehandlingStegType value();

    /**
     * AnnotationLiteral som kan brukes i CDI søk.
     * <p>
     * Eks. for bruk i:<br>
     * {@link CDI#current#select(jakarta.enterprise.util.TypeLiteral,
     * Annotation...)}.
     */
    class BehandlingStegRefLiteral extends AnnotationLiteral<BehandlingStegRef> implements BehandlingStegRef {

        private BehandlingStegType stegType;

        public BehandlingStegRefLiteral(BehandlingStegType stegType) {
            this.stegType = stegType;
        }


        @Override
        public BehandlingStegType value() {
            return stegType;
        }

    }

    @SuppressWarnings("unchecked")
    public static final  class Lookup {

        private Lookup() {
        }

        public static <I> Optional<I> find(Class<I> cls, Instance<I> instances, BehandlingType behandlingType, BehandlingStegType behandlingStegRef) {
            Objects.requireNonNull(instances, "instances");

            for (var behandlingLiteral : coalesce(behandlingType, BehandlingType.UDEFINERT)) {
                var binst = select(cls, instances, new BehandlingTypeRef.BehandlingTypeRefLiteral(behandlingLiteral));
                if (binst.isUnsatisfied()) {
                    continue;
                }
                var cinst = select(cls, binst, new BehandlingStegRefLiteral(behandlingStegRef));
                if (cinst.isResolvable()) {
                    return Optional.of(getInstance(cinst));
                }
                if (cinst.isAmbiguous()) {
                    throw new IllegalStateException("Har flere matchende instanser for klasse : " + cls.getName() + ", behandlingType=" + behandlingLiteral + ", behandlingStegRef=" + behandlingStegRef);
                }
            }

            return Optional.empty();
        }

        private static <I> Instance<I> select(Class<I> cls, Instance<I> instances, Annotation anno) {
            return cls != null
                    ? instances.select(cls, anno)
                    : instances.select(anno);
        }

        private static List<BehandlingType> coalesce(BehandlingType... vals) {
            return Arrays.stream(vals).filter(Objects::nonNull).distinct().toList();
        }

        private static <I> I getInstance(Instance<I> inst) {
            var i = inst.get();
            if (i.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                        "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + i.getClass());
            }
            return i;
        }

    }

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Documented
    public @interface ContainerOfBehandlingStegRef {
        BehandlingStegRef[] value();
    }

}
