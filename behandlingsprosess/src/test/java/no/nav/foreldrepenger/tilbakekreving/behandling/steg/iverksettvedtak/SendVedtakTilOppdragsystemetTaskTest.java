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

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.UkjentKvitteringFraOSException;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
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

    private final ØkonomiProxyKlient økonomiConsumer = Mockito.mock(ØkonomiProxyKlient.class);

    private SendVedtakTilOppdragsystemetTask task;

    @BeforeEach
    void setup() {
        task = new SendVedtakTilOppdragsystemetTask(entityManager, oppdragIverksettingStatusRepository, beregningsresultatTjeneste, tilbakekrevingsvedtakTjeneste, økonomiConsumer, true);
    }

    @Test
    void skal_lagre_iverksettingstatus_og_sende_vedtak_til_os() {
        ScenarioSimple scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medFullInnkreving();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);

        ProsessTaskData data = lagProsessTaskKonfigurasjon(behandling);

        task.doTask(data);

        //har sendt til OS:
        Mockito.verify(økonomiConsumer).iverksettTilbakekrevingsvedtak(any(TilbakekrevingVedtakDTO.class));

        //har lagret status riktig
        Optional<OppdragIverksettingStatusEntitet> status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
        assertThat(status).isPresent();
        assertThat(status.get().getKvitteringOk()).isTrue();

        //skal ikke lenger lagre noe XML
        assertThat(økonomiSendtXmlRepository.finnXml(behandling.getId(), MeldingType.VEDTAK)).isEmpty();
    }

    @Test
    void skal_få_exception_når_kvittering_ikke_er_OK() {
        when(økonomiConsumer.iverksettTilbakekrevingsvedtak(any())).thenThrow(UkjentKvitteringFraOSException.class);

        ScenarioSimple scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medFullInnkreving();
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        ProsessTaskData data = lagProsessTaskKonfigurasjon(behandling);

        assertThatThrownBy(() -> task.doTask(data)).hasMessageContaining("Fikk feil fra OS ved iverksetting av behandling");
        //har lagret status riktig
        Optional<OppdragIverksettingStatusEntitet> status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
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

}
