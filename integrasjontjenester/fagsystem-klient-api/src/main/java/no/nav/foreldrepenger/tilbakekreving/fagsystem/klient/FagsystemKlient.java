package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient;

import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;

public interface FagsystemKlient {

    default boolean finnesBehandlingIFagsystem(String saksnummer, Henvisning henvisning) {
        return hentBehandlingForSaksnummerHenvisning(saksnummer, henvisning).isPresent();
    }

    SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon);

    Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon);

    Optional<EksternBehandlingsinfoDto> hentBehandlingOptional(UUID eksternUuid);

    EksternBehandlingsinfoDto hentBehandling(UUID eksternUuid);

    Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid);

    Optional<EksternBehandlingsinfoDto> hentBehandlingForSaksnummerHenvisning(String saksnummer, Henvisning henvisning);

    FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning);

    default FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning, UUID behandlingUuid, String saksnummer) {;
        return hentFeilutbetaltePerioder(henvisning);
    }

}
