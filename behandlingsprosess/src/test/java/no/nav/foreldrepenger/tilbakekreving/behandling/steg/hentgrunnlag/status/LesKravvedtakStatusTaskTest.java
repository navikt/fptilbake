package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask.UtvidetProsessTaskRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class LesKravvedtakStatusTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository = new KravVedtakStatusRepository(repoRule.getEntityManager());
    private BehandlingresultatRepository behandlingresultatRepository = new BehandlingresultatRepository(repoRule.getEntityManager());
    private UtvidetProsessTaskRepository utvidetProsessTaskRepository = new UtvidetProsessTaskRepository(repoRule.getEntityManager());

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, prosessTaskRepository, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository, prosessTaskRepository, utvidetProsessTaskRepository,
        repositoryProvider, henleggBehandlingTjeneste, behandlingskontrollTjeneste);
    private KravVedtakStatusMapper kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);
    private LesKravvedtakStatusTask lesKravvedtakStatusTask = new LesKravvedtakStatusTask(mottattXmlRepository, repositoryProvider, kravVedtakStatusTjeneste, kravVedtakStatusMapper, fagsystemKlientMock);
    private InternalManipulerBehandling manipulerInternBehandling = new InternalManipulerBehandling(repositoryProvider);

    private Long mottattXmlId;
    private static final long REFERANSE = 1174551l;
    private Behandling behandling;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(REFERANSE);

    @Before
    public void setup() {
        behandling = lagBehandling();
        lagEksternBehandling(behandling);
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);
        when(fagsystemKlientMock.hentBehandlingForSaksnummer("139015144")).thenReturn(lagResponsFraFagsystemKlient());

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_sper_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertTrue(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_manu_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_MANU.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertTrue(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.MANUELL));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_avsl_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertTrue(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
        assertThat(behandling.erAvsluttet()).isTrue();
        Optional<Behandlingsresultat> resultat = behandlingresultatRepository.hent(behandling);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.AVSLUTTET));

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
        when(fagsystemKlientMock.finnesBehandlingIFagsystem(anyString(), any(Henvisning.class))).thenReturn(false);

        expectedException.expectMessage("FPT-587196");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_når_fptilbake_har_ingen_åpenBehandling() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fagsystemKlientMock.finnesBehandlingIFagsystem(anyString(), any(Henvisning.class))).thenReturn(true);

        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEmpty();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_er_ugyldig() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigreferanse.xml"));

        expectedException.expectMessage("Mottok et kravOgVedtakStatus fra Økonomi med henvisning i ikke-støttet format, henvisning=ABC. KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));

        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
        List<ØkonomiXmlMottatt> mottattMeldinger = finnAlleForHenvisning(HENVISNING);
        assertThat(mottattMeldinger).isNotEmpty();
        assertThat(mottattMeldinger.stream().filter(økonomiXmlMottatt -> !økonomiXmlMottatt.isTilkoblet()).findAny()).isEmpty();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag_med_samme_referanse() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        List<ØkonomiXmlMottatt> xmlMottatt = finnAlleForHenvisning(HENVISNING);
        assertThat(xmlMottatt.size()).isEqualTo(2);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
    }

    @Test
    public void skal_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_med_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        List<ØkonomiXmlMottatt> xmlMottatt = finnAlleForHenvisning(HENVISNING);
        assertThat(xmlMottatt.size()).isEqualTo(3);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.ENDRET));
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();

        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(FortsettBehandlingTaskProperties.TASKTYPE);
    }

    @Test
    public void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_sper_melding_når_koblede_grunnlag_ikke_finnes(){
        grunnlagRepository.getEntityManager().createNativeQuery("update GR_KRAV_GRUNNLAG set aktiv='N' where behandling_id="+behandling.getId()).executeUpdate();
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertFalse(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
    }

    @Test
    public void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_når_grunnlag_ikke_sperret() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-107929");

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_når_grunnlag_er_ugyldig() {
        expectedException.expect(KravgrunnlagValidator.UgyldigKravgrunnlagException.class);
        expectedException.expectMessage("FPT-930235");

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_ugyldig_skatt.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_håndtere_sper_melding_når_siste_ekstern_behandling_har_henlagt_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), BehandlingResultatType.HENLAGT_FEILOPPRETTET);

        when(fagsystemKlientMock.finnesBehandlingIFagsystem(anyString(), any(Henvisning.class))).thenReturn(true);
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER_annen_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        Optional<ØkonomiXmlMottatt> xmlMottatt = mottattXmlRepository.finnForHenvisning(Henvisning.fraEksternBehandlingId(100000001L));
        assertThat(xmlMottatt).isPresent();
        assertThat(xmlMottatt.get().getSaksnummer()).isEqualTo("139015144");
        assertThat(xmlMottatt.get().isTilkoblet()).isFalse();
    }


    private List<ØkonomiXmlMottatt> finnAlleForHenvisning(Henvisning henvisning) {
        TypedQuery<ØkonomiXmlMottatt> query = repoRule.getEntityManager().createQuery("from ØkonomiXmlMottatt where henvisning=:henvisning", ØkonomiXmlMottatt.class);
        query.setParameter("henvisning", henvisning);
        return query.getResultList();
    }

    private Behandling lagBehandling() {
        NavBruker navBruker = NavBruker.opprettNy(TestFagsakUtil.genererBruker().getAktørId(), Språkkode.nb);
        Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }

    private void lagEksternBehandling(Behandling behandling){
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(REFERANSE), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    private List<EksternBehandlingsinfoDto> lagResponsFraFagsystemKlient(){
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(REFERANSE));
        return List.of(eksternBehandlingsinfoDto);
    }

}
