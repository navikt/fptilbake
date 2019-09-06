package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevMetadataMapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.DokumentbestillingsinfoMapper;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErRedigerbart;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.ProduserIkkeredigerbartDokumentDokumentErVedlegg;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserIkkeredigerbartDokumentResponse;
import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.jpa.Transaction;

@ApplicationScoped
@Transaction
public class BestillDokumentTjeneste {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final String DOKUMENTTYPEID_FRITEKST = "000096";
    private static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";
    private static final String TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG = "Vedtaksbrev Tilbakekreving";

    private DokumentproduksjonConsumer dokumentproduksjonConsumer;
    private ObjectFactory objectFactory;
    private BrevMetadataMapper brevMetadataMapper;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private BrevdataRepository brevdataRepository;

    public BestillDokumentTjeneste() {
    }

    @Inject
    public BestillDokumentTjeneste(DokumentproduksjonConsumer dokumentproduksjonConsumer,
                                   BrevMetadataMapper brevMetadataMapper,
                                   HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                   BrevdataRepository brevdataRepository) {
        this.dokumentproduksjonConsumer = dokumentproduksjonConsumer;
        this.brevMetadataMapper = brevMetadataMapper;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.brevdataRepository = brevdataRepository;
        this.objectFactory = new ObjectFactory();
    }

    public JournalpostIdOgDokumentId sendFritekstbrev(FritekstbrevData data) {
        Element ferdigXml = lagXmlDokument(data);

        Dokumentbestillingsinformasjon dokumentbestillingsinfo = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(data.getBrevMetadata());
        ProduserIkkeredigerbartDokumentResponse bestillingSvar = sendBrevbestilling(ferdigXml, dokumentbestillingsinfo);

        String journalpostId = bestillingSvar.getJournalpostId();
        String dokumentId = bestillingSvar.getDokumentId();
        return new JournalpostIdOgDokumentId(new JournalpostId(journalpostId), dokumentId);
    }

    public byte[] hentForhåndsvisningFritekstbrev(FritekstbrevData data) {
        Element ferdigXml = lagXmlDokument(data);
        return sendForhåndsvisningRequest(ferdigXml);
    }

    private ProduserIkkeredigerbartDokumentResponse sendBrevbestilling(Element ferdigXml, Dokumentbestillingsinformasjon dokumentbestillingsinformasjon) {
        ProduserIkkeredigerbartDokumentRequest produserIkkeredigerbartDokumentRequest = new ProduserIkkeredigerbartDokumentRequest();
        produserIkkeredigerbartDokumentRequest.setBrevdata(ferdigXml);
        produserIkkeredigerbartDokumentRequest.setDokumentbestillingsinformasjon(dokumentbestillingsinformasjon);

        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = null;
        try {
            produserIkkeredigerbartDokumentResponse = dokumentproduksjonConsumer.produserIkkeredigerbartDokument(produserIkkeredigerbartDokumentRequest);
        } catch (ProduserIkkeredigerbartDokumentDokumentErRedigerbart | ProduserIkkeredigerbartDokumentDokumentErVedlegg funksjonellFeil) {
            throw DokumentbestillingFeil.FACTORY.feilFraDokumentProduksjon(funksjonellFeil).toException();
        }
        return produserIkkeredigerbartDokumentResponse;
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
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(heleXml));
            Document doc = db.parse(is);
            return doc.getDocumentElement();
        } catch (JAXBException | SAXException | ParserConfigurationException | IOException e) {
            throw DokumentbestillingFeil.FACTORY.feiletVedKonverteringTilXml(e).toException();
        }
    }

    public FagType lagInnholdsdelFraOverskriftOgFritekst(String overskrift, String brevtekst) {
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
        return BrevUtil.fjernNamespaceFra(brevXmlMedNamespace);
    }

}
