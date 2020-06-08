package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

public class YtelsesbehandlingResultatType extends Kodeliste {

    public static final YtelsesbehandlingResultatType OPPHØR = new YtelsesbehandlingResultatType("OPPHØR");

    protected YtelsesbehandlingResultatType() {
    }

    protected YtelsesbehandlingResultatType(String kode) {
        super(kode, "BEHANDLING_RESULTAT_TYPE");
    }
}
