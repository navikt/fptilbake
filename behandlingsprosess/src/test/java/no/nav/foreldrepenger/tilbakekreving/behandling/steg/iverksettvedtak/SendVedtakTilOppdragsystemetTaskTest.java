package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.UkjentKvitteringFraOSException;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;


@CdiDbAwareTest
class SendVedtakTilOppdragsystemetTaskTest {

    @Inject
    private EntityManager entityManager;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    @Inject
    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    @Inject
    private BeregningsresultatTjeneste beregningsresultatTjeneste;

    private final ØkonomiProxyKlient økonomiProxyKlient = Mockito.mock(ØkonomiProxyKlient.class);

    private SendVedtakTilOppdragsystemetTask task;

    @BeforeEach
    void setup() {
        task = new SendVedtakTilOppdragsystemetTask(entityManager, behandlingRepository, oppdragIverksettingStatusRepository, beregningsresultatTjeneste, tilbakekrevingsvedtakTjeneste, økonomiProxyKlient);
    }

    @Test
    void skal_lagre_iverksettingstatus_og_sende_vedtak_til_os() {
        var scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medFullInnkreving();
        var behandling = scenario.lagre(behandlingRepositoryProvider);

        var data = lagProsessTaskKonfigurasjon(behandling);

        task.doTask(data);

        //har sendt til OS:
        verify(økonomiProxyKlient).iverksettTilbakekrevingsvedtak(any(TilbakekrevingVedtakDTO.class));

        //har lagret status riktig
        var status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
        assertThat(status).isPresent();
        assertThat(status.get().getKvitteringOk()).isTrue();
    }

    @Test
    void skal_få_exception_når_kvittering_ikke_er_OK() {
        doThrow(new UkjentKvitteringFraOSException("FPT-539080", "Fikk feil fra OS ved iverksetting av tilbakekrevginsvedtak. Sjekk loggen til fpwsproxy for mer info."))
            .when(økonomiProxyKlient).iverksettTilbakekrevingsvedtak(any());

        var scenario = ScenarioSimple
            .simple()
            .medDefaultKravgrunnlag()
            .medFullInnkreving();
        var behandling = scenario.lagre(behandlingRepositoryProvider);
        var data = lagProsessTaskKonfigurasjon(behandling);

        assertThatThrownBy(() -> task.doTask(data)).hasMessageContaining("Fikk feil fra OS ved iverksetting av behandling");
        //har lagret status riktig
        var status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
        assertThat(status).isPresent();
        assertThat(status.get().getKvitteringOk()).isFalse();
    }

    private ProsessTaskData lagProsessTaskKonfigurasjon(Behandling behandling) {
        var data = ProsessTaskData.forProsessTask(SendVedtakTilOppdragsystemetTask.class);
        data.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        return data;
    }

}
