package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;

public interface FagsystemKlient {

    boolean finnesBehandlingIFagsystem(String saksnummer, Henvisning henvisning);

    SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon);

    Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon);

    Optional<EksternBehandlingsinfoDto> hentBehandlingOptional(UUID eksternUuid);

    EksternBehandlingsinfoDto hentBehandling(UUID eksternUuid);

    Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid);

    List<EksternBehandlingsinfoDto> hentBehandlingForSaksnummer(String saksnummer);

    FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning);

    default FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning, UUID behandlingUuid, String saksnummer) {;
        return hentFeilutbetaltePerioder(henvisning);
    }

}
