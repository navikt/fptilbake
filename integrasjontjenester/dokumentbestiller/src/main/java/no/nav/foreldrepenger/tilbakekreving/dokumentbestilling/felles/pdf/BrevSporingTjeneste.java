package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;

@ApplicationScoped
public class BrevSporingTjeneste {

    private BrevSporingRepository brevSporingRepository;

    public BrevSporingTjeneste() {
        //for CDI proxy
    }

    @Inject
    public BrevSporingTjeneste(BrevSporingRepository brevSporingRepository) {
        this.brevSporingRepository = brevSporingRepository;
    }

    public void lagreInfoOmUtsendtBrev(long behandlingId, JournalpostIdOgDokumentId dokumentreferanse, DetaljertBrevType detaljertBrevType) {
        BrevSporing brevSporing = new BrevSporing.Builder()
                .medBehandlingId(behandlingId)
                .medDokumentId(dokumentreferanse.getDokumentId())
                .medJournalpostId(dokumentreferanse.getJournalpostId())
                .medBrevType(detaljertBrevType.getBrevType())
                .build();
        brevSporingRepository.lagre(brevSporing);
    }

    public boolean erVarselBrevSendtFor(long behandlingId) {
        return brevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId);
    }
}
