package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.PeriodeDto;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

public class VarselbrevUtilTest {

    private KodeDto foreldrepengerkode = new KodeDto("FAGSAK_YTELSE", "FP", "");
    private KodeDto engangsstønadkode = new KodeDto("FAGSAK_YTELSE", "ES", "");
    private KodeDto svangerskapspengerkode = new KodeDto("FAGSAK_YTELSE", "SVP", "");

    @Test
    public void skal_sammenstille_data_fra_fpsak_fpoppdrag_og_tps_for_forhåndsvisning() {
        String saksnummer = "11111111";
        String varseltekst = "Dette ser ikke bra ut as";
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = lagFeilutbetaltePerioderMock(9999999999L);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setBehandlendeEnhetId("behandlendeenhetId 556677");
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn("behandlende enhet i Bærum");
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nn);
        eksternBehandlingsinfoDto.setFagsaktype(engangsstønadkode);
        eksternBehandlingsinfoDto.setAnsvarligSaksbehandler("Saksbehandler Bodil");

        Adresseinfo adresseinfo = lagStandardNorskAdresse();

        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setNavn("Fiona");
        personopplysningDto.setFødselsnummer("12345678900");
        eksternBehandlingsinfoDto.setPersonopplysningDto(personopplysningDto);

        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk("eingongsstønad");
        ytelseNavn.setNavnPåBokmål("engangsstønad");

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
            saksnummer,
            varseltekst,
            adresseinfo,
            eksternBehandlingsinfoDto,
            feilutbetaltePerioderDto,
            Period.ofWeeks(3),
            ytelseNavn);

        Assertions.assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetId()).isEqualTo(eksternBehandlingsinfoDto.getBehandlendeEnhetId());
        Assertions.assertThat(varselbrev.getBrevMetadata().getBehandlendeEnhetNavn()).isEqualTo(eksternBehandlingsinfoDto.getBehandlendeEnhetNavn());
        Assertions.assertThat(varselbrev.getBrevMetadata().getFagsaktype()).isEqualTo(eksternBehandlingsinfoDto.getFagsaktype());
        Assertions.assertThat(varselbrev.getFritekstFraSaksbehandler()).isEqualTo(varseltekst);
        Assertions.assertThat(varselbrev.getBrevMetadata().getSaksnummer()).isEqualTo(saksnummer);
        Assertions.assertThat(varselbrev.getBrevMetadata().getAnsvarligSaksbehandler()).isEqualTo(eksternBehandlingsinfoDto.getAnsvarligSaksbehandler());
        Assertions.assertThat(varselbrev.getBrevMetadata().getSpråkkode()).isEqualTo(eksternBehandlingsinfoDto.getSprakkode());
        Assertions.assertThat(varselbrev.getSumFeilutbetaling()).isEqualTo(feilutbetaltePerioderDto.getSumFeilutbetaling());
        Assertions.assertThat(varselbrev.getBrevMetadata().getFagsaktypenavnPåSpråk()).isEqualTo("eingongsstønad");
        Assertions.assertThat(varselbrev.getBrevMetadata().getTittel()).isEqualTo("Varsel tilbakebetaling engangsstønad");

        Assertions.assertThat(varselbrev.getBrevMetadata().getSakspartNavn()).isEqualTo(eksternBehandlingsinfoDto.getPersonopplysningDto().getNavn());
        Assertions.assertThat(varselbrev.getBrevMetadata().getSakspartId()).isEqualTo(eksternBehandlingsinfoDto.getPersonopplysningDto().getFødselsnummer());

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
        eksternBehandlingsinfoDto.setVarseltekst("Dette ser ikke bra ut as");
        eksternBehandlingsinfoDto.setAnsvarligSaksbehandler("Line Saksbehandler");
        eksternBehandlingsinfoDto.setFagsaktype(svangerskapspengerkode);
        eksternBehandlingsinfoDto.setBehandlendeEnhetId("behandlendeEnhetId 1234");
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn("behandlende enhet i Rogaland");

        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk("svangerskapspengar");
        ytelseNavn.setNavnPåBokmål("svangerskapspenger");

        VarselbrevSamletInfo varselbrev = VarselbrevUtil.sammenstillInfoFraFagsystemerForSending(
            eksternBehandlingsinfoDto,
            Saksnummer.infotrygd("11111111"),
            adresseinfo,
            personinfo,
            feilutbetaltePerioderDto,
            Period.ofWeeks(3),
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
