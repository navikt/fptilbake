package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.util.Set;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.typer.v1.MmelDto;

public class ØkonomiKvitteringTolk {

    private static final Set<String> KVITTERING_OK_KODE = Set.of("00", "04");
    private static final String KODE_MELDING_KRAVGRUNNLAG_IKKE_FINNES = "B420010I";
    public static final String KODE_MELDING_KRAVGRUNNLAG_ER_SPERRET = "B420012I";

    ØkonomiKvitteringTolk() {
        // privat construktor
    }

    public static boolean erKvitteringOK(MmelDto kvittering) {
        return kvittering != null && KVITTERING_OK_KODE.contains(kvittering.getAlvorlighetsgrad());
    }

    public static boolean erKvitteringOK(TilbakekrevingsvedtakResponse response) {
        return response != null && erKvitteringOK(response.getMmel());
    }

    public static boolean erKravgrunnlagetIkkeFinnes(MmelDto kvittering) {
        return kvittering != null && KODE_MELDING_KRAVGRUNNLAG_IKKE_FINNES.equals(kvittering.getKodeMelding());
    }

    public static boolean erKravgrunnlagetSperret(MmelDto kvittering) {
        return kvittering != null && KODE_MELDING_KRAVGRUNNLAG_ER_SPERRET.equals(kvittering.getKodeMelding());
    }

    public static boolean harKravgrunnlagNoeUkjentFeil(MmelDto kvittering) {
        return kvittering != null && !nullOrEmpty(kvittering.getKodeMelding()) && !nullOrEmpty(kvittering.getBeskrMelding());
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

}

