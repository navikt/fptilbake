package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import java.time.LocalDate;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.jpa.BaseEntitet;

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

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HendelseType.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "hendelse_type", referencedColumnName = "kode")),
    })
    private HendelseType hendelseType;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HendelseUnderType.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "hendelse_undertype", referencedColumnName = "kode")),
    })
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
        //TODO returner IKKE_SATT direkte isdf null, må tilpasse håndtering andre steder i koden
        return FellesUndertyper.IKKE_SATT.equals(hendelseUndertype) ? null : hendelseUndertype;
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
            this.kladd.hendelseUndertype = hendelseUndertype != null ? hendelseUndertype : FellesUndertyper.IKKE_SATT;
            return this;
        }

        @Deprecated // FIXME bruk medHendelseType
        public Builder medÅrsak(String årsak, KodeverkRepository kodeverkRepository) {
            this.kladd.hendelseType = kodeverkRepository.finn(HendelseType.class, årsak);
            return this;
        }

        @Deprecated // FIXME bruk medHendelseUndertype
        public Builder medUnderÅrsak(String underÅrsak, KodeverkRepository kodeverkRepository) {
            String kode = underÅrsak == null ? FellesUndertyper.IKKE_SATT.getKode() : underÅrsak;
            this.kladd.hendelseUndertype = kodeverkRepository.finn(HendelseUnderType.class, kode);
            return this;
        }

        public FaktaFeilutbetalingPeriode build() {
            //FIXME valider at bygd riktig
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
            + ", periode=" + periode //$NON-NLS-1$
            + ", hendelseType=" + hendelseType //$NON-NLS-1$
            + ", hendelseUndertype=" + hendelseUndertype //$NON-NLS-1$
            + ">"; //$NON-NLS-1$

    }
}
