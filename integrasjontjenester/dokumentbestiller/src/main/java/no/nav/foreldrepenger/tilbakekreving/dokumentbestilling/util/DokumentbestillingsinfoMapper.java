package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Adresse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Fagomraader;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.NorskPostadresse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.UtenlandskPostadresse;

public class DokumentbestillingsinfoMapper {

    private static final String UKJENT_ADRESSE = "Ukjent adresse";
    private static final String SVERIGE_LANDKODE_TPS = "SWE";
    private static final String SVERIGE_LANDKODE_DOKPRODINFO = "SE";
    private static final String NORGE_LANDKODE_DOKPRODINFO = "NO";
    private static final String UKJENT_LANDKODE_DOKPRODINFO = "???";

    private DokumentbestillingsinfoMapper(){
        // for static access
    }

    public static Dokumentbestillingsinformasjon opprettDokumentbestillingsinformasjon(BrevMetadata brevMetadata) {
        Dokumentbestillingsinformasjon dokumentinfo = new Dokumentbestillingsinformasjon();

        Fagsystemer fptilbakeFagsystem = new Fagsystemer();
        fptilbakeFagsystem.setKodeRef(Fagsystem.FPSAK.getOffisiellKode());
        fptilbakeFagsystem.setValue(Fagsystem.FPSAK.getOffisiellKode());
        dokumentinfo.setBestillendeFagsystem(fptilbakeFagsystem);
        dokumentinfo.setUstrukturertTittel(brevMetadata.getTittel());

        dokumentinfo.setAdresse(settAdresse(brevMetadata.getMottakerAdresse()));

        Person bruker = new Person();
        bruker.setIdent(brevMetadata.getSakspartId());
        bruker.setNavn(brevMetadata.getSakspartNavn());
        dokumentinfo.setBruker(bruker);

        dokumentinfo.setDokumenttypeId("000096");
        dokumentinfo.setFerdigstillForsendelse(true);
        dokumentinfo.setInkludererEksterneVedlegg(false);
        dokumentinfo.setJournalfoerendeEnhet(brevMetadata.getBehandlendeEnhetId());
        dokumentinfo.setSaksbehandlernavn(brevMetadata.getAnsvarligSaksbehandler());
        dokumentinfo.setJournalsakId(brevMetadata.getSaksnummer());

        Person mottaker = new Person();
        mottaker.setIdent(brevMetadata.getMottakerAdresse().getPersonIdent().getIdent());
        mottaker.setNavn(brevMetadata.getMottakerAdresse().getMottakerNavn());
        dokumentinfo.setMottaker(mottaker);

        Fagomraader dokumenttilhørendeFagområde = new Fagomraader();
        dokumenttilhørendeFagområde.setKodeRef("FOR"); //Egen for svangerskappenger??
        dokumenttilhørendeFagområde.setValue("FOR");
        dokumentinfo.setDokumenttilhoerendeFagomraade(dokumenttilhørendeFagområde);

        Fagsystemer gsak = new Fagsystemer();
        gsak.setKodeRef(Fagsystem.GOSYS.getOffisiellKode());
        gsak.setValue(Fagsystem.GOSYS.getOffisiellKode());
        dokumentinfo.setSakstilhoerendeFagsystem(gsak);

        return dokumentinfo;
    }

    private static Adresse settAdresse(Adresseinfo mottakerAdresse) {
        if (BrevUtil.erNorskAdresse(mottakerAdresse)) {
            return lagNorskAdresseForDokumentinfo(mottakerAdresse);
        } else {
            return lagUtenlandskAdresseForDokumentinfo(mottakerAdresse);
        }
    }

    private static UtenlandskPostadresse lagUtenlandskAdresseForDokumentinfo(Adresseinfo adresseinfo) {
        UtenlandskPostadresse adresse = new UtenlandskPostadresse();
        adresse.setAdresselinje1(adresseinfo.getAdresselinje1() == null ? UKJENT_ADRESSE : adresseinfo.getAdresselinje1());
        adresse.setAdresselinje2(adresseinfo.getAdresselinje2());
        adresse.setAdresselinje3(adresseinfo.getAdresselinje3());
        no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder land = new no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder();
        String landkode = finnLandkode(adresseinfo.getLand());
        land.setKodeRef(landkode);
        land.setValue(landkode);
        adresse.setLand(land);
        return adresse;
    }

    private static String finnLandkode(String landekodeFraTps) {
        if (SVERIGE_LANDKODE_TPS.equals(landekodeFraTps)){
            return SVERIGE_LANDKODE_DOKPRODINFO;
        } else {
            return UKJENT_LANDKODE_DOKPRODINFO;
        }
    }

    public static NorskPostadresse lagNorskAdresseForDokumentinfo(Adresseinfo adresseinfo) {
        NorskPostadresse adresse = new NorskPostadresse();
        adresse.setAdresselinje1(adresseinfo.getAdresselinje1());
        adresse.setAdresselinje2(adresseinfo.getAdresselinje2());
        adresse.setAdresselinje3(adresseinfo.getAdresselinje3());
        no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder landkode = new no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder();
        landkode.setKodeRef(NORGE_LANDKODE_DOKPRODINFO);
        landkode.setValue(NORGE_LANDKODE_DOKPRODINFO);
        adresse.setLand(landkode);
        adresse.setPostnummer(adresseinfo.getPostNr());
        adresse.setPoststed(adresseinfo.getPoststed());
        return adresse;
    }
}
