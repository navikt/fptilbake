package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.HentKravgrunnlagMapperProxy;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
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
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.xmlutils.DateUtil;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.typer.v1.JaNeiDto;
import no.nav.tilbakekreving.typer.v1.PeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
public class HentKorrigertKravgrunnlagTaskTest {

    private final PersoninfoAdapter tpsAdapterMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(tpsAdapterMock);
    private final ØkonomiConsumer økonomiConsumerMock = mock(ØkonomiConsumer.class);
    private final HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy = mock(HentKravgrunnlagMapperProxy.class);
    private final ØkonomiProxyKlient økonomiProxyKlient = mock(ØkonomiProxyKlient.class);
    private final FagsystemKlient fagsystemKlient = mock(FagsystemKlient.class);
    private KravgrunnlagRepository kravgrunnlagRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private HentKorrigertKravgrunnlagTask hentKorrigertGrunnlagTask;

    private Behandling behandling;
    private long behandlingId;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        HentKravgrunnlagMapper hentKravgrunnlagMapper = new HentKravgrunnlagMapper(tpsAdapterWrapper);
        var kravgrunnlagHenter = new KravgrunnlagHenter(økonomiProxyKlient, hentKravgrunnlagMapperProxy, økonomiConsumerMock, hentKravgrunnlagMapper);
        hentKorrigertGrunnlagTask = new HentKorrigertKravgrunnlagTask(repositoryProvider, fagsystemKlient, kravgrunnlagHenter);

        entityManager.setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        behandlingId = behandling.getId();
        when(tpsAdapterMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(økonomiConsumerMock.hentKravgrunnlag(anyLong(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(true));
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1L), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @Test
    public void skal_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_er_samme_i_hentet_grunnlaget() {
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
    public void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_hentet_grunnlaget_er_ugyldig() {
        when(økonomiConsumerMock.hentKravgrunnlag(anyLong(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(false));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(IntegrasjonException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-734548");
    }

    @Test
    public void skal_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_men_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fagsystemKlient.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList(
                lagEksternBehandlingsInfo(1l),
                lagEksternBehandlingsInfo(2l)));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isFalse();
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(2l);
        hentKorrigertGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(kravgrunnlag431.getReferanse());
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(1l);
        verify(fagsystemKlient, atLeastOnce()).hentBehandlingForSaksnummer(anyString());
    }

    @Test
    public void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_og_ikke_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fagsystemKlient.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList(lagEksternBehandlingsInfo(2l)));
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

    private no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto lagKravgrunnlag(boolean erGyldig) {
        no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto detaljertKravgrunnlag = new DetaljertKravgrunnlagDto();
        detaljertKravgrunnlag.setVedtakId(BigInteger.valueOf(207406));
        detaljertKravgrunnlag.setKravgrunnlagId(BigInteger.valueOf(152806));
        detaljertKravgrunnlag.setDatoVedtakFagsystem(konvertDato(LocalDate.of(2019, 3, 14)));
        detaljertKravgrunnlag.setEnhetAnsvarlig(HentKorrigertKravgrunnlagTask.ANSVARLIG_ENHET_NØS);
        detaljertKravgrunnlag.setFagsystemId("10000000000000000");
        detaljertKravgrunnlag.setKodeFagomraade("FP");
        detaljertKravgrunnlag.setKodeHjemmel("1234239042304");
        detaljertKravgrunnlag.setKontrollfelt("42354353453454");
        detaljertKravgrunnlag.setReferanse("1");
        detaljertKravgrunnlag.setRenterBeregnes(JaNeiDto.N);
        detaljertKravgrunnlag.setSaksbehId("Z9901136");
        detaljertKravgrunnlag.setUtbetalesTilId("12345678901");
        detaljertKravgrunnlag.setEnhetBehandl(HentKorrigertKravgrunnlagTask.ANSVARLIG_ENHET_NØS);
        detaljertKravgrunnlag.setEnhetBosted(HentKorrigertKravgrunnlagTask.ANSVARLIG_ENHET_NØS);
        detaljertKravgrunnlag.setKodeStatusKrav("BEHA");
        detaljertKravgrunnlag.setTypeGjelderId(TypeGjelderDto.PERSON);
        detaljertKravgrunnlag.setTypeUtbetId(TypeGjelderDto.PERSON);
        detaljertKravgrunnlag.setVedtakGjelderId("12345678901");
        detaljertKravgrunnlag.setVedtakIdOmgjort(BigInteger.valueOf(207407));
        detaljertKravgrunnlag.getTilbakekrevingsPeriode().addAll(lagPerioder(erGyldig));

        return detaljertKravgrunnlag;
    }

    private List<DetaljertKravgrunnlagPeriodeDto> lagPerioder(boolean erGyldig) {
        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode1 = new DetaljertKravgrunnlagPeriodeDto();
        PeriodeDto periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 3, 16)));
        periode.setTom(konvertDato(LocalDate.of(2016, 3, 31)));
        kravgrunnlagPeriode1.setPeriode(periode);
        if (erGyldig) {
            kravgrunnlagPeriode1.setBelopSkattMnd(BigDecimal.valueOf(600.00));
        }
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(lagKravgrunnlagBeløp(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(lagKravgrunnlagBeløp(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        return Lists.newArrayList(kravgrunnlagPeriode1);
    }

    private DetaljertKravgrunnlagBelopDto lagKravgrunnlagBeløp(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp,
                                                               BigDecimal opprUtbetBeløp, BigDecimal uInnkrevdBeløp, TypeKlasseDto typeKlasse) {
        DetaljertKravgrunnlagBelopDto detaljertKravgrunnlagBelop = new DetaljertKravgrunnlagBelopDto();
        detaljertKravgrunnlagBelop.setTypeKlasse(typeKlasse);
        detaljertKravgrunnlagBelop.setBelopNy(nyBeløp);
        detaljertKravgrunnlagBelop.setBelopOpprUtbet(opprUtbetBeløp);
        detaljertKravgrunnlagBelop.setBelopTilbakekreves(tilbakekrevesBeløp);
        detaljertKravgrunnlagBelop.setBelopUinnkrevd(uInnkrevdBeløp);
        detaljertKravgrunnlagBelop.setKodeKlasse("FPATAL");
        detaljertKravgrunnlagBelop.setSkattProsent(BigDecimal.valueOf(10.0000));

        return detaljertKravgrunnlagBelop;
    }

    private XMLGregorianCalendar konvertDato(LocalDate localDate) {
        return DateUtil.convertToXMLGregorianCalendar(localDate);
    }

}
