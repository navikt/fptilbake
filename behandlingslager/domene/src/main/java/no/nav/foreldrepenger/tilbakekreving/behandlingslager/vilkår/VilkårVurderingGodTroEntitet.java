package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VilkårVurderingGodTro")
@Table(name = "VILKAAR_GOD_TRO")
public class VilkårVurderingGodTroEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAAR_GOD_TRO")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "vilkaar_periode_id", nullable = false, updatable = false)
    private VilkårVurderingPeriodeEntitet periode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "beloep_er_i_behold", nullable = false, updatable = false)
    private boolean beløpErIBehold;

    @Column(name = "beloep_tilbakekreves")
    private BigDecimal beløpTilbakekreves;

    @Column(name = "begrunnelse", nullable = false, updatable = false)
    private String begrunnelse;

    VilkårVurderingGodTroEntitet() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public VilkårVurderingPeriodeEntitet getPeriode() {
        return periode;
    }

    public boolean isBeløpErIBehold() {
        return beløpErIBehold;
    }

    public BigDecimal getBeløpTilbakekreves() {
        return beløpTilbakekreves;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VilkårVurderingGodTroEntitet kladd = new VilkårVurderingGodTroEntitet();

        public Builder medPeriode(VilkårVurderingPeriodeEntitet periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medBeløpErIBehold(boolean beløpErIBehold) {
            this.kladd.beløpErIBehold = beløpErIBehold;
            return this;
        }

        public Builder medBeløpTilbakekreves(BigDecimal beløpTilbakekreves) {
            this.kladd.beløpTilbakekreves = beløpTilbakekreves;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public VilkårVurderingGodTroEntitet build() {
            Objects.requireNonNull(this.kladd.periode, "periode");
            Objects.requireNonNull(this.kladd.begrunnelse, "begrunnelse");
            Objects.requireNonNull(this.kladd.beløpErIBehold, "beløpErIBehold");
            return kladd;
        }
    }
}
