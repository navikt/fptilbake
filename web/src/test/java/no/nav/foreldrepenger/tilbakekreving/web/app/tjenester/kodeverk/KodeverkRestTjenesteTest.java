package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Begrunnelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Tema;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;

public class KodeverkRestTjenesteTest {

    @Test
    public void skal_hente_kodeverk_og_gruppere_på_kodeverknavn() {
        HentKodeverkTjeneste hentKodeverkTjeneste = Mockito.mock(HentKodeverkTjeneste.class);
        Mockito.when(hentKodeverkTjeneste.hentGruppertKodeliste()).thenReturn(getGruppertKodeliste());

        KodeverkRestTjeneste tjeneste = new KodeverkRestTjeneste(hentKodeverkTjeneste);
        Map<String, Object> gruppertKodeliste = tjeneste.hentGruppertKodeliste();

        assertThat(gruppertKodeliste.keySet()).containsOnly(Tema.class.getSimpleName(), Begrunnelse.class.getSimpleName());
        assertThat(gruppertKodeliste.get(Tema.class.getSimpleName()))
                .isEqualTo(Arrays.asList(Tema.FORELDREPENGER, Tema.SYKEPENGER));
    }

    private static Map<String, List<Kodeliste>> getGruppertKodeliste() {
        Map<String, List<Kodeliste>> map = new HashMap<>();
        map.put(Tema.class.getSimpleName(), Arrays.asList(Tema.FORELDREPENGER, Tema.SYKEPENGER));
        map.put(Begrunnelse.class.getSimpleName(), Arrays.asList(Begrunnelse.ARBEID_OPPHOERT));
        return map;
    }
}
