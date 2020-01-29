package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

public class FpsakBehandlingResultatType extends Kodeliste {

    public static final FpsakBehandlingResultatType OPPHØR = new FpsakBehandlingResultatType("OPPHØR");

    protected FpsakBehandlingResultatType() {
    }

    protected FpsakBehandlingResultatType(String kode) {
        super(kode, "BEHANDLING_RESULTAT_TYPE");
    }
}
