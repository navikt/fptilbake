package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class TekstformatererHenleggelsesbrevTest {

    private final LocalDate niendeMars = LocalDate.of(2019, 3, 9);
    private static final String REVURDERING_HENLEGGELSESBREV_FRITEKST = "Revurderingen ble henlagt";

    @Test
    public void skal_generere_henleggelsesbrev() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_frisinn() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk(FagsakYtelseType.FRISINN.getNavn().toLowerCase())
            .medFagsaktype(FagsakYtelseType.FRISINN)
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_frisinn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_for_tilbakekreving_revurdering() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .medBehandlingtype(BehandlingType.REVURDERING_TILBAKEKREVING)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setFritekstFraSaksbehandler(REVURDERING_HENLEGGELSESBREV_FRITEKST);
        String generertBrev = TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_revurdering.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_for_tilbakekreving_revurdering_frisinn() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk(FagsakYtelseType.FRISINN.getNavn().toLowerCase())
            .medFagsaktype(FagsakYtelseType.FRISINN)
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .medBehandlingtype(BehandlingType.REVURDERING_TILBAKEKREVING)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setFritekstFraSaksbehandler(REVURDERING_HENLEGGELSESBREV_FRITEKST);
        String generertBrev = TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_revurdering_frisinn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_med_verge() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .medVergeNavn("John Doe")
            .medFinnesVerge(true)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev.txt");
        String vergeTekst = les("/varselbrev/nb/verge.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit + "\n" + "\n" + vergeTekst);
    }

    @Test
    public void skal_generere_henleggelsesbrev_for_tilbakekreving_revurdering_med_verge() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .medVergeNavn("John Doe")
            .medFinnesVerge(true)
            .medBehandlingtype(BehandlingType.REVURDERING_TILBAKEKREVING)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setFritekstFraSaksbehandler(REVURDERING_HENLEGGELSESBREV_FRITEKST);
        String generertBrev = TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_revurdering.txt");
        String vergeTekst = les("/varselbrev/nb/verge.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit + "\n" + "\n" + vergeTekst);
    }

    @Test
    public void skal_generere_henleggelsesbrev_nynorsk() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepengar")
            .medSprakkode(Språkkode.nn)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setVarsletDato(niendeMars);
        String generertBrev = TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_nn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_nynorsk_for_tilbakekreving_revurderning() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepengar")
            .medSprakkode(Språkkode.nn)
            .medMottakerAdresse(lagAdresseInfo())
            .medSakspartNavn("Test")
            .medBehandlingtype(BehandlingType.REVURDERING_TILBAKEKREVING)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setFritekstFraSaksbehandler(REVURDERING_HENLEGGELSESBREV_FRITEKST);
        String generertBrev = TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevFritekst(henleggelsesbrevSamletInfo);
        String fasit = les("/henleggelsesbrev/henleggelsesbrev_revurdering_nn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_overskrift() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
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
    public void skal_generere_henleggelsesbrev_overskrift_for_tilbakekreving_revurdering() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .medBehandlingtype(BehandlingType.REVURDERING_TILBAKEKREVING)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(brevMetadata);
        henleggelsesbrevSamletInfo.setFritekstFraSaksbehandler(REVURDERING_HENLEGGELSESBREV_FRITEKST);
        String overskrift = TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevOverskrift(henleggelsesbrevSamletInfo);
        String fasit = "Tilbakebetaling foreldrepenger";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_henleggelsesbrev_overskrift_nynorsk() throws Exception {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.FORELDREPENGER)
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

    private Adresseinfo lagAdresseInfo() {
        return new Adresseinfo.Builder(new PersonIdent("123456"), "Test").build();
    }
}
