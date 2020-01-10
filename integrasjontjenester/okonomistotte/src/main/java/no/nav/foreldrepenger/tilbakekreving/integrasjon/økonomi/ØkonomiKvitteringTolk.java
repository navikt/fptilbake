package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.util.Set;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.typer.v1.MmelDto;

public class ØkonomiKvitteringTolk {

    private static final Set<String> KVITTERING_OK_KODE = Set.of("00", "04");

    public static boolean erKvitteringOK(MmelDto kvittering) {
        return kvittering != null && KVITTERING_OK_KODE.contains(kvittering.getAlvorlighetsgrad());
    }

    public static boolean erKvitteringOK(TilbakekrevingsvedtakResponse response) {
        return response != null && erKvitteringOK(response.getMmel());
    }
}

