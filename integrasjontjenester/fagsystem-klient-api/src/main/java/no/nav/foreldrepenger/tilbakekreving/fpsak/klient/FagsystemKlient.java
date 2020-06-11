package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.simulering.FeilutbetaltePerioderDto;

public interface FagsystemKlient {

    boolean finnesBehandlingIFpsak(String saksnummer, Henvisning henvisning);

    SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon);

    Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon);

    Optional<EksternBehandlingsinfoDto> hentBehandlingOptional(UUID eksternUuid);

    EksternBehandlingsinfoDto hentBehandling(UUID eksternUuid);

    Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid);

    List<EksternBehandlingsinfoDto> hentBehandlingForSaksnummer(String saksnummer);

    FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning);

}
