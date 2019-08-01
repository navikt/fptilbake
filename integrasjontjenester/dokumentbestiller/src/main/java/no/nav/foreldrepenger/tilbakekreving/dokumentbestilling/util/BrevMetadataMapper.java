package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.integrasjon.dokument.felles.AvsenderAdresseType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.IdKodeType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.KontaktInformasjonType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.MottakerAdresseType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.MottakerType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.SakspartType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.SignerendeSaksbehandlerType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.ObjectFactory;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.ReturadresseKonfigurasjon;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.util.FPDateUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class BrevMetadataMapper {

    private KodeverkRepository kodeverkRepository;

    @Inject
    public BrevMetadataMapper(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    public BrevMetadataMapper() {
    }

    public FellesType leggTilMetadataIDokumentet(BrevMetadata brevMetadata) {
        final FellesType fellesType = new FellesType();
        fellesType.setSpraakkode(BrevUtil.mapSpråkkode(brevMetadata.getSpråkkode()));
        fellesType.setFagsaksnummer(brevMetadata.getSaksnummer());
        fellesType.setAutomatiskBehandlet(false);

        SakspartType sakspartType = new SakspartType();
        sakspartType.setSakspartId(brevMetadata.getSakspartId());
        sakspartType.setSakspartNavn(brevMetadata.getSakspartNavn());
        sakspartType.setSakspartTypeKode(IdKodeType.PERSON);
        fellesType.setSakspart(sakspartType);

        //TODO (Trine) (PFP-6220): Hvis verge, må vi hente verges adresse istedet, og sette riktig mottakerId, navn etc.
        MottakerType mottakerType = new MottakerType();
        mottakerType.setMottakerTypeKode(IdKodeType.PERSON);
        mottakerType.setMottakerId(brevMetadata.getMottakerAdresse().getPersonIdent().getIdent());
        mottakerType.setMottakerNavn(brevMetadata.getMottakerAdresse().getMottakerNavn());
        mottakerType.setMottakerAdresse(settMottakeradresse(brevMetadata.getMottakerAdresse()));
        fellesType.setMottaker(mottakerType);

        SignerendeSaksbehandlerType signerendeSaksbehandler = new SignerendeSaksbehandlerType();
        signerendeSaksbehandler.setSignerendeSaksbehandlerNavn(brevMetadata.getAnsvarligSaksbehandler());
        fellesType.setSignerendeSaksbehandler(signerendeSaksbehandler);

        fellesType.setNavnAvsenderEnhet(brevMetadata.getBehandlendeEnhetNavn() != null ? brevMetadata.getBehandlendeEnhetNavn() : ReturadresseKonfigurasjon.getBrevReturadresseEnhetNavn());
        fellesType.setNummerAvsenderEnhet(brevMetadata.getBehandlendeEnhetId());

        fellesType.setKontaktInformasjon(hentKontaktinformasjon());

        fellesType.setDokumentDato(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(FPDateUtil.iDag()));

        return fellesType;
    }

    private MottakerAdresseType settMottakeradresse(Adresseinfo adresse) {
        if (BrevUtil.erNorskAdresse(adresse)) {
            return lagNorskAdresse(adresse);
        } else {
            return lagUtenlandskAdresse(adresse);
        }
    }

    public KontaktInformasjonType hentKontaktinformasjon() {
        KontaktInformasjonType kontaktInformasjonType = new KontaktInformasjonType();
        kontaktInformasjonType.setKontaktTelefonnummer(ReturadresseKonfigurasjon.getBrevTelefonnummerKlageEnhet());
        AvsenderAdresseType avsenderadresse = new AvsenderAdresseType();
        avsenderadresse.setAdresselinje(ReturadresseKonfigurasjon.getBrevReturadresseAdresselinje1());
        avsenderadresse.setNavEnhetsNavn(ReturadresseKonfigurasjon.getBrevReturadresseEnhetNavn());
        avsenderadresse.setPostNr(ReturadresseKonfigurasjon.getBrevReturadressePostnummer());
        avsenderadresse.setPoststed(ReturadresseKonfigurasjon.getBrevReturadressePoststed());
        kontaktInformasjonType.setPostadresse(avsenderadresse);
        kontaktInformasjonType.setReturadresse(avsenderadresse);
        return kontaktInformasjonType;
    }

    private MottakerAdresseType lagUtenlandskAdresse(Adresseinfo adresseinfo) {
        MottakerAdresseType adresse = new MottakerAdresseType();
        adresse.setAdresselinje1(adresseinfo.getAdresselinje1());
        adresse.setAdresselinje2(adresseinfo.getAdresselinje2());
        adresse.setAdresselinje3(adresseinfo.getAdresselinje3());
        Landkoder land = adresseinfo.getLand() == null ? kodeverkRepository.finn(Landkoder.class, Landkoder.NOR) :
            kodeverkRepository.finn(Landkoder.class, adresseinfo.getLand());
        adresse.setLand(BrevUtil.finnLandnavnPåSpråk(land.getKodelisteNavnI18NList(), Språkkode.nb));
        return adresse;
    }

    private MottakerAdresseType lagNorskAdresse(Adresseinfo adresseinfo) {
        MottakerAdresseType adresse = new MottakerAdresseType();
        adresse.setAdresselinje1(adresseinfo.getAdresselinje1());
        adresse.setAdresselinje2(adresseinfo.getAdresselinje2());
        adresse.setAdresselinje3(adresseinfo.getAdresselinje3());
        adresse.setLand("NORGE");
        adresse.setPostNr(adresseinfo.getPostNr());
        adresse.setPoststed(adresseinfo.getPoststed());
        return adresse;
    }

    public FagType settFritekstdelAvVarselbrev(String brødtekst, ObjectFactory objectFactory, String fagsaktypePåSpråk) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setHovedoverskrift(TittelOverskriftUtil.finnOverskriftVarselbrev(fagsaktypePåSpråk));
        fagType.setBrødtekst(brødtekst);
        return fagType;
    }

    public FagType settFritekstdelAvVedtaksbrev(String brødtekst, ObjectFactory objectFactory, String fagsaktypePåSpråk) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setHovedoverskrift(TittelOverskriftUtil.finnOverskriftVedtaksbrev(fagsaktypePåSpråk));
        fagType.setBrødtekst(brødtekst);
        return fagType;
    }
}
