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

        Assertions.assertThat(dokumentbestillingsinformasjon.getDokumenttypeId()).isEqualTo("000096");
        Assertions.assertThat(dokumentbestillingsinformasjon.getJournalfoerendeEnhet()).isEqualTo(brevMetadata.getBehandlendeEnhetId());
        Assertions.assertThat(((Person) dokumentbestillingsinformasjon.getBruker()).getIdent()).isEqualTo(brevMetadata.getSakspartId());
        Assertions.assertThat(((Person) dokumentbestillingsinformasjon.getBruker()).getNavn()).isEqualTo(brevMetadata.getSakspartNavn());
        Assertions.assertThat(((Person) dokumentbestillingsinformasjon.getMottaker()).getIdent()).isEqualTo(brevMetadata.getMottakerAdresse().getPersonIdent().getIdent());
        Assertions.assertThat(((Person) dokumentbestillingsinformasjon.getMottaker()).getNavn()).isEqualTo(brevMetadata.getMottakerAdresse().getMottakerNavn());
        Assertions.assertThat(dokumentbestillingsinformasjon.getSaksbehandlernavn()).isEqualTo(brevMetadata.getAnsvarligSaksbehandler());
        Assertions.assertThat(dokumentbestillingsinformasjon.getJournalsakId()).isEqualTo(brevMetadata.getSaksnummer());
    }

    @Test
    public void skal_sette_utlandsk_adresse() {

        BrevMetadata metadata = new BrevMetadata.Builder()
                .medMottakerAdresse(opprettUtenlandskAdresseMock())
                .build();

        Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(metadata);

        UtenlandskPostadresse adresse = (UtenlandskPostadresse) dokumentbestillingsinformasjon.getAdresse();
        Assertions.assertThat(adresse.getAdresselinje1()).isEqualTo(metadata.getMottakerAdresse().getAdresselinje1());
        Assertions.assertThat(adresse.getAdresselinje2()).isEqualTo(metadata.getMottakerAdresse().getAdresselinje2());
        Assertions.assertThat(adresse.getAdresselinje3()).isEqualTo(metadata.getMottakerAdresse().getAdresselinje3());
        Assertions.assertThat(adresse.getLand().getValue()).isEqualTo("???");
    }

    @Test
    public void skal_sette_norsk_adresse() {

        BrevMetadata brevMetadata = new BrevMetadata.Builder()
                .medMottakerAdresse(opprettNorskAdresseMock())
                .build();

        Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(brevMetadata);

        NorskPostadresse adresse = (NorskPostadresse) dokumentbestillingsinformasjon.getAdresse();
        Assertions.assertThat(adresse.getAdresselinje1()).isEqualTo(brevMetadata.getMottakerAdresse().getAdresselinje1());
        Assertions.assertThat(adresse.getAdresselinje2()).isEqualTo(brevMetadata.getMottakerAdresse().getAdresselinje2());
        Assertions.assertThat(adresse.getAdresselinje3()).isEqualTo(brevMetadata.getMottakerAdresse().getAdresselinje3());
        Assertions.assertThat(adresse.getLand().getValue()).isEqualTo("NO");
        Assertions.assertThat(adresse.getPostnummer()).isEqualTo(brevMetadata.getMottakerAdresse().getPostNr());
        Assertions.assertThat(adresse.getPoststed()).isEqualTo(brevMetadata.getMottakerAdresse().getPoststed());
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
