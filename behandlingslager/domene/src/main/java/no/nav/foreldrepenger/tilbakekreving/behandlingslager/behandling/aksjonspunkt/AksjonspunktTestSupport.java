package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


/**
 * Skal kun brukes av tester som av en eller annen grunn må tukle
 */
public final class AksjonspunktTestSupport {

    private AksjonspunktTestSupport() {
    }

    public static Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, null);
    }

    public static Aksjonspunkt leggTilAksjonspunkt(Behandling behandling,
                                                    AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                    BehandlingStegType behandlingStegType) {
        // sjekk at alle parametere er spesifisert
        Objects.requireNonNull(behandling, "behandling");
        Objects.requireNonNull(aksjonspunktDefinisjon, "aksjonspunktDefinisjon");

        // slå opp for å få riktig konfigurasjon.
        var adBuilder = behandlingStegType != null
            ? new Aksjonspunkt.Builder(aksjonspunktDefinisjon, behandlingStegType)
            : new Aksjonspunkt.Builder(aksjonspunktDefinisjon);

        if (aksjonspunktDefinisjon.getFristPeriod() != null) {
            adBuilder.medFristTid(LocalDateTime.now().plus(aksjonspunktDefinisjon.getFristPeriod()));
        }
        adBuilder.medVenteårsak(Venteårsak.UDEFINERT);

        return adBuilder.buildFor(behandling);

    }

    public static void setToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt) {
        var apDef = aksjonspunkt.getAksjonspunktDefinisjon();
        if (!aksjonspunkt.isToTrinnsBehandling()) {
            if (!aksjonspunkt.erÅpentAksjonspunkt()) {
                setReåpnet(aksjonspunkt);
            }
            aksjonspunkt.settToTrinnsFlag();
        }
    }

    public static void fjernToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt) {
        aksjonspunkt.fjernToTrinnsFlagg();
    }

    public static boolean setTilUtført(Aksjonspunkt aksjonspunkt, String begrunnelse) {
        return aksjonspunkt.setStatus(AksjonspunktStatus.UTFØRT);
    }

    public static void setTilAvbrutt(Aksjonspunkt aksjonspunkt) {
        aksjonspunkt.setStatus(AksjonspunktStatus.AVBRUTT);
    }

    public static void setReåpnet(Aksjonspunkt aksjonspunkt) {
        aksjonspunkt.setStatus(AksjonspunktStatus.OPPRETTET);
    }

    public static void setFrist(Aksjonspunkt ap, LocalDateTime fristTid, Venteårsak venteårsak) {
        ap.setFristTid(fristTid);
        ap.setVenteårsak(venteårsak);
    }
}