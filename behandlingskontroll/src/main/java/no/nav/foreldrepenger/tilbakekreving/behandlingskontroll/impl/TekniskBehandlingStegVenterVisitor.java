package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegProsesseringResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

/**
 * Tekniske oppsett ved kjøring av et steg:<br>
 * <ul>
 * <li>Setter savepoint slik at dersom steg feiler så beholdes tidligere resultater.</li>
 * <li>Setter LOG_CONTEXT slik at ytterligere detaljer blir med i logging.</li>
 * </ul>
 */
public class TekniskBehandlingStegVenterVisitor extends TekniskBehandlingStegVisitor {

    private boolean gjenoppta = true;

    public TekniskBehandlingStegVenterVisitor(BehandlingRepositoryProvider repositoryProvider,
                                              BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                              BehandlingskontrollKontekst kontekst,
                                              BehandlingskontrollEventPubliserer eventPubliserer) {
        super(repositoryProvider, behandlingskontrollTjeneste, kontekst, eventPubliserer);
    }

    @Override
    protected BehandlingStegProsesseringResultat prosesserSteg(BehandlingStegVisitor stegVisitor) {
        BehandlingStegProsesseringResultat resultat = gjenoppta ? stegVisitor.gjenoppta() : stegVisitor.prosesser();
        gjenoppta = false;
        return resultat;
    }
}
