package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class AutomatiskSaksbehandlingBatchTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private ScenarioSimple scenarioSimple = ScenarioSimple.simple();
    private static final String ENHET = "8020";
    private Behandling behandling;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(repositoryRule.getEntityManager());
    private InternalManipulerBehandling manipulerBehandling = new InternalManipulerBehandling(repositoryProvider);
    private ProsessTaskRepository taskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
    private AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository = new AutomatiskSaksbehandlingRepository(repositoryRule.getEntityManager());
    private Clock clock = Clock.fixed(Instant.parse(getDateString()), ZoneId.systemDefault());
    private AutomatiskSaksbehandlingBatchTask automatiskSaksbehandlingBatchTask = new AutomatiskSaksbehandlingBatchTask(taskRepository, automatiskSaksbehandlingRepository, clock, Period.ofWeeks(-1));

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        scenarioSimple.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        behandling = scenarioSimple.medBehandlingType(BehandlingType.TILBAKEKREVING).lagre(repositoryProvider);
        lagKravgrunnlag(behandling.getId(), BigDecimal.valueOf(500l), behandling.getFagsak().getSaksnummer().getVerdi(), 123l);
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegStatus.UTGANG);
    }

    @Test
    public void skal_opprette_prosess_task_for_å_saksbehandle_behandling_automatisk() {
        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker.size()).isEqualTo(1);
        ProsessTaskData prosessTaskData = prosessTasker.get(0);
        assertThat(Long.valueOf(prosessTaskData.getBehandlingId())).isEqualTo(behandling.getId());
        assertThat(prosessTaskData.getSekvens()).isEqualTo("10");
    }

    @Test
    public void skal_ikke_kjøre_batch_i_helgen() {
        Clock helgeClock = Clock.fixed(Instant.parse("2020-05-03T12:00:00.00Z"), ZoneId.systemDefault());
        AutomatiskSaksbehandlingBatchTask automatiskSaksbehandlingBatchTask = new AutomatiskSaksbehandlingBatchTask(taskRepository, automatiskSaksbehandlingRepository, helgeClock, Period.ofWeeks(-1));

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_prosess_tasker_for_behandlinger_med_større_feilutbetalt_beløp() {
        lagKravgrunnlag(behandling.getId(), BigDecimal.valueOf(1500l), behandling.getFagsak().getSaksnummer().getVerdi(), 123l);

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_prosess_tasker_for_avsluttet_behandling() {
        behandling.avsluttBehandling();

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_prosess_tasker_når_behandling_er_allerede_saksbehandlet() {
        behandling.setAnsvarligSaksbehandler("124");

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_prosess_tasker_når_behandling_er_sett_på_vent() {
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling,AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,BehandlingStegType.FAKTA_FEILUTBETALING);

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_prosess_tasker_når_behandling_er_allerede_varslet() {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medBehandlingId(behandling.getId())
            .medDokumentId("sdfkjsdlfsd")
            .medJournalpostId(new JournalpostId("dkasfjsklfsd"))
            .medBrevType(BrevType.VARSEL_BREV)
            .build();
        repositoryProvider.getBrevSporingRepository().lagre(brevSporing);;

        automatiskSaksbehandlingBatchTask.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isEmpty();
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
            .medVedtakId(123l).medSaksBehId("K231B433")
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
        return new ProsessTaskData(AutomatiskSaksbehandlingBatchTask.BATCHNAVN);
    }
}
