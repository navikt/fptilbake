package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import java.math.BigDecimal;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingBeløp;

/**
 * utvider prod-klasse med hjelpemetoder nyttig for test
 */
public class TbkBeløp extends TilbakekrevingBeløp {

    public TbkBeløp(KlasseType klasseType, String klassekode) {
        super(klasseType, klassekode);
    }

    public static TbkBeløp feil(int beløp) {
        return feil(beløp, "foobar");
    }

    public static TbkBeløp feil(int beløp, String klassekode) {
        return new TbkBeløp(KlasseType.FEIL, klassekode)
            .medNyttBeløp(beløp)
            .medUtbetBeløp(0)
            .medUinnkrevdBeløp(0)
            .medTilbakekrevBeløp(0)
            .medSkattBeløp(0);
    }

    public static TbkBeløp ytelse(KlasseKode klasseKode) {
        return new TbkBeløp(KlasseType.YTEL, klasseKode.getKode());
    }

    public static TbkBeløp trekk(int beløp) {
        return trekk(beløp, "foobaz");
    }

    public static TbkBeløp trekk(int beløp, String klassekode) {
        return new TbkBeløp(KlasseType.TREK, klassekode)
            .medNyttBeløp(beløp);
    }

    public TbkBeløp medUtbetBeløp(BigDecimal utbetaltBeløp) {
        super.medUtbetBeløp(utbetaltBeløp);
        return this;
    }

    public TbkBeløp medTilbakekrevBeløp(BigDecimal tilbakekrevBeløp) {
        super.medTilbakekrevBeløp(tilbakekrevBeløp);
        return this;
    }

    public TbkBeløp medUinnkrevdBeløp(BigDecimal uinnkrevdBeløp) {
        super.medUinnkrevdBeløp(uinnkrevdBeløp);
        return this;
    }

    public TbkBeløp medNyttBeløp(BigDecimal nyttBeløp) {
        super.medNyttBeløp(nyttBeløp);
        return this;
    }

    public TbkBeløp medUtbetBeløp(int utbetaltBeløp) {
        super.medUtbetBeløp(BigDecimal.valueOf(utbetaltBeløp));
        return this;
    }

    public TbkBeløp medTilbakekrevBeløp(int tilbakekrevBeløp) {
        super.medTilbakekrevBeløp(BigDecimal.valueOf(tilbakekrevBeløp));
        return this;
    }

    public TbkBeløp medUinnkrevdBeløp(int uinnkrevdBeløp) {
        super.medUinnkrevdBeløp(BigDecimal.valueOf(uinnkrevdBeløp));
        return this;
    }

    public TbkBeløp medNyttBeløp(int nyttBeløp) {
        super.medNyttBeløp(BigDecimal.valueOf(nyttBeløp));
        return this;
    }

    public TbkBeløp medSkattBeløp(int skattBeløp) {
        super.medSkattBeløp(BigDecimal.valueOf(skattBeløp));
        return this;
    }
}
