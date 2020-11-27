package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Vurdering;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;

public class HbVurderinger {

    @JsonProperty("vilkår-resultat")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private VilkårResultat vilkårResultat;
    @JsonProperty("fritekst")
    private String fritekstVilkår;
    @JsonProperty("aktsomhet-resultat")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private Vurdering aktsomhetResultat;
    @JsonProperty("unntas-innkreving-pga-lavt-beløp")
    private boolean unntasInnkrevingPgaLavtBeløp;

    @JsonProperty("særlige-grunner")
    private HbSærligeGrunner særligeGrunner;

    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    @JsonProperty("foreldelsevurdering")
    private ForeldelseVurderingType foreldelsevurdering;

    @JsonProperty("foreldelsesfrist")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate foreldelsesfrist;

    @JsonProperty("oppdagelses-dato")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate oppdagelsesDato;

    @JsonProperty("fritekst-foreldelse")
    private String fritekstForeldelse;

    @JsonProperty("beløp-i-behold")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal beløpIBehold;

    private HbVurderinger() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public HbSærligeGrunner getSærligeGrunner() {
        return særligeGrunner;
    }

    public void setFritekstVilkår(String fritekstVilkår) {
        this.fritekstVilkår = fritekstVilkår;
    }

    public String getFritekstVilkår() {
        return fritekstVilkår;
    }

    public String getFritekstForeldelse() {
        return fritekstForeldelse;
    }

    public void setFritekstForeldelse(String fritekstForeldelse) {
        this.fritekstForeldelse = fritekstForeldelse;
    }

    @JsonProperty("har-foreldelse-avsnitt")
    public boolean harForeldelseAvsnitt() {
        return ForeldelseVurderingType.FORELDET.equals(foreldelsevurdering) || ForeldelseVurderingType.TILLEGGSFRIST.equals(foreldelsevurdering);
    }

    public static class Builder {
        private HbVurderinger kladd = new HbVurderinger();

        public HbVurderinger.Builder medVilkårResultat(VilkårResultat vilkårResultat) {
            kladd.vilkårResultat = vilkårResultat;
            return this;
        }

        public HbVurderinger.Builder medFritekstVilkår(String fritekstVilkår) {
            kladd.fritekstVilkår = fritekstVilkår;
            return this;
        }

        public HbVurderinger.Builder medAktsomhetResultat(Vurdering vurdering) {
            kladd.aktsomhetResultat = vurdering;
            return this;
        }

        public HbVurderinger.Builder medUnntasInnkrevingPgaLavtBeløp(boolean unntasInnkrevingPgaLavtBeløp) {
            kladd.unntasInnkrevingPgaLavtBeløp = unntasInnkrevingPgaLavtBeløp;
            return this;
        }

        public HbVurderinger.Builder medForeldelsevurdering(ForeldelseVurderingType foreldelsevurdering) {
            kladd.foreldelsevurdering = foreldelsevurdering;
            return this;
        }

        public HbVurderinger.Builder medForeldelsesfrist(LocalDate foreldelsesfrist) {
            kladd.foreldelsesfrist = foreldelsesfrist;
            return this;
        }

        public HbVurderinger.Builder medOppdagelsesDato(LocalDate oppdagelsesDato) {
            kladd.oppdagelsesDato = oppdagelsesDato;
            return this;
        }

        public HbVurderinger.Builder medFritekstForeldelse(String fritekstForeldelse) {
            kladd.fritekstForeldelse = fritekstForeldelse;
            return this;
        }

        public HbVurderinger.Builder medBeløpIBehold(BigDecimal beløpIBehold) {
            kladd.beløpIBehold = beløpIBehold;
            return this;
        }

        public HbVurderinger.Builder medSærligeGrunner(Collection<SærligGrunn> særligeGrunner, String fritekstSærligeGrunner, String fritekstSærligeGrunnerAnnet) {
            kladd.særligeGrunner = HbSærligeGrunner.builder()
                .medSærligeGrunner(særligeGrunner)
                .medFritekstSærligeGrunner(fritekstSærligeGrunner)
                .medFritekstSærligeGrunnerAnnet(fritekstSærligeGrunnerAnnet)
                .build();
            return this;
        }

        public HbVurderinger build() {
            Objects.requireNonNull(kladd.foreldelsevurdering, "foreldelsevurdering er ikke satt");
            if (ForeldelseVurderingType.IKKE_VURDERT.equals(kladd.foreldelsevurdering) ||
                ForeldelseVurderingType.IKKE_FORELDET.equals(kladd.foreldelsevurdering)) {
                Objects.requireNonNull(kladd.vilkårResultat, "vilkårResultat er ikke satt");
            } else if (ForeldelseVurderingType.FORELDET.equals(kladd.foreldelsevurdering)) {
                Objects.requireNonNull(kladd.foreldelsesfrist, "foreldelsesfrist er ikke satt");
            } else if (ForeldelseVurderingType.TILLEGGSFRIST.equals(kladd.foreldelsevurdering)) {
                Objects.requireNonNull(kladd.foreldelsesfrist, "foreldelsesfrist er ikke satt");
                Objects.requireNonNull(kladd.oppdagelsesDato, "oppdagelsesDato er ikke satt");
            }

            if (AnnenVurdering.GOD_TRO.equals(kladd.aktsomhetResultat)) {
                Objects.requireNonNull(kladd.beløpIBehold, "beløp i behold er ikke satt");
            } else {
                if (kladd.beløpIBehold != null) {
                    throw new IllegalArgumentException("beløp i behold skal ikke være satt når aktsomhetresultat er " + kladd.aktsomhetResultat);
                }
            }
            return kladd;
        }

    }
}
