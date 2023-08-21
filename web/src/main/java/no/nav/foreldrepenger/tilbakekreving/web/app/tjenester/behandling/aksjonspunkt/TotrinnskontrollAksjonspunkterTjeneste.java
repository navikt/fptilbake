package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.VurderÅrsakTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.SkjermlenkeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn.TotrinnskontrollAksjonspunkterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn.TotrinnskontrollSkjermlenkeContextDto;

@ApplicationScoped
public class TotrinnskontrollAksjonspunkterTjeneste {

    private TotrinnTjeneste totrinnTjeneste;

    TotrinnskontrollAksjonspunkterTjeneste() {
        //for CDI-proxy
    }

    @Inject
    public TotrinnskontrollAksjonspunkterTjeneste(TotrinnTjeneste totrinnTjeneste) {
        this.totrinnTjeneste = totrinnTjeneste;
    }

    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnsSkjermlenkeContext(Behandling behandling) {
        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContext = new ArrayList<>();
        List<Aksjonspunkt> aksjonspunkter = behandling.getAksjonspunkterMedTotrinnskontroll();
        Map<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenkeMap = new HashMap<>();
        Collection<Totrinnsvurdering> totrinnsVurderinger = totrinnTjeneste.hentTotrinnsvurderinger(behandling);
        // Behandling er ikkje i fatte vedtak og har ingen totrinnsvurderinger -> returnerer tom liste
        if (!BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus()) && totrinnsVurderinger.isEmpty()) {
            return Collections.emptyList();
        }
        for (Aksjonspunkt ap : aksjonspunkter) {
            Optional<Totrinnsvurdering> vurdering = totrinnsVurderinger.stream()
                    .filter(v -> v.getAksjonspunktDefinisjon().equals(ap.getAksjonspunktDefinisjon()))
                    .findFirst();
            Totrinnsvurdering.Builder builder = new Totrinnsvurdering.Builder();
            builder.medAksjonspunktDefinisjon(ap.getAksjonspunktDefinisjon());
            builder.medBehandling(behandling);
            vurdering.ifPresent(ttVurdering -> {
                builder.medBegrunnelse(ttVurdering.getBegrunnelse());
                builder.medVurderÅrsaker(ttVurdering.getVurderÅrsaker());
                if (ttVurdering.isGodkjent()) {
                    builder.medGodkjent(ttVurdering.isGodkjent());
                }
            });
            lagTotrinnsaksjonspunkt(skjermlenkeMap, builder.build());
        }
        for (Map.Entry<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenke : skjermlenkeMap.entrySet()) {
            TotrinnskontrollSkjermlenkeContextDto context = new TotrinnskontrollSkjermlenkeContextDto(skjermlenke.getKey().getKode(), skjermlenke.getValue());
            skjermlenkeContext.add(context);
        }
        return skjermlenkeContext;
    }

    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnsvurderingSkjermlenkeContext(Behandling behandling) {
        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContext = new ArrayList<>();
        Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger = totrinnTjeneste.hentTotrinnsvurderinger(behandling);
        Map<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenkeMap = new HashMap<>();
        for (Totrinnsvurdering vurdering : totrinnaksjonspunktvurderinger) {
            lagTotrinnsaksjonspunkt(skjermlenkeMap, vurdering);
        }
        for (Map.Entry<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenke : skjermlenkeMap.entrySet()) {
            TotrinnskontrollSkjermlenkeContextDto context = new TotrinnskontrollSkjermlenkeContextDto(skjermlenke.getKey().getKode(), skjermlenke.getValue());
            skjermlenkeContext.add(context);
        }
        return skjermlenkeContext;
    }

    private void lagTotrinnsaksjonspunkt(Map<SkjermlenkeType, List<TotrinnskontrollAksjonspunkterDto>> skjermlenkeMap, Totrinnsvurdering vurdering) {
        TotrinnskontrollAksjonspunkterDto totrinnsAksjonspunkt = lagTotrinnskontrollAksjonspunktDto(vurdering);
        SkjermlenkeType skjermlenkeType = SkjermlenkeTjeneste.finnSkjermlenkeType(vurdering.getAksjonspunktDefinisjon());
        if (skjermlenkeType != SkjermlenkeType.UDEFINERT) {
            List<TotrinnskontrollAksjonspunkterDto> aksjonspktContextListe = skjermlenkeMap.computeIfAbsent(skjermlenkeType,
                    k -> new ArrayList<>());
            aksjonspktContextListe.add(totrinnsAksjonspunkt);
        }
    }

    private TotrinnskontrollAksjonspunkterDto lagTotrinnskontrollAksjonspunktDto(Totrinnsvurdering totrinnsvurdering) {
        return TotrinnskontrollAksjonspunkterDto.builder()
                .medAksjonspunktKode(totrinnsvurdering.getAksjonspunktDefinisjon().getKode())
                .medBesluttersBegrunnelse(totrinnsvurdering.getBegrunnelse())
                .medTotrinnskontrollGodkjent(totrinnsvurdering.isGodkjent())
                .medVurderPaNyttArsaker(hentVurderPåNyttÅrsaker(totrinnsvurdering)).build();
    }

    private Set<VurderÅrsak> hentVurderPåNyttÅrsaker(Totrinnsvurdering totrinnsvurdering) {
        return totrinnsvurdering.getVurderÅrsaker().stream()
                .map(VurderÅrsakTotrinnsvurdering::getÅrsaksType)
                .collect(Collectors.toSet());
    }


}
