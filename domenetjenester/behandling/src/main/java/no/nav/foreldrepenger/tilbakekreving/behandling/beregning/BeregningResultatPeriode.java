package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Vurdering;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeResultat;

public class BeregningResultatPeriode {
    private Periode periode;
    private Vurdering vurdering;
    private BigDecimal feilutbetaltBeløp;
    private BigDecimal andelAvBeløp;
    private BigDecimal renterProsent;
    private BigDecimal manueltSattTilbakekrevingsbeløp;
    private BigDecimal tilbakekrevingBeløpUtenRenter;
    private BigDecimal renteBeløp;
    private BigDecimal tilbakekrevingBeløp;
    private BigDecimal skattBeløp;
    private BigDecimal tilbakekrevingBeløpEtterSkatt;
    private BigDecimal utbetaltYtelseBeløp; //rått beløp, ikke justert for evt. trekk
    private BigDecimal riktigYtelseBeløp; //rått beløp, ikke justert for evt. trekk

    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public Vurdering getVurdering() {
        return vurdering;
    }

    public void setVurdering(Vurdering vurdering) {
        this.vurdering = vurdering;
    }

    public BigDecimal getFeilutbetaltBeløp() {
        return feilutbetaltBeløp;
    }

    public void setFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
        this.feilutbetaltBeløp = feilutbetaltBeløp;
    }

    public BigDecimal getAndelAvBeløp() {
        return andelAvBeløp;
    }

    public void setAndelAvBeløp(BigDecimal andelAvBeløp) {
        this.andelAvBeløp = andelAvBeløp;
    }

    public BigDecimal getRenterProsent() {
        return renterProsent;
    }

    public void setRenterProsent(BigDecimal renterProsent) {
        this.renterProsent = renterProsent;
    }

    public BigDecimal getTilbakekrevingBeløp() {
        return tilbakekrevingBeløp;
    }

    public void setTilbakekrevingBeløp(BigDecimal tilbakekrevingBeløp) {
        this.tilbakekrevingBeløp = tilbakekrevingBeløp;
    }

    public BigDecimal getTilbakekrevingBeløpUtenRenter() {
        return tilbakekrevingBeløpUtenRenter;
    }

    public void setTilbakekrevingBeløpUtenRenter(BigDecimal tilbakekrevingBeløpUtenRenter) {
        this.tilbakekrevingBeløpUtenRenter = tilbakekrevingBeløpUtenRenter;
    }

    public BigDecimal getRenteBeløp() {
        return renteBeløp;
    }

    public void setRenteBeløp(BigDecimal renteBeløp) {
        this.renteBeløp = renteBeløp;
    }

    public void setManueltSattTilbakekrevingsbeløp(BigDecimal manueltSattTilbakekrevingsbeløp) {
        this.manueltSattTilbakekrevingsbeløp = manueltSattTilbakekrevingsbeløp;
    }

    public BigDecimal getSkattBeløp() {
        return skattBeløp;
    }

    public void setSkattBeløp(BigDecimal skattBeløp) {
        this.skattBeløp = skattBeløp;
    }

    public BigDecimal getTilbakekrevingBeløpEtterSkatt() {
        return tilbakekrevingBeløpEtterSkatt;
    }

    public void setTilbakekrevingBeløpEtterSkatt(BigDecimal tilbakekrevingBeløpEtterSkatt) {
        this.tilbakekrevingBeløpEtterSkatt = tilbakekrevingBeløpEtterSkatt;
    }

    public BigDecimal getManueltSattTilbakekrevingsbeløp() {
        return manueltSattTilbakekrevingsbeløp;
    }

    public BigDecimal getUtbetaltYtelseBeløp() {
        return utbetaltYtelseBeløp;
    }

    public void setUtbetaltYtelseBeløp(BigDecimal utbetaltYtelseBeløp) {
        this.utbetaltYtelseBeløp = utbetaltYtelseBeløp;
    }

    public BigDecimal getRiktigYtelseBeløp() {
        return riktigYtelseBeløp;
    }

    public void setRiktigYtelseBeløp(BigDecimal riktigYtelseBeløp) {
        this.riktigYtelseBeløp = riktigYtelseBeløp;
    }

    public KodeResultat getKodeResultat() {
        if (AnnenVurdering.FORELDET.equals(vurdering)) {
            return KodeResultat.FORELDET;
        }

        if (tilbakekrevingBeløpUtenRenter.signum() == 0) {
            return KodeResultat.INGEN_TILBAKEKREVING;
        }
        if (feilutbetaltBeløp.compareTo(tilbakekrevingBeløpUtenRenter) == 0) {
            return KodeResultat.FULL_TILBAKEKREVING;
        }
        return KodeResultat.DELVIS_TILBAKEKREVING;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BeregningResultatPeriode) {
            BeregningResultatPeriode annen = (BeregningResultatPeriode) o;
            return Objects.equals(periode, annen.periode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
