package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevMetadataMapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.DokumentbestillingsinfoMapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.TittelOverskriftUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
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
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;
    private VarselbrevTjeneste varselbrevTjeneste;
    private ObjectFactory objectFactory;
    private BrevMetadataMapper brevMetadataMapper;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private BrevdataRepository brevdataRepository;

    public BestillDokumentTjeneste() {
    }

    @Inject
    public BestillDokumentTjeneste(DokumentproduksjonConsumer dokumentproduksjonConsumer,
                                   VedtaksbrevTjeneste vedtaksbrevTjeneste,
                                   VarselbrevTjeneste varselbrevTjeneste,
                                   BrevMetadataMapper brevMetadataMapper,
                                   HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                   BrevdataRepository brevdataRepository) {
        this.dokumentproduksjonConsumer = dokumentproduksjonConsumer;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
        this.varselbrevTjeneste = varselbrevTjeneste;
        this.brevMetadataMapper = brevMetadataMapper;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.brevdataRepository = brevdataRepository;
        this.objectFactory = new ObjectFactory();
    }

    public void sendVarselbrev(Long fagsakId, String aktørId, Long behandlingId) {
        VarselbrevSamletInfo varselbrevSamletInfo = varselbrevTjeneste.lagVarselbrevForSending(behandlingId);
        Element ferdigXml = lagXmlDokumentVarselbrev(varselbrevSamletInfo);

        Dokumentbestillingsinformasjon dokumentbestillingsinfo = DokumentbestillingsinfoMapper
            .opprettDokumentbestillingsinformasjon(varselbrevSamletInfo.getBrevMetadata());
        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = sendBrevbestilling(ferdigXml, dokumentbestillingsinfo);

        String journalpostId = produserIkkeredigerbartDokumentResponse.getJournalpostId();
        String dokumentId = produserIkkeredigerbartDokumentResponse.getDokumentId();

        opprettHistorikkinnslag(behandlingId, journalpostId, dokumentId, fagsakId, aktørId, TITTEL_VARSELBREV_HISTORIKKINNSLAG);
        lagreInfoOmVarselbrev(behandlingId, journalpostId, dokumentId);
    }

    public void sendVedtaksbrev(Long fagsakId, String aktørId, Long behandlingId) {
        VedtaksbrevData vedtaksbrevData = vedtaksbrevTjeneste.hentDataForVedtaksbrev(behandlingId);

        Element ferdigXml = lagXmlDokumentVedtaksbrev(vedtaksbrevData);

        Dokumentbestillingsinformasjon dokumentbestillingsinfo = DokumentbestillingsinfoMapper.opprettDokumentbestillingsinformasjon(vedtaksbrevData.getMetadata());
        ProduserIkkeredigerbartDokumentResponse produserIkkeredigerbartDokumentResponse = sendBrevbestilling(ferdigXml, dokumentbestillingsinfo);

        String journalpostId = produserIkkeredigerbartDokumentResponse.getJournalpostId();
        String dokumentId = produserIkkeredigerbartDokumentResponse.getDokumentId();

        opprettHistorikkinnslag(behandlingId, journalpostId, dokumentId, fagsakId, aktørId, TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG);
        lagreInfoOmVedtaksbrev(behandlingId, journalpostId, dokumentId);
    }

    public byte[] hentForhåndsvisningVedtaksbrevSomPdf(HentForhåndvisningVedtaksbrevPdfDto dto) {
        VedtaksbrevData vedtaksbrevData = vedtaksbrevTjeneste.hentDataForVedtaksbrev(dto.getBehandlingId(), dto.getOppsummeringstekst(), dto.getPerioderMedTekst());

        Element ferdigXml = lagXmlDokumentVedtaksbrev(vedtaksbrevData);
        return sendForhåndsvisningRequest(ferdigXml);
    }

    public List<Avsnitt> hentForhåndsvisningVedtaksbrevSomTekst(Long behandlingId) {
        VedtaksbrevData vedtaksbrevData = vedtaksbrevTjeneste.hentDataForVedtaksbrev(behandlingId);
        String hovedoverskrift = TittelOverskriftUtil.finnOverskriftVedtaksbrev(vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk());
        return TekstformattererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(vedtaksbrevData.getVedtaksbrevData(), hovedoverskrift);
    }

    public byte[] hentForhåndsvisningVarselbrev(HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) {
        VarselbrevSamletInfo varselbrevSamletInfo = varselbrevTjeneste.lagVarselbrevForForhåndsvisning(
            hentForhåndsvisningVarselbrevDto.getBehandlingUuid(),
            new Saksnummer(hentForhåndsvisningVarselbrevDto.getSaksnummer()),
            hentForhåndsvisningVarselbrevDto.getVarseltekst(),
            hentForhåndsvisningVarselbrevDto.getFagsakYtelseType());

        Element ferdigXml = lagXmlDokumentVarselbrev(varselbrevSamletInfo);
        return sendForhåndsvisningRequest(ferdigXml);
    }

    private void opprettHistorikkinnslag(Long behandlingId, String journalpostId, String dokumentId, Long fagsakId, String aktørId, String tittel) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            new JournalpostId(journalpostId),
            dokumentId,
            fagsakId,
            behandlingId,
            new AktørId(aktørId),
            tittel);
    }

    private void lagreInfoOmVedtaksbrev(Long behandlingId, String journalpostId, String dokumentId) {
        VedtaksbrevSporing vedtaksbrevSporing = new VedtaksbrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentId)
            .medJournalpostId(journalpostId)
            .build();
        brevdataRepository.lagreVedtaksbrevData(vedtaksbrevSporing);
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, String journalpostId, String dokumentId) {
        VarselbrevSporing varselbrevSporing = new VarselbrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentId)
            .medJournalpostId(journalpostId)
            .build();
        brevdataRepository.lagreVarselbrevData(varselbrevSporing);
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

    private Element lagXmlDokumentVarselbrev(VarselbrevSamletInfo varselbrevSamletInfo) {
        FagType fritekstXml = lagFritekstXmlVarselbrev(varselbrevSamletInfo);
        FellesType fellesXml = brevMetadataMapper.leggTilMetadataIDokumentet(varselbrevSamletInfo.getBrevMetadata());
        return lagXml(fritekstXml, fellesXml);
    }

    private Element lagXmlDokumentVedtaksbrev(VedtaksbrevData vedtaksbrevData) {
        FagType fritekstXml = lagFritekstXmlVedtaksbrev(vedtaksbrevData);
        FellesType fellesXml = brevMetadataMapper.leggTilMetadataIDokumentet(vedtaksbrevData.getMetadata());
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

    private FagType lagFritekstXmlVedtaksbrev(VedtaksbrevData vedtaksbrevData) {
        String brødtekst = TekstformattererVedtaksbrev.lagVedtaksbrevFritekst(vedtaksbrevData.getVedtaksbrevData());
        return brevMetadataMapper.settFritekstdelAvVedtaksbrev(
            brødtekst,
            objectFactory,
            vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk());
    }

    private FagType lagFritekstXmlVarselbrev(VarselbrevSamletInfo varselbrevSamletInfo) {
        String brødtekst = TekstformattererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        return brevMetadataMapper.settFritekstdelAvVarselbrev(
            brødtekst,
            objectFactory,
            varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
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

    public void sendVedtaksbrevTest(HentForhåndvisningVedtaksbrevPdfDto vedtaksbrevPdfDto) {
        VedtaksbrevData vedtaksbrevData = vedtaksbrevTjeneste.hentDataForVedtaksbrev(
            vedtaksbrevPdfDto.getBehandlingId(),
            vedtaksbrevPdfDto.getOppsummeringstekst(),
            vedtaksbrevPdfDto.getPerioderMedTekst());

        Element ferdigXml = lagXmlDokumentVedtaksbrev(vedtaksbrevData);

        Dokumentbestillingsinformasjon dokumentbestillingsinfo = DokumentbestillingsinfoMapper
            .opprettDokumentbestillingsinformasjon(vedtaksbrevData.getMetadata());
        sendBrevbestilling(ferdigXml, dokumentbestillingsinfo);
    }
}
