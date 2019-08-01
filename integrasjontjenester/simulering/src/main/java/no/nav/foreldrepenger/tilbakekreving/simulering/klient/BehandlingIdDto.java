package no.nav.foreldrepenger.tilbakekreving.simulering.klient;

class BehandlingIdDto {
    private Long behandlingId;

    public BehandlingIdDto(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }
}
