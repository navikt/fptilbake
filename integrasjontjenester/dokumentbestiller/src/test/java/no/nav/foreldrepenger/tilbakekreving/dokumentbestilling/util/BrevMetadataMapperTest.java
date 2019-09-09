package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.integrasjon.dokument.felles.AvsenderAdresseType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.IdKodeType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.KontaktInformasjonType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.MottakerAdresseType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.MottakerType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.SakspartType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.SignerendeSaksbehandlerType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMetadataMapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

public class BrevMetadataMapperTest {

    private KodeverkRepository kodeverkRepositoryMock = Mockito.mock(KodeverkRepository.class);
    private BrevMetadataMapper brevMetadataMapper = new BrevMetadataMapper(kodeverkRepositoryMock);

    @Test
    public void skal_mappe_behandlingsinfo_fra_fpsak_til_xmlen() {

        BrevMetadata brevMetadata = new BrevMetadata.Builder()
                .medMottakerAdresse(lagStandardNorskAdresse())
                .medSakspartId("12345678900")
                .medSakspartNavn("Ola Nordmann")
                .medSaksnummer("saksnummer 665544")
                .medBehandlendeEnhetId("behandlendeenhetId 556677")
                .medBehandlendeEnhetNavn("behandlende enhet i Bærum")
                .medAnsvarligSaksbehandler("Beslutter Harald")
                .medSprakkode(Språkkode.nn)
                .build();

        //resultat av mapping:
        FellesType fellesTypeResultat = brevMetadataMapper.leggTilMetadataIDokumentet(brevMetadata);
        Assertions.assertThat(fellesTypeResultat.getFagsaksnummer()).isEqualTo("saksnummer 665544");
        Assertions.assertThat(fellesTypeResultat.getNavnAvsenderEnhet()).isEqualTo("behandlende enhet i Bærum");
        Assertions.assertThat(fellesTypeResultat.getNummerAvsenderEnhet()).isEqualTo("behandlendeenhetId 556677");
        Assertions.assertThat(fellesTypeResultat.getSpraakkode().value()).isEqualTo("NN");
        Assertions.assertThat(fellesTypeResultat.isAutomatiskBehandlet()).isFalse();

        MottakerType mottaker = fellesTypeResultat.getMottaker();
        Assertions.assertThat(mottaker.getMottakerId()).isEqualTo("12345678901");
        Assertions.assertThat(mottaker.getMottakerNavn()).isEqualTo("Jens Trallala");

        IdKodeType mottakerTypeKode = mottaker.getMottakerTypeKode();
        Assertions.assertThat(mottakerTypeKode.value()).isEqualTo("PERSON");
        MottakerAdresseType mottakerAdresse = mottaker.getMottakerAdresse();
        Assertions.assertThat(mottakerAdresse.getAdresselinje1()).isEqualTo("adresselinje 1");
        Assertions.assertThat(mottakerAdresse.getAdresselinje2()).isEqualTo("adresselinje 2");
        Assertions.assertThat(mottakerAdresse.getAdresselinje3()).isEqualTo("adresselinje 3");
        Assertions.assertThat(mottakerAdresse.getLand()).isEqualTo("NORGE");
        Assertions.assertThat(mottakerAdresse.getPostNr()).isEqualTo("0688");
        Assertions.assertThat(mottakerAdresse.getPoststed()).isEqualTo("OSLO");

        SakspartType sakspartType = fellesTypeResultat.getSakspart();
        Assertions.assertThat(sakspartType.getSakspartId()).isEqualTo("12345678900");
        Assertions.assertThat(sakspartType.getSakspartNavn()).isEqualTo("Ola Nordmann");
        Assertions.assertThat(sakspartType.getSakspartTypeKode().value()).isEqualTo("PERSON");

        SignerendeSaksbehandlerType signerendeSaksbehandler = fellesTypeResultat.getSignerendeSaksbehandler();
        Assertions.assertThat(signerendeSaksbehandler.getSignerendeSaksbehandlerNavn()).isEqualTo("Beslutter Harald");
    }

    @Test
    public void skal_sette_utenlandsk_adresse() {

        Landkoder landkoder = new Landkoder();
        KodelisteNavnI18N kodelisteNavnI18N = new KodelisteNavnI18N();
        kodelisteNavnI18N.setNavn("SVERIGE");
        kodelisteNavnI18N.setSpråk("NB");
        landkoder.setKodelisteNavnI18NList(List.of(kodelisteNavnI18N));

        Mockito.when(kodeverkRepositoryMock.finn(Mockito.any(), Mockito.anyString())).thenReturn(landkoder);

        Adresseinfo adresseinfo = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE,
                new PersonIdent("12345678900"),
                "Jens Trallala", null)
                .medAdresselinje1("utenlandsk adresselinje 1")
                .medAdresselinje2("utenlandsk adresselinje 2")
                .medAdresselinje3("utenlandsk adresselinje 3")
                .medLand("SWE")
                .medPostNr("0898")
                .medPoststed("Berlin")
                .build();

        BrevMetadata metadata = new BrevMetadata.Builder()
                .medMottakerAdresse(adresseinfo)
                .medSprakkode(Språkkode.nn)
                .build();

        FellesType fellesTypeResultat = brevMetadataMapper.leggTilMetadataIDokumentet(metadata);
        MottakerAdresseType mottakerAdresse = fellesTypeResultat.getMottaker().getMottakerAdresse();
        Assertions.assertThat(mottakerAdresse.getAdresselinje1()).isEqualTo("utenlandsk adresselinje 1");
        Assertions.assertThat(mottakerAdresse.getAdresselinje2()).isEqualTo("utenlandsk adresselinje 2");
        Assertions.assertThat(mottakerAdresse.getAdresselinje3()).isEqualTo("utenlandsk adresselinje 3");
        Assertions.assertThat(mottakerAdresse.getLand()).isEqualTo("SVERIGE");
        Assertions.assertThat(mottakerAdresse.getPostNr()).isNull();
        Assertions.assertThat(mottakerAdresse.getPoststed()).isNull();
    }

    @Test
    public void skal_sette_returadresseinfo_fra_konfig_klasse() {
        KontaktInformasjonType kontaktInformasjon = brevMetadataMapper.hentKontaktinformasjon();
        Assertions.assertThat(kontaktInformasjon.getKontaktTelefonnummer()).isEqualTo("55 55 33 33");

        AvsenderAdresseType postadresse = kontaktInformasjon.getPostadresse();
        Assertions.assertThat(postadresse.getAdresselinje()).isEqualTo("Postboks 6600 Etterstad");
        Assertions.assertThat(postadresse.getNavEnhetsNavn()).isEqualTo("NAV Familie- og pensjonsytelser");
        Assertions.assertThat(postadresse.getPostNr()).isEqualTo("0607");
        Assertions.assertThat(postadresse.getPoststed()).isEqualTo("OSLO");

        AvsenderAdresseType returadresse = kontaktInformasjon.getReturadresse();
        Assertions.assertThat(returadresse.getPoststed()).isEqualTo("OSLO");
        Assertions.assertThat(returadresse.getPostNr()).isEqualTo("0607");
        Assertions.assertThat(returadresse.getNavEnhetsNavn()).isEqualTo("NAV Familie- og pensjonsytelser");
        Assertions.assertThat(returadresse.getAdresselinje()).isEqualTo("Postboks 6600 Etterstad");
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
