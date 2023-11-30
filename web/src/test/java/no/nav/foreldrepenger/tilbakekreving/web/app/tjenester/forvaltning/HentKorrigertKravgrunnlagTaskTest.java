package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.FagOmrådeKode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.GjelderType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KlasseType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravStatusKode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagBelop433Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagPeriode432Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Periode;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.HentKravgrunnlagMapperProxy;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
class HentKorrigertKravgrunnlagTaskTest {

    private final PersoninfoAdapter tpsAdapterMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(tpsAdapterMock);
    private final ØkonomiProxyKlient økonomiProxyKlient = mock(ØkonomiProxyKlient.class);
    private final FagsystemKlient fagsystemKlient = mock(FagsystemKlient.class);
    private KravgrunnlagRepository kravgrunnlagRepository;
    private HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy ;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private HentKorrigertKravgrunnlagTask hentKorrigertGrunnlagTask;

    private Behandling behandling;
    private long behandlingId;

    @BeforeEach
    void setup(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        hentKravgrunnlagMapperProxy = new HentKravgrunnlagMapperProxy(tpsAdapterWrapper);
        var kravgrunnlagHenter = new KravgrunnlagHenter(økonomiProxyKlient, hentKravgrunnlagMapperProxy);
        hentKorrigertGrunnlagTask = new HentKorrigertKravgrunnlagTask(repositoryProvider, fagsystemKlient, kravgrunnlagHenter);
        entityManager.setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        behandlingId = behandling.getId();
        when(tpsAdapterMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlagNY(true));
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @Test
    void skal_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_er_samme_i_hentet_grunnlaget() {
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isFalse();
        hentKorrigertGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(kravgrunnlag431.getReferanse());
        verify(fagsystemKlient, never()).hentBehandlingForSaksnummer(anyString());
    }

    @Test
    void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_hentet_grunnlaget_er_ugyldig() {
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlagNY(false));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-930235");
    }

    @Test
    void skal_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_men_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fagsystemKlient.hentBehandlingForSaksnummer(anyString())).thenReturn(List.of(
                lagEksternBehandlingsInfo(1L),
                lagEksternBehandlingsInfo(2L)));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isFalse();
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(2L);
        hentKorrigertGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(kravgrunnlag431.getReferanse());
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(1L);
        verify(fagsystemKlient, atLeastOnce()).hentBehandlingForSaksnummer(anyString());
    }

    @Test
    void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_og_ikke_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fagsystemKlient.hentBehandlingForSaksnummer(anyString())).thenReturn(List.of(lagEksternBehandlingsInfo(2L)));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(TekniskException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-587197");
    }


    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HentKorrigertKravgrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());
        prosessTaskData.setProperty(HentKorrigertKravgrunnlagTask.KRAVGRUNNLAG_ID, "152806");
        return prosessTaskData;
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingsInfo(Long eksternBehandlingId) {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(eksternBehandlingId));
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        return eksternBehandlingsinfoDto;
    }


    static Kravgrunnlag431Dto lagKravgrunnlagNY(boolean erGyldig) {
        return new Kravgrunnlag431Dto.Builder()
            .vedtakId(207406L)
            .eksternKravgrunnlagId("123456789")
            .vedtakFagSystemDato(LocalDate.now().minusYears(2))
            .ansvarligEnhet(HentKorrigertKravgrunnlagTask.ANSVARLIG_ENHET_NØS)
            .fagSystemId("10000000000000000")
            .fagOmrådeKode(FagOmrådeKode.FP)
            .hjemmelKode("1234239042304")
            .kontrollFelt("kontrolll-123")
            .referanse("1")
            .beregnesRenter("N")
            .saksBehId("Z111111")
            .utbetalesTilId("12345678901")
            .utbetGjelderType(GjelderType.PERSON)
            .gjelderType(GjelderType.PERSON)
            .behandlendeEnhet(HentKorrigertKravgrunnlagTask.ANSVARLIG_ENHET_NØS)
            .bostedEnhet(HentKorrigertKravgrunnlagTask.ANSVARLIG_ENHET_NØS)
            .kravStatusKode(KravStatusKode.NY)
            .gjelderVedtakId("12345678901")
            .omgjortVedtakId(207407L)
            .perioder(lagPeriode(erGyldig))
            .build();
    }

    private static List<KravgrunnlagPeriode432Dto> lagPeriode(boolean erGyldig) {
        var KravgrunnlagPeriode432DtoListe = new ArrayList<KravgrunnlagPeriode432Dto>();


        var kravgrunnlagBuilder = new KravgrunnlagPeriode432Dto.Builder()
            .periode(new Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 22)))
            .kravgrunnlagBeloper433(
                List.of(
                    hentBeløp(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                    hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), KlasseType.YTEL)
                )
        );
        if (erGyldig) {
            kravgrunnlagBuilder.beløpSkattMnd(BigDecimal.valueOf(600.00));
        }
        KravgrunnlagPeriode432DtoListe.add(kravgrunnlagBuilder.build());
        return KravgrunnlagPeriode432DtoListe;
    }

    private static KravgrunnlagBelop433Dto hentBeløp(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp,
                                                     BigDecimal opprUtbetBeløp, KlasseType klasseType) {
        return new KravgrunnlagBelop433Dto.Builder()
            .klasseType(klasseType)
            .nyBelop(nyBeløp)
            .opprUtbetBelop(opprUtbetBeløp)
            .tilbakekrevesBelop(tilbakekrevesBeløp)
            .uinnkrevdBelop(BigDecimal.ZERO)
            .klasseKode("FPATAL")
            .skattProsent(BigDecimal.valueOf(10.0000))
            .build();
    }
}
