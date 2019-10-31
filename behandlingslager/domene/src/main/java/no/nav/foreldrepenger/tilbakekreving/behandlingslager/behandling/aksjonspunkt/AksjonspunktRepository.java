package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


public interface AksjonspunktRepository extends BehandlingslagerRepository {

    AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode);

    void deaktiver(Aksjonspunkt aksjonspunkt);

    void setTilAvbrutt(Aksjonspunkt aksjonspunkt);

    void setTilManueltOpprettet(Aksjonspunkt aksjonspunkt);

    boolean setTilUtført(Aksjonspunkt aksjonspunkt);

    void reaktiver(Aksjonspunkt aksjonspunkt);

    void setReåpnet(Aksjonspunkt aksjonspunkt);

    Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon def, BehandlingStegType steg);

    Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon def);

    void setFrist(Aksjonspunkt ap, LocalDateTime fristTid, Venteårsak venteårsak);

    void fjernAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon);

    Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingStegType stegType,
                                      LocalDateTime fristTid, Venteårsak venteårsak);

    AksjonspunktStatus finnAksjonspunktStatus(String kode);

    void setToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt);
}
