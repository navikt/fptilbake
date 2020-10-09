package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningFritekstVedtaksbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;

@ApplicationScoped
public class FritekstVedtaksbrevTjeneste {

    private static final String TITTEL_FRITEKST_VEDTAK_TILBAKEBETALING = "Fritekst Vedtak tilbakebetaling ";
    private static final String TITTEL_FRITEKST_VEDTAK_INGEN_TILBAKEBETALING = "Fritekst Vedtak ingen tilbakebetaling ";

    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private PdfBrevTjeneste pdfBrevTjeneste;

    FritekstVedtaksbrevTjeneste(){
        // for CDI
    }

    @Inject
    public FritekstVedtaksbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                       EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                       TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                                       FritekstbrevTjeneste bestillDokumentTjeneste,
                                       PdfBrevTjeneste pdfBrevTjeneste){
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public byte[] hentForhåndsvisningFritekstVedtaksbrev(HentForhåndsvisningFritekstVedtaksbrevDto dto) {
        Behandling behandling = behandlingRepository.hentBehandling(dto.getBehandlingUuid());
        long behandlingId = behandling.getId();
        String fritekst = dto.getFritekst();
        SamletEksternBehandlingInfo eksternBehandlingInfo = hentDataFraFagsystem(behandling);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
        BeregningResultat beregnetResultat = tilbakekrevingBeregningTjeneste.beregn(behandlingId);
        VedtakResultatType vedtakResultatType = beregnetResultat.getVedtakResultatType();
        BrevMottaker brevMottaker = getBrevMottaker(behandlingId);

        BrevMetadata brevMetadata = lagMetadataForVedtaksbrev(behandling,vedtakResultatType,eksternBehandlingInfo, personinfo, brevMottaker);
        /*VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId, dto.getOppsummeringstekst(),
            dto.getPerioderMedTekst(), getBrevMottaker(behandlingId));
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode()))
            .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(hbVedtaksbrevData))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        if (!BrevToggle.brukDokprod()) {
            return pdfBrevTjeneste.genererForhåndsvisning(BrevData.builder()
                .setMottaker(getBrevMottaker(behandlingId))
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .setVedleggHtml(TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(vedtaksbrevData.getVedtaksbrevData()))
                .build());
        } else {
            byte[] vedtaksbrevPdf = bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
            byte[] vedlegg = lagVedtaksbrevVedleggTabellPdf(vedtaksbrevData, DokumentVariant.UTKAST);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PDFMergerUtility mergerUtil = new PDFMergerUtility();
            mergerUtil.setDestinationStream(baos);
            mergerUtil.addSource(new ByteArrayInputStream(vedtaksbrevPdf));
            mergerUtil.addSource(new ByteArrayInputStream(vedlegg));
            try {
                mergerUtil.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            } catch (IOException e) {
                throw new RuntimeException("Fikk IO exception ved forhåndsvisning inkl vedlegg", e);
            }
            return baos.toByteArray();
        }*/
        return null;
    }

    private SamletEksternBehandlingInfo hentDataFraFagsystem(Behandling behandling) {
        UUID fagsystemBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandling.getId()).getEksternUuid();
        return eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fagsystemBehandlingUuid,ønsketTillegsinformasjon(behandling));
    }

    private Tillegsinformasjon[] ønsketTillegsinformasjon(Behandling behandling) {
        List<Tillegsinformasjon> tillegsinformasjons = new ArrayList<>();
        tillegsinformasjons.add(Tillegsinformasjon.PERSONOPPLYSNINGER);

        // Kan ikke hente søknadsinformasjon for FRISINN-behandlinger. Er ikke nødvendigvis en 1-til-1-mapping mellom behandling
        // og søknad for FRISINN i k9-sak. Kan risikere exception i k9-sak og/eller exception i k9-tilbake
        if (!FagsakYtelseType.FRISINN.equals(behandling.getFagsak().getFagsakYtelseType())) {
            tillegsinformasjons.add(Tillegsinformasjon.SØKNAD);
        }
        return tillegsinformasjons.toArray(Tillegsinformasjon[]::new);
    }

    BrevMetadata lagMetadataForVedtaksbrev(Behandling behandling, VedtakResultatType vedtakResultatType,
                                           SamletEksternBehandlingInfo eksternBehandlingsinfo, Personinfo personinfo,
                                           BrevMottaker brevMottaker) {
        FagsakYtelseType fagsakType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = eksternBehandlingsinfo.getGrunninformasjon().getSpråkkodeEllerDefault();

        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandling.getId());
        boolean finnesVerge = vergeEntitet.isPresent();

        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakType, språkkode);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        boolean tilbakekreves = VedtakResultatType.FULL_TILBAKEBETALING.equals(vedtakResultatType) ||
            VedtakResultatType.DELVIS_TILBAKEBETALING.equals(vedtakResultatType);

        return new BrevMetadata.Builder()
            .medAnsvarligSaksbehandler(StringUtils.isNotEmpty(behandling.getAnsvarligSaksbehandler()) ? behandling.getAnsvarligSaksbehandler() : "VL")
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medMottakerAdresse(adresseinfo)
            .medFagsaktype(fagsakType)
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medSakspartNavn(personinfo.getNavn())
            .medSprakkode(eksternBehandlingsinfo.getGrunninformasjon().getSpråkkodeEllerDefault())
            .medTittel(finnTittelVedtaksbrev(ytelseNavn.getNavnPåBokmål(), tilbakekreves))
            .medFinnesVerge(finnesVerge)
            .medVergeNavn(vergeNavn)
            .build();
    }

    private BrevMottaker getBrevMottaker(Long behandlingId) {
        return vergeRepository.finnesVerge(behandlingId) ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
    }

    private String finnTittelVedtaksbrev(String fagsaktypenavnBokmål, boolean tilbakekreves) {
        if (tilbakekreves) {
            return TITTEL_FRITEKST_VEDTAK_TILBAKEBETALING + fagsaktypenavnBokmål;
        } else {
            return TITTEL_FRITEKST_VEDTAK_INGEN_TILBAKEBETALING + fagsaktypenavnBokmål;
        }
    }
}
