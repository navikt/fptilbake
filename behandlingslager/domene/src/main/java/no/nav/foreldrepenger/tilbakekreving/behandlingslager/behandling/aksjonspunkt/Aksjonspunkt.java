package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalUtil;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Aksjonspunkt")
@Table(name = "AKSJONSPUNKT")
public class Aksjonspunkt extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKSJONSPUNKT")
    private Long id;

    @Column(name = "frist_tid")
    private LocalDateTime fristTid;

    @Convert(converter = AksjonspunktDefinisjon.KodeverdiConverter.class)
    @Column(name = "aksjonspunkt_def", nullable = false, updatable = false)
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    @Convert(converter = BehandlingStegType.KodeverdiConverter.class)
    @Column(name = "behandling_steg_funnet")
    private BehandlingStegType behandlingSteg;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @Convert(converter = AksjonspunktStatus.KodeverdiConverter.class)
    @Column(name = "aksjonspunkt_status", nullable = false)
    private AksjonspunktStatus status;

    @Convert(converter = Venteårsak.KodeverdiConverter.class)
    @Column(name = "vent_aarsak", nullable = false)
    private Venteårsak venteårsak = Venteårsak.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    // OK med orphanRemoval=true siden årsakene er eid av et aksjonspunkt
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "aksjonspunkt")
    private Set<VurderPåNyttÅrsak> vurderPåNyttÅrsaker = new HashSet<>();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINN_BEHANDLING", nullable = false)
    private boolean toTrinnsBehandling;

    Aksjonspunkt() {
        // for hibernate
    }

    protected Aksjonspunkt(AksjonspunktDefinisjon aksjonspunktDef, BehandlingStegType behandlingStegFunnet) {
        Objects.requireNonNull(behandlingStegFunnet, "behandlingStegFunnet"); //$NON-NLS-1$
        Objects.requireNonNull(aksjonspunktDef, "aksjonspunktDef"); //$NON-NLS-1$
        this.behandlingSteg = behandlingStegFunnet;
        this.aksjonspunktDefinisjon = aksjonspunktDef;
        this.toTrinnsBehandling = aksjonspunktDef.getDefaultTotrinnBehandling();
        this.status = AksjonspunktStatus.OPPRETTET;
    }

    protected Aksjonspunkt(AksjonspunktDefinisjon aksjonspunktDef) {
        Objects.requireNonNull(aksjonspunktDef, "aksjonspunktDef"); //$NON-NLS-1$
        this.aksjonspunktDefinisjon = aksjonspunktDef;
        this.toTrinnsBehandling = aksjonspunktDef.getDefaultTotrinnBehandling();
        this.status = AksjonspunktStatus.OPPRETTET;
    }

    public Long getId() {
        return id;
    }

    public boolean erManuell() {
        return getAksjonspunktDefinisjon() != null && AksjonspunktType.MANUELL.equals(getAksjonspunktDefinisjon().getAksjonspunktType());
    }

    public boolean erOverstyrt() {
        return getAksjonspunktDefinisjon() != null && AksjonspunktType.OVERSTYRING.equals(getAksjonspunktDefinisjon().getAksjonspunktType());
    }

    /**
     * Hvorvidt et Aksjonspunkt er av typen Autopunkt.
     * <p>
     * NB: Ikke bruk dette til å styre på vent eller lignende. Bruk
     * egenskapene til Aksjonspunktet i stedet (eks. hvorvidt det har en frist).
     */
    public boolean erAutopunkt() {
        return getAksjonspunktDefinisjon() != null && AksjonspunktType.AUTOPUNKT.equals(getAksjonspunktDefinisjon().getAksjonspunktType());
    }

    public boolean erManueltOpprettet() {
        return this.aksjonspunktDefinisjon.getAksjonspunktType() != null && this.aksjonspunktDefinisjon.getAksjonspunktType().erOverstyringpunkt();
    }

    void setBehandlingsresultat(Behandling behandling) {
        // brukes kun internt for å koble sammen aksjonspunkt og behandling
        this.behandling = behandling;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public AksjonspunktStatus getStatus() {
        return status;
    }

    public boolean tilbakehoppVedGjenopptakelse() {
        return aksjonspunktDefinisjon.tilbakehoppVedGjenopptakelse();
    }

    /**
     * Sett til utført med gitt begrunnelse. Returner true dersom ble endret, false dersom allerede var utfør og hadde
     * samme begrunnelse.
     *
     * @return true hvis status eller begrunnelse er endret.
     */
    boolean setStatus(AksjonspunktStatus nyStatus) {
        boolean statusEndret = !Objects.equals(getStatus(), nyStatus);

        if (statusEndret) {
            if (Objects.equals(nyStatus, AksjonspunktStatus.UTFØRT)) {
                validerIkkeAvbruttAllerede();
            }
            this.status = nyStatus;
        }

        return statusEndret;
    }

    public BehandlingStegType getBehandlingStegFunnet() {
        return behandlingSteg;
    }

    public LocalDateTime getFristTid() {
        return fristTid;
    }

    void setFristTid(LocalDateTime fristTid) {
        this.fristTid = fristTid;
    }

    public boolean erOpprettet() {
        return Objects.equals(getStatus(), AksjonspunktStatus.OPPRETTET);
    }

    public boolean erÅpentAksjonspunkt() {
        return status.erÅpentAksjonspunkt();
    }

    public boolean erBehandletAksjonspunkt() {
        return status.erBehandletAksjonspunkt();
    }

    static Optional<Aksjonspunkt> finnEksisterende(Behandling behandling, AksjonspunktDefinisjon ap) {
        return behandling.getAlleAksjonspunkterInklInaktive().stream()
                .filter(a -> a.getAksjonspunktDefinisjon().equals(ap))
                .findFirst();
    }

    /**
     * Returner liste av abstraktpunkt definisjon koder.
     */
    public static List<String> getKoder(List<Aksjonspunkt> abstraktpunkter) {
        return abstraktpunkter.stream().map(ap -> ap.getAksjonspunktDefinisjon().getKode()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Aksjonspunkt)) {
            return false;
        }
        Aksjonspunkt kontrollpunkt = (Aksjonspunkt) object;
        return Objects.equals(getAksjonspunktDefinisjon(), kontrollpunkt.getAksjonspunktDefinisjon())
                && Objects.equals(getBehandling(), kontrollpunkt.getBehandling())
                && Objects.equals(getStatus(), kontrollpunkt.getStatus())
                && Objects.equals(getFristTid(), kontrollpunkt.getFristTid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAksjonspunktDefinisjon(), getBehandling(), getStatus(), getFristTid());
    }

    public boolean isToTrinnsBehandling() {
        return toTrinnsBehandling || aksjonspunktDefinisjon.getDefaultTotrinnBehandling();
    }

    void settToTrinnsFlag() {
        validerIkkeUtførtAvbruttAllerede();
        this.setToTrinnsBehandling(true);
    }

    void fjernToTrinnsFlagg() {
        validerIkkeUtførtAvbruttAllerede();
        this.setToTrinnsBehandling(false);
    }

    private void validerIkkeUtførtAvbruttAllerede() {
        if (erUtført() || erAvbrutt()) {
            // TODO (FC): håndteres av låsing allerede? Kaster exception nå for å se om GUI kan være ute av synk.
            throw new IllegalStateException("Forsøkte å bekrefte et allerede lukket aksjonspunkt:" + this); //$NON-NLS-1$
        }
    }

    private void validerIkkeAvbruttAllerede() {
        if (erAvbrutt()) {
            throw new IllegalStateException("Forsøkte å bekrefte et allerede lukket aksjonspunkt:" + this); //$NON-NLS-1$
        }
    }

    public Set<VurderPåNyttÅrsak> getVurderPåNyttÅrsaker() {
        return vurderPåNyttÅrsaker;
    }

    public Venteårsak getVenteårsak() {
        return venteårsak;
    }

    void setVenteårsak(Venteårsak venteårsak) {
        this.venteårsak = venteårsak;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Intern Builder. Bruk BehandlingskontrollTjeneste til å legge til og endre {@link Aksjonspunkt}.
     */
    static class Builder {
        private Aksjonspunkt opprinneligAp;
        private Aksjonspunkt aksjonspunkt;
        private boolean slettet = false;

        Builder(AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingStegType behandlingStegFunnet) {
            this.aksjonspunkt = new Aksjonspunkt(aksjonspunktDefinisjon, behandlingStegFunnet);
        }

        Builder(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
            this.aksjonspunkt = new Aksjonspunkt(aksjonspunktDefinisjon);
        }

        Builder(Aksjonspunkt opprinneligAp) {
            this.opprinneligAp = opprinneligAp;
            this.aksjonspunkt = new Aksjonspunkt(opprinneligAp.getAksjonspunktDefinisjon());
        }

        Aksjonspunkt buildFor(Behandling behandling) {
            Aksjonspunkt ap = this.aksjonspunkt;
            if (this.opprinneligAp != null) {
                kopierAlleFelter(opprinneligAp, ap, true);
            }
            Optional<Aksjonspunkt> eksisterende = finnEksisterende(behandling, ap.aksjonspunktDefinisjon);
            if (slettet) {
                InternalUtil.fjernAksjonspunkt(behandling, ap);
                return ap;
            } else if (eksisterende.isPresent()) {
                // Oppdater eksisterende. Aktiver dersom ikke allerede aktivt.
                Aksjonspunkt eksisterendeAksjonspunkt = eksisterende.get();
                kopierBasisfelter(ap, eksisterendeAksjonspunkt);
                return eksisterendeAksjonspunkt;
            } else {
                // Opprett ny og knytt til behandlingsresultat
                ap.setBehandlingsresultat(behandling);
                InternalUtil.leggTilAksjonspunkt(behandling, ap);
                return ap;
            }
        }

        private void kopierAlleFelter(Aksjonspunkt fra, Aksjonspunkt til, boolean medTotrinnsfelter) {
            kopierBasisfelter(fra, til);
            if (medTotrinnsfelter) {
                til.setToTrinnsBehandling(fra.isToTrinnsBehandling());
            }
            if (fra.getVurderPåNyttÅrsaker() != null) {
                for (VurderPåNyttÅrsak årsak : fra.getVurderPåNyttÅrsaker()) {
                    til.getVurderPåNyttÅrsaker().add(new VurderPåNyttÅrsak(årsak.getÅrsaksType(), til));
                }
            }
            til.setBehandlingSteg(fra.getBehandlingStegFunnet());
        }

        private void kopierBasisfelter(Aksjonspunkt fra, Aksjonspunkt til) {
            til.setVenteårsak(fra.getVenteårsak());
            til.setFristTid(fra.getFristTid());
            til.setStatus(fra.getStatus());
        }

        Aksjonspunkt.Builder medFristTid(LocalDateTime fristTid) {
            aksjonspunkt.setFristTid(fristTid);
            return this;
        }

        Aksjonspunkt.Builder medVenteårsak(Venteårsak venteårsak) {
            aksjonspunkt.setVenteårsak(venteårsak);
            return this;
        }

        Aksjonspunkt.Builder medSletting() {
            slettet = true;
            return this;
        }

        Aksjonspunkt.Builder medTotrinnskontroll(boolean toTrinnsbehandling) {
            aksjonspunkt.setToTrinnsBehandling(toTrinnsbehandling);
            return this;
        }
    }

    public boolean erUtført() {
        return Objects.equals(status, AksjonspunktStatus.UTFØRT);
    }

    public boolean erAvbrutt() {
        return Objects.equals(status, AksjonspunktStatus.AVBRUTT);
    }

    @Override
    public String toString() {
        return "Aksjonspunkt{" +
                "id=" + id +
                ", aksjonspunktDefinisjon=" + getAksjonspunktDefinisjon() +
                ", status=" + status +
                ", behandlingStegFunnet=" + getBehandlingStegFunnet() +
                ", versjon=" + versjon +
                ", vurderPåNyttÅrsaker=" + vurderPåNyttÅrsaker +
                ", toTrinnsBehandling=" + isToTrinnsBehandling() +
                ", fristTid=" + getFristTid() +
                '}';
    }

    void setBehandlingSteg(BehandlingStegType stegType) {
        this.behandlingSteg = stegType;
    }

    private void setToTrinnsBehandling(boolean toTrinnsBehandling) {
        this.toTrinnsBehandling = toTrinnsBehandling;
    }

}
