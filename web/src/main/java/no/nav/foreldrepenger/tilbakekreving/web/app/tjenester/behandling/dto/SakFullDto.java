package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDto;

public record SakFullDto(String saksnummer,
                         List<BehandlingOpprettingDto> behandlingTypeKanOpprettes,
                         List<BehandlingDto> behandlinger,
                         List<HistorikkinnslagDto> historikkinnslag) {
}
