package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class TekstformatererInnhentDokumentasjonbrevTest  {

    @Test
    public void skal_generere_innhentdokumentasjonbrev() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagStandardNorskAdresse())
            .medSakspartNavn("Test")
            .build();

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .medFritekstFraSaksbehandler("Dette er ein fritekst.")
            .medFristDato(LocalDate.of(2020, 3, 2))
            .build();
        String generertBrev = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevFritekst(innhentDokumentasjonBrevSamletInfo);
        String fasit = les("/innhentdokumentasjonbrev/innhentdokumentasjonbrev.txt");;
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_innhentdokumentasjonbrev_for_verge() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagStandardNorskAdresse())
            .medSakspartNavn("Test")
            .medVergeNavn("John Doe")
            .medFinnesVerge(true)
            .build();

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .medFritekstFraSaksbehandler("Dette er ein fritekst.")
            .medFristDato(LocalDate.of(2020, 3, 2))
            .build();
        String generertBrev = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevFritekst(innhentDokumentasjonBrevSamletInfo);
        String fasit = les("/innhentdokumentasjonbrev/innhentdokumentasjonbrev.txt");;
        String vergeTekst = les("/varselbrev/nb/verge.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit+"\n"+"\n"+ vergeTekst);
    }

    @Test
    public void skal_generere_innhentdokumentasjonbrev_for_verge_organisasjon() throws Exception {
        Adresseinfo orgAdresse = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
            new PersonIdent("12345678901"),
            "Semba AS c/o John Doe", null)
            .medAdresselinje1("adresselinje 1")
            .medAdresselinje2("adresselinje 2")
            .medAdresselinje3("adresselinje 3")
            .medLand("NOR")
            .medPostNr("0688")
            .medPoststed("OSLO")
            .build();
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(orgAdresse)
            .medSakspartNavn("Test")
            .medVergeNavn("John Doe")
            .medFinnesVerge(true)
            .build();

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .medFritekstFraSaksbehandler("Dette er ein fritekst.")
            .medFristDato(LocalDate.of(2020, 3, 2))
            .build();
        String generertBrev = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevFritekst(innhentDokumentasjonBrevSamletInfo);
        String fasit = les("/innhentdokumentasjonbrev/innhentdokumentasjonbrev.txt");;
        String vergeTekst = "Brev med likt innhold er sendt til Test";
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit+"\n"+"\n"+ vergeTekst);
    }

    @Test
    public void skal_generere_innhentdokumentasjonbrev_nynorsk() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nn)
            .medMottakerAdresse(lagStandardNorskAdresse())
            .medSakspartNavn("Test")
            .build();

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .medFritekstFraSaksbehandler("Dette er ein fritekst.")
            .medFristDato(LocalDate.of(2020, 3, 2))
            .build();
        String generertBrev = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevFritekst(innhentDokumentasjonBrevSamletInfo);
        String fasit = les("/innhentdokumentasjonbrev/innhentdokumentasjonbrev_nn.txt");;
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_innhentdokumentasjonbrev_overskrift() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .build();

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .build();
        String overskrift = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevOverskrift(innhentDokumentasjonBrevSamletInfo);
        String fasit = "Vi trenger flere opplysninger";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_innhentdokumentasjonbrev_overskrift_nynorsk() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nn)
            .build();

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .build();
        String overskrift = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevOverskrift(innhentDokumentasjonBrevSamletInfo);
        String fasit = "Vi trenger fleire opplysningar";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    private String les(String filnavn) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(filnavn);
             Scanner scanner = new Scanner(resource, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
    }

    private Adresseinfo lagStandardNorskAdresse() {
        return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
            new PersonIdent("12345678901"),
            "Test", null)
            .medAdresselinje1("adresselinje 1")
            .medAdresselinje2("adresselinje 2")
            .medAdresselinje3("adresselinje 3")
            .medLand("NOR")
            .medPostNr("0688")
            .medPoststed("OSLO")
            .build();
    }
}
