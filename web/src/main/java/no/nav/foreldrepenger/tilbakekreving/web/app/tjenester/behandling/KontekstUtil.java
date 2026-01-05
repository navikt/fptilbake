package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;

public class KontekstUtil {
    private KontekstUtil() {
    }

    public static boolean kanSaksbehandle() {
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            return true;
        }
        if (!KontekstHolder.harKontekst() || !KontekstHolder.getKontekst().erAutentisert() || !(KontekstHolder.getKontekst() instanceof RequestKontekst kontekst)) {
            return false;
        }
        return kontekst.harGruppe(AnsattGruppe.SAKSBEHANDLER);
    }
}
