package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class TekstformatererHenleggelsesbrevTest {

    private final LocalDate niendeMars = LocalDate.of(2019, 3, 9);

    @Test
    public void skal_generere_henleggelsesbrev() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_med_verge() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .medVergeNavn("John Doe")
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        henleggelsesbrevSamletInfo.setFinnesVerge(true);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev.txt");
        String vergeTekst = les("/varselbrev/nb/verge.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit+"\n"+"\n"+ vergeTekst);
    }

    @Test
    public void skal_generere_henleggelsesbrev_nynorsk() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepengar")
            .medSprakkode(Språkkode.nn)
            .medMottakerAdresse(lagAdresseInfo())
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_nn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_overskrift() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String overskrift = TekstformatererHenleggelsesbrev.lagHenleggelsebrevOverskrift(henleggelsesbrevSamletInfo);
        String fasit = "NAV har avsluttet saken din om tilbakebetaling";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_overskrift_nynorsk() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nn)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String overskrift = TekstformatererHenleggelsesbrev.lagHenleggelsebrevOverskrift(henleggelsesbrevSamletInfo);
        String fasit = "NAV har avslutta saka di om tilbakebetaling";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    private String les(String filnavn) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(filnavn);
             Scanner scanner = new Scanner(resource, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
    }

    private Adresseinfo lagAdresseInfo(){
        return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,new PersonIdent("123456"),"Test", PersonstatusType.BOSA).build();
    }
}
