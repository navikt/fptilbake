package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;

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

        var historikkinnslager = historikkinnslagRepository.hent(internBehandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(2);
        assertThat(historikkinnslager.get(0).getTittel()).isEqualTo("Tilbakekreving opprettet");
        assertThat(historikkinnslager.get(0).getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslager.get(1).getTittel()).isEqualTo("Bytt enhet");
        assertThat(historikkinnslager.get(1).getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
    }
}
