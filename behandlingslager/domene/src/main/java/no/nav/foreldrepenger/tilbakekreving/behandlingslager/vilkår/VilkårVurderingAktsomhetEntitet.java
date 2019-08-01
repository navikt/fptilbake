package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VilkårVurderingAktsomhet")
@Table(name = "VILKAAR_AKTSOMHET")
public class VilkårVurderingAktsomhetEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAAR_AKTSOMHET")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "vilkaar_periode_id", nullable = false, updatable = false)
    private VilkårVurderingPeriodeEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + Aktsomhet.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "aktsomhet", referencedColumnName = "kode")),
    })
    private Aktsomhet aktsomhet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "sarlig_grunner_til_reduksjon", updatable = false)
    private Boolean særligGrunnerTilReduksjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ilegg_renter", updatable = false)
    private Boolean ileggRenter;

    @Column(name = "andel_tilbakekreves")
    private Integer andelSomTilbakekreves;

    @Column(name = "manuelt_satt_beloep")
    private BigDecimal manueltTilbakekrevesBeløp;

    @Column(name = "begrunnelse", nullable = false, updatable = false)
    private String begrunnelse;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "tilbakekrev_smaabeloep", updatable = false)
    private Boolean tilbakekrevSmåBeløp;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vurdertAktsomhet")
    private List<VilkårVurderingSærligGrunnEntitet> særligGrunner = new ArrayList<>();

    VilkårVurderingAktsomhetEntitet() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public VilkårVurderingPeriodeEntitet getPeriode() {
        return periode;
    }

    public Aktsomhet getAktsomhet() {
        return aktsomhet;
    }

    public Boolean getSærligGrunnerTilReduksjon() {
        return særligGrunnerTilReduksjon;
    }

    public Boolean getIleggRenter() {
        return ileggRenter;
    }

    public Integer getAndelSomTilbakekreves() {
        return andelSomTilbakekreves;
    }

    public BigDecimal getManueltTilbakekrevesBeløp() {
        return manueltTilbakekrevesBeløp;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public Boolean getTilbakekrevSmåBeløp() {
        return tilbakekrevSmåBeløp;
    }

    public List<VilkårVurderingSærligGrunnEntitet> getSærligGrunner() {
        return Collections.unmodifiableList(særligGrunner);
    }

    public void leggTilSærligGrunn(VilkårVurderingSærligGrunnEntitet særligGrunn) {
        this.særligGrunner.add(særligGrunn);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VilkårVurderingAktsomhetEntitet kladd = new VilkårVurderingAktsomhetEntitet();

        public Builder medPeriode(VilkårVurderingPeriodeEntitet periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medAktsomhet(Aktsomhet aktsomhet) {
            this.kladd.aktsomhet = aktsomhet;
            return this;
        }

        public Builder medSærligGrunnerTilReduksjon(Boolean særligGrunnerTilReduksjon) {
            this.kladd.særligGrunnerTilReduksjon = særligGrunnerTilReduksjon;
            return this;
        }

        public Builder medIleggRenter(Boolean ileggRenter) {
            this.kladd.ileggRenter = ileggRenter;
            return this;
        }

        public Builder medAndelSomTilbakekreves(Integer andelSomTilbakekreves) {
            this.kladd.andelSomTilbakekreves = andelSomTilbakekreves;
            return this;
        }

        public Builder medBeløpTilbakekreves(BigDecimal beløpTilbakekreves) {
            this.kladd.manueltTilbakekrevesBeløp = beløpTilbakekreves;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public Builder medTilbakekrevSmåBeløp(Boolean tilbakekrevSmåBeløp) {
            this.kladd.tilbakekrevSmåBeløp = tilbakekrevSmåBeløp;
            return this;
        }

        public VilkårVurderingAktsomhetEntitet build() {
            Objects.requireNonNull(this.kladd.periode, "periode");
            Objects.requireNonNull(this.kladd.aktsomhet, "aktsomhet");
            Objects.requireNonNull(this.kladd.begrunnelse, "begrunnelse");
            if (kladd.andelSomTilbakekreves != null && kladd.manueltTilbakekrevesBeløp != null) {
                throw new IllegalArgumentException("Kan ikke sette både andelSomTilbakekreves og beløpSomTilbakekreves");
            }
            if (kladd.aktsomhet.equals(Aktsomhet.FORSETT)) {
                no.nav.vedtak.util.Objects.check(kladd.ileggRenter == null, "Ved FORSETT er rentebeslutning automatisk, og skal ikke settes her");
                no.nav.vedtak.util.Objects.check(kladd.særligGrunnerTilReduksjon == null, "Ved FORSETT skal ikke særligeGrunnerTilReduksjon settes her");
                no.nav.vedtak.util.Objects.check(kladd.manueltTilbakekrevesBeløp == null, "Ved FORSETT er beløp automatisk, og skal ikke settes her");
                no.nav.vedtak.util.Objects.check(kladd.andelSomTilbakekreves == null, "Ved FORSETT er andel automatisk, og skal ikke settes her");
                no.nav.vedtak.util.Objects.check(kladd.tilbakekrevSmåBeløp == null, "Dette er gyldig bare for Simpel uaktsom");
            }
            if (kladd.aktsomhet.equals(Aktsomhet.GROVT_UAKTSOM)) {
                no.nav.vedtak.util.Objects.check(kladd.tilbakekrevSmåBeløp == null, "Dette er gyldig bare for Simpel uaktsom");
            }

            return kladd;
        }
    }

}
