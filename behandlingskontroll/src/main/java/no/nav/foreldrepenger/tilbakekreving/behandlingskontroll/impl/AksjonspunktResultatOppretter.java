package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKontrollRepository;


/**
 * Håndterer aksjonspunktresultat og oppretter/reaktiverer aksjonspunkt Brukes
 * fra StegVisitor og Behandlingskontroll for lik håndtering
 */
class AksjonspunktResultatOppretter {

    private final Behandling behandling;

    private final AksjonspunktKontrollRepository aksjonspunktKontrollRepository;

    private Map<AksjonspunktDefinisjon, Aksjonspunkt> eksisterende = new LinkedHashMap<>();

    AksjonspunktResultatOppretter(AksjonspunktKontrollRepository aksjonspunktKontrollRepository, Behandling behandling) {
        this.behandling = Objects.requireNonNull(behandling, "behandling");
        this.aksjonspunktKontrollRepository = aksjonspunktKontrollRepository;
        behandling.getAksjonspunkter().forEach(ap -> this.eksisterende.putIfAbsent(ap.getAksjonspunktDefinisjon(), ap));
    }

    /**
     * Lagrer nye aksjonspunkt, og gjenåpner dem hvis de alleerede står til
     * avbrutt/utført
     */
    List<Aksjonspunkt> opprettAksjonspunkter(List<AksjonspunktResultat> apResultater, BehandlingStegType behandlingStegType) {

        if (!apResultater.isEmpty()) {
            List<Aksjonspunkt> endringAksjonspunkter = new ArrayList<>();
            endringAksjonspunkter.addAll(leggTilResultatPåBehandling(behandlingStegType, apResultater));
            return endringAksjonspunkter;
        }
        return new ArrayList<>();
    }

    private List<Aksjonspunkt> leggTilResultatPåBehandling(BehandlingStegType behandlingStegType, List<AksjonspunktResultat> resultat) {
        return resultat.stream()
                .map(ar -> oppdaterAksjonspunktMedResultat(behandlingStegType, ar))
                .collect(Collectors.toList());
    }

    private Aksjonspunkt oppdaterAksjonspunktMedResultat(BehandlingStegType behandlingStegType, AksjonspunktResultat resultat) {
        var oppdatert = eksisterende.get(resultat.getAksjonspunktDefinisjon());
        if (oppdatert == null) {
            oppdatert = aksjonspunktKontrollRepository.leggTilAksjonspunkt(behandling, resultat.getAksjonspunktDefinisjon(), behandlingStegType);
            eksisterende.putIfAbsent(oppdatert.getAksjonspunktDefinisjon(), oppdatert);
        }
        if (oppdatert.erUtført() || oppdatert.erAvbrutt()) {
            aksjonspunktKontrollRepository.setReåpnet(oppdatert);
        }
        if (resultat.getFrist() != null || resultat.getVenteårsak() != null) {
            aksjonspunktKontrollRepository.setFrist(oppdatert, resultat.getFrist(), resultat.getVenteårsak());
        }
        return oppdatert;
    }

}
