package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.PeriodeDto;

public class VarselbrevUtilTest {

    private static final String BEHANDLENDE_ENHET_NAVN = "behandlende enhet i Rogaland";
    private static final String BEHANDLENDE_ENHET_ID = "behandlendeEnhetId 1234";
    private static final String VARSEL_TEKST = "Dette ser ikke bra ut as";
    private static final String PERSONNUMMER = "12345678900";
    private FagsakYtelseType svangerskapspengerkode = FagsakYtelseType.SVANGERSKAPSPENGER;

    @Test
    public void skal_sammenstille_data_fra_fpsak_fpoppdrag_og_tps_for_forhåndsvisning() {
        Saksnummer saksnummer = new Saksnummer("11111111");
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = lagFeilutbetaltePerioderMock(9999999999L);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setBehandlendeEnhetId(BEHANDLENDE_ENHET_ID);
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn(BEHANDLENDE_ENHET_NAVN);
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nn);

        Adresseinfo adresseinfo = lagStandardNorskAdresse();

        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setNavn("Fiona");
        personopplysningDto.setFødselsnummer(PERSONNUMMER);

        YtelseNavn ytelseNavn = lagYtelseNavn("eingongsstønad", "engangsstønad");

        SamletEksternBehandlingInfo behandingsinfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER)
            .setGrunninformasjon(eksternBehandlingsinfoDto)
            .setPersonopplysninger(personopplysningDto)
            .build();

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
            saksnummer,
            VARSEL_TEKST,
            adresseinfo,
            behandingsinfo,
            feilutbetaltePerioderDto,
            Period.ofWeeks(3),
            FagsakYtelseType.ENGANGSTØNAD,
            ytelseNavn,
            false);

        assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetId()).isEqualTo(eksternBehandlingsinfoDto.getBehandlendeEnhetId());
        assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetNavn()).isEqualTo(eksternBehandlingsinfoDto.getBehandlendeEnhetNavn());
        assertThat(varselbrev.getBrevMetadata().getFagsaktype()).isEqualTo(FagsakYtelseType.ENGANGSTØNAD);
        assertThat(varselbrev.getFritekstFraSaksbehandler()).isEqualTo(VARSEL_TEKST);
        assertThat(varselbrev.getBrevMetadata().getSaksnummer()).isEqualTo(saksnummer.getVerdi());
        assertThat(varselbrev.getBrevMetadata().getSpråkkode()).isEqualTo(eksternBehandlingsinfoDto.getSpråkkodeEllerDefault());
        assertThat(varselbrev.getSumFeilutbetaling()).isEqualTo(feilutbetaltePerioderDto.getSumFeilutbetaling());
        assertThat(varselbrev.getBrevMetadata().getFagsaktypenavnPåSpråk()).isEqualTo("eingongsstønad");
        assertThat(varselbrev.getBrevMetadata().getTittel()).isEqualTo("Varsel tilbakebetaling engangsstønad");

        assertThat(varselbrev.getBrevMetadata().getSakspartNavn()).isEqualTo(behandingsinfo.getPersonopplysninger().getNavn());
        assertThat(varselbrev.getBrevMetadata().getSakspartId()).isEqualTo(behandingsinfo.getPersonopplysninger().getFødselsnummer());

        assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getFom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getFom());
        assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getTom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getTom());
        assertThat(varselbrev.getBrevMetadata().getMottakerAdresse()).isEqualTo(adresseinfo);
    }

    @Test
    public void skal_sammenstille_data_fra_fpsak_fpoppdrag_og_tps_for_sending() {
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = lagFeilutbetaltePerioderMock(9999999999L);
        Adresseinfo adresseinfo = lagStandardNorskAdresse();
        Personinfo personinfo = byggStandardPerson("Fiona", PERSONNUMMER);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setBehandlendeEnhetId(BEHANDLENDE_ENHET_ID);
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn(BEHANDLENDE_ENHET_NAVN);
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nn);

        YtelseNavn ytelseNavn = lagYtelseNavn("svangerskapspengar", "svangerskapspenger");

        SamletEksternBehandlingInfo behandingsinfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER)
            .setGrunninformasjon(eksternBehandlingsinfoDto)
            .build();

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForSending(
            behandingsinfo,
            Saksnummer.infotrygd("11111111"),
            adresseinfo,
            personinfo,
            feilutbetaltePerioderDto,
            Period.ofWeeks(3),
            FagsakYtelseType.SVANGERSKAPSPENGER,
            ytelseNavn, VARSEL_TEKST, false);

        assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetId()).isEqualTo(BEHANDLENDE_ENHET_ID);
        assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetNavn()).isEqualTo(BEHANDLENDE_ENHET_NAVN);
        assertThat(varselbrev.getBrevMetadata().getFagsaktype()).isEqualTo(svangerskapspengerkode);
        assertThat(varselbrev.getFritekstFraSaksbehandler()).isEqualTo(VARSEL_TEKST);
        assertThat(varselbrev.getBrevMetadata().getSaksnummer()).isEqualTo("11111111");
        assertThat(varselbrev.getBrevMetadata().getAnsvarligSaksbehandler()).isEqualTo("VL");
        assertThat(varselbrev.getBrevMetadata().getSpråkkode()).isEqualTo(Språkkode.nn);
        assertThat(varselbrev.getSumFeilutbetaling()).isEqualTo(feilutbetaltePerioderDto.getSumFeilutbetaling());
        assertThat(varselbrev.getBrevMetadata().getFagsaktypenavnPåSpråk()).isEqualTo("svangerskapspengar");
        assertThat(varselbrev.getBrevMetadata().getTittel()).isEqualTo("Varsel tilbakebetaling svangerskapspenger");

        assertThat(varselbrev.getBrevMetadata().getSakspartNavn()).isEqualTo("Fiona");
        assertThat(varselbrev.getBrevMetadata().getSakspartId()).isEqualTo(PERSONNUMMER);

        assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getFom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getFom());
        assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getTom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getTom());
        assertThat(varselbrev.getBrevMetadata().getMottakerAdresse()).isEqualTo(adresseinfo);
    }

    @Test
    public void skal_sette_fristdato() {
        Period ventetid = Period.ofWeeks(3);
        LocalDateTime dagensDato = LocalDateTime.of(2020, 1, 1, 12, 0);
        assertThat(VarselbrevUtil.finnFristForTilbakemeldingFraBruker(dagensDato, ventetid)).isEqualTo(LocalDate.of(2020, 1, 22));
    }

    @Test
    public void skal_sammenstille_data_fra_grunnlag_og_tps_for_åsende_manuelt_varselbrev() {
        UtbetaltPeriode utbetaltPeriode = UtbetaltPeriode.lagPeriode(LocalDate.of(2019, 10, 1),
            LocalDate.of(2019, 10, 30),
            BigDecimal.valueOf(9000));
        BehandlingFeilutbetalingFakta feilutbetalingFakta = BehandlingFeilutbetalingFakta.builder().medAktuellFeilUtbetaltBeløp(BigDecimal.valueOf(9000))
            .medPerioder(Lists.newArrayList(utbetaltPeriode)).build();

        Saksnummer saksnummer = new Saksnummer("11111111");
        NavBruker navBruker = NavBruker.opprettNy(new AktørId("1232132423"), Språkkode.nb);
        Fagsak fagsak = Fagsak.opprettNy(saksnummer, navBruker);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet(BEHANDLENDE_ENHET_ID, BEHANDLENDE_ENHET_NAVN);
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);

        Adresseinfo adresseinfo = lagStandardNorskAdresse();
        Personinfo personinfo = byggStandardPerson("Fiona", PERSONNUMMER);
        YtelseNavn ytelseNavn = lagYtelseNavn("foreldrepenger", "foreldrepenger");

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForSendingManueltVarselBrev(behandling,
            personinfo,
            adresseinfo,
            FagsakYtelseType.FORELDREPENGER,
            Språkkode.nb,
            ytelseNavn,
            Period.ofWeeks(3),
            VARSEL_TEKST,
            feilutbetalingFakta,
            false,
            false);

        assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetId()).isEqualTo(BEHANDLENDE_ENHET_ID);
        assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetNavn()).isEqualTo(BEHANDLENDE_ENHET_NAVN);
        assertThat(varselbrev.getBrevMetadata().getFagsaktype()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(varselbrev.getFritekstFraSaksbehandler()).isEqualTo(VARSEL_TEKST);
        assertThat(varselbrev.getBrevMetadata().getSaksnummer()).isEqualTo("11111111");
        assertThat(varselbrev.getBrevMetadata().getAnsvarligSaksbehandler()).isEqualTo("VL");
        assertThat(varselbrev.getBrevMetadata().getSpråkkode()).isEqualTo(Språkkode.nb);
        assertThat(varselbrev.getSumFeilutbetaling()).isEqualTo(feilutbetalingFakta.getAktuellFeilUtbetaltBeløp().longValue());
        assertThat(varselbrev.getBrevMetadata().getFagsaktypenavnPåSpråk()).isEqualTo("foreldrepenger");
        assertThat(varselbrev.getBrevMetadata().getTittel()).isEqualTo("Varsel tilbakebetaling foreldrepenger");
        assertThat(varselbrev.getFeilutbetaltePerioder().get(0)).isEqualTo(utbetaltPeriode.tilPeriode());

        assertThat(varselbrev.getBrevMetadata().getSakspartNavn()).isEqualTo("Fiona");
        assertThat(varselbrev.getBrevMetadata().getSakspartId()).isEqualTo(PERSONNUMMER);
    }

    private FeilutbetaltePerioderDto lagFeilutbetaltePerioderMock(Long sumFeilutbetalinger) {
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = new FeilutbetaltePerioderDto();
        feilutbetaltePerioderDto.setSumFeilutbetaling(sumFeilutbetalinger);
        feilutbetaltePerioderDto.setPerioder(lagPerioderDtoMock());
        return feilutbetaltePerioderDto;
    }

    private Personinfo byggStandardPerson(String navn, String personnummer) {
        return new Personinfo.Builder()
            .medPersonIdent(PersonIdent.fra(personnummer))
            .medNavn(navn)
            .medAktørId(new AktørId(9000000030014L))
            .medFødselsdato(LocalDate.of(1990, 2, 2))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
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

    private List<PeriodeDto> lagPerioderDtoMock() {
        PeriodeDto periode = new PeriodeDto();
        periode.setFom(LocalDate.of(2019, 1, 1));
        periode.setTom(LocalDate.of(2020, 2, 1));
        return List.of(periode);
    }

    private YtelseNavn lagYtelseNavn(String navnPåBrukersSpråk, String navnPåBokmål) {
        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk(navnPåBrukersSpråk);
        ytelseNavn.setNavnPåBokmål(navnPåBokmål);
        return ytelseNavn;
    }
}
