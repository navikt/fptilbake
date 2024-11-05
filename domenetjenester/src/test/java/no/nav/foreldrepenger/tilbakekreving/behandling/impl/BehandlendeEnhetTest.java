package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

class BehandlendeEnhetTest extends FellesTestOppsett {

    private static final String NY_ENHET_ID = "4849";
    private static final String NY_ENHET_NAVN = "Nav familie- og pensjonsytelser Tromsø";
    private BehandlingEventPubliserer mockEventPubliserer;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;

    @BeforeEach
    void setUp() {
        mockEventPubliserer = mock(BehandlingEventPubliserer.class);
        behandlendeEnhetTjeneste = new BehandlendeEnhetTjeneste(repoProvider, mockEventPubliserer);
    }

    @Test
    void skal_byttBehandlendeEnhet_med_gyldig_behandling() {
        assertThat(behandling.getBehandlendeEnhetId()).isEqualTo(BEHANDLENDE_ENHET_ID);
        assertThat(behandling.getBehandlendeEnhetNavn()).isEqualTo(BEHANDLENDE_ENHET_NAVN);

        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet(NY_ENHET_ID, NY_ENHET_NAVN);
        behandlendeEnhetTjeneste.byttBehandlendeEnhet(internBehandlingId, organisasjonsEnhet, HistorikkAktør.SAKSBEHANDLER);

        Behandling behandling = behandlingRepository.hentBehandling(internBehandlingId);
        assertThat(behandling.getBehandlendeEnhetId()).isEqualTo(NY_ENHET_ID);
        assertThat(behandling.getBehandlendeEnhetNavn()).isEqualTo(NY_ENHET_NAVN);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(internBehandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BYTT_ENHET);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
    }
}
