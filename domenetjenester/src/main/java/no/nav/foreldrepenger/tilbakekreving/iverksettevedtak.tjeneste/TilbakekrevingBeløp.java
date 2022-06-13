package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeResultat;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

public class TilbakekrevingBeløp {
    private KlasseType klasseType;
    private String klassekode;
    private BigDecimal nyttBeløp;
    private BigDecimal utbetaltBeløp;
    private BigDecimal tilbakekrevBeløp;
    private BigDecimal uinnkrevdBeløp;
    private BigDecimal skattBeløp;
    private KodeResultat kodeResultat;

    public TilbakekrevingBeløp(KlasseType klasseType, String klassekode) {
        this.klasseType = klasseType;
        this.klassekode = klassekode;
    }

    public TilbakekrevingBeløp medKodeResultat(KodeResultat kodeResultat) {
        this.kodeResultat = kodeResultat;
        return this;
    }

    public TilbakekrevingBeløp medUtbetBeløp(BigDecimal utbetaltBeløp) {
        this.utbetaltBeløp = utbetaltBeløp;
        return this;
    }

    public TilbakekrevingBeløp medTilbakekrevBeløp(BigDecimal tilbakekrevBeløp) {
        this.tilbakekrevBeløp = tilbakekrevBeløp;
        return this;
    }

    public TilbakekrevingBeløp medUinnkrevdBeløp(BigDecimal uinnkrevdBeløp) {
        this.uinnkrevdBeløp = uinnkrevdBeløp;
        return this;
    }

    public TilbakekrevingBeløp medNyttBeløp(BigDecimal nyttBeløp) {
        this.nyttBeløp = nyttBeløp;
        return this;
    }

    public TilbakekrevingBeløp medSkattBeløp(BigDecimal skattBeløp) {
        this.skattBeløp = skattBeløp;
        return this;
    }

    public KodeResultat getKodeResultat() {
        return kodeResultat;
    }

    public KlasseType getKlasseType() {
        return klasseType;
    }

    public String getKlassekode() {
        return klassekode;
    }

    public BigDecimal getNyttBeløp() {
        return nyttBeløp;
    }

    public BigDecimal getUtbetaltBeløp() {
        return utbetaltBeløp;
    }

    public BigDecimal getTilbakekrevBeløp() {
        return tilbakekrevBeløp;
    }

    public BigDecimal getUinnkrevdBeløp() {
        return uinnkrevdBeløp;
    }

    public BigDecimal getSkattBeløp() {
        return skattBeløp;
    }

    public boolean erIkkeSkattepliktig() {
        return !erSkattepliktig();
    }

    private static final Pattern SKATTEPLIKTIGE_YTELSE_PATTERN = Pattern.compile("^((FP|FPAD|FPSV|PNBS|PPNP|OM|OPP)(ATORD|ATFRI|ATAL|ATSJO)|FRISINN-FRILANS)$");
    public boolean erSkattepliktig(){
        //nav trekker skatt bare for arbeidstaker/frilans-type klassekoder
        // se for eksempel https://github.com/navikt/k9-oppdrag/blob/master/domene/oppdragslager/src/main/java/no/nav/k9/oppdrag/oppdragslager/%C3%B8konomioppdrag/%C3%98konomiKodeKlassifik.java for mønser
        return SKATTEPLIKTIGE_YTELSE_PATTERN.matcher(klassekode).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TilbakekrevingBeløp annen) {
            return Objects.equals(klasseType, annen.klasseType) &&
                    Objects.equals(klassekode, annen.klassekode) &&
                    equals(nyttBeløp, annen.nyttBeløp) &&
                    equals(utbetaltBeløp, annen.utbetaltBeløp) &&
                    equals(tilbakekrevBeløp, annen.tilbakekrevBeløp) &&
                    equals(uinnkrevdBeløp, annen.uinnkrevdBeløp) &&
                    equals(skattBeløp, annen.skattBeløp);
        }
        return false;
    }

    static boolean equals(BigDecimal a, BigDecimal b) {
        return a == b || a != null && b != null && a.compareTo(b) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(klasseType, klassekode);
    }

    @Override
    public String toString() {
        return "TilbakekrevingBeløp{" +
                "type=" + (klasseType != null ? klasseType.getKode() : "null") +
                ", kode='" + klassekode + '\'' +
                ", nytt=" + nyttBeløp +
                ", utbetalt=" + utbetaltBeløp +
                ", tilbakekrev=" + tilbakekrevBeløp +
                ", uinnkrevd=" + uinnkrevdBeløp +
                ", skattBeløp=" + skattBeløp +
                '}';
    }
}
