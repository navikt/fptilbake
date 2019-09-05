package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.ØkonomiXmlMottatt;

public class LesKravvedtakStatusTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository = new KravVedtakStatusRepository(repoRule.getEntityManager());
    private BehandlingresultatRepository behandlingresultatRepository = new BehandlingresultatRepositoryImpl(repoRule.getEntityManager());

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, prosessTaskRepository, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository, behandlingRepository, henleggBehandlingTjeneste, behandlingskontrollTjeneste);
    private KravVedtakStatusMapper kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);
    private LesKravvedtakStatusTask lesKravvedtakStatusTask = new LesKravvedtakStatusTask(mottattXmlRepository, repositoryProvider, prosessTaskRepository,
        kravVedtakStatusTjeneste, kravVedtakStatusMapper, fpsakKlientMock);

    private Long mottattXmlId;
    private static final Long FPSAK_BEHANDLING_ID = 1174551l;

    @Before
    public void setup() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_sper_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(mottattXmlRepository.finnForEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID))).isPresent();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Optional<KravVedtakStatusAggregate> aggregate = kravVedtakStatusRepository.finnForBehandlingId(behandling.getId());
        assertThat(aggregate).isPresent();
        KravVedtakStatus437 kravVedtakStatus437 = aggregate.get().getKravVedtakStatus();
        assertThat(kravVedtakStatus437.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.SPERRET);
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_manu_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_MANU.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(mottattXmlRepository.finnForEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID))).isPresent();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Optional<KravVedtakStatusAggregate> aggregate = kravVedtakStatusRepository.finnForBehandlingId(behandling.getId());
        assertThat(aggregate).isPresent();
        KravVedtakStatus437 kravVedtakStatus437 = aggregate.get().getKravVedtakStatus();
        assertThat(kravVedtakStatus437.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.MANUELL);
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_avsl_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(mottattXmlRepository.finnForEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID))).isPresent();
        assertThat(behandling.erAvsluttet()).isTrue();
        Optional<Behandlingsresultat> resultat = behandlingresultatRepository.hent(behandling);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        Optional<KravVedtakStatusAggregate> aggregate = kravVedtakStatusRepository.finnForBehandlingId(behandling.getId());
        assertThat(aggregate).isPresent();
        KravVedtakStatus437 kravVedtakStatus437 = aggregate.get().getKravVedtakStatus();
        assertThat(kravVedtakStatus437.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.AVSLUTTET);

        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandling.getId());
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_ugyldig_status_melding() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigstatus.xml")); // den xml-en har ugyldig status kode

        expectedException.expectMessage("FPT-107928");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_finnes_ikke_iFpsak() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fpsakKlientMock.finnesBehandlingIFpsak(anyString(), anyLong())).thenReturn(false);

        expectedException.expectMessage("FPT-587196");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_når_fptilbake_har_ingen_åpenBehandling() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fpsakKlientMock.finnesBehandlingIFpsak(anyString(), anyLong())).thenReturn(true);

        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(kravVedtakStatusRepository.finnForBehandlingId(behandling.getId())).isEmpty();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_er_ugyldig() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigreferanse.xml"));

        expectedException.expectMessage("FPT-675364");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag() {
        repoRule.getEntityManager().createQuery("delete from EksternBehandling").executeUpdate();
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, 100000001L, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternId()).isEqualTo(FPSAK_BEHANDLING_ID);

        assertThat(mottattXmlRepository.finnForEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID))).isPresent();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Optional<KravVedtakStatusAggregate> aggregate = kravVedtakStatusRepository.finnForBehandlingId(behandling.getId());
        assertThat(aggregate).isPresent();
        KravVedtakStatus437 kravVedtakStatus437 = aggregate.get().getKravVedtakStatus();
        assertThat(kravVedtakStatus437.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.SPERRET);
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag_med_samme_referanse() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternId()).isEqualTo(FPSAK_BEHANDLING_ID);

        List<ØkonomiXmlMottatt> xmlMottatt = mottattXmlRepository.finnAlleForEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID));
        assertThat(xmlMottatt.size()).isEqualTo(2);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Optional<KravVedtakStatusAggregate> aggregate = kravVedtakStatusRepository.finnForBehandlingId(behandling.getId());
        assertThat(aggregate).isPresent();
        KravVedtakStatus437 kravVedtakStatus437 = aggregate.get().getKravVedtakStatus();
        assertThat(kravVedtakStatus437.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.SPERRET);
    }
}
