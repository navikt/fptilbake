package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
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
import no.nav.vedtak.felles.prosesstask.api.TaskType;

class LesKravvedtakStatusTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository;
    private BehandlingresultatRepository behandlingresultatRepository;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private LesKravvedtakStatusTask lesKravvedtakStatusTask;

    private Long mottattXmlId;
    private static final long REFERANSE = 1174551L;
    private Behandling behandling;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(REFERANSE);

    private static final TaskType LES_KRAV_GRUNNLAG_TASK = TaskType.forProsessTask(LesKravgrunnlagTask.class);
    private static final TaskType LES_KRAV_STATUS_TASK = TaskType.forProsessTask(LesKravvedtakStatusTask.class);

    @BeforeEach
    void setup() {
        kravVedtakStatusRepository = new KravVedtakStatusRepository(entityManager);
        behandlingresultatRepository = new BehandlingresultatRepository(entityManager);
        henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, taskTjeneste, behandlingskontrollTjeneste, historikkinnslagTjeneste);
        var kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository,
                taskTjeneste, repositoryProvider.getBehandlingRepository(), repositoryProvider.getGrunnlagRepository(), henleggBehandlingTjeneste,
                behandlingskontrollTjeneste);
        var kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);
        lesKravvedtakStatusTask = new LesKravvedtakStatusTask(mottattXmlRepository, repositoryProvider.getBehandlingRepository(),
                kravVedtakStatusTjeneste, kravVedtakStatusMapper, fagsystemKlientMock);

        behandling = lagBehandling();
        lagEksternBehandling(behandling);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);
        when(fagsystemKlientMock.hentBehandlingForSaksnummer("139015144")).thenReturn(lagResponsFraFagsystemKlient());

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_GRUNNLAG_TASK));
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_mottatt_sper_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        assertTrue(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_mottatt_manu_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_MANU.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        assertTrue(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.MANUELL));
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_mottatt_avsl_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        assertTrue(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId));
        assertThat(behandling.erAvsluttet()).isTrue();
        var resultat = behandlingresultatRepository.hent(behandling);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.AVSLUTTET));

        var historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandling.getId());
        assertThat(historikkinnslager).hasSize(1);
        var historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_mottatt_ugyldig_status_melding() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigstatus.xml")); // den xml-en har ugyldig status kode

        assertThatThrownBy(() -> lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK)))
                .hasMessageContaining("FPT-107928");
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_behandling_som_finnes_ikke_iFpsak() {
        // den xml-en har behandlingId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fagsystemKlientMock.finnesBehandlingIFagsystem(anyString(), any(Henvisning.class))).thenReturn(false);

        assertThatThrownBy(() -> lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK)))
                .hasMessageContaining("FPT-587196");
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_når_fptilbake_har_ingen_åpenBehandling() {
        // den xml-en har behandlingId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fagsystemKlientMock.finnesBehandlingIFagsystem(anyString(), any(Henvisning.class))).thenReturn(true);

        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEmpty();
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_behandling_som_er_ugyldig() {
        // den xml-en har behandlingId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigreferanse.xml"));

        assertThatThrownBy(() -> lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK)))
                .hasMessageContaining("Mottok en statusmelding fra Økonomi med henvisning i ikke-støttet format, henvisning=ABC. Statusmeldingen "
                        + "skulle kanskje til et annet system. Si i fra til Økonomi!");
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_GRUNNLAG_TASK));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        var eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));

        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
        var mottattMeldinger = finnAlleForHenvisning(HENVISNING);
        assertThat(mottattMeldinger).isNotEmpty();
        assertThat(mottattMeldinger.stream().filter(økonomiXmlMottatt -> !økonomiXmlMottatt.isTilkoblet()).findAny()).isEmpty();
    }

    @Test
    void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag_med_samme_referanse() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        var eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        var xmlMottatt = finnAlleForHenvisning(HENVISNING);
        assertThat(xmlMottatt).hasSize(2);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
    }

    @Test
    void skal_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_med_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        var eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        var xmlMottatt = finnAlleForHenvisning(HENVISNING);
        assertThat(xmlMottatt).hasSize(3);

        assertThat(kravVedtakStatusRepository.finnKravStatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.ENDRET));
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste).lagre(captor.capture());
        var prosessTasker = captor.getAllValues();
        assertThat(prosessTasker).isNotEmpty().hasSize(1);
        assertThat(prosessTasker.get(0).taskType()).isEqualTo(TaskType.forProsessTask(FortsettBehandlingTask.class));
    }

    @Test
    void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_når_grunnlag_ikke_sperret() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        var prosessTaskData = lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK);
        assertThatThrownBy(() -> lesKravvedtakStatusTask.doTask(prosessTaskData))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FPT-107929");
    }

    @Test
    void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_når_grunnlag_er_ugyldig() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_ugyldig_skatt.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_GRUNNLAG_TASK));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));

        assertThatThrownBy(() -> lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK)))
                .isInstanceOf(KravgrunnlagValidator.UgyldigKravgrunnlagException.class)
                .hasMessageContaining("FPT-930235");
    }

    @Test
    void skal_håndtere_sper_melding_når_siste_ekstern_behandling_har_henlagt_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_GRUNNLAG_TASK));
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), BehandlingResultatType.HENLAGT_FEILOPPRETTET);

        when(fagsystemKlientMock.finnesBehandlingIFagsystem(anyString(), any(Henvisning.class))).thenReturn(true);
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER_annen_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LES_KRAV_STATUS_TASK));

        var xmlMottatt = mottattXmlRepository.finnForHenvisning(Henvisning.fraEksternBehandlingId(100000001L));
        assertThat(xmlMottatt).isPresent();
        assertThat(xmlMottatt.get().getSaksnummer()).isEqualTo("139015144");
        assertThat(xmlMottatt.get().isTilkoblet()).isFalse();
    }


    private List<ØkonomiXmlMottatt> finnAlleForHenvisning(Henvisning henvisning) {
        var query = entityManager.createQuery("""
            FROM ØkonomiXmlMottatt
            WHERE henvisning=:henvisning
            """, ØkonomiXmlMottatt.class);
        query.setParameter("henvisning", henvisning);
        return query.getResultList();
    }

    private Behandling lagBehandling() {
        var navBruker = NavBruker.opprettNy(TestFagsakUtil.genererBruker().getAktørId(), Språkkode.nb);
        var fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        var behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        var behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }

    private void lagEksternBehandling(Behandling behandling) {
        var eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(REFERANSE), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    private List<EksternBehandlingsinfoDto> lagResponsFraFagsystemKlient() {
        var eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(REFERANSE));
        return List.of(eksternBehandlingsinfoDto);
    }
}
