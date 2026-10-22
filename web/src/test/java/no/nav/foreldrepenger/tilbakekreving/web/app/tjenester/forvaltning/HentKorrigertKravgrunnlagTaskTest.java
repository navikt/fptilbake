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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.HentKravgrunnlagMapperProxy;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.sokos.HentKravgrunnlagMapperSokos;
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
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerResponse;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosTilbakekrevingKlient;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
class HentKorrigertKravgrunnlagTaskTest {

    private final PersoninfoAdapter tpsAdapterMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(tpsAdapterMock);
    private final SokosTilbakekrevingKlient sokosTilbakekrevingKlient = mock(SokosTilbakekrevingKlient.class);
    private final FagsystemKlient fagsystemKlient = mock(FagsystemKlient.class);
    private KravgrunnlagRepository kravgrunnlagRepository;
    private HentKravgrunnlagMapperSokos hentKravgrunnlagMapperSokos;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private HentKorrigertKravgrunnlagTask hentKorrigertGrunnlagTask;

    private Behandling behandling;
    private long behandlingId;

    @BeforeEach
    void setup(EntityManager entityManager) {
        System.setProperty("app.name", "fptilbake");
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        hentKravgrunnlagMapperSokos = new HentKravgrunnlagMapperSokos(tpsAdapterWrapper);
        var kravgrunnlagHenter = new KravgrunnlagHenter(mock(ØkonomiProxyKlient.class), mock(HentKravgrunnlagMapperProxy.class),
            sokosTilbakekrevingKlient, hentKravgrunnlagMapperSokos, () -> true);
        hentKorrigertGrunnlagTask = new HentKorrigertKravgrunnlagTask(repositoryProvider, fagsystemKlient, kravgrunnlagHenter);
        entityManager.setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        behandlingId = behandling.getId();
        when(tpsAdapterMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(sokosTilbakekrevingKlient.hentKravgrunnlag(any())).thenReturn(lagKravgrunnlagNY(true));
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("app.name");
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
        verify(fagsystemKlient, never()).hentBehandlingForSaksnummerHenvisning(anyString(), any());
    }

    @Test
    void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_hentet_grunnlaget_er_ugyldig() {
        when(sokosTilbakekrevingKlient.hentKravgrunnlag(any())).thenReturn(lagKravgrunnlagNY(false));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-930235");
    }

    @Test
    void skal_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_men_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fagsystemKlient.hentBehandlingForSaksnummerHenvisning(anyString(), any())).thenReturn(Optional.of(lagEksternBehandlingsInfo(1L)));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isFalse();
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(2L);
        hentKorrigertGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(kravgrunnlag431.getReferanse());
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(1L);
        verify(fagsystemKlient, atLeastOnce()).hentBehandlingForSaksnummerHenvisning(anyString(), any());
    }

    @Test
    void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_og_ikke_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fagsystemKlient.hentBehandlingForSaksnummerHenvisning(anyString(), any())).thenReturn(Optional.empty());
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(TekniskException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-587197");
    }


    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HentKorrigertKravgrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandlingId);
        prosessTaskData.setProperty(HentKorrigertKravgrunnlagTask.KRAVGRUNNLAG_ID, "152806");
        return prosessTaskData;
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingsInfo(Long eksternBehandlingId) {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(eksternBehandlingId));
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        return eksternBehandlingsinfoDto;
    }


    public static HentKravgrunnlagDetaljerResponse lagKravgrunnlagNY(boolean erGyldig) {
        return new HentKravgrunnlagDetaljerResponse(0, "", lagKravgrunnlagDetaljer(erGyldig));
    }

    private static HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer lagKravgrunnlagDetaljer(boolean erGyldig) {
        return new HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer(123456789L,
            207406L,
            no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode.NYTT,
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode.FORELDREPENGER,
            "139015144100",
            LocalDate.now().minusYears(2),
            207407L,
            "12345678901",
            no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.PERSON,
            "12345678901",
            no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.PERSON,
            "1234239042304",
            false,
            KravgrunnlagHenter.ANSVARLIG_ENHET_NØS,
            KravgrunnlagHenter.ANSVARLIG_ENHET_NØS,
            KravgrunnlagHenter.ANSVARLIG_ENHET_NØS,
            "kontrolll-123",
            "Z111111",
            "1",
            null,
            lagPerioder(erGyldig));
    }


    private static List<HentKravgrunnlagDetaljerResponse.DetaljerPeriode> lagPerioder(boolean erGyldig) {
        var kravgrunnlagPeriode1 = new HentKravgrunnlagDetaljerResponse.DetaljerPeriode(
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 22),
            erGyldig ? BigDecimal.valueOf(600) : BigDecimal.ZERO, List.of(
                lagPostering(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType.FEIL),
                lagPostering(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType.YTEL)
            )
        );
        return List.of(kravgrunnlagPeriode1);
    }

    private static HentKravgrunnlagDetaljerResponse.DetaljerPostering lagPostering(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp, BigDecimal opprUtbetBeløp, no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType klasseType) {
        return new HentKravgrunnlagDetaljerResponse.DetaljerPostering(
            "FPATAL",
            klasseType,
            opprUtbetBeløp,
            nyBeløp,
            tilbakekrevesBeløp,
            BigDecimal.ZERO,
            BigDecimal.valueOf(10.0000),
            null,
            null,
            null
        );
    }


}
