package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;


/**
 * Event publiseres av {@link BehandlingskontrollTjeneste} når en {@link Behandling} endrer steg.
 * Kan brukes til å lytte på flyt i en Behandling og utføre logikk når det skjer.
 */
public class BehandlingStatusEvent implements BehandlingEvent {

    private BehandlingskontrollKontekst kontekst;

    private BehandlingStatus nyStatus;

    BehandlingStatusEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
        this.kontekst = kontekst;
        this.nyStatus = nyStatus;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return kontekst.getSaksnummer();
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    public BehandlingskontrollKontekst getKontekst() {
        return kontekst;
    }

    public BehandlingStatus getNyStatus() {
        return nyStatus;
    }

    static void validerRiktigStatus(BehandlingStatus nyStatus, BehandlingStatus expected) {
        if (!Objects.equals(expected, nyStatus)) {
            throw new IllegalArgumentException("Kan bare være " + expected + ", fikk: " + nyStatus);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + kontekst +
                ", nyStatus=" + nyStatus +
                ">";
    }

    public static class BehandlingAvsluttetEvent extends BehandlingStatusEvent {
        BehandlingAvsluttetEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
            super(kontekst, nyStatus);
            validerRiktigStatus(nyStatus, BehandlingStatus.AVSLUTTET);
        }
    }

    public static class BehandlingOpprettetEvent extends BehandlingStatusEvent {
        BehandlingOpprettetEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
            super(kontekst, nyStatus);
            validerRiktigStatus(nyStatus, BehandlingStatus.OPPRETTET);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V extends BehandlingStatusEvent> V nyEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
        if (BehandlingStatus.AVSLUTTET.equals(nyStatus)) {
            return (V) new BehandlingAvsluttetEvent(kontekst, nyStatus);
        } else if (BehandlingStatus.OPPRETTET.equals(nyStatus)) {
            return (V) new BehandlingOpprettetEvent(kontekst, nyStatus);
        } else {
            return (V) new BehandlingStatusEvent(kontekst, nyStatus);
        }
    }
}
