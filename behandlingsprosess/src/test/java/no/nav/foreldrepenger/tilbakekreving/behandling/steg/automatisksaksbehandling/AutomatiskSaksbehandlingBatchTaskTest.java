package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktTestSupport;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(JpaExtension.class)
class AutomatiskSaksbehandlingBatchTaskTest {

    private static final String ENHET = "8020";

    private final ScenarioSimple scenarioSimple = ScenarioSimple.simple();
    private Behandling behandling;

    private BehandlingRepositoryProvider repositoryProvider;
    private ProsessTaskTjeneste taskTjeneste;
    private AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository;
    private final Clock clock = Clock.fixed(Instant.parse(getDateString()), ZoneId.systemDefault());
    private AutomatiskSaksbehandlingBatchTask automatiskSaksbehandlingBatchTask;

    @BeforeEach
    void setup(EntityManager entityManager) {
        entityManager.setFlushMode(FlushModeType.AUTO);
        scenarioSimple.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        automatiskSaksbehandlingRepository = new AutomatiskSaksbehandlingRepository(entityManager);
        automatiskSaksbehandlingBatchTask = new AutomatiskSaksbehandlingBatchTask(taskTjeneste, automatiskSaksbehandlingRepository, clock, Period.ofWeeks(-1));
        behandling = scenarioSimple.medBehandlingType(BehandlingType.TILBAKEKREVING).lagre(repositoryProvider);
        lagKravgrunnlag(behandling.getId(), BigDecimal.valueOf(500L), behandling.getFagsak().getSaksnummer().getVerdi(),
                123L);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegStatus.UTGANG, BehandlingStegStatus.UTGANG);
    }

    @Test
    void skal_opprette_prosess_task_for_å_saksbehandle_behandling_automatisk() {
        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTasker = captor.getAllValues();
        assertThat(prosessTasker).hasSize(1);
        var prosessTaskData = prosessTasker.get(0);
        assertThat(Long.valueOf(prosessTaskData.getBehandlingId())).isEqualTo(behandling.getId());
        assertThat(prosessTaskData.getSekvens()).isEqualTo("10");
    }

    @Test
    void skal_ikke_kjøre_batch_i_helgen() {
        var helgeClock = Clock.fixed(Instant.parse("2020-05-03T12:00:00.00Z"), ZoneId.systemDefault());
        var automatiskSaksbehandlingBatchTask = new AutomatiskSaksbehandlingBatchTask(taskTjeneste, automatiskSaksbehandlingRepository, helgeClock, Period.ofWeeks(-1));

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_kjøre_batch_hvis_hellidag() {
        var helgeClock = Clock.fixed(Instant.parse("2021-05-17T12:00:00.00Z"), ZoneId.systemDefault());
        var automatiskSaksbehandlingBatchTask = new AutomatiskSaksbehandlingBatchTask(taskTjeneste, automatiskSaksbehandlingRepository, helgeClock, Period.ofWeeks(-1));

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_opprette_prosess_tasker_for_behandlinger_med_større_feilutbetalt_beløp() {
        lagKravgrunnlag(behandling.getId(), BigDecimal.valueOf(1500L), behandling.getFagsak().getSaksnummer().getVerdi(), 123L);

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_opprette_prosess_tasker_for_avsluttet_behandling() {
        behandling.avsluttBehandling();

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_opprette_prosess_tasker_når_behandling_er_allerede_saksbehandlet() {
        behandling.setAnsvarligSaksbehandler("124");

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_opprette_prosess_tasker_når_behandling_er_sett_på_vent() {
        AksjonspunktTestSupport.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.FAKTA_FEILUTBETALING);

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_opprette_prosess_tasker_når_behandling_er_allerede_varslet() {
        var brevSporing = new BrevSporing.Builder()
                .medBehandlingId(behandling.getId())
                .medDokumentId("sdfkjsdlfsd")
                .medJournalpostId(new JournalpostId("dkasfjsklfsd"))
                .medBrevType(BrevType.VARSEL_BREV)
                .build();
        repositoryProvider.getBrevSporingRepository().lagre(brevSporing);

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        verifyNoInteractions(taskTjeneste);
    }

    private String getDateString() {
        return (LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY || LocalDate.now().getDayOfWeek() == DayOfWeek.SATURDAY) ?
                Instant.now().plus(2, ChronoUnit.DAYS).toString() :
                Instant.now().toString();
    }

    private void lagKravgrunnlag(Long behandlingId, BigDecimal beløp, String saksnummer, long referanse) {
        Periode april2020 = Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Kravgrunnlag431 kravgrunnlag431 = Kravgrunnlag431.builder().medEksternKravgrunnlagId("162818")
                .medAnsvarligEnhet(ENHET).medBehandlendeEnhet(ENHET).medBostedEnhet(ENHET)
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER).medFagSystemId(saksnummer + 100)
                .medGjelderType(GjelderType.PERSON).medGjelderVedtakId("???")
                .medUtbetIdType(GjelderType.PERSON).medUtbetalesTilId("???")
                .medFeltKontroll("2019-11-22-19.09.31.458065").medKravStatusKode(KravStatusKode.NYTT)
                .medVedtakId(123L).medSaksBehId("K231B433")
                .medReferanse(Henvisning.fraEksternBehandlingId(referanse)).medVedtakFagSystemDato(LocalDate.now())
                .build();

        KravgrunnlagPeriode432 periode432 = KravgrunnlagPeriode432.builder().medPeriode(april2020)
                .medBeløpSkattMnd(BigDecimal.ZERO).medKravgrunnlag431(kravgrunnlag431).build();

        KravgrunnlagBelop433 feilPostering = KravgrunnlagBelop433.builder()
                .medKlasseKode(KlasseKode.KL_KODE_FEIL_KORTTID).medKlasseType(KlasseType.FEIL)
                .medKravgrunnlagPeriode432(periode432).medSkattProsent(BigDecimal.ZERO)
                .medNyBelop(beløp)
                .medTilbakekrevesBelop(BigDecimal.ZERO).medOpprUtbetBelop(BigDecimal.ZERO).build();

        KravgrunnlagBelop433 ytelPostering = KravgrunnlagBelop433.builder()
                .medKlasseKode(KlasseKode.FPADATORD).medKlasseType(KlasseType.YTEL)
                .medKravgrunnlagPeriode432(periode432).medSkattProsent(BigDecimal.ZERO)
                .medNyBelop(BigDecimal.ZERO)
                .medTilbakekrevesBelop(beløp).medOpprUtbetBelop(beløp).build();

        periode432.leggTilBeløp(feilPostering);
        periode432.leggTilBeløp(ytelPostering);
        kravgrunnlag431.leggTilPeriode(periode432);
        repositoryProvider.getGrunnlagRepository().lagre(behandlingId, kravgrunnlag431);
    }

    private ProsessTaskData lagProsessTaskData() {
        return ProsessTaskData.forProsessTask(AutomatiskSaksbehandlingBatchTask.class);
    }
}
