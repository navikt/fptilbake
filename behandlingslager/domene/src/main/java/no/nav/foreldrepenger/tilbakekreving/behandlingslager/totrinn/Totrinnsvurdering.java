package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Totrinnsvurdering")
@Table(name = "TOTRINNSVURDERING")
public class Totrinnsvurdering extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TOTRINNSVURDERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @Convert(converter = AksjonspunktDefinisjon.KodeverdiConverter.class)
    @Column(name = "aksjonspunkt_def", nullable = false, updatable = false)
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "totrinnsvurdering")
    private Set<VurderÅrsakTotrinnsvurdering> vurderÅrsaker = new HashSet<>();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "godkjent", nullable = false)
    private Boolean godkjent;

    @Column(name = "begrunnelse")
    private String begrunnelse;


    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Totrinnsvurdering() {
        // for hibernate
    }


    public Set<VurderÅrsakTotrinnsvurdering> getVurderÅrsaker() {
        return vurderÅrsaker;
    }

    public void leggTilVurderÅrsakTotrinnsvurdering(VurderÅrsak vurderÅrsak) {
        VurderÅrsakTotrinnsvurdering vurderPåNyttÅrsak = new VurderÅrsakTotrinnsvurdering(vurderÅrsak, this);
        vurderÅrsaker.add(vurderPåNyttÅrsak);
    }

    public Boolean isGodkjent() {
        return godkjent;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public void disable() {
        this.aktiv = false;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private Totrinnsvurdering totrinnsvurderingMal = new Totrinnsvurdering();

        public Builder medAksjonspunktDefinisjon(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
            this.totrinnsvurderingMal.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
            return this;
        }

        public Builder medBehandling(Behandling behandling) {
            this.totrinnsvurderingMal.behandling = behandling;
            return this;
        }

        public Builder medGodkjent(boolean godkjent) {
            totrinnsvurderingMal.godkjent = godkjent;
            return this;
        }


        public Builder medBegrunnelse(String begrunnelse) {
            totrinnsvurderingMal.begrunnelse = begrunnelse;
            return this;
        }

        public Builder medVurderÅrsaker(Set<VurderÅrsakTotrinnsvurdering> vurderÅrsaker) {
            totrinnsvurderingMal.getVurderÅrsaker().addAll(vurderÅrsaker);
            return this;
        }

        public Totrinnsvurdering build() {
            verifyStateForBuild();
            return totrinnsvurderingMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(totrinnsvurderingMal.aksjonspunktDefinisjon, "aksjonspunktDefinisjon");
            Objects.requireNonNull(totrinnsvurderingMal.behandling, "behandling");

        }
    }
}
