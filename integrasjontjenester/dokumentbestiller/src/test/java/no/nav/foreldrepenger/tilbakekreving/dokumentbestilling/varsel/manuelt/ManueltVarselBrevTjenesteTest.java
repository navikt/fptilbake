package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.util.FPDateUtil;

public class ManueltVarselBrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String VARSEL_TEKST = "Sender manuelt varselbrev";
    private final String KORRIGERT_VARSEL_TEKST = "Sender korrigert varselbrev";

    private VarselRepository varselRepository = new VarselRepository(repositoryRule.getEntityManager());

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FaktaFeilutbetalingTjeneste mockFeilutbetalingTjeneste = mock(FaktaFeilutbetalingTjeneste.class);
    private FritekstbrevTjeneste mockFritekstbrevTjeneste = mock(FritekstbrevTjeneste.class);
    private JournalTjeneste mockJournalTjeneste = mock(JournalTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);


    private HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(repositoryProvider.getHistorikkRepository(),
        mockJournalTjeneste,
        mockPersoninfoAdapter);

    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste = new ManueltVarselBrevTjeneste(repositoryProvider,
        mockEksternDataForBrevTjeneste,
        mockFeilutbetalingTjeneste,
        mockFritekstbrevTjeneste,
        historikkinnslagTjeneste);

    private Long behandlingId;

    @Before
    public void setup() {
        behandlingId = behandling.getId();
        when(mockFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId)).thenReturn(lagFeilutbetalingFakta());
        when(mockFritekstbrevTjeneste.sendFritekstbrev(any(FritekstbrevData.class))).thenReturn(lagJournalOgDokument());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId)).thenReturn(lagStandardNorskAdresse());
        when(mockEksternDataForBrevTjeneste.getBrukersSvarfrist()).thenReturn(Period.ofWeeks(3));

    }

    @Test
    public void skal_sende_manuelt_varselbrev() {
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, DokumentMalType.VARSEL_DOK, VARSEL_TEKST);

        Optional<VarselInfo> varselInfo = varselRepository.finnVarsel(behandlingId);
        assertThat(varselInfo).isPresent();
        VarselInfo varsel = varselInfo.get();
        assertThat(varsel.getVarselTekst()).isEqualTo(VARSEL_TEKST);
        assertThat(varsel.getVarselBeløp()).isEqualTo(9000l);
        assertThat(varsel.isAktiv()).isTrue();

        assertThat(varselbrevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId)).isTrue();

        List<Historikkinnslag> historikkInnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkInnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkInnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker().get(0).getLinkTekst()).isEqualTo(ManueltVarselBrevTjeneste.TITTEL_VARSELBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_sende_korrigert_varselbrev() {
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, DokumentMalType.VARSEL_DOK, VARSEL_TEKST);
        manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, DokumentMalType.KORRIGERT_VARSEL_DOK, KORRIGERT_VARSEL_TEKST);

        Optional<VarselInfo> varselInfo = varselRepository.finnVarsel(behandlingId);
        assertThat(varselInfo).isPresent();
        VarselInfo varsel = varselInfo.get();
        assertThat(varsel.getVarselTekst()).isEqualTo(KORRIGERT_VARSEL_TEKST);
        assertThat(varsel.getVarselBeløp()).isEqualTo(9000l);
        assertThat(varsel.isAktiv()).isTrue();

        assertThat(varselbrevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId)).isTrue();

        List<Historikkinnslag> historikkInnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkInnslager.size()).isEqualTo(2);
        historikkInnslager.sort(Comparator.comparing(Historikkinnslag::getId));
        Historikkinnslag historikkinnslag = historikkInnslager.get(1);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker().get(0).getLinkTekst()).isEqualTo(ManueltVarselBrevTjeneste.TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_forhåndsvise_manuelt_varselbrev() {
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    @Test
    public void skal_forhåndsvise_korrigert_varselbrev() {
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        varselRepository.lagre(behandling.getId(),KORRIGERT_VARSEL_TEKST,32000l);
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.KORRIGERT_VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    private BehandlingFeilutbetalingFakta lagFeilutbetalingFakta() {
        UtbetaltPeriode utbetaltPeriode = UtbetaltPeriode.lagPeriode(LocalDate.of(2019, 10, 1),
            LocalDate.of(2019, 10, 30),
            BigDecimal.valueOf(9000));
        return BehandlingFeilutbetalingFakta.builder().medAktuellFeilUtbetaltBeløp(BigDecimal.valueOf(9000))
            .medPerioder(Lists.newArrayList(utbetaltPeriode))
            .medDatoForRevurderingsvedtak(FPDateUtil.iDag()).build();
    }

    private JournalpostIdOgDokumentId lagJournalOgDokument() {
        JournalpostId journalpostId = new JournalpostId(12344l);
        return new JournalpostIdOgDokumentId(journalpostId, "qwr12334");
    }

    private YtelseNavn lagYtelseNavn(String navnPåBrukersSpråk, String navnPåBokmål) {
        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk(navnPåBrukersSpråk);
        ytelseNavn.setNavnPåBokmål(navnPåBokmål);
        return ytelseNavn;
    }

    private Personinfo byggStandardPerson(String navn, String personnummer, Språkkode språkkode) {
        return new Personinfo.Builder()
            .medPersonIdent(PersonIdent.fra(personnummer))
            .medNavn(navn)
            .medAktørId(new AktørId(9000000030014L))
            .medFødselsdato(LocalDate.of(1990, 2, 2))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medForetrukketSpråk(språkkode)
            .build();
    }

    private Adresseinfo lagStandardNorskAdresse() {
        return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
            new PersonIdent("12345678901"),
            "Jens Trallala", null)
            .medAdresselinje1("adresselinje 1")
            .medAdresselinje2("adresselinje 2")
            .medAdresselinje3("adresselinje 3")
            .medLand("NOR")
            .medPostNr("0688")
            .medPoststed("OSLO")
            .build();
    }
}
