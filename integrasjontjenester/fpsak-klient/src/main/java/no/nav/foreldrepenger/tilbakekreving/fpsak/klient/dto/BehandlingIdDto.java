package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO: Er denne klassen i det heile tatt i bruk?
@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingIdDto {
    private Long behandlingId;
    private String saksnummer;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }
}
