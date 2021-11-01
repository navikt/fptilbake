package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerFeil;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.typer.v1.PeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.xmlutils.DateUtil;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class HåndterGamleKravgrunnlagTaskTest {

    private final PersoninfoAdapter tpsTjenesteMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(tpsTjenesteMock);
    private final ØkonomiConsumer økonomiConsumerMock = mock(ØkonomiConsumer.class);
    private final BehandlingskontrollEventPubliserer behandlingskontrollEventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
    private final FagsystemKlient fagsystemKlientMock = mock(FagsystemKlient.class);

    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;

    private HentKravgrunnlagMapper hentKravgrunnlagMapper;

    private BehandlingTjeneste behandlingTjeneste;
    private HåndterGamleKravgrunnlagTask håndterGamleKravgrunnlagTask;

    private Behandling behandling;
    Long mottattXmlId = null;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        mottattXmlRepository = new ØkonomiMottattXmlRepository(entityManager);
        grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        ProsessTaskTjeneste taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        NavBrukerRepository navBrukerRepository = new NavBrukerRepository(entityManager);
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(
            entityManager, new BehandlingModellRepository(), behandlingskontrollEventPublisererMock));
        hentKravgrunnlagMapper = new HentKravgrunnlagMapper(tpsAdapterWrapper);
        KravgrunnlagMapper lesKravgrunnlagMapper = new KravgrunnlagMapper(tpsAdapterWrapper);
        BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(
            behandlingskontrollTjeneste, mock(BehandlingskontrollAsynkTjeneste.class));
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(
            repositoryProvider.getHistorikkRepository(), null);
        FagsakTjeneste fagsakTjeneste = new FagsakTjeneste(tpsTjenesteMock, fagsakRepository, navBrukerRepository);
        behandlingTjeneste = new BehandlingTjeneste(repositoryProvider,
                behandlingskontrollProvider, fagsakTjeneste, historikkinnslagTjeneste, fagsystemKlientMock, Period.ofWeeks(4));
        HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste = new HåndterGamleKravgrunnlagTjeneste(
            mottattXmlRepository, grunnlagRepository, hentKravgrunnlagMapper, lesKravgrunnlagMapper, behandlingTjeneste,
            økonomiConsumerMock, fagsystemKlientMock);
        håndterGamleKravgrunnlagTask = new HåndterGamleKravgrunnlagTask(håndterGamleKravgrunnlagTjeneste);

        behandling = ScenarioSimple.simple().lagMocked();
        when(tpsTjenesteMock.hentBrukerForAktør(any(AktørId.class))).thenReturn(lagPersonInfo(behandling.getFagsak().getAktørId()));
        when(tpsTjenesteMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagDetaljertKravgrunnlagDto(true));
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = lagEksternBehandlingData();
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(lagSamletEksternBehandlingData(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandlingOptional(any(UUID.class))).thenReturn(Optional.of(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandling(any(UUID.class))).thenReturn(eksternBehandlingsinfoDto);
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML());
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_uten_behandling() {
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"));
        assertThat(behandlinger).isNotEmpty();
        assertThat(behandlinger.size()).isEqualTo(1);
        ØkonomiXmlMottatt økonomiXmlMottatt = mottattXmlRepository.finnMottattXml(mottattXmlId);
        assertThat(økonomiXmlMottatt).isNotNull();
        assertThat(økonomiXmlMottatt.getSaksnummer()).isEqualTo("139015144");
        assertThat(økonomiXmlMottatt.getHenvisning()).isNotNull();
        assertThat(økonomiXmlMottatt.isTilkoblet()).isTrue();
        long behandlingId = behandlinger.get(0).getId();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_grunnlaget_ikke_finnes_i_økonomi() {
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class)))
            .thenThrow(ØkonomiConsumerFeil.fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagIkkeFinnes(behandling.getId(), 100000001L, "kravgrunnlag ikke finnes"));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_økonomi_svarer_ukjent_feil() {
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class)))
            .thenThrow(ØkonomiConsumerFeil.fikkUkjentFeilkodeVedHentingAvKravgrunnlag(behandling.getId(), 100000001L, "ukjent feil"));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_grunnlaget_er_sperret() {
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class)))
            .thenThrow(ØkonomiConsumerFeil.fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagErSperret(behandling.getId(), 100000001L, "sperret"));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        ØkonomiXmlMottatt økonomiXmlMottatt = mottattXmlRepository.finnMottattXml(mottattXmlId);
        assertThat(økonomiXmlMottatt).isNotNull();
        assertThat(økonomiXmlMottatt.isTilkoblet()).isTrue();
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"));
        assertThat(behandlinger).isNotEmpty();
        Behandling nyBehandling = behandlinger.get(0);
        long behandlingId = nyBehandling.getId();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandlingId)).isTrue();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_eksternBehandling_ikke_finnes_i_fpsak() {
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList());
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_kravgrunnlaget_ikke_er_gyldig() {
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagDetaljertKravgrunnlagDto(false));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_behandling_allerede_finnes_med_samme_saksnummer_uten_kravgrunnlag() {
        NavBruker navBruker = TestFagsakUtil.genererBruker();
        Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagDetaljertKravgrunnlagDto(true));

        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isNotEmpty();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_behandling_allerede_finnes_med_samme_saksnummer_med_kravgrunnlag() {
        NavBruker navBruker = TestFagsakUtil.genererBruker();
        Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        DetaljertKravgrunnlagDto detaljertKravgrunnlagDto = lagDetaljertKravgrunnlagDto(true);
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(detaljertKravgrunnlagDto);
        Kravgrunnlag431 kravgrunnlag431 = hentKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlagDto);
        grunnlagRepository.lagre(behandling.getId(), kravgrunnlag431);

        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isTrue();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isNotEmpty();
    }

    @Test
    public void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_behandling_allerede_finnes_med_samme_saksnummer_og_kravgrunnlaget_er_sperret() {
        NavBruker navBruker = TestFagsakUtil.genererBruker();
        Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class)))
            .thenThrow(ØkonomiConsumerFeil.fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagErSperret(behandling.getId(), 100000001L, "sperret"));

        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isTrue();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
    }

    private DetaljertKravgrunnlagDto lagDetaljertKravgrunnlagDto(boolean erGyldig) {
        DetaljertKravgrunnlagDto kravgrunnlagDto = new DetaljertKravgrunnlagDto();
        kravgrunnlagDto.setKravgrunnlagId(BigInteger.valueOf(123456789));
        kravgrunnlagDto.setVedtakId(BigInteger.valueOf(100));
        kravgrunnlagDto.setKodeStatusKrav(KravStatusKode.NYTT.getKode());
        kravgrunnlagDto.setKodeFagomraade(FagOmrådeKode.FORELDREPENGER.getKode());
        kravgrunnlagDto.setFagsystemId("139015144100");
        kravgrunnlagDto.setDatoVedtakFagsystem(konvertDato(LocalDate.of(2019, 10, 26)));
        kravgrunnlagDto.setVedtakGjelderId("12345678901");
        kravgrunnlagDto.setUtbetalesTilId("12345678901");
        kravgrunnlagDto.setTypeGjelderId(TypeGjelderDto.PERSON);
        kravgrunnlagDto.setTypeUtbetId(TypeGjelderDto.PERSON);
        kravgrunnlagDto.setEnhetAnsvarlig("8020");
        kravgrunnlagDto.setEnhetBehandl("8020");
        kravgrunnlagDto.setEnhetBosted("8020");
        kravgrunnlagDto.setKontrollfelt("kontrolll-123");
        kravgrunnlagDto.setSaksbehId("Z111111");
        kravgrunnlagDto.setReferanse("100000001");
        kravgrunnlagDto.getTilbakekrevingsPeriode().addAll(lagPerioder(erGyldig));
        return kravgrunnlagDto;
    }

    private List<DetaljertKravgrunnlagPeriodeDto> lagPerioder(boolean erGyldig) {
        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode1 = new DetaljertKravgrunnlagPeriodeDto();
        PeriodeDto periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2018, 1, 1)));
        periode.setTom(konvertDato(LocalDate.of(2018, 1, 22)));
        kravgrunnlagPeriode1.setPeriode(periode);
        if (erGyldig) {
            kravgrunnlagPeriode1.setBelopSkattMnd(BigDecimal.valueOf(4500.00));
        }
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(lagKravgrunnlagBeløp(BigDecimal.valueOf(9000), BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(lagKravgrunnlagBeløp(BigDecimal.ZERO, BigDecimal.valueOf(9000), BigDecimal.valueOf(9000), TypeKlasseDto.YTEL));

        return Lists.newArrayList(kravgrunnlagPeriode1);
    }

    private DetaljertKravgrunnlagBelopDto lagKravgrunnlagBeløp(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp,
                                                               BigDecimal opprUtbetBeløp, TypeKlasseDto typeKlasse) {
        DetaljertKravgrunnlagBelopDto detaljertKravgrunnlagBelop = new DetaljertKravgrunnlagBelopDto();
        detaljertKravgrunnlagBelop.setTypeKlasse(typeKlasse);
        detaljertKravgrunnlagBelop.setBelopNy(nyBeløp);
        detaljertKravgrunnlagBelop.setBelopOpprUtbet(opprUtbetBeløp);
        detaljertKravgrunnlagBelop.setBelopTilbakekreves(tilbakekrevesBeløp);
        detaljertKravgrunnlagBelop.setBelopUinnkrevd(BigDecimal.ZERO);
        detaljertKravgrunnlagBelop.setKodeKlasse("FPATAL");
        detaljertKravgrunnlagBelop.setSkattProsent(BigDecimal.valueOf(50.0000));

        return detaljertKravgrunnlagBelop;
    }

    private XMLGregorianCalendar konvertDato(LocalDate localDate) {
        return DateUtil.convertToXMLGregorianCalendar(localDate);
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingData() {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(100000001L));
        return eksternBehandlingsinfoDto;
    }

    private SamletEksternBehandlingInfo lagSamletEksternBehandlingData(EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        FagsakDto fagsakDto = new FagsakDto();
        fagsakDto.setSaksnummer("139015144");
        fagsakDto.setSakstype(FagsakYtelseType.FORELDREPENGER);
        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setAktoerId(behandling.getAktørId().getId());
        return SamletEksternBehandlingInfo.builder(Tillegsinformasjon.FAGSAK, Tillegsinformasjon.PERSONOPPLYSNINGER)
            .setGrunninformasjon(eksternBehandlingsinfoDto)
            .setFagsak(fagsakDto)
            .setPersonopplysninger(personopplysningDto).build();
    }

    private String getInputXML() {
        try {
            Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("xml/kravgrunnlag_periode_YTEL.xml")).toURI());
            return Files.readString(path);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }

    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HåndterGamleKravgrunnlagTask.class);
        prosessTaskData.setProperty("mottattXmlId", String.valueOf(mottattXmlId));
        prosessTaskData.setCallIdFraEksisterende();
        return prosessTaskData;
    }

    private Optional<Personinfo> lagPersonInfo(AktørId aktørId) {
        Personinfo personinfo = Personinfo.builder()
            .medAktørId(aktørId)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medPersonIdent(new PersonIdent(aktørId.getId()))
            .medNavn("testnavn")
            .build();
        return Optional.of(personinfo);
    }
}
