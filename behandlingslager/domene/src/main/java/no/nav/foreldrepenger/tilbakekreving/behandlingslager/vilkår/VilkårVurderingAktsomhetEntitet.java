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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkBaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VilkårVurderingAktsomhet")
@Table(name = "VILKAAR_AKTSOMHET")
public class VilkårVurderingAktsomhetEntitet extends KodeverkBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAAR_AKTSOMHET")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "vilkaar_periode_id", nullable = false, updatable = false)
    private VilkårVurderingPeriodeEntitet periode;

    @Convert(converter = Aktsomhet.KodeverdiConverter.class)
    @Column(name = "aktsomhet")
    private Aktsomhet aktsomhet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "sarlig_grunner_til_reduksjon", updatable = false)
    private Boolean særligGrunnerTilReduksjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ilegg_renter", updatable = false)
    private Boolean ileggRenter;

    @Column(name = "andel_tilbakekreves")
    private BigDecimal prosenterSomTilbakekreves;

    @Column(name = "manuelt_satt_beloep")
    private BigDecimal manueltTilbakekrevesBeløp;

    @Column(name = "begrunnelse", nullable = false, updatable = false)
    private String begrunnelse;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "tilbakekrev_smaabeloep", updatable = false)
    private Boolean tilbakekrevSmåBeløp;

    @Column(name = "sarlig_grunner_begrunnelse",updatable = false)
    private String særligGrunnerBegrunnelse;

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

    public BigDecimal getProsenterSomTilbakekreves() {
        return prosenterSomTilbakekreves;
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

    public String getSærligGrunnerBegrunnelse() {
        return særligGrunnerBegrunnelse;
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

        public Builder medProsenterSomTilbakekreves(BigDecimal prosenterSomTilbakekreves) {
            this.kladd.prosenterSomTilbakekreves = prosenterSomTilbakekreves;
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

        public Builder medSærligGrunnerBegrunnelse(String særligGrunnerBegrunnelse) {
            this.kladd.særligGrunnerBegrunnelse = særligGrunnerBegrunnelse;
            return this;
        }

        public VilkårVurderingAktsomhetEntitet build() {
            Objects.requireNonNull(this.kladd.periode, "periode");
            Objects.requireNonNull(this.kladd.aktsomhet, "aktsomhet");
            Objects.requireNonNull(this.kladd.begrunnelse, "begrunnelse");
            if (kladd.prosenterSomTilbakekreves != null && kladd.manueltTilbakekrevesBeløp != null) {
                throw new IllegalArgumentException("Kan ikke sette både prosenterSomTilbakekreves og beløpSomTilbakekreves");
            }
            if (kladd.aktsomhet.equals(Aktsomhet.FORSETT)) {
                check(kladd.særligGrunnerTilReduksjon == null, "Ved FORSETT skal ikke særligeGrunnerTilReduksjon settes her");
                check(kladd.manueltTilbakekrevesBeløp == null, "Ved FORSETT er beløp automatisk, og skal ikke settes her");
                check(kladd.prosenterSomTilbakekreves == null, "Ved FORSETT er andel automatisk, og skal ikke settes her");
                check(kladd.tilbakekrevSmåBeløp == null, "Dette er gyldig bare for Simpel uaktsom");
                if (kladd.periode.getVilkårResultat().equals(VilkårResultat.FORSTO_BURDE_FORSTÅTT)) {
                    Objects.requireNonNull(this.kladd.ileggRenter, "ileggRenter");
                } else {
                    check(kladd.ileggRenter == null, "Ved FORSETT er rentebeslutning automatisk, og skal ikke settes her");
                }
            }
            if (kladd.aktsomhet.equals(Aktsomhet.GROVT_UAKTSOM)) {
                check(kladd.tilbakekrevSmåBeløp == null, "Dette er gyldig bare for Simpel uaktsom");
            }
            return kladd;
        }
    }

    private static void check(boolean check, String message, Object... params) {
        if (!check) {
            throw new IllegalArgumentException(String.format(message, params));
        }
    }

}
