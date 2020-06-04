package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;

public class BrevAktørIdUtil {

    public static String getMottakerAktørId(Behandling behandling, Optional<VergeEntitet> vergeEntitet) {
        String aktørId = behandling.getAktørId().getId();
        if (vergeEntitet.isPresent()) {
            VergeEntitet verge = vergeEntitet.get();
            if (!VergeType.ADVOKAT.equals(verge.getVergeType())) {
                aktørId = verge.getVergeAktørId().getId();
            }
        }
        return aktørId;
    }
}
