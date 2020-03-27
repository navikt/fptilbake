package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;

public class YtelseTypeMapper {


    private static final Map<FagsakYtelseType, YtelseType> MAPPING = Arrays.stream(YtelseType.values())
        .collect(Collectors.toMap(yt -> FagsakYtelseType.fraKode(yt.name()), Function.identity()));

    public static YtelseType getYtelseType(FagsakYtelseType fagsakYtelseType) {
        var verdi = MAPPING.get(fagsakYtelseType);
        if (verdi == null) {
            throw new IllegalArgumentException("Mangler mapping fra " + FagsakYtelseType.class + " til " + YtelseType.class + " for " + fagsakYtelseType);
        }
        return verdi;
    }
}
