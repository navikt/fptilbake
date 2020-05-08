package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabell;


@Entity(name = "BehandlingStegType")
@Table(name = "BEHANDLING_STEG_TYPE")
public class BehandlingStegType extends KodeverkTabell {

    public static final BehandlingStegType VARSEL = new BehandlingStegType("VARSELSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType FORESLÅ_VEDTAK = new BehandlingStegType("FORVEDSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType FATTE_VEDTAK = new BehandlingStegType("FVEDSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType IVERKSETT_VEDTAK = new BehandlingStegType("IVEDSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType TBKGSTEG = new BehandlingStegType("TBKGSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType FORELDELSEVURDERINGSTEG = new BehandlingStegType("VFORELDETSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType FAKTA_FEILUTBETALING = new BehandlingStegType("FAKTFEILUTSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType FAKTA_VERGE = new BehandlingStegType("FAKTAVERGESTEG"); //$NON-NLS-1$
    public static final BehandlingStegType VTILBSTEG = new BehandlingStegType("VTILBSTEG");

    @Valid
    @Size(max = 10)
    @OneToMany(mappedBy = "behandlingSteg")
    protected List<VurderingspunktDefinisjon> vurderingspunkter = new ArrayList<>();

    /**
     * Definisjon av hvilken status behandlingen skal rapporteres som når dette steget er aktivt.
     */
    @Valid
    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_status_def", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingStatus.DISCRIMINATOR
                    + "'"))})
    private BehandlingStatus definertBehandlingStatus;

    protected BehandlingStegType() {
        // Hibernate trenger denne
    }

    protected BehandlingStegType(String kode) {
        super(kode);
    }

    // har brukt bare for å teste behandling med steg og status
    protected BehandlingStegType(String kode, BehandlingStatus behandlingStatus) {
        super(kode);
        this.definertBehandlingStatus = behandlingStatus;
    }

    public BehandlingStatus getDefinertBehandlingStatus() {
        validerBehandlingStatusHentet();
        return definertBehandlingStatus;
    }

    private void validerBehandlingStatusHentet() {
        if (definertBehandlingStatus == null) {
            throw new IllegalArgumentException(
                    "Denne koden er ikke hentet fra databasen, kan ikke brukes til å konfigurere steg (kun skriving):" + this); //$NON-NLS-1$
        }
    }

    public List<VurderingspunktDefinisjon> getVurderingspunkter() {
        return Collections.unmodifiableList(vurderingspunkter);
    }

    public Optional<VurderingspunktDefinisjon> getVurderingspunktInngang() {
        return Optional.ofNullable(finnVurderingspunkt(VurderingspunktDefinisjon.Type.INNGANG));
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjonerInngang() {
        Optional<VurderingspunktDefinisjon> vurd = getVurderingspunktInngang();
        if (!vurd.isPresent()) {
            return Collections.emptyList();
        } else {
            return vurd.get().getAksjonspunktDefinisjoner();
        }
    }

    public Optional<VurderingspunktDefinisjon> getVurderingspunktUtgang() {
        return Optional.ofNullable(finnVurderingspunkt(VurderingspunktDefinisjon.Type.UTGANG));
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjonerUtgang() {
        Optional<VurderingspunktDefinisjon> vurd = getVurderingspunktUtgang();
        if (!vurd.isPresent()) {
            return Collections.emptyList();
        } else {
            return vurd.get().getAksjonspunktDefinisjoner();
        }
    }

    private VurderingspunktDefinisjon finnVurderingspunkt(VurderingspunktDefinisjon.Type type) {
        List<VurderingspunktDefinisjon> list = vurderingspunkter.stream()
                .filter(v -> Objects.equals(type, v.getType()))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new IllegalStateException("Mer enn en definisjon matcher type : " + type.getDbKode() + ": " + list); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
