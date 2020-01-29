package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingsresultatDto {

    private FpsakBehandlingResultatType type;
    private List<KonsekvensForYtelsen> konsekvenserForYtelsen;

    public BehandlingsresultatDto() {
        // trengs for deserialisering av JSON
    }

    public FpsakBehandlingResultatType getType() {
        return type;
    }

    public void setType(FpsakBehandlingResultatType type) {
        this.type = type;
    }

    public List<KonsekvensForYtelsen> getKonsekvenserForYtelsen() {
        return konsekvenserForYtelsen;
    }

    public void setKonsekvenserForYtelsen(List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        this.konsekvenserForYtelsen = konsekvenserForYtelsen;
    }
}
