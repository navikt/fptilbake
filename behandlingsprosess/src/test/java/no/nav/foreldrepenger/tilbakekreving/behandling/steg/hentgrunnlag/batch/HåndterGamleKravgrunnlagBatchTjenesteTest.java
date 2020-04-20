package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.EmptyBatchArguments;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn.FinnGrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerFeil;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.typer.v1.PeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class HåndterGamleKravgrunnlagBatchTjenesteTest extends FellesTestOppsett {

    private HentKravgrunnlagMapper mapper = new HentKravgrunnlagMapper(tpsAdapterWrapper);
    private BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(behandlingskontrollTjeneste, mock(BehandlingskontrollAsynkTjeneste.class));
    private TpsTjeneste tpsTjenesteMock = mock(TpsTjeneste.class);
    private ØkonomiConsumer økonomiConsumerMock = mock(ØkonomiConsumer.class);
    private NavBrukerRepository navBrukerRepository = new NavBrukerRepositoryImpl(repoRule.getEntityManager());
    private FagsakTjeneste fagsakTjeneste = new FagsakTjeneste(tpsTjenesteMock, fagsakRepository, navBrukerRepository);
    private BehandlingTjeneste behandlingTjeneste = new BehandlingTjenesteImpl(repositoryProvider, prosessTaskRepository, behandlingskontrollProvider,
        fagsakTjeneste, historikkinnslagTjeneste, fpsakKlientMock, Period.ofWeeks(4));
    private HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste = new HåndterGamleKravgrunnlagTjeneste(mottattXmlRepository, mapper, behandlingTjeneste,
        økonomiConsumerMock, fpsakKlientMock);
    private HåndterGamleKravgrunnlagBatchTjeneste gamleKravgrunnlagBatchTjeneste = new HåndterGamleKravgrunnlagBatchTjeneste(håndterGamleKravgrunnlagTjeneste, Period.ofWeeks(-1));
    Long mottattXmlId = null;

    @Before
    public void setup() {
        System.setProperty("environment.name","devimg");
        when(tpsAdapterMock.hentAktørIdForPersonIdent(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getFagsak().getAktørId()));
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagDetaljertKravgrunnlagDto(true));
        when(fpsakKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList(lagEksternBehandlingData()));
        when(fpsakKlientMock.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(lagSamletEksternBehandlingData());
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
    }

    @Test
    public void skal_kjøre_batch_for_å_prosessere_gammel_kravgrunnlag_uten_behandling() {
        BatchArguments emptyBatchArguments = new EmptyBatchArguments(Collections.EMPTY_MAP);
        gamleKravgrunnlagBatchTjeneste.launch(emptyBatchArguments);
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"));
        assertThat(behandlinger).isNotEmpty();
        assertThat(behandlinger.size()).isEqualTo(1);
        ØkonomiXmlMottatt økonomiXmlMottatt = mottattXmlRepository.finnMottattXml(mottattXmlId);
        assertThat(økonomiXmlMottatt).isNotNull();
        assertThat(økonomiXmlMottatt.getSaksnummer()).isEqualTo("139015144");
        assertThat(økonomiXmlMottatt.getEksternBehandlingId()).isNotEmpty();
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(FinnGrunnlagTask.TASKTYPE);
    }

    @Test
    public void skal_kjøre_batch_for_å_prosessere_gammel_kravgrunnlag_når_grunnlaget_ikke_finnes_i_økonomi() {
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class)))
            .thenThrow(ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagIkkeFinnes(any(), anyString()).toException());
        BatchArguments emptyBatchArguments = new EmptyBatchArguments(Collections.EMPTY_MAP);
        gamleKravgrunnlagBatchTjeneste.launch(emptyBatchArguments);
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    public void skal_kjøre_batch_for_å_prosessere_gammel_kravgrunnlag_når_eksternBehandling_ikke_finnes_i_fpsak() {
        when(fpsakKlientMock.hentBehandlingForSaksnummer(anyString())).thenReturn(Lists.newArrayList());
        BatchArguments emptyBatchArguments = new EmptyBatchArguments(Collections.EMPTY_MAP);
        gamleKravgrunnlagBatchTjeneste.launch(emptyBatchArguments);
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
    }

    @Test
    public void skal_kjøre_batch_for_å_prosessere_gammel_kravgrunnlag_når_kravgrunnlaget_ikke_er_gyldig() {
        when(økonomiConsumerMock.hentKravgrunnlag(any(), any(HentKravgrunnlagDetaljDto.class))).thenReturn(lagDetaljertKravgrunnlagDto(false));
        BatchArguments emptyBatchArguments = new EmptyBatchArguments(Collections.EMPTY_MAP);
        gamleKravgrunnlagBatchTjeneste.launch(emptyBatchArguments);
        assertThat(mottattXmlRepository.finnArkivertMottattXml(mottattXmlId)).isNotNull();
        assertThat(mottattXmlRepository.finnMottattXml(mottattXmlId)).isNull();
        assertThat(behandlingTjeneste.hentBehandlinger(new Saksnummer("139015144"))).isEmpty();
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
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(lagKravgrunnlagBeløp(BigDecimal.valueOf(9000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(lagKravgrunnlagBeløp(BigDecimal.ZERO, BigDecimal.valueOf(9000), BigDecimal.valueOf(9000), BigDecimal.ZERO, TypeKlasseDto.YTEL));

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
        detaljertKravgrunnlagBelop.setSkattProsent(BigDecimal.valueOf(50.0000));

        return detaljertKravgrunnlagBelop;
    }

    private XMLGregorianCalendar konvertDato(LocalDate localDate) {
        return DateUtil.convertToXMLGregorianCalendar(localDate);
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingData() {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        eksternBehandlingsinfoDto.setId(100000001l);
        return eksternBehandlingsinfoDto;
    }

    private SamletEksternBehandlingInfo lagSamletEksternBehandlingData() {
        FagsakDto fagsakDto = new FagsakDto();
        fagsakDto.setSaksnummer(139015144l);
        fagsakDto.setSakstype(FagsakYtelseType.FORELDREPENGER);
        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setAktoerId(behandling.getAktørId().getId());
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.FAGSAK, Tillegsinformasjon.PERSONOPPLYSNINGER)
            .setGrunninformasjon(lagEksternBehandlingData())
            .setFagsak(fagsakDto)
            .setPersonopplysninger(personopplysningDto).build();
        return samletEksternBehandlingInfo;
    }

}
