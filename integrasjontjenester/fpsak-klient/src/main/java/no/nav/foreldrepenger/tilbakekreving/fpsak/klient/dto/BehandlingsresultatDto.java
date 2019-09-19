package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingsresultatDto {

    private BehandlingResultatType type;
    private List<KonsekvensForYtelsen> konsekvenserForYtelsen;

    public BehandlingsresultatDto() {
        // trengs for deserialisering av JSON
    }

    public BehandlingResultatType getType() {
        return type;
    }

    public void setType(BehandlingResultatType type) {
        this.type = type;
    }

    public List<KonsekvensForYtelsen> getKonsekvenserForYtelsen() {
        return konsekvenserForYtelsen;
    }

    public void setKonsekvenserForYtelsen(List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        this.konsekvenserForYtelsen = konsekvenserForYtelsen;
    }
}
