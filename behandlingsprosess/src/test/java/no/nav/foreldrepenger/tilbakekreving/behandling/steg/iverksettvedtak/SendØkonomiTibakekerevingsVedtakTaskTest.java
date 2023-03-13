package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;


@CdiDbAwareTest
class SendØkonomiTibakekerevingsVedtakTaskTest {

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    @Inject
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;

    private final ØkonomiConsumer økonomiConsumer = Mockito.mock(ØkonomiConsumer.class);
    private final ØkonomiProxyKlient økonomiProxyKlient = Mockito.mock(ØkonomiProxyKlient.class);

    private SendØkonomiTibakekerevingsVedtakTask task;

    @BeforeEach
    void setup() {
        task = new SendØkonomiTibakekerevingsVedtakTask(tilbakekrevingsvedtakTjeneste, økonomiConsumer, økonomiProxyKlient, økonomiSendtXmlRepository);
    }

    @Test
    void skal_lagre_xml_og_sende_vedtak_til_os() {
        when(økonomiConsumer.iverksettTilbakekrevingsvedtak(any(), any())).thenReturn(responseMedPositivKvittering());

        ScenarioSimple scenario = ScenarioSimple
                .simple()
                .medDefaultKravgrunnlag()
                .medFullInnkreving();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        ProsessTaskData data = lagProsessTaskKonfigurasjon(behandling);

        task.doTask(data);

        assertThat(økonomiSendtXmlRepository.finnXml(behandling.getId(), MeldingType.VEDTAK)).hasSize(1);
        Mockito.verify(økonomiConsumer).iverksettTilbakekrevingsvedtak(Mockito.eq(behandling.getId()), any(TilbakekrevingsvedtakRequest.class));
    }

    @Test
    void skal_få_exception_når_kvittering_ikke_er_OK() {
        when(økonomiConsumer.iverksettTilbakekrevingsvedtak(any(), any())).thenReturn(responseMedNegativKvittering());

        ScenarioSimple scenario = ScenarioSimple
                .simple()
                .medDefaultKravgrunnlag()
                .medFullInnkreving();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        ProsessTaskData data = lagProsessTaskKonfigurasjon(behandling);

        assertThatThrownBy(() -> task.doTask(data))
                .hasMessageContaining("Fikk feil fra OS ved iverksetting av behandling");
    }

    private ProsessTaskData lagProsessTaskKonfigurasjon(Behandling behandling) {
        ProsessTaskData data = ProsessTaskData.forProsessTask(SendØkonomiTibakekerevingsVedtakTask.class);
        data.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        return data;
    }

    private TilbakekrevingsvedtakResponse responseMedPositivKvittering() {
        return lagResponse("00");
    }

    private TilbakekrevingsvedtakResponse responseMedNegativKvittering() {
        return lagResponse("08");
    }

    private TilbakekrevingsvedtakResponse lagResponse(String kode) {
        MmelDto kvittering = new MmelDto();
        kvittering.setAlvorlighetsgrad(kode);
        TilbakekrevingsvedtakResponse response = new TilbakekrevingsvedtakResponse();
        response.setMmel(kvittering);
        return response;
    }


}
