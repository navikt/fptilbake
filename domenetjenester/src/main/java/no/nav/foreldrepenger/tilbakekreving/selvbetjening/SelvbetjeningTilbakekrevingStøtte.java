package no.nav.foreldrepenger.tilbakekreving.selvbetjening;

import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class SelvbetjeningTilbakekrevingStøtte {

    private static final Set<FagsakYtelseType> YTELSETYPER_STØTTET_I_SELVBETJENING = Set.of(
            FagsakYtelseType.FORELDREPENGER,
            FagsakYtelseType.SVANGERSKAPSPENGER,
            FagsakYtelseType.ENGANGSTØNAD);

    public static boolean harStøtteFor(Behandling behandling) {
        return harStøtteFor(behandling.getFagsak().getFagsakYtelseType());
    }

    public static boolean harStøtteFor(FagsakYtelseType fagsakYtelseType) {
        return YTELSETYPER_STØTTET_I_SELVBETJENING.contains(fagsakYtelseType);
    }

}
