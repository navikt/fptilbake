package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModellVisitor;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegProsesseringResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.TekniskRepository;
import no.nav.vedtak.felles.jpa.savepoint.Work;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

/**
 * Tekniske oppsett ved kjøring av et steg:<br>
 * <ul>
 * <li>Setter savepoint slik at dersom steg feiler så beholdes tidligere resultater.</li>
 * <li>Setter LOG_CONTEXT slik at ytterligere detaljer blir med i logging.</li>
 * </ul>
 */
public class TekniskBehandlingStegVisitor implements BehandlingModellVisitor {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private final BehandlingskontrollKontekst kontekst;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private BehandlingskontrollEventPubliserer eventPubliserer;

    private BehandlingRepositoryProvider repositoryProvider;

    public TekniskBehandlingStegVisitor(BehandlingRepositoryProvider repositoryProvider,
                                        BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                        BehandlingskontrollKontekst kontekst,
                                        BehandlingskontrollEventPubliserer eventPubliserer) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.kontekst = kontekst;
        this.eventPubliserer = eventPubliserer;
    }

    @Override
    public BehandlingStegProsesseringResultat prosesser(BehandlingStegModell steg) {
        LOG_CONTEXT.add("fagsak", kontekst.getFagsakId()); // NOSONAR //$NON-NLS-1$
        LOG_CONTEXT.add("behandling", kontekst.getBehandlingId()); // NOSONAR //$NON-NLS-1$
        LOG_CONTEXT.add("steg", steg.getBehandlingStegType().getKode()); // NOSONAR //$NON-NLS-1$

        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(kontekst.getBehandlingId());
        Optional<BehandlingStegTilstand> før = behandling.getSisteBehandlingStegTilstand();
        // lag ny for hvert steg som kjøres
        BehandlingStegVisitor stegVisitor = new BehandlingStegVisitor(repositoryProvider, behandling, behandlingskontrollTjeneste, steg, kontekst, eventPubliserer);

        // kjøres utenfor savepoint. Ellers står vi nakne, med kun utførte steg
        stegVisitor.markerOvergangTilNyttSteg(før, steg.getBehandlingStegType());

        BehandlingStegProsesseringResultat resultat = prosesserStegISavepoint(behandling, stegVisitor);

        /*
         * NB: nullstiller her og ikke i finally block, siden det da fjernes før vi får logget det.
         * Hele settet fjernes så i MDCFilter eller tilsvarende uansett. Steg er del av koden så fanges uansett i
         * stacktrace men trengs her for å kunne ta med i log eks. på DEBUG/INFO/WARN nivå.
         *
         * behandling og fagsak kan være satt utenfor, så nullstiller ikke de i log context her
         */
        LOG_CONTEXT.remove("steg"); // NOSONAR //$NON-NLS-1$

        return resultat;
    }

    protected BehandlingStegProsesseringResultat prosesserStegISavepoint(Behandling behandling, BehandlingStegVisitor stegVisitor) {
        // legger steg kjøring i et savepiont
        class DoInSavepoint implements Work<BehandlingStegProsesseringResultat> {
            @Override
            public BehandlingStegProsesseringResultat doWork() {
                BehandlingStegProsesseringResultat resultat = prosesserSteg(stegVisitor);
                lagreNedBehandling(behandling);
                return resultat;
            }
        }

        BehandlingStegProsesseringResultat resultat = new TekniskRepository(repositoryProvider).doWorkInSavepoint(new DoInSavepoint());
        return resultat;
    }

    protected BehandlingStegProsesseringResultat prosesserSteg(BehandlingStegVisitor stegVisitor) {
        return stegVisitor.prosesser();
    }

    protected void lagreNedBehandling(Behandling behandling) {
        repositoryProvider.getBehandlingRepository().lagreOgClear(behandling, kontekst.getSkriveLås());
    }
}
