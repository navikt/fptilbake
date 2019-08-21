package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevMetadataMapper;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.klient.FpOppdragRestKlient;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.PeriodeDto;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErRedigerbart;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;

public class BestillDokumentTjenesteTest extends DokumentBestillerTestOppsett {


    private BrevMetadataMapper brevMetadataMapper = new BrevMetadataMapper(kodeverkRepository);

    private DokumentproduksjonConsumer dokumentproduksjonConsumerMock = mock(DokumentproduksjonConsumer.class);
    private VedtaksbrevTjeneste vedtaksbrevTjenesteMock = mock(VedtaksbrevTjeneste.class);
    private HistorikkinnslagTjeneste historikkinnslagTjenesteMock = mock(HistorikkinnslagTjeneste.class);
    private FpOppdragRestKlient fpOppdragRestKlientMock = mock(FpOppdragRestKlient.class);
    private TpsTjeneste tpsTjenesteMock = mock(TpsTjeneste.class);
    private FpsakKlient fpsakKlientMock = mock(FpsakKlient.class);
    private BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);

    private FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste = new FellesInfoTilBrevTjeneste(fpOppdragRestKlientMock, tpsTjenesteMock, fpsakKlientMock,
        kodeverkRepository, Period.ofWeeks(3));
    private VarselbrevTjeneste varselbrevTjeneste = new VarselbrevTjeneste(fellesInfoTilBrevTjeneste, behandlingTjenesteMock,
        eksternBehandlingRepository);
    private BestillDokumentTjeneste bestillDokumentTjeneste = new BestillDokumentTjeneste(dokumentproduksjonConsumerMock, vedtaksbrevTjenesteMock,
        varselbrevTjeneste, brevMetadataMapper, historikkinnslagTjenesteMock, brevdataRepository);

    @Before
    public void setup() throws Exception {
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        when(fpsakKlientMock.hentBehandlingsinfo(eksternBehandling.getEksternId(), fagsak.getSaksnummer().getVerdi()))
            .thenReturn(Optional.of(lagMockEksternBehandlingsinfoDto(fagsak)));
        Personinfo personinfo = lagMockPersonInfo(fagsak.getAktørId());
        when(tpsTjenesteMock.hentBrukerForAktør(fagsak.getAktørId())).thenReturn(Optional.of(personinfo));
        when(tpsTjenesteMock.hentAdresseinformasjon(personinfo.getPersonIdent())).thenReturn(lagMockAddresseInfo());
        when(fpOppdragRestKlientMock.hentFeilutbetaltePerioder(eksternBehandling.getEksternId())).thenReturn(Optional.of(lagMockSimuleringData()));
        when(dokumentproduksjonConsumerMock.produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class)))
            .thenReturn(lagMockVarselbrevRespons());
        when(dokumentproduksjonConsumerMock.produserDokumentutkast(any(ProduserDokumentutkastRequest.class))).thenReturn(lagMockVarselbrevForhåndsvisningRespon());
    }

    @Test
    public void sendVarselbrev() {
        bestillDokumentTjeneste.sendVarselbrev(fagsak.getId(), fagsak.getAktørId().getId(), behandling.getId());
        verify(historikkinnslagTjenesteMock, atLeastOnce()).opprettHistorikkinnslagForBrevsending(any(JournalpostId.class), anyString(),
            anyLong(), anyLong(), any(AktørId.class), anyString());
        List<VarselbrevSporing> varselbrevSporinger = brevdataRepository.hentVarselbrevData(behandling.getId());
        assertThat(varselbrevSporinger).isNotEmpty();
        assertThat(varselbrevSporinger.size()).isEqualTo(1);
        VarselbrevSporing varselbrevSporing = varselbrevSporinger.get(0);
        assertThat(varselbrevSporing.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(varselbrevSporing.getDokumentId()).isEqualTo("12345");
        assertThat(varselbrevSporing.getJournalpostId()).isEqualTo("10000");
    }

    @Test
    public void sendVarselbrev_nårBrevkanIkke_sendeTilDokumentProduksjon() throws Exception {
        when(dokumentproduksjonConsumerMock.produserIkkeredigerbartDokument(any(ProduserIkkeredigerbartDokumentRequest.class)))
            .thenThrow(ProduserIkkeredigerbartDokumentDokumentErRedigerbart.class);
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-227659");

        bestillDokumentTjeneste.sendVarselbrev(fagsak.getId(), fagsak.getAktørId().getId(), behandling.getId());
    }

    @Test
    public void sendVarselbrev_nårFantIkke_behandlingIFpsak() {
        when(fpsakKlientMock.hentBehandlingsinfo(eksternBehandling.getEksternId(), fagsak.getSaksnummer().getVerdi()))
            .thenReturn(Optional.empty());
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-841932");

        bestillDokumentTjeneste.sendVarselbrev(fagsak.getId(), fagsak.getAktørId().getId(), behandling.getId());
    }

    @Test
    public void sendVarselbrev_nårFantIkke_personITps() {
        when(tpsTjenesteMock.hentBrukerForAktør(fagsak.getAktørId())).thenReturn(Optional.empty());
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-089912");

        bestillDokumentTjeneste.sendVarselbrev(fagsak.getId(), fagsak.getAktørId().getId(), behandling.getId());
    }

    @Test
    public void sendVarselbrev_nårFantIkke_behandlingIFpoppdrag() {
        when(fpOppdragRestKlientMock.hentFeilutbetaltePerioder(eksternBehandling.getEksternId())).thenReturn(Optional.empty());
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-748279");

        bestillDokumentTjeneste.sendVarselbrev(fagsak.getId(), fagsak.getAktørId().getId(), behandling.getId());
    }

    @Test
    public void hentForhåndsvisningVarselbrev() {
        HentForhåndsvisningVarselbrevDto forhåndsvisningVarselbrevDto = new HentForhåndsvisningVarselbrevDto();
        forhåndsvisningVarselbrevDto.setBehandlingId(FPSAK_BEHANDLING_ID);
        forhåndsvisningVarselbrevDto.setSaksnummer(fagsak.getSaksnummer().getVerdi());
        forhåndsvisningVarselbrevDto.setVarseltekst("");
        byte[] respons = bestillDokumentTjeneste.hentForhåndsvisningVarselbrev(forhåndsvisningVarselbrevDto);
        assertThat(respons).isNotEmpty();
    }

    private EksternBehandlingsinfoDto lagMockEksternBehandlingsinfoDto(Fagsak fagsak) {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setFagsakId(fagsak.getId());
        eksternBehandlingsinfoDto.setId(FPSAK_BEHANDLING_ID);
        eksternBehandlingsinfoDto.setSaksnummer(fagsak.getSaksnummer().getVerdi());
        eksternBehandlingsinfoDto.setBehandlendeEnhetId("8020");
        eksternBehandlingsinfoDto.setAnsvarligSaksbehandler("Z9901136");
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nb);
        eksternBehandlingsinfoDto.setFagsaktype(new KodeDto(FagsakYtelseType.FORELDREPENGER.getKodeverk(),
            FagsakYtelseType.FORELDREPENGER.getKode(), null));
        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setAktoerId(fagsak.getAktørId().getId());
        personopplysningDto.setFødselsnummer(DUMMY_FØDSELSNUMMER);
        personopplysningDto.setNavn("John Doe");
        eksternBehandlingsinfoDto.setPersonopplysningDto(personopplysningDto);
        return eksternBehandlingsinfoDto;
    }

    private Personinfo lagMockPersonInfo(AktørId aktørId) {
        return Personinfo.builder().medAktørId(aktørId)
            .medPersonIdent(new PersonIdent(DUMMY_FØDSELSNUMMER))
            .medNavn("John Doe")
            .medFødselsdato(LocalDate.of(1343, 12, 12))
            .medForetrukketSpråk(Språkkode.nb)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE).build();
    }

    private Adresseinfo lagMockAddresseInfo() {
        Adresseinfo.Builder builder = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
            new PersonIdent(DUMMY_FØDSELSNUMMER),
            "Tjoms",
            PersonstatusType.BOSA);
        return builder.medAdresselinje1("Veien 17").build();
    }

    private FeilutbetaltePerioderDto lagMockSimuleringData() {
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = new FeilutbetaltePerioderDto();
        feilutbetaltePerioderDto.setSumFeilutbetaling(30000L);
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setFom(LocalDate.of(2016, 3, 16));
        periodeDto.setTom(LocalDate.of(2016, 5, 26));
        feilutbetaltePerioderDto.setPerioder(Lists.newArrayList(periodeDto));
        return feilutbetaltePerioderDto;
    }

    private ProduserIkkeredigerbartDokumentResponse lagMockVarselbrevRespons() {
        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = new ProduserIkkeredigerbartDokumentResponse();
        produserIkkeredigerbartDokumentResponse.setDokumentId("12345");
        produserIkkeredigerbartDokumentResponse.setJournalpostId("10000");
        return produserIkkeredigerbartDokumentResponse;
    }

    private ProduserDokumentutkastResponse lagMockVarselbrevForhåndsvisningRespon() {
        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        produserDokumentutkastResponse.setDokumentutkast("Test brev".getBytes());
        return produserDokumentutkastResponse;
    }

}
