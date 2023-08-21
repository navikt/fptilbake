package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "FaktaFeilutbetalingPeriode")
@Table(name = "FAKTA_FEILUTBETALING_PERIODE")
public class FaktaFeilutbetalingPeriode extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAKTA_FEILUTB_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fakta_feilutbetaling_id", nullable = false, updatable = false)
    private FaktaFeilutbetaling faktaFeilutbetaling;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
            @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @Convert(converter = HendelseType.KodeverdiConverter.class)
    @Column(name = "hendelse_type")
    private HendelseType hendelseType;

    @Convert(converter = HendelseUnderType.KodeverdiConverter.class)
    @Column(name = "hendelse_undertype")
    private HendelseUnderType hendelseUndertype;

    FaktaFeilutbetalingPeriode() {
        // FOR CDI
    }

    public Periode getPeriode() {
        return periode;
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public HendelseUnderType getHendelseUndertype() {
        return hendelseUndertype;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FaktaFeilutbetalingPeriode kladd = new FaktaFeilutbetalingPeriode();

        public Builder medFeilutbetalinger(FaktaFeilutbetaling faktaFeilutbetaling) {
            this.kladd.faktaFeilutbetaling = faktaFeilutbetaling;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            this.kladd.periode = Periode.of(fom, tom);
            return this;
        }

        public Builder medPeriode(Periode periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medHendelseType(HendelseType hendelseType) {
            this.kladd.hendelseType = hendelseType;
            return this;
        }

        public Builder medHendelseUndertype(HendelseUnderType hendelseUndertype) {
            this.kladd.hendelseUndertype = hendelseUndertype;
            return this;
        }

        public FaktaFeilutbetalingPeriode build() {
            Objects.requireNonNull(kladd.getPeriode(), "Periode må være satt");
            Objects.requireNonNull(kladd.getHendelseType(), "HendelseType må være satt");
            Objects.requireNonNull(kladd.getHendelseUndertype(), "HendelseUndertype må være satt");
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "<" //$NON-NLS-1$
                + "periode=" + periode //$NON-NLS-1$
                + ", hendelseType=" + hendelseType.getKode() //$NON-NLS-1$
                + ", hendelseUndertype=" + hendelseUndertype.getKode() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$

    }
}
