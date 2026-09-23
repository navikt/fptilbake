package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;

@ApplicationScoped
public class VarselresponsTjeneste {

    public static final String HISTORIKK_TITTEL_UTTALELSE = "Bruker har uttalt seg";

    private VarselresponsRepository varselresponsRepository;
    private HistorikkinnslagRepository historikkinnslagRepository;
    private BehandlingRepository behandlingRepository;

    public VarselresponsTjeneste() {
        // CDI
    }

    @Inject
    public VarselresponsTjeneste(VarselresponsRepository varselresponsRepository,
                                 HistorikkinnslagRepository historikkinnslagRepository,
                                 BehandlingRepository behandlingRepository) {
        this.varselresponsRepository = varselresponsRepository;
        this.historikkinnslagRepository = historikkinnslagRepository;
        this.behandlingRepository = behandlingRepository;
    }

    public void lagreRespons(long behandlingId, ResponsKanal responsKanal, Boolean akseptertFaktagrunnlag) {
        Objects.requireNonNull(behandlingId);
        Optional<Varselrespons> eksisterende = varselresponsRepository.hentRespons(behandlingId);
        if (!eksisterende.isPresent()) {
            Varselrespons varselrespons = Varselrespons.builder()
                    .medBehandlingId(behandlingId)
                    .setAkseptertFaktagrunnlag(akseptertFaktagrunnlag)
                    .setKilde(responsKanal.getDbKode())
                    .build();
            varselresponsRepository.lagre(varselrespons);
        }
        // Varselresponsen lagres kun for første uttalelse siden den er en markør for at bruker har svart,
        // men hver enkelt uttalelse skal dokumenteres i historikken. Innslaget lages her og ikke ved
        // gjenopptak, slik at uttalelsen blir dokumentert også når behandlingen ikke kan gjenopptas.
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        opprettHistorikkinnslagForBrukerUttalelse(behandling.getFagsakId(), behandlingId);
    }

    /**
     * Dokumenterer uttalelsen på fagsaken når det ikke finnes en behandling å knytte den til, typisk
     * fordi uttalelsen kommer inn etter at behandlingen er avsluttet.
     */
    public void opprettHistorikkinnslagForUttalelseUtenBehandling(long fagsakId) {
        opprettHistorikkinnslagForBrukerUttalelse(fagsakId, null);
    }

    public void lagreRespons(long behandlingId, ResponsKanal kanal) {
        lagreRespons(behandlingId, kanal, null);
    }

    public Optional<Varselrespons> hentRespons(long behandlingId) {
        return varselresponsRepository.hentRespons(behandlingId);
    }

    private void opprettHistorikkinnslagForBrukerUttalelse(long fagsakId, Long behandlingId) {
        var historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.SØKER)
            .medFagsakId(fagsakId)
            .medBehandlingId(behandlingId)
            .medTittel(HISTORIKK_TITTEL_UTTALELSE)
            .build();
        historikkinnslagRepository.lagre(historikkinnslag);
    }

}
