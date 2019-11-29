package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev;

import java.io.IOException;
import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.FritekstbrevConstants;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.ObjectFactory;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMetadataMapper;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseDokumentTillatesIkkeGjenbrukt;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseEksterntVedleggIkkeTillatt;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseJournalpostIkkeFerdigstilt;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.KnyttVedleggTilForsendelseUlikeFagomraader;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErRedigerbart;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErVedlegg;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.FerdigstillForsendelseRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.KnyttVedleggTilForsendelseRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentResponse;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

@ApplicationScoped
public class FritekstbrevTjeneste {

    private static final String DOKUMENTTYPEID_FRITEKST = "000096";
    private static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private DokumentproduksjonConsumer dokumentproduksjonConsumer;
    private ObjectFactory objectFactory;
    private BrevMetadataMapper brevMetadataMapper;

    public FritekstbrevTjeneste() {
    }

    @Inject
    public FritekstbrevTjeneste(DokumentproduksjonConsumer dokumentproduksjonConsumer, BrevMetadataMapper brevMetadataMapper) {
        this.dokumentproduksjonConsumer = dokumentproduksjonConsumer;
        this.brevMetadataMapper = brevMetadataMapper;
        this.objectFactory = new ObjectFactory();
    }

    public JournalpostIdOgDokumentId sendFritekstbrev(FritekstbrevData data) {
        boolean skalLeggeTilVedlegg = false;
        return sendEllerKlargjørFritekstbrev(data, skalLeggeTilVedlegg);
    }

    public JournalpostIdOgDokumentId sendFritekstbrev(FritekstbrevData data, JournalpostIdOgDokumentId vedlegg) {
        boolean skalLeggeTilVedlegg = true;
        JournalpostIdOgDokumentId fritekstbrevReferanse = sendEllerKlargjørFritekstbrev(data, skalLeggeTilVedlegg);
        knyttVedleggTilForsendelse(fritekstbrevReferanse.getJournalpostId(), vedlegg);
        ferdigstillForsendelse(fritekstbrevReferanse.getJournalpostId());
        return fritekstbrevReferanse;
    }

    private JournalpostIdOgDokumentId sendEllerKlargjørFritekstbrev(FritekstbrevData data, boolean skalLeggeTilVedlegg) {
        Element ferdigXml = lagXmlDokument(data);

        Dokumentbestillingsinformasjon dokumentbestillingsinfo = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(data.getBrevMetadata(), skalLeggeTilVedlegg);
        ProduserIkkeredigerbartDokumentResponse bestillingSvar = sendBrevbestilling(ferdigXml, dokumentbestillingsinfo);

        String journalpostId = bestillingSvar.getJournalpostId();
        String dokumentId = bestillingSvar.getDokumentId();
        return new JournalpostIdOgDokumentId(new JournalpostId(journalpostId), dokumentId);
    }

    public void knyttVedleggTilForsendelse(JournalpostId journalpost, JournalpostIdOgDokumentId vedlegg) {
        KnyttVedleggTilForsendelseRequest request = new KnyttVedleggTilForsendelseRequest();
        try {
            dokumentproduksjonConsumer.knyttVedleggTilForsendelse(request);
        } catch (KnyttVedleggTilForsendelseDokumentIkkeFunnet | KnyttVedleggTilForsendelseEksterntVedleggIkkeTillatt | KnyttVedleggTilForsendelseJournalpostIkkeFunnet | KnyttVedleggTilForsendelseJournalpostIkkeUnderArbeid | KnyttVedleggTilForsendelseDokumentTillatesIkkeGjenbrukt | KnyttVedleggTilForsendelseUlikeFagomraader | KnyttVedleggTilForsendelseJournalpostIkkeFerdigstilt e) {
            throw FritekstbrevFeil.FACTORY.feilVedTilknytningAvVedlegg(vedlegg, journalpost, e).toException();
        }
    }

    private void ferdigstillForsendelse(JournalpostId journalpostId) {
        FerdigstillForsendelseRequest request = new FerdigstillForsendelseRequest();
        request.setJournalpostId(journalpostId.getVerdi());
        request.setEndretAvNavn("VL");
        try {
            dokumentproduksjonConsumer.ferdigstillForsendelse(request);
        } catch (Exception e) {
            throw FritekstbrevFeil.FACTORY.feilVedFerdigstillelse(journalpostId, e).toException();
        }
    }

    public byte[] hentForhåndsvisningFritekstbrev(FritekstbrevData data) {
        Element ferdigXml = lagXmlDokument(data);
        return sendForhåndsvisningRequest(ferdigXml);
    }

    private ProduserIkkeredigerbartDokumentResponse sendBrevbestilling(Element ferdigXml, Dokumentbestillingsinformasjon dokumentbestillingsinformasjon) {
        ProduserIkkeredigerbartDokumentRequest produserIkkeredigerbartDokumentRequest = new ProduserIkkeredigerbartDokumentRequest();
        produserIkkeredigerbartDokumentRequest.setBrevdata(ferdigXml);
        produserIkkeredigerbartDokumentRequest.setDokumentbestillingsinformasjon(dokumentbestillingsinformasjon);

        try {
            return dokumentproduksjonConsumer.produserIkkeredigerbartDokument(produserIkkeredigerbartDokumentRequest);
        } catch (ProduserIkkeredigerbartDokumentDokumentErRedigerbart | ProduserIkkeredigerbartDokumentDokumentErVedlegg funksjonellFeil) {
            throw FritekstbrevFeil.FACTORY.feilFraDokumentProduksjon(funksjonellFeil).toException();
        }
    }

    private byte[] sendForhåndsvisningRequest(Element ferdigXml) {
        ProduserDokumentutkastRequest produserDokumentutkastRequest = new ProduserDokumentutkastRequest();
        produserDokumentutkastRequest.setDokumenttypeId(DOKUMENTTYPEID_FRITEKST);
        produserDokumentutkastRequest.setBrevdata(ferdigXml);

        byte[] dokument = null;
        ProduserDokumentutkastResponse produserDokumentutkastResponse = dokumentproduksjonConsumer.produserDokumentutkast(produserDokumentutkastRequest);
        if (produserDokumentutkastResponse != null && produserDokumentutkastResponse.getDokumentutkast() != null) {
            dokument = produserDokumentutkastResponse.getDokumentutkast();
        }
        return dokument;
    }

    private Element lagXmlDokument(FritekstbrevData data) {
        FagType fritekstXml = lagInnholdsdelFraOverskriftOgFritekst(data.getOverskrift(), data.getBrevtekst());
        FellesType fellesXml = brevMetadataMapper.leggTilMetadataIDokumentet(data.getBrevMetadata());
        return lagXml(fritekstXml, fellesXml);
    }

    private Element lagXml(FagType fritekstXml, FellesType fellesXml) {
        try {
            String heleXml = parseDTOtilXmlString(sammenstillFellesXmlOgFritekst(fritekstXml, fellesXml));
            DOCUMENT_BUILDER_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DOCUMENT_BUILDER_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(heleXml));
            Document doc = db.parse(is);
            return doc.getDocumentElement();
        } catch (JAXBException | SAXException | ParserConfigurationException | IOException e) {
            throw FritekstbrevFeil.FACTORY.feiletVedKonverteringTilXml(e).toException();
        }
    }

    private FagType lagInnholdsdelFraOverskriftOgFritekst(String overskrift, String brevtekst) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setHovedoverskrift(overskrift);
        fagType.setBrødtekst(brevtekst);
        return fagType;
    }

    private JAXBElement<BrevdataType> sammenstillFellesXmlOgFritekst(FagType fagType, FellesType fellesType) {
        BrevdataType brevdataType = objectFactory.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return objectFactory.createBrevdata(brevdataType);
    }

    private String parseDTOtilXmlString(JAXBElement<BrevdataType> komplettXml) throws JAXBException, SAXException {
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(FritekstbrevConstants.JAXB_CLASS, komplettXml, FritekstbrevConstants.XSD_LOCATION);
        return FritekstbrevXmlUtil.fjernNamespace(brevXmlMedNamespace);
    }

}
