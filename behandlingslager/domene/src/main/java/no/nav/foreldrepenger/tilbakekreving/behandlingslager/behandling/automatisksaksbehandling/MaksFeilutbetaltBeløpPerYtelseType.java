package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling;

import java.math.BigDecimal;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public enum MaksFeilutbetaltBeløpPerYtelseType {

    FP_MAKS_FEILUTBETALT_BELØP(FagsakYtelseType.FORELDREPENGER, BigDecimal.valueOf(586l)),
    EN_MAKS_FEILUTBETALT_BELØP(FagsakYtelseType.ENGANGSTØNAD, BigDecimal.valueOf(586l)),
    SVP_MAKS_FEILUTBETALT_BELØP(FagsakYtelseType.SVANGERSKAPSPENGER, BigDecimal.valueOf(586l));

    private FagsakYtelseType fagsakYtelseType;
    private BigDecimal maksFeilutbetaltBeløp;

    MaksFeilutbetaltBeløpPerYtelseType(FagsakYtelseType fagsakYtelseType, BigDecimal maksFeilutbetaltBeløp) {
        this.fagsakYtelseType = fagsakYtelseType;
        this.maksFeilutbetaltBeløp = maksFeilutbetaltBeløp;
    }

    public static BigDecimal getMaksFeilutbetaltBeløp(FagsakYtelseType fagsakYtelseType) {
        for (var v : values()) {
            if (v.fagsakYtelseType.equals(fagsakYtelseType)) {
                return v.maksFeilutbetaltBeløp;
            }
        }
        return null;
    }

}
