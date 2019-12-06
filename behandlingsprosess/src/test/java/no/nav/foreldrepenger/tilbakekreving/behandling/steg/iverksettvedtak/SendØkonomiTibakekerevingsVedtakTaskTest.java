package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;


import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.avstemming.IverksattVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;


@RunWith(CdiRunner.class)
public class SendØkonomiTibakekerevingsVedtakTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    @Inject
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;
    @Inject
    private IverksattVedtakRepository iverksattVedtakRepository;

    private ØkonomiConsumer økonomiConsumer = Mockito.mock(ØkonomiConsumer.class);

    @Test
    public void skal_lagre_xml_og_sende_vedtak_til_os() {
        ScenarioSimple scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medDefaultVilkårsvurdering();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);

        ProsessTaskData data = new ProsessTaskData(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE);
        data.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());

        SendØkonomiTibakekerevingsVedtakTask task = new SendØkonomiTibakekerevingsVedtakTask(tilbakekrevingsvedtakTjeneste, økonomiConsumer, økonomiSendtXmlRepository, iverksattVedtakRepository);
        task.doTask(data);

        assertThat(økonomiSendtXmlRepository.finnXml(behandling.getId(), MeldingType.VEDTAK)).hasSize(1);
        Mockito.verify(økonomiConsumer).iverksettTilbakekrevingsvedtak(Mockito.eq(behandling.getId()), Mockito.any(TilbakekrevingsvedtakDto.class));
    }


}
