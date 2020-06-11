package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingsresultatDto {

    //FIXME k9-tilbake Bruker k9-tilbake likt nok kodeverk til at dette virker?
    private YtelsesbehandlingResultatType type;
    //FIXME k9-tilbake Bruker k9-tilbake likt nok kodeverk til at dette virker?
    private List<KonsekvensForYtelsen> konsekvenserForYtelsen;

    public BehandlingsresultatDto() {
        // trengs for deserialisering av JSON
    }

    public YtelsesbehandlingResultatType getType() {
        return type;
    }

    public void setType(YtelsesbehandlingResultatType type) {
        this.type = type;
    }

    public List<KonsekvensForYtelsen> getKonsekvenserForYtelsen() {
        return konsekvenserForYtelsen;
    }

    public void setKonsekvenserForYtelsen(List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        this.konsekvenserForYtelsen = konsekvenserForYtelsen;
    }
}
