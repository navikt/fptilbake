package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;


@RunWith(CdiRunner.class)
public class SendØkonomiTibakekerevingsVedtakTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    @Inject
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;

    private ØkonomiConsumer økonomiConsumer = Mockito.mock(ØkonomiConsumer.class);

    private SendØkonomiTibakekerevingsVedtakTask task;

    @Before
    public void setup(){
         task = new SendØkonomiTibakekerevingsVedtakTask(tilbakekrevingsvedtakTjeneste, økonomiConsumer, økonomiSendtXmlRepository);
    }

    @Test
    public void skal_lagre_xml_og_sende_vedtak_til_os() {
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
    public void skal_få_exception_når_kvittering_ikke_er_OK() {
        when(økonomiConsumer.iverksettTilbakekrevingsvedtak(any(), any())).thenReturn(responseMedNegativKvittering());

        ScenarioSimple scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medFullInnkreving();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        ProsessTaskData data = lagProsessTaskKonfigurasjon(behandling);

        expectedException.expectMessage("Fikk feil fra OS ved iverksetting av behandling");

        task.doTask(data);
    }

    private ProsessTaskData lagProsessTaskKonfigurasjon(Behandling behandling) {
        ProsessTaskData data = new ProsessTaskData(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE);
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
