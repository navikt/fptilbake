package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem.FPTILBAKE;

@ApplicationScoped
public class HistorikkRepositoryTeamAware {
    private static final Fagsystem fagsystem = ApplicationName.hvilkenTilbake();
    private HistorikkRepository historikkRepository;
    private Historikkinnslag2Repository historikkinnslag2Repository;

    public HistorikkRepositoryTeamAware() {
        // CDI
    }

    @Inject
    public HistorikkRepositoryTeamAware(HistorikkRepository historikkRepository, Historikkinnslag2Repository historikkinnslag2Repository) {
        this.historikkRepository = historikkRepository;
        this.historikkinnslag2Repository = historikkinnslag2Repository;
    }


    @Deprecated(forRemoval = true)
    public void lagre(Historikkinnslag historikkinnslag) {
        historikkRepository.lagre(historikkinnslag);
    }

    public void lagre(Historikkinnslag historikkinnslag, Historikkinnslag2 historikkinnslag2) {
        if (FPTILBAKE.equals(fagsystem)) {
            historikkinnslag2Repository.lagre(historikkinnslag2);
        } else {
            historikkRepository.lagre(historikkinnslag);
        }
    }


}
