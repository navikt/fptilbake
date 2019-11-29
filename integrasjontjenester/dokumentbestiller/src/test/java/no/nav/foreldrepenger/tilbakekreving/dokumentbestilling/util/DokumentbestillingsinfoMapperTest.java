package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.DokumentbestillingsinfoMapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.NorskPostadresse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.UtenlandskPostadresse;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DokumentbestillingsinfoMapperTest {

    @Test
    public void skal_opprette_et_dokumentbestillingsobjekt() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medSakspartNavn("Ola Nordmann")
            .medSaksnummer("56578978979")
            .medAnsvarligSaksbehandler("Saksbehandler Linda")
            .medBehandlendeEnhetId("82")
            .medSakspartId("12345678900")
            .medMottakerAdresse(opprettNorskAdresseMock())
            .build();

        Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(brevMetadata);

        assertThat(dokumentbestillingsinformasjon.getDokumenttypeId()).isEqualTo("000096");
        assertThat(dokumentbestillingsinformasjon.getJournalfoerendeEnhet()).isEqualTo(brevMetadata.getBehandlendeEnhetId());
        assertThat(((Person) dokumentbestillingsinformasjon.getBruker()).getIdent()).isEqualTo(brevMetadata.getSakspartId());
        assertThat(((Person) dokumentbestillingsinformasjon.getBruker()).getNavn()).isEqualTo(brevMetadata.getSakspartNavn());
        assertThat(((Person) dokumentbestillingsinformasjon.getMottaker()).getIdent()).isEqualTo(brevMetadata.getMottakerAdresse().getPersonIdent().getIdent());
        assertThat(((Person) dokumentbestillingsinformasjon.getMottaker()).getNavn()).isEqualTo(brevMetadata.getMottakerAdresse().getMottakerNavn());
        assertThat(dokumentbestillingsinformasjon.getSaksbehandlernavn()).isEqualTo(brevMetadata.getAnsvarligSaksbehandler());
        assertThat(dokumentbestillingsinformasjon.getJournalsakId()).isEqualTo(brevMetadata.getSaksnummer());
    }

    @Test
    public void skal_vente_på_vedlegg_når_det_er_valgt() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medMottakerAdresse(opprettNorskAdresseMock())
            .build();

        boolean ventPåVedlegg = true;
        Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(brevMetadata, ventPåVedlegg);

        assertThat(dokumentbestillingsinformasjon.isFerdigstillForsendelse()).isFalse();
        assertThat(dokumentbestillingsinformasjon.isInkludererEksterneVedlegg()).isTrue();
    }

    @Test
    public void skal_sette_utlandsk_adresse() {

        BrevMetadata metadata = new BrevMetadata.Builder()
            .medMottakerAdresse(opprettUtenlandskAdresseMock())
            .build();

        Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(metadata);

        UtenlandskPostadresse adresse = (UtenlandskPostadresse) dokumentbestillingsinformasjon.getAdresse();
        assertThat(adresse.getAdresselinje1()).isEqualTo(metadata.getMottakerAdresse().getAdresselinje1());
        assertThat(adresse.getAdresselinje2()).isEqualTo(metadata.getMottakerAdresse().getAdresselinje2());
        assertThat(adresse.getAdresselinje3()).isEqualTo(metadata.getMottakerAdresse().getAdresselinje3());
        assertThat(adresse.getLand().getValue()).isEqualTo("???");
    }

    @Test
    public void skal_sette_norsk_adresse() {

        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medMottakerAdresse(opprettNorskAdresseMock())
            .build();

        Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(brevMetadata);

        NorskPostadresse adresse = (NorskPostadresse) dokumentbestillingsinformasjon.getAdresse();
        assertThat(adresse.getAdresselinje1()).isEqualTo(brevMetadata.getMottakerAdresse().getAdresselinje1());
        assertThat(adresse.getAdresselinje2()).isEqualTo(brevMetadata.getMottakerAdresse().getAdresselinje2());
        assertThat(adresse.getAdresselinje3()).isEqualTo(brevMetadata.getMottakerAdresse().getAdresselinje3());
        assertThat(adresse.getLand().getValue()).isEqualTo("NO");
        assertThat(adresse.getPostnummer()).isEqualTo(brevMetadata.getMottakerAdresse().getPostNr());
        assertThat(adresse.getPoststed()).isEqualTo(brevMetadata.getMottakerAdresse().getPoststed());
    }

    private Adresseinfo opprettNorskAdresseMock() {
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

    public Adresseinfo opprettUtenlandskAdresseMock() {
        return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
            new PersonIdent("12345678901"),
            "Jens Trallala", null)
            .medAdresselinje1("utenlandsk adresselinje 1")
            .medAdresselinje2("utenlandsk adresselinje 2")
            .medAdresselinje3("utenlandsk adresselinje 3")
            .medLand("GE")
            .medPostNr("0898")
            .medPoststed("Berlin")
            .build();
    }

}
