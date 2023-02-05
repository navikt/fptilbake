package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
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
class SendVedtakTilOppdragsystemetTaskTest {

    @Inject
    private EntityManager entityManager;
    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    @Inject
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;
    @Inject
    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    @Inject
    private BeregningsresultatTjeneste beregningsresultatTjeneste;

    private final ØkonomiConsumer økonomiConsumer = Mockito.mock(ØkonomiConsumer.class);

    private SendVedtakTilOppdragsystemetTask task;

    @BeforeEach
    void setup() {
        task = new SendVedtakTilOppdragsystemetTask(entityManager, oppdragIverksettingStatusRepository, beregningsresultatTjeneste, tilbakekrevingsvedtakTjeneste, økonomiConsumer, true);
    }

    @Test
    void skal_lagre_iverksettingstatus_og_sende_vedtak_til_os() {
        when(økonomiConsumer.iverksettTilbakekrevingsvedtak(any(), any())).thenReturn(responseMedPositivKvittering());

        ScenarioSimple scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medFullInnkreving();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);

        ProsessTaskData data = lagProsessTaskKonfigurasjon(behandling);

        task.doTask(data);

        //har sendt til OS:
        Mockito.verify(økonomiConsumer).iverksettTilbakekrevingsvedtak(Mockito.eq(behandling.getId()), any(TilbakekrevingsvedtakRequest.class));

        //har lagret status riktig
        Optional<OppdragIverksettingStatus> status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
        assertThat(status).isPresent();
        assertThat(status.get().getKvitteringOk()).isTrue();

        //skal ikke lenger lagre noe XML
        assertThat(økonomiSendtXmlRepository.finnXml(behandling.getId(), MeldingType.VEDTAK)).isEmpty();
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

        assertThatThrownBy(() -> task.doTask(data)).hasMessageContaining("Fikk feil fra OS ved iverksetting av behandling");
        //har lagret status riktig
        Optional<OppdragIverksettingStatus> status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
        assertThat(status).isPresent();
        assertThat(status.get().getKvitteringOk()).isFalse();

        //skal ikke lenger lagre noe XML
        assertThat(økonomiSendtXmlRepository.finnXml(behandling.getId(), MeldingType.VEDTAK)).isEmpty();
    }

    private ProsessTaskData lagProsessTaskKonfigurasjon(Behandling behandling) {
        ProsessTaskData data = ProsessTaskData.forProsessTask(SendVedtakTilOppdragsystemetTask.class);
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
