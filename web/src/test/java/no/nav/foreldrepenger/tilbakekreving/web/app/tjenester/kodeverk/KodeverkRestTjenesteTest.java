package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;

public class KodeverkRestTjenesteTest {

    @Test
    public void skal_hente_kodeverk_og_gruppere_på_kodeverknavn() throws IOException {

        KodeverkRestTjeneste tjeneste = new KodeverkRestTjeneste(new HentKodeverkTjeneste());
        var rawJson = (String) tjeneste.hentGruppertKodeliste().getEntity();
        assertThat(rawJson).isNotNull();

        Map<String, Object> gruppertKodeliste = new JacksonJsonConfig(true).getObjectMapper().readValue(rawJson, Map.class);
        assertThat(gruppertKodeliste.keySet())
            .contains(Fagsystem.class.getSimpleName(), SærligGrunn.class.getSimpleName(), VidereBehandling.class.getSimpleName());

        assertThat(gruppertKodeliste.keySet())
            .containsAll(new HashSet<>(HentKodeverkTjeneste.KODEVERDIER_SOM_BRUKES_PÅ_KLIENT.keySet()));

        assertThat(gruppertKodeliste.keySet()).hasSize(HentKodeverkTjeneste.KODEVERDIER_SOM_BRUKES_PÅ_KLIENT.size());

        var fagsakStatuser = (List<Map<String, String>>) gruppertKodeliste.get(Fagsystem.class.getSimpleName());
        assertThat(fagsakStatuser.stream().map(k -> k.get("kode")).collect(Collectors.toList())).contains(Fagsystem.FPSAK.getKode(),
            Fagsystem.K9SAK.getKode());
    }


    @Test
    public void serialize_kodeverdi_grunn() throws Exception {

        var jsonConfig = new JacksonJsonConfig(false);

        var om = jsonConfig.getObjectMapper();

        var json = om.writer().withDefaultPrettyPrinter().writeValueAsString(new X(SærligGrunn.STØRRELSE_BELØP));

        assertThat(json).contains("\"særligGrunn\" : \"STOERRELSE_BELOEP\"");
    }

    @Test
    public void serialize_kodeverdi_grunn_full() throws Exception {

        var jsonConfig = new JacksonJsonConfig(true);

        var om = jsonConfig.getObjectMapper();

        var json = om.writer().withDefaultPrettyPrinter().writeValueAsString(new X(SærligGrunn.STØRRELSE_BELØP));

        assertThat(json).contains("\"kode\" : \"STOERRELSE_BELOEP\"");
        assertThat(json).contains("\"navn\" : \"Størrelsen på feilutbetalt beløp\"");
    }

    private static record X(SærligGrunn særligGrunn) {}
}
