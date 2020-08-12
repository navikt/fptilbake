package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;

public class KodeverkRestTjenesteTest {

    @Test
    public void skal_hente_kodeverk_og_gruppere_på_kodeverknavn() {
        HentKodeverkTjeneste hentKodeverkTjeneste = Mockito.mock(HentKodeverkTjeneste.class);
        Mockito.when(hentKodeverkTjeneste.hentGruppertKodeliste()).thenReturn(getGruppertKodeliste());

        KodeverkRestTjeneste tjeneste = new KodeverkRestTjeneste(hentKodeverkTjeneste);
        Map<String, Object> gruppertKodeliste = tjeneste.hentGruppertKodeliste();

        assertThat(gruppertKodeliste.keySet()).containsOnly(KonsekvensForYtelsen.class.getSimpleName());
        assertThat(gruppertKodeliste.get(KonsekvensForYtelsen.class.getSimpleName()))
                .isEqualTo(Arrays.asList(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK));
    }

    private static Map<String, Collection<? extends Kodeverdi>> getGruppertKodeliste() {
        Map<String, Collection<? extends Kodeverdi>> map = new HashMap<>();
        map.put(KonsekvensForYtelsen.class.getSimpleName(), Arrays.asList(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK));
        return map;
    }
}
