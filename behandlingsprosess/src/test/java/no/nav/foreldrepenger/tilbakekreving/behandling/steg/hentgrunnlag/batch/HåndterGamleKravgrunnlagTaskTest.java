package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.KravgrunnlagTestUtils.lagKravgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.HentKravgrunnlagMapperProxy;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ManglendeKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.SperringKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.UkjentKvitteringFraOSException;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
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
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
class HåndterGamleKravgrunnlagTaskTest {

    private static final String ENHET = "8020";

    private final PersoninfoAdapter tpsTjenesteMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(tpsTjenesteMock);
    private final BehandlingskontrollEventPubliserer behandlingskontrollEventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
    private final FagsystemKlient fagsystemKlientMock = mock(FagsystemKlient.class);
    private final ØkonomiProxyKlient økonomiProxyKlient = mock(ØkonomiProxyKlient.class);

    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;

    private HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy;

    private BehandlingTjeneste behandlingTjeneste;
    private HåndterGamleKravgrunnlagTask håndterGamleKravgrunnlagTask;

    private Behandling behandling;
    Long mottattXmlId = null;

    @BeforeEach
    void setup(EntityManager entityManager) {
        var repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        mottattXmlRepository = new ØkonomiMottattXmlRepository(entityManager);
        grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        var navBrukerRepository = new NavBrukerRepository(entityManager);
        var behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(
                entityManager, new BehandlingModellRepository(), behandlingskontrollEventPublisererMock));
        hentKravgrunnlagMapperProxy = new HentKravgrunnlagMapperProxy(tpsAdapterWrapper);
        var lesKravgrunnlagMapper = new KravgrunnlagMapper(tpsAdapterWrapper);
        var behandlingskontrollProvider = new BehandlingskontrollProvider(
                behandlingskontrollTjeneste, mock(BehandlingskontrollAsynkTjeneste.class));
        var historikkinnslagTjeneste = new HistorikkinnslagTjeneste(
                repositoryProvider.getHistorikkRepository());
        var fagsakTjeneste = new FagsakTjeneste(tpsTjenesteMock, fagsakRepository, navBrukerRepository);
        behandlingTjeneste = new BehandlingTjeneste(repositoryProvider,
                behandlingskontrollProvider, fagsakTjeneste, historikkinnslagTjeneste, fagsystemKlientMock);
        var kravgrunnlagHenter = new KravgrunnlagHenter(økonomiProxyKlient, hentKravgrunnlagMapperProxy);
        var håndterGamleKravgrunnlagTjeneste = new HåndterGamleKravgrunnlagTjeneste(
                mottattXmlRepository, grunnlagRepository, lesKravgrunnlagMapper, behandlingTjeneste,
                fagsystemKlientMock, kravgrunnlagHenter);
        håndterGamleKravgrunnlagTask = new HåndterGamleKravgrunnlagTask(håndterGamleKravgrunnlagTjeneste);

        behandling = ScenarioSimple.simple().lagMocked();
        when(tpsTjenesteMock.hentBrukerForAktør(any(AktørId.class))).thenReturn(lagPersonInfo(behandling.getFagsak().getAktørId()));
        when(tpsTjenesteMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(true, ENHET, true));
        var eksternBehandlingsinfoDto = lagEksternBehandlingData();

        when(fagsystemKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(List.of(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class), any(Tillegsinformasjon.class))).thenReturn(lagSamletEksternBehandlingData(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(lagSamletEksternBehandlingData(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandlingOptional(any(UUID.class))).thenReturn(Optional.of(eksternBehandlingsinfoDto));
        when(fagsystemKlientMock.hentBehandling(any(UUID.class))).thenReturn(eksternBehandlingsinfoDto);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML());
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_uten_behandling() {
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        var behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"));
        assertThat(behandlinger).isNotEmpty();
        assertThat(behandlinger.size()).isEqualTo(1);
        var økonomiXmlMottatt = mottattXmlRepository.finnMottattXml(mottattXmlId);
        assertThat(økonomiXmlMottatt).isNotNull();
        assertThat(økonomiXmlMottatt.getSaksnummer()).isEqualTo("139015144");
        assertThat(økonomiXmlMottatt.getHenvisning()).isNotNull();
        assertThat(økonomiXmlMottatt.isTilkoblet()).isTrue();
        long behandlingId = behandlinger.get(0).getId();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_grunnlaget_ikke_finnes_i_økonomi() {
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class)))
                .thenThrow(new ManglendeKravgrunnlagException("FPT-539080", "kravgrunnlag ikke finnes"));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_økonomi_svarer_ukjent_feil() {
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class)))
                .thenThrow(new UkjentKvitteringFraOSException("FPT-539085", "ukjent feil"));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_grunnlaget_er_sperret() {
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class)))
                .thenThrow(new SperringKravgrunnlagException("FPT-539081", "sperret"));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        var økonomiXmlMottatt = mottattXmlRepository.finnMottattXml(mottattXmlId);
        assertThat(økonomiXmlMottatt).isNotNull();
        assertThat(økonomiXmlMottatt.isTilkoblet()).isTrue();
        var behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"));
        assertThat(behandlinger).isNotEmpty();
        var nyBehandling = behandlinger.get(0);
        var behandlingId = nyBehandling.getId();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandlingId)).isTrue();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_eksternBehandling_ikke_finnes_i_fpsak() {
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(new ArrayList<>());
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_kravgrunnlaget_ikke_er_gyldig() {
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(true, ENHET, false));
        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_behandling_allerede_finnes_med_samme_saksnummer_uten_kravgrunnlag() {
        var navBruker = TestFagsakUtil.genererBruker();
        var fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        var behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        var behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagKravgrunnlag(true, ENHET,true));

        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isNotEmpty();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_behandling_allerede_finnes_med_samme_saksnummer_med_kravgrunnlag() {
        var navBruker = TestFagsakUtil.genererBruker();
        var fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        var behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        var behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        var kravgrunnlag431Dto = lagKravgrunnlag(true, ENHET, true);
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class))).thenReturn(kravgrunnlag431Dto);
        var kravgrunnlag431 = hentKravgrunnlagMapperProxy.mapTilDomene(kravgrunnlag431Dto);
        grunnlagRepository.lagre(behandling.getId(), kravgrunnlag431);

        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isTrue();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isNotEmpty();
    }

    @Test
    void skal_kjøre_tasken_for_å_prosessere_gammel_kravgrunnlag_når_behandling_allerede_finnes_med_samme_saksnummer_og_kravgrunnlaget_er_sperret() {
        var navBruker = TestFagsakUtil.genererBruker();
        var fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        var behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        var behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        when(økonomiProxyKlient.hentKravgrunnlag(any(HentKravgrunnlagDetaljDto.class)))
                .thenThrow(new SperringKravgrunnlagException("FPT-539081", "sperret"));

        håndterGamleKravgrunnlagTask.doTask(lagProsessTaskData());
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isTrue();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingData() {
        var eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(100000001L));
        return eksternBehandlingsinfoDto;
    }

    private SamletEksternBehandlingInfo lagSamletEksternBehandlingData(EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        var fagsakDto = new FagsakDto();
        fagsakDto.setSaksnummer("139015144");
        fagsakDto.setSakstype(FagsakYtelseType.FORELDREPENGER);
        var personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setAktoerId(behandling.getAktørId().getId());
        return SamletEksternBehandlingInfo.builder(Tillegsinformasjon.FAGSAK, Tillegsinformasjon.PERSONOPPLYSNINGER)
                .setGrunninformasjon(eksternBehandlingsinfoDto)
                .setFagsak(fagsakDto)
                .setPersonopplysninger(personopplysningDto).build();
    }

    private String getInputXML() {
        try {
            var path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("xml/kravgrunnlag_periode_YTEL.xml")).toURI());
            return Files.readString(path);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }

    private ProsessTaskData lagProsessTaskData() {
        var prosessTaskData = ProsessTaskData.forProsessTask(HåndterGamleKravgrunnlagTask.class);
        prosessTaskData.setProperty("mottattXmlId", String.valueOf(mottattXmlId));
        prosessTaskData.setCallIdFraEksisterende();
        return prosessTaskData;
    }

    private Optional<Personinfo> lagPersonInfo(AktørId aktørId) {
        var personinfo = Personinfo.builder()
                .medAktørId(aktørId)
                .medFødselsdato(LocalDate.now().minusYears(20))
                .medPersonIdent(new PersonIdent(aktørId.getId()))
                .medNavn("testnavn")
                .build();
        return Optional.of(personinfo);
    }
}
