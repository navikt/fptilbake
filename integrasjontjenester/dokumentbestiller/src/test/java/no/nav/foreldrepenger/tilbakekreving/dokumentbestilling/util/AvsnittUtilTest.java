package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.PeriodeMedBrevtekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;

public class AvsnittUtilTest {

    private static final LocalDate FEBRUAR_1_2018 = LocalDate.of(2018, 2, 1);
    private static final LocalDate FEBRUAR_15_2018 = LocalDate.of(2018, 2, 15);

    @Test
    public void skal_lage_introduksjonsavsnitt() {
        //Arrange
        String oppsummeringFritekst = "oppsummering";
        Long sumBeløpSomSkalTilbakekreves = 123456L;
        Long sumFeilutbetaling = 9876543L;
        LocalDate varselbrevSendtUt = FEBRUAR_1_2018;
        String fagsaktypenavnPåSpråk = "eingongsstønad";

        //Act
        Avsnitt avsnitt = AvsnittUtil.lagIntroduksjonsavsnitt(sumBeløpSomSkalTilbakekreves, sumFeilutbetaling, varselbrevSendtUt, fagsaktypenavnPåSpråk, oppsummeringFritekst);

        //Assert
        Assertions.assertThat(avsnitt.getAvsnittstype()).isEqualTo(Avsnitt.Avsnittstype.OPPSUMMERING);
        Assertions.assertThat(avsnitt.getOverskrift()).isEqualTo("Du må betale tilbake eingongsstønad");
        Assertions.assertThat(avsnitt.getFom()).isNull();
        Assertions.assertThat(avsnitt.getTom()).isNull();

        List<Underavsnitt> underavsnittsliste = avsnitt.getUnderavsnittsliste();
        Assertions.assertThat(underavsnittsliste).hasSize(1);

        Underavsnitt underavsnitt = underavsnittsliste.get(0);
        Assertions.assertThat(underavsnitt.isFritekstTillatt()).isTrue();
        Assertions.assertThat(underavsnitt.getBrødtekst()).isEqualTo("Vi sendte deg et varsel 1. februar 2018 hvor vi fortalte at du hadde fått utbetalt 9876543 kroner for mye. Du må betale 123456 kroner tilbake. ");
        Assertions.assertThat(underavsnitt.getBrødtekst()).isEqualTo("Vi sendte deg et varsel 1. februar 2018 hvor vi fortalte at du hadde fått utbetalt 9876543 kroner for mye. Du må betale 123456 kroner tilbake. ");
        Assertions.assertThat(underavsnitt.getFritekst()).isEqualTo("oppsummering");
        Assertions.assertThat(underavsnitt.getOverskrift()).isNull();
    }

    @Test
    public void skal_lage_avsnitt_med_ekstrainformasjon() {

        //Arrange
        int antallUkerKlagefrist = 2;
        String kontakttelefonnummer = "22 33 44 55";

        //Act
        Avsnitt avsnitt = AvsnittUtil.lagEkstrainformasjonsavsnitt(antallUkerKlagefrist, kontakttelefonnummer);

        //Assert
        Assertions.assertThat(avsnitt.getOverskrift()).isEqualTo("Hvordan betale tilbake pengene du skylder");
        Assertions.assertThat(avsnitt.getAvsnittstype()).isEqualTo(Avsnitt.Avsnittstype.TILLEGGSINFORMASJON);

        List<Underavsnitt> underavsnittsliste = avsnitt.getUnderavsnittsliste();
        Assertions.assertThat(underavsnittsliste).hasSize(5);

        Underavsnitt betaleTilbakeAvsnitt = underavsnittsliste.get(0);
        Assertions.assertThat(betaleTilbakeAvsnitt.getBrødtekst()).isEqualTo("Vi har overført beløpet du skylder til NAV Innkreving, som vil sende deg " +
                "et krav om å betale tilbake det feilutbetalte beløpet. Har du spørsmål om dette, kan du kontakte NAV Innkreving. ");
        Assertions.assertThat(betaleTilbakeAvsnitt.isFritekstTillatt()).isFalse();

        Underavsnitt angKlageAvsnitt = underavsnittsliste.get(1);
        Assertions.assertThat(angKlageAvsnitt.getOverskrift()).isEqualTo("Du har rett til å klage");
        Assertions.assertThat(angKlageAvsnitt.getBrødtekst().trim()).isEqualTo("Du kan klage innen 2 uker fra den datoen du mottok vedtaket. Du finner skjema og informasjon " +
            "på nav.no/klage. \nDu må betale tilbake, selv om du klager på dette vedtaket. Vi vil betale " +
            "tilbake pengene du har betalt inn, om du får vedtak om at du ikke trengte å betale tilbake hele " +
            "eller deler av beløpet du skyldte.".trim());
        Assertions.assertThat(angKlageAvsnitt.isFritekstTillatt()).isFalse();

        Underavsnitt rettTilInnsyn = underavsnittsliste.get(2);
        Assertions.assertThat(rettTilInnsyn.getOverskrift()).isEqualTo("Du har rett til innsyn");
        Assertions.assertThat(rettTilInnsyn.getBrødtekst()).isEqualTo("På nav.no/dittnav kan du se dokumentene i saken din.");
        Assertions.assertThat(rettTilInnsyn.isFritekstTillatt()).isFalse();

        Underavsnitt harDuSpørsmål = underavsnittsliste.get(3);
        Assertions.assertThat(harDuSpørsmål.getOverskrift()).isEqualTo("Har du spørsmål?");
        Assertions.assertThat(harDuSpørsmål.getBrødtekst()).isEqualTo("Du finner nyttig informasjon på nav.no/familie. Du kan også kontakte oss på telefon 22 33 44 55");
        Assertions.assertThat(harDuSpørsmål.isFritekstTillatt()).isFalse();

        Underavsnitt hilsenNav = underavsnittsliste.get(4);
        Assertions.assertThat(hilsenNav.getOverskrift()).isNull();
        Assertions.assertThat(hilsenNav.getBrødtekst()).isEqualTo("Med vennlig hilsen \nNAV Familie- og pensjonsytelser");
        Assertions.assertThat(hilsenNav.isFritekstTillatt()).isFalse();
    }

    @Test
    public void skal_lage_avsnitt_for_periode() {
        //Arrange
        PeriodeMedBrevtekst periodeMedFritekstFraSaksbehandler = new PeriodeMedBrevtekst.Builder()
            .medFritekstFakta("Fritekst fakta")
            .medFritekstVilkår("Fritekst til vilkår")
            .medFritekstSærligeGrunner("Fritekst til særlige grunner")
            .medGenerertFaktaAvsnitt("Generert tekst til fakta")
            .medGenerertVilkårAvsnitt("Generert tekst til vilkår")
            .medGenerertSærligeGrunnerAvsnitt("Generert tekst til særlige grunner")
            .medFom(FEBRUAR_1_2018)
            .medTom(FEBRUAR_15_2018)
            .build();

        //Act
        Avsnitt avsnitt = AvsnittUtil.lagPeriodeAvsnitt(periodeMedFritekstFraSaksbehandler);

        //Assert
        Assertions.assertThat(avsnitt.getAvsnittstype()).isEqualTo(Avsnitt.Avsnittstype.PERIODE);
        Assertions.assertThat(avsnitt.getOverskrift()).isEqualTo("Perioden fra og med 1. februar 2018 til og med 15. februar 2018");
        Assertions.assertThat(avsnitt.getFom()).isEqualTo(FEBRUAR_1_2018);
        Assertions.assertThat(avsnitt.getTom()).isEqualTo(FEBRUAR_15_2018);

        List<Underavsnitt> underavsnittsliste = avsnitt.getUnderavsnittsliste();
        Assertions.assertThat(underavsnittsliste).hasSize(3);

        Underavsnitt faktaAvsnitt = underavsnittsliste.get(0);
        Assertions.assertThat(faktaAvsnitt.getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.FAKTA);
        Assertions.assertThat(faktaAvsnitt.getFritekst()).isEqualTo("Fritekst fakta");
        Assertions.assertThat(faktaAvsnitt.getBrødtekst()).isEqualTo("Generert tekst til fakta");
        Assertions.assertThat(faktaAvsnitt.isFritekstTillatt()).isTrue();

        Underavsnitt vilkårAvsnitt = underavsnittsliste.get(1);
        Assertions.assertThat(vilkårAvsnitt.getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.VILKÅR);
        Assertions.assertThat(vilkårAvsnitt.getFritekst()).isEqualTo("Fritekst til vilkår");
        Assertions.assertThat(vilkårAvsnitt.getBrødtekst()).isEqualTo("Generert tekst til vilkår");
        Assertions.assertThat(vilkårAvsnitt.isFritekstTillatt()).isTrue();


        Underavsnitt særligeGrunnerAvsnitt = underavsnittsliste.get(2);
        Assertions.assertThat(særligeGrunnerAvsnitt.getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER);
        Assertions.assertThat(særligeGrunnerAvsnitt.getFritekst()).isEqualTo("Fritekst til særlige grunner");
        Assertions.assertThat(særligeGrunnerAvsnitt.getBrødtekst()).isEqualTo("Generert tekst til særlige grunner");
        Assertions.assertThat(særligeGrunnerAvsnitt.isFritekstTillatt()).isTrue();


    }

}
