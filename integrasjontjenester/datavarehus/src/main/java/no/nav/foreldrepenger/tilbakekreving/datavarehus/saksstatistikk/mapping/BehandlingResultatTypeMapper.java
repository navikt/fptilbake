package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingResultat;

public class BehandlingResultatTypeMapper {


    private static final Map<BehandlingResultatType, BehandlingResultat> MAPPING = Arrays.stream(BehandlingResultat.values())
        .collect(Collectors.toMap(yt -> BehandlingResultatType.fraKode(yt.name()), Function.identity()));

    public static BehandlingResultat getBehandlingResultatType(BehandlingResultatType behandlingResultatType) {
        var verdi = MAPPING.get(behandlingResultatType);
        if (verdi == null) {
            throw new IllegalArgumentException("Mangler mapping fra " + BehandlingResultatType.class + " til " + BehandlingResultat.class + " for " + behandlingResultatType);
        }
        return verdi;
    }

}
