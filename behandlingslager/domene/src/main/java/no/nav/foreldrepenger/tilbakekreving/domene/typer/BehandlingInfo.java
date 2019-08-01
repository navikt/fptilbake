package no.nav.foreldrepenger.tilbakekreving.domene.typer;

public class BehandlingInfo {

    private Long behandlingId;
    private String saksnummer;
    private AktørId aktørId;
    private String behandlingStatus;
    private String ansvarligSaksbehandler;

    /**
     *  denne brukes fra {@link no.nav.foreldrepenger.tilbakekreving.pip.PipRepository}
     */
    public BehandlingInfo(Long behandlingId, String saksnummer, String aktørId, String behandlingstatus, String ansvarligSaksbehandler) {
        this(behandlingId, saksnummer, new AktørId(aktørId), behandlingstatus, ansvarligSaksbehandler);
    }

    public BehandlingInfo(Long behandlingId, String saksnummer, AktørId aktørId, String behandlingstatus, String ansvarligSaksbehandler) {
        this.behandlingId = behandlingId;
        this.saksnummer = saksnummer;
        this.aktørId = aktørId;
        this.behandlingStatus = behandlingstatus;
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

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

    public AktørId getAktørId() {
        return aktørId;
    }

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public String getBehandlingStatus() {
        return behandlingStatus;
    }

    public void setBehandlingStatus(String behandlingStatus) {
        this.behandlingStatus = behandlingStatus;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

}
