package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.ReaktiveringStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.VurderÅrsakTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårType;

public class AksjonspunktDtoMapper {

    private AksjonspunktDtoMapper() {
    }

    public static Set<AksjonspunktDto> lagAksjonspunktDto(Behandling behandling, Collection<Totrinnsvurdering> ttVurderinger) {
        return behandling.getAlleAksjonspunkterInklInaktive().stream()
                .filter(aksjonspunkt -> !aksjonspunkt.erAvbrutt())
                .filter(aksjonspunkt -> !aksjonspunkt.getReaktiveringStatus().equals(ReaktiveringStatus.SLETTET))
                .map(aksjonspunkt -> mapFra(aksjonspunkt, behandling, ttVurderinger))
                .collect(Collectors.toSet());
    }

    private static AksjonspunktDto mapFra(Aksjonspunkt aksjonspunkt, Behandling behandling, Collection<Totrinnsvurdering> ttVurderinger) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunkt.getAksjonspunktDefinisjon();

        AksjonspunktDto dto = new AksjonspunktDto();
        dto.setDefinisjon(aksjonspunktDefinisjon);
        dto.setStatus(aksjonspunkt.getStatus());
        dto.setBegrunnelse(aksjonspunkt.getBegrunnelse());
        dto.setVilkårType(VilkårType.UDEFINERT); //TODO Hvordan blir dette?
        dto.setToTrinnsBehandling(aksjonspunkt.isToTrinnsBehandling() || aksjonspunktDefinisjon.getDefaultTotrinnBehandling());

        Optional<Totrinnsvurdering> vurdering = ttVurderinger.stream().filter(v -> v.getAksjonspunktDefinisjon() == aksjonspunkt.getAksjonspunktDefinisjon()).findFirst();
        vurdering.ifPresent(ttVurdering -> {
                    dto.setBesluttersBegrunnelse(ttVurdering.getBegrunnelse());
                    dto.setToTrinnsBehandlingGodkjent(ttVurdering.isGodkjent());
                    dto.setVurderPaNyttArsaker(ttVurdering.getVurderÅrsaker().stream()
                            .map(VurderÅrsakTotrinnsvurdering::getÅrsaksType).collect(Collectors.toSet()));
                }
        );

        dto.setAksjonspunktType(aksjonspunktDefinisjon.getAksjonspunktType());
        dto.setKanLoses(kanLøses(aksjonspunktDefinisjon, behandling));
        dto.setErAktivt(aksjonspunkt.erAktivt());
        return dto;
    }

    private static Boolean kanLøses(AksjonspunktDefinisjon def, Behandling behandling) {
        if (behandling.getBehandlingStegStatus() == null) {
            // Stegstatus ikke satt, kan derfor ikke sette noen aksjonspunkt som løsbart
            return false;
        }
        Optional<BehandlingStegType> aktivtBehandlingSteg = Optional.ofNullable(behandling.getAktivtBehandlingSteg());
        return aktivtBehandlingSteg.map(steg ->
                skalLøsesIStegKode(def, behandling.getBehandlingStegStatus().getKode(), steg))
                .orElse(false);
    }

    private static Boolean skalLøsesIStegKode(AksjonspunktDefinisjon def, String stegKode, BehandlingStegType steg) {
        if (BehandlingStegStatus.INNGANG.getKode().equals(stegKode)) {
            return steg.getAksjonspunktDefinisjonerInngang().contains(def);
        } else
            return BehandlingStegStatus.UTGANG.getKode().equals(stegKode) && steg.getAksjonspunktDefinisjonerUtgang().contains(def);
    }
}
