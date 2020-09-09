package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class MaksFeilutbetaltBeløpPerYtelseType {

    private static Map<FagsakYtelseType,BigDecimal> maksFeilutbetaltBeløpPerYtelseType = new HashMap<>();

    static {
        maksFeilutbetaltBeløpPerYtelseType.put(FagsakYtelseType.FORELDREPENGER, BigDecimal.valueOf(586l));
        maksFeilutbetaltBeløpPerYtelseType.put(FagsakYtelseType.ENGANGSTØNAD, BigDecimal.valueOf(586l));
        maksFeilutbetaltBeløpPerYtelseType.put(FagsakYtelseType.SVANGERSKAPSPENGER, BigDecimal.valueOf(586l));
    }

    public static BigDecimal getMaksFeilutbetaltBeløp(FagsakYtelseType fagsakYtelseType) {
        return maksFeilutbetaltBeløpPerYtelseType.get(fagsakYtelseType);
    }

}
