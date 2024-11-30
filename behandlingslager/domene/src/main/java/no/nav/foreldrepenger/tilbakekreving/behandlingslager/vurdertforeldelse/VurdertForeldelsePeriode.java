package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "VurdertForeldelsePeriode")
@Table(name = "FORELDELSE_PERIODE")
public class VurdertForeldelsePeriode extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FORELDELSE_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vurdert_foreldelse_id", nullable = false, updatable = false)
    private VurdertForeldelse vurdertForeldelse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
            @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @Convert(converter = ForeldelseVurderingType.KodeverdiConverter.class)
    @Column(name = "foreldelse_vurdering_type", nullable = false)
    private ForeldelseVurderingType foreldelseVurderingType;

    @Column(name = "foreldelsesfrist")
    private LocalDate foreldelsesfrist;

    @Column(name = "oppdagelses_dato")
    private LocalDate oppdagelsesDato;

    @Column(name = "begrunnelse", nullable = false)
    private String begrunnelse;


    VurdertForeldelsePeriode() {
        // For hibernate
    }

    public Long getId() {
        return id;
    }

    public VurdertForeldelse getVurdertForeldelse() {
        return vurdertForeldelse;
    }

    public boolean erForeldet() {
        return ForeldelseVurderingType.FORELDET.equals(foreldelseVurderingType);
    }

    public Periode getPeriode() {
        return periode;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public ForeldelseVurderingType getForeldelseVurderingType() {
        return foreldelseVurderingType;
    }

    public LocalDate getForeldelsesfrist() {
        return foreldelsesfrist;
    }

    public LocalDate getOppdagelsesDato() {
        return oppdagelsesDato;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VurdertForeldelsePeriode kladd = new VurdertForeldelsePeriode();

        public Builder medVurdertForeldelse(VurdertForeldelse vurdertForeldelse) {
            this.kladd.vurdertForeldelse = vurdertForeldelse;
            return this;
        }

        public Builder medPeriode(Periode periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            this.kladd.periode = Periode.of(fom, tom);
            return this;
        }


        public Builder medForeldelseVurderingType(ForeldelseVurderingType foreldelseVurderingType) {
            this.kladd.foreldelseVurderingType = foreldelseVurderingType;
            return this;
        }

        public Builder medForeldelsesFrist(LocalDate foreldelsesFrist) {
            this.kladd.foreldelsesfrist = foreldelsesFrist;
            return this;
        }

        public Builder medOppdagelseDato(LocalDate oppdagelseDato) {
            this.kladd.oppdagelsesDato = oppdagelseDato;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public VurdertForeldelsePeriode build() {
            if (ForeldelseVurderingType.TILLEGGSFRIST.equals(kladd.foreldelseVurderingType)) {
                Objects.requireNonNull(kladd.oppdagelsesDato, "oppdagelsesdato");
                Objects.requireNonNull(kladd.foreldelsesfrist, "foreldelsesFrist");
            } else if (ForeldelseVurderingType.FORELDET.equals(kladd.foreldelseVurderingType)) {
                Objects.requireNonNull(kladd.foreldelsesfrist, "foreldelsesFrist");
            }
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id
                + ", periode=" + periode
                + ", foreldelseVurderingType=" + foreldelseVurderingType
                + ">";

    }
}
