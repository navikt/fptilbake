package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
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

    private FagsakYtelseType foreldrepengerkode = FagsakYtelseType.FORELDREPENGER;
    private FagsakYtelseType engangsstønadkode = FagsakYtelseType.ENGANGSTØNAD;
    private FagsakYtelseType svangerskapspengerkode = FagsakYtelseType.SVANGERSKAPSPENGER;

    @Test
    public void skal_sammenstille_data_fra_fpsak_fpoppdrag_og_tps_for_forhåndsvisning() {
        Saksnummer saksnummer = new Saksnummer("11111111");
        String varseltekst = "Dette ser ikke bra ut as";
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = lagFeilutbetaltePerioderMock(9999999999L);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setBehandlendeEnhetId("behandlendeenhetId 556677");
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn("behandlende enhet i Bærum");
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nn);
        eksternBehandlingsinfoDto.setAnsvarligSaksbehandler("Saksbehandler Bodil");

        Adresseinfo adresseinfo = lagStandardNorskAdresse();

        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setNavn("Fiona");
        personopplysningDto.setFødselsnummer("12345678900");

        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk("eingongsstønad");
        ytelseNavn.setNavnPåBokmål("engangsstønad");

        SamletEksternBehandlingInfo behandingsinfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.VARSELTEKST)
            .setGrunninformasjon(eksternBehandlingsinfoDto)
            .setPersonopplysninger(personopplysningDto)
            .build();

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
            saksnummer,
            varseltekst,
            adresseinfo,
            behandingsinfo,
            feilutbetaltePerioderDto,
            Period.ofWeeks(3),
            FagsakYtelseType.ENGANGSTØNAD,
            ytelseNavn);

        Assertions.assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetId()).isEqualTo(eksternBehandlingsinfoDto.getBehandlendeEnhetId());
        Assertions.assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetNavn()).isEqualTo(eksternBehandlingsinfoDto.getBehandlendeEnhetNavn());
        Assertions.assertThat(varselbrev.getBrevMetadata().getFagsaktype()).isEqualTo(FagsakYtelseType.ENGANGSTØNAD);
        Assertions.assertThat(varselbrev.getFritekstFraSaksbehandler()).isEqualTo(varseltekst);
        Assertions.assertThat(varselbrev.getBrevMetadata().getSaksnummer()).isEqualTo(saksnummer.getVerdi());
        Assertions.assertThat(varselbrev.getBrevMetadata().getAnsvarligSaksbehandler()).isEqualTo(eksternBehandlingsinfoDto.getAnsvarligSaksbehandler());
        Assertions.assertThat(varselbrev.getBrevMetadata().getSpråkkode()).isEqualTo(eksternBehandlingsinfoDto.getSpråkkode());
        Assertions.assertThat(varselbrev.getSumFeilutbetaling()).isEqualTo(feilutbetaltePerioderDto.getSumFeilutbetaling());
        Assertions.assertThat(varselbrev.getBrevMetadata().getFagsaktypenavnPåSpråk()).isEqualTo("eingongsstønad");
        Assertions.assertThat(varselbrev.getBrevMetadata().getTittel()).isEqualTo("Varsel tilbakebetaling engangsstønad");

        Assertions.assertThat(varselbrev.getBrevMetadata().getSakspartNavn()).isEqualTo(behandingsinfo.getPersonopplysninger().getNavn());
        Assertions.assertThat(varselbrev.getBrevMetadata().getSakspartId()).isEqualTo(behandingsinfo.getPersonopplysninger().getFødselsnummer());

        Assertions.assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getFom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getFom());
        Assertions.assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getTom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getTom());
        Assertions.assertThat(varselbrev.getBrevMetadata().getMottakerAdresse()).isEqualTo(adresseinfo);
    }

    @Test
    public void skal_sammenstille_data_fra_fpsak_fpoppdrag_og_tps_for_sending() {
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = lagFeilutbetaltePerioderMock(9999999999L);
        Adresseinfo adresseinfo = lagStandardNorskAdresse();
        Personinfo personinfo = byggStandardPerson("Fiona", "12345678900", Språkkode.nn);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setAnsvarligSaksbehandler("Line Saksbehandler");
        eksternBehandlingsinfoDto.setBehandlendeEnhetId("behandlendeEnhetId 1234");
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn("behandlende enhet i Rogaland");

        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk("svangerskapspengar");
        ytelseNavn.setNavnPåBokmål("svangerskapspenger");

        SamletEksternBehandlingInfo behandingsinfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.VARSELTEKST)
            .setGrunninformasjon(eksternBehandlingsinfoDto)
            .setVarseltekst("Dette ser ikke bra ut as")
            .build();

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForSending(
            behandingsinfo,
            Saksnummer.infotrygd("11111111"),
            adresseinfo,
            personinfo,
            feilutbetaltePerioderDto,
            Period.ofWeeks(3),
            FagsakYtelseType.SVANGERSKAPSPENGER,
            ytelseNavn);

        Assertions.assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetId()).isEqualTo("behandlendeEnhetId 1234");
        Assertions.assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetNavn()).isEqualTo("behandlende enhet i Rogaland");
        Assertions.assertThat(varselbrev.getBrevMetadata().getFagsaktype()).isEqualTo(svangerskapspengerkode);
        Assertions.assertThat(varselbrev.getFritekstFraSaksbehandler()).isEqualTo("Dette ser ikke bra ut as");
        Assertions.assertThat(varselbrev.getBrevMetadata().getSaksnummer()).isEqualTo("11111111");
        Assertions.assertThat(varselbrev.getBrevMetadata().getAnsvarligSaksbehandler()).isEqualTo("Line Saksbehandler");
        Assertions.assertThat(varselbrev.getBrevMetadata().getSpråkkode()).isEqualTo(Språkkode.nn);
        Assertions.assertThat(varselbrev.getSumFeilutbetaling()).isEqualTo(feilutbetaltePerioderDto.getSumFeilutbetaling());
        Assertions.assertThat(varselbrev.getBrevMetadata().getFagsaktypenavnPåSpråk()).isEqualTo("svangerskapspengar");
        Assertions.assertThat(varselbrev.getBrevMetadata().getTittel()).isEqualTo("Varsel tilbakebetaling svangerskapspenger");

        Assertions.assertThat(varselbrev.getBrevMetadata().getSakspartNavn()).isEqualTo("Fiona");
        Assertions.assertThat(varselbrev.getBrevMetadata().getSakspartId()).isEqualTo("12345678900");

        Assertions.assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getFom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getFom());
        Assertions.assertThat(varselbrev.getFeilutbetaltePerioder().get(0).getTom()).isEqualTo(feilutbetaltePerioderDto.getPerioder().get(0).getTom());
        Assertions.assertThat(varselbrev.getBrevMetadata().getMottakerAdresse()).isEqualTo(adresseinfo);
    }

    @Test
    public void skal_sette_fristdato() {
        Period ventetid = Period.ofWeeks(3);
        LocalDateTime dagensDato = LocalDateTime.of(2020, 1, 1, 12, 0);
        Assertions.assertThat(VarselbrevUtil.finnFristForTilbakemeldingFraBruker(dagensDato, ventetid)).isEqualTo(LocalDate.of(2020, 1, 22));
    }

    private FeilutbetaltePerioderDto lagFeilutbetaltePerioderMock(Long sumFeilutbetalinger) {
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = new FeilutbetaltePerioderDto();
        feilutbetaltePerioderDto.setSumFeilutbetaling(sumFeilutbetalinger);
        feilutbetaltePerioderDto.setPerioder(lagPerioderDtoMock());
        return feilutbetaltePerioderDto;
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

    private List<PeriodeDto> lagPerioderDtoMock() {
        PeriodeDto periode = new PeriodeDto();
        periode.setFom(LocalDate.of(2019, 1, 1));
        periode.setTom(LocalDate.of(2020, 2, 1));
        return List.of(periode);
    }
}
