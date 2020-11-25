package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.felles.HalvRettsgebyr;
import no.nav.foreldrepenger.tilbakekreving.felles.Rettsgebyr;

public class MaksFeilutbetaltBeløpPerYtelseType {

    MaksFeilutbetaltBeløpPerYtelseType(){
        // for CDI
    }

    private static Map<FagsakYtelseType,BigDecimal> maksFeilutbetaltBeløpPerYtelseTypeMap = new EnumMap<>(FagsakYtelseType.class);

    static {
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.FORELDREPENGER, HalvRettsgebyr.getGebyr());
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.ENGANGSTØNAD, HalvRettsgebyr.getGebyr());
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.SVANGERSKAPSPENGER, HalvRettsgebyr.getGebyr());
        maksFeilutbetaltBeløpPerYtelseTypeMap.put(FagsakYtelseType.FRISINN, Rettsgebyr.getGebyr());
    }

    public static BigDecimal getMaksFeilutbetaltBeløp(FagsakYtelseType fagsakYtelseType) {
        return maksFeilutbetaltBeløpPerYtelseTypeMap.get(fagsakYtelseType);
    }

}
