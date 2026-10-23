package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.UkjentKvitteringFraOSException;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosTilbakekrevingKlient;
import no.nav.foreldrepenger.tilbakekreving.sokos.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;


@CdiDbAwareTest
class SendVedtakTilOppdragsystemetTaskTest {

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    @Inject
    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    @Inject
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    @Inject
    private KravgrunnlagRepository kravgrunnlagRepository;

    private final ØkonomiProxyKlient økonomiProxyKlient = mock(ØkonomiProxyKlient.class);
    private final SokosTilbakekrevingKlient sokosTilbakekrevingKlient = mock(SokosTilbakekrevingKlient.class);

    private SendVedtakTilOppdragsystemetTask task;

    @BeforeEach
    void setup() {
        System.setProperty("app.name", "fptilbake");
        task = new SendVedtakTilOppdragsystemetTask(oppdragIverksettingStatusRepository, beregningsresultatTjeneste,
            tilbakekrevingsvedtakTjeneste, økonomiProxyKlient, sokosTilbakekrevingKlient, () -> true);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("app.name");
    }

    @Test
    void skal_lagre_iverksettingstatus_og_sende_vedtak_til_os() {
        var behandling = ScenarioSimple.simple().medDefaultKravgrunnlag().medFullInnkreving().lagre(behandlingRepositoryProvider);
        brukSokos(behandling.getId());

        task.doTask(lagProsessTaskKonfigurasjon(behandling));

        var request = ArgumentCaptor.forClass(TilbakekrevingsvedtakRequest.class);
        verify(sokosTilbakekrevingKlient).iverksettTilbakekrevingsvedtak(request.capture());
        verify(økonomiProxyKlient, never()).iverksettTilbakekrevingsvedtak(any());
        assertThat(request.getValue().kodeAksjon()).isEqualTo(KodeAksjon.FATTE_VEDTAK);
        assertThat(request.getValue().vedtakId().longValue()).isEqualTo(kravgrunnlagRepository.finnKravgrunnlag(behandling.getId()).getVedtakId());
        assertThat(request.getValue().kodeHjemmel()).isEqualTo("22-15");
        assertThat(DefaultJsonMapper.toJson(request.getValue())).doesNotContain("\"renterBeregnes\"", "\"renterPeriodeBeregnes\"");
        assertThat(request.getValue().perioder()).isNotEmpty();
        assertThat(request.getValue().perioder().getFirst().posteringer()).isNotEmpty();
        var status = oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId());
        assertThat(status).isPresent();
        assertThat(status.get().getKvitteringOk()).isTrue();
    }

    @Test
    void skal_få_exception_når_kvittering_ikke_er_OK() {
        doThrow(new UkjentKvitteringFraOSException("FPT-539080", "Avvist av Sokos"))
            .when(sokosTilbakekrevingKlient).iverksettTilbakekrevingsvedtak(any());
        var behandling = ScenarioSimple.simple().medDefaultKravgrunnlag().medFullInnkreving().lagre(behandlingRepositoryProvider);
        brukSokos(behandling.getId());

        assertThatThrownBy(() -> task.doTask(lagProsessTaskKonfigurasjon(behandling))).hasMessageContaining("Avvist av Sokos");
        assertThat(oppdragIverksettingStatusRepository.hentOppdragIverksettingStatus(behandling.getId())).isEmpty();
    }

    private void brukSokos(Long behandlingId) {
        var sokosTjeneste = mock(TilbakekrevingsvedtakTjeneste.class);
        var dato = LocalDate.of(2026, 1, 15);
        var request = new TilbakekrevingsvedtakRequest(KodeAksjon.FATTE_VEDTAK, 1412, dato,
            "22-15", "8020", "kontrollfelt", "Z999999", null,
            List.of(new TilbakekrevingsvedtakRequest.Periode(
                dato, dato, BigDecimal.ZERO,
                List.of(new TilbakekrevingsvedtakRequest.Postering("FPATORD", BigDecimal.ONE,
                    BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    "FULL_TILBAKEKREV", "ANNET", "IKKE_FORDELT")))));
        when(sokosTjeneste.lagSokosTilbakekrevingsvedtak(behandlingId)).thenReturn(request);
        task = new SendVedtakTilOppdragsystemetTask(oppdragIverksettingStatusRepository, beregningsresultatTjeneste,
            sokosTjeneste, økonomiProxyKlient, sokosTilbakekrevingKlient, () -> true);
    }

    private ProsessTaskData lagProsessTaskKonfigurasjon(Behandling behandling) {
        var data = ProsessTaskData.forProsessTask(SendVedtakTilOppdragsystemetTask.class);
        data.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        return data;
    }

}
