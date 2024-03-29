package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


/**
 * Håndter all endring av aksjonspunkt for behandlingskontroll. Skal IKKE brukes utenfor Behandlingskontroll uten avklaring
 */
@ApplicationScoped
public class AksjonspunktKontrollRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AksjonspunktKontrollRepository.class);

    @Inject
    public AksjonspunktKontrollRepository() {
        // Hibernate
    }

    public Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             BehandlingStegType stegType,
                                             LocalDateTime fristTid, Venteårsak venteårsak) {

        LOG.info("Setter behandling på vent for steg={}, aksjonspunkt={}, fristTid={}, venteÅrsak={}", stegType, aksjonspunktDefinisjon, fristTid, venteårsak);

        Aksjonspunkt aksjonspunkt;
        var eksisterendeAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);
        if (eksisterendeAksjonspunkt.isPresent()) {
            // håndter har allerede angit aksjonpunkt, oppdaterer
            aksjonspunkt = eksisterendeAksjonspunkt.get();
            if (!aksjonspunkt.erOpprettet()) {
                this.setReåpnet(aksjonspunkt);
            }
            this.setFrist(aksjonspunkt, fristTid, venteårsak);
        } else {
            // nytt aksjonspunkt
            aksjonspunkt = this.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, Optional.empty(),
                    Optional.ofNullable(fristTid), Optional.ofNullable(venteårsak), Optional.empty());
        }
        aksjonspunkt.setBehandlingSteg(stegType);
        return aksjonspunkt;
    }

    private Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             Optional<BehandlingStegType> behandlingStegType, Optional<LocalDateTime> frist, Optional<Venteårsak> venteÅrsak,
                                             Optional<Boolean> toTrinnskontroll) {
        // sjekk at alle parametere er spesifisert
        Objects.requireNonNull(behandling, "behandling");
        Objects.requireNonNull(aksjonspunktDefinisjon, "aksjonspunktDefinisjon");
        Objects.requireNonNull(behandlingStegType, "behandlingStegType");
        Objects.requireNonNull(frist, "frist");
        Objects.requireNonNull(venteÅrsak, "venteÅrsak");
        Objects.requireNonNull(toTrinnskontroll, "toTrinnskontroll");

        // slå opp for å få riktig konfigurasjon.
        var adBuilder = behandlingStegType.map(stegType -> new Aksjonspunkt.Builder(aksjonspunktDefinisjon, stegType)).orElseGet(() -> new Aksjonspunkt.Builder(aksjonspunktDefinisjon));

        if (frist.isPresent()) {
            adBuilder.medFristTid(frist.get());
        } else if (aksjonspunktDefinisjon.getFristPeriod() != null) {
            adBuilder.medFristTid(LocalDateTime.now().plus(aksjonspunktDefinisjon.getFristPeriod()));
        }

        if (venteÅrsak.isPresent()) {
            adBuilder.medVenteårsak(venteÅrsak.get());
        } else {
            adBuilder.medVenteårsak(Venteårsak.UDEFINERT);
        }

        var aksjonspunkt = adBuilder.buildFor(behandling);
        LOG.info("Legger til aksjonspunkt: {}", aksjonspunktDefinisjon);
        return aksjonspunkt;
    }

    public Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                            BehandlingStegType behandlingStegType) {
        Objects.requireNonNull(behandlingStegType, "behandlingStegType");
        return leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, Optional.ofNullable(behandlingStegType), Optional.empty(), Optional.empty(),
                Optional.empty());
    }

    public Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty());
    }

    public void setReåpnet(Aksjonspunkt aksjonspunkt) {
        LOG.info("Setter aksjonspunkt reåpnet: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setStatus(AksjonspunktStatus.OPPRETTET);
    }

    public void setReåpnetMedTotrinn(Aksjonspunkt aksjonspunkt, boolean setToTrinn) {
        LOG.info("Setter aksjonspunkt reåpnet: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setStatus(AksjonspunktStatus.OPPRETTET);
        if (setToTrinn && !aksjonspunkt.isToTrinnsBehandling()) {
            aksjonspunkt.settToTrinnsFlag();
        }
    }

    public void setTilAvbrutt(Aksjonspunkt aksjonspunkt) {
        LOG.info("Setter aksjonspunkt avbrutt: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setStatus(AksjonspunktStatus.AVBRUTT);
    }

    public boolean setTilUtført(Aksjonspunkt aksjonspunkt) {
        LOG.info("Setter aksjonspunkt utført: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        return aksjonspunkt.setStatus(AksjonspunktStatus.UTFØRT);
    }

    public void setFrist(Aksjonspunkt ap, LocalDateTime fristTid, Venteårsak venteårsak) {
        ap.setFristTid(fristTid);
        ap.setVenteårsak(venteårsak);
    }

}
