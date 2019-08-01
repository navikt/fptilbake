package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app;

import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

public interface HentKodeverkTjeneste {

    Map<String, List<Kodeliste>> hentGruppertKodeliste();
}
