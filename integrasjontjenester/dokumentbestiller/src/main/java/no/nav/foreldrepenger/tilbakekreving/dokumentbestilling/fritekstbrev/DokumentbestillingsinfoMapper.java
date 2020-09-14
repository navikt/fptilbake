package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.AdresseUtil;
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

    private final String appName;

    private DokumentbestillingsinfoMapper(String appName) {
        this.appName = appName;
    }

    public static DokumentbestillingsinfoMapper opprett() {
        return new DokumentbestillingsinfoMapper(System.getProperty("application.name"));
    }

    public static DokumentbestillingsinfoMapper forK9() {
        return new DokumentbestillingsinfoMapper("k9-tilbake");
    }

    public static DokumentbestillingsinfoMapper forFp() {
        return new DokumentbestillingsinfoMapper("fptilbake");
    }

    public Dokumentbestillingsinformasjon opprettDokumentbestillingsinformasjon(BrevMetadata brevMetadata) {
        return opprettDokumentbestillingsinformasjon(brevMetadata, false);
    }

    public Dokumentbestillingsinformasjon opprettDokumentbestillingsinformasjon(BrevMetadata brevMetadata, boolean skalLeggeTilVedlegg) {
        Dokumentbestillingsinformasjon dokumentinfo = new Dokumentbestillingsinformasjon();

        dokumentinfo.setBestillendeFagsystem(utledJournalførendeFagsystem());
        dokumentinfo.setUstrukturertTittel(brevMetadata.getTittel());

        dokumentinfo.setAdresse(settAdresse(brevMetadata.getMottakerAdresse()));

        Person bruker = new Person();
        bruker.setIdent(brevMetadata.getSakspartId());
        bruker.setNavn(brevMetadata.getSakspartNavn());
        dokumentinfo.setBruker(bruker);
        dokumentinfo.setDokumenttypeId("000096");
        dokumentinfo.setFerdigstillForsendelse(!skalLeggeTilVedlegg);
        dokumentinfo.setInkludererEksterneVedlegg(skalLeggeTilVedlegg);
        dokumentinfo.setJournalfoerendeEnhet(brevMetadata.getBehandlendeEnhetId());
        dokumentinfo.setSaksbehandlernavn(brevMetadata.getAnsvarligSaksbehandler());
        dokumentinfo.setJournalsakId(brevMetadata.getSaksnummer());

        Person mottaker = new Person();
        mottaker.setIdent(brevMetadata.getMottakerAdresse().getPersonIdent().getIdent());
        mottaker.setNavn(brevMetadata.getMottakerAdresse().getMottakerNavn());
        dokumentinfo.setMottaker(mottaker);

        dokumentinfo.setDokumenttilhoerendeFagomraade(utledTilhørendeFagområde(brevMetadata.getFagsaktype()));
        dokumentinfo.setSakstilhoerendeFagsystem(utledFagsakEier());

        return dokumentinfo;
    }

    private static Fagomraader utledTilhørendeFagområde(FagsakYtelseType fagsaktype) {
        //se kodeverk-klienten, kodeverk = Tema
        switch (fagsaktype) {
            case ENGANGSTØNAD:
            case FORELDREPENGER:
            case SVANGERSKAPSPENGER: {
                Fagomraader f = new Fagomraader();
                f.setKodeRef("FOR");
                f.setValue("FOR");
                return f;
            }
            case PLEIEPENGER_SYKT_BARN:
            case PLEIEPENGER_NÆRSTÅENDE:
            case OMSORGSPENGER:
            case OPPLÆRINGSPENGER: {
                Fagomraader f = new Fagomraader();
                f.setKodeRef("OMS");
                f.setValue("OMS");
                return f;
            }
            case FRISINN: {
                Fagomraader f = new Fagomraader();
                f.setKodeRef("FRI");
                f.setValue("FRI");
                return f;
            }
            default:
                throw new IllegalArgumentException("Ikke-støttet fagsakytelsetype: " + fagsaktype);
        }
    }

    private Fagsystemer utledJournalførendeFagsystem() {
        switch (appName) {
            case "fptilbake": {
                Fagsystemer fpsak = new Fagsystemer();
                fpsak.setKodeRef(Fagsystem.FPSAK.getOffisiellKode());
                fpsak.setValue(Fagsystem.FPSAK.getOffisiellKode());
                return fpsak;
            }
            case "k9-tilbake": {
                Fagsystemer k9sak = new Fagsystemer();
                k9sak.setKodeRef(Fagsystem.K9SAK.getOffisiellKode());
                k9sak.setValue(Fagsystem.K9SAK.getOffisiellKode());
                return k9sak;
            }
            default:
                throw new IllegalArgumentException("Ikke-støttet application.name: " + appName);
        }
    }

    private Fagsystemer utledFagsakEier() {
        switch (appName) {
            case "fptilbake": {
                Fagsystemer gsak = new Fagsystemer();
                gsak.setKodeRef(Fagsystem.GOSYS.getOffisiellKode());
                gsak.setValue(Fagsystem.GOSYS.getOffisiellKode());
                return gsak;
            }
            case "k9-tilbake": {
                Fagsystemer k9sak = new Fagsystemer();
                k9sak.setKodeRef(Fagsystem.K9SAK.getOffisiellKode());
                k9sak.setValue(Fagsystem.K9SAK.getOffisiellKode());
                return k9sak;
            }
            default:
                throw new IllegalArgumentException("Ikke-støttet application.name: " + appName);
        }
    }

    private static Adresse settAdresse(Adresseinfo mottakerAdresse) {
        if (AdresseUtil.erNorskAdresse(mottakerAdresse)) {
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
        if (SVERIGE_LANDKODE_TPS.equals(landekodeFraTps)) {
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
