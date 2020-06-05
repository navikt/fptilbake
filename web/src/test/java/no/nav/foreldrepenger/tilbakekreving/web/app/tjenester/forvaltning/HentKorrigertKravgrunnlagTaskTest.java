package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
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

import javax.persistence.FlushModeType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TpsAdapterWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
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
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HentKorrigertKravgrunnlagTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private KravgrunnlagRepository kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
    private EksternBehandlingRepository eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
    private TpsAdapter tpsAdapterMock = mock(TpsAdapter.class);
    private TpsAdapterWrapper tpsAdapterWrapper = new TpsAdapterWrapper(tpsAdapterMock);
    private ØkonomiConsumer økonomiConsumerMock = mock(ØkonomiConsumer.class);
    private FpsakKlient fpsakKlientMock = mock(FpsakKlient.class);
    private HentKravgrunnlagMapper hentKravgrunnlagMapper = new HentKravgrunnlagMapper(tpsAdapterWrapper);
    private HentKorrigertKravgrunnlagTask hentKorrigertGrunnlagTask = new HentKorrigertKravgrunnlagTask(repositoryProvider, hentKravgrunnlagMapper, økonomiConsumerMock, fpsakKlientMock);

    private Behandling behandling;
    private long behandlingId;

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        behandlingId = behandling.getId();
        when(tpsAdapterMock.hentAktørIdForPersonIdent(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(økonomiConsumerMock.hentKravgrunnlag(anyLong(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(true));
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
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
        assertThat(eksternBehandling.getHenvisning().getVerdi()).isEqualTo(kravgrunnlag431.getReferanse());
        verify(fpsakKlientMock, never()).hentBehandlingForSaksnummer(anyString());
    }

    @Test
    public void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_hentet_grunnlaget_er_ugyldig() {
        when(økonomiConsumerMock.hentKravgrunnlag(anyLong(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(false));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThrows("FPT-879715", IntegrasjonException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
    }

    @Test
    public void skal_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_men_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fpsakKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList(
            lagEksternBehandlingsInfo(1l),
            lagEksternBehandlingsInfo(2l)));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isFalse();
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(2l);
        hentKorrigertGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravgrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        assertThat(eksternBehandling.getHenvisning().getVerdi()).isEqualTo(kravgrunnlag431.getReferanse());
        assertThat(eksternBehandling.getHenvisning().toLong()).isEqualTo(1l);
        verify(fpsakKlientMock, atLeastOnce()).hentBehandlingForSaksnummer(anyString());
    }

    @Test
    public void skal_ikke_hente_og_lagre_korrigert_kravgrunnlag_når_ekstern_behandlingid_ikke_er_samme_i_hentet_grunnlaget_og_ikke_finnes_i_fpsak() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(2l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        when(fpsakKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList(lagEksternBehandlingsInfo(2l)));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        assertThrows("FPT-587197", TekniskException.class, () -> hentKorrigertGrunnlagTask.doTask(prosessTaskData));
    }


    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(HentKorrigertKravgrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());
        prosessTaskData.setProperty(HentKorrigertKravgrunnlagTask.KRAVGRUNNLAG_ID, "152806");
        return prosessTaskData;
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingsInfo(Long eksternBehandlingId) {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setId(eksternBehandlingId);
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
