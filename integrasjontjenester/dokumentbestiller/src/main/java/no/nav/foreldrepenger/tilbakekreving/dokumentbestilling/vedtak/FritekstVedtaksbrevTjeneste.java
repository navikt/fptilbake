package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningFritekstVedtaksbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;

@ApplicationScoped
public class FritekstVedtaksbrevTjeneste {

    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private PdfBrevTjeneste pdfBrevTjeneste;

    FritekstVedtaksbrevTjeneste(){
        // for CDI
    }

    @Inject
    public FritekstVedtaksbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                       FritekstbrevTjeneste bestillDokumentTjeneste,
                                       PdfBrevTjeneste pdfBrevTjeneste){
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public byte[] hentForhåndsvisningFritekstVedtaksbrev(HentForhåndsvisningFritekstVedtaksbrevDto dto) {
        Behandling behandling = behandlingRepository.hentBehandling(dto.getBehandlingUuid());
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

    private BrevMottaker getBrevMottaker(Long behandlingId) {
        return vergeRepository.finnesVerge(behandlingId) ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
    }
}
