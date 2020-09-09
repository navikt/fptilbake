package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class MaksFeilutbetaltBeløpPerYtelseType {

    MaksFeilutbetaltBeløpPerYtelseType(){
        // for CDI
    }

    private static Map<FagsakYtelseType,BigDecimal> maksFeilutbetaltBeløpPerYtelseTypeMap = new EnumMap<>(FagsakYtelseType.class);

    static {
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.FORELDREPENGER, BigDecimal.valueOf(586l));
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.ENGANGSTØNAD, BigDecimal.valueOf(586l));
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.SVANGERSKAPSPENGER, BigDecimal.valueOf(586l));
    }

    public static BigDecimal getMaksFeilutbetaltBeløp(FagsakYtelseType fagsakYtelseType) {
        return maksFeilutbetaltBeløpPerYtelseTypeMap.get(fagsakYtelseType);
    }

}
