package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;

public class AksjonspunktDto {
    private AksjonspunktDefinisjon definisjon;
    private AksjonspunktStatus status;
    private Boolean toTrinnsBehandling;
    private Boolean toTrinnsBehandlingGodkjent;
    private Set<VurderÅrsak> vurderPaNyttArsaker;
    private String besluttersBegrunnelse;
    private AksjonspunktType aksjonspunktType;
    private Boolean kanLoses;
    private Boolean erAktivt;

    public AksjonspunktDto() {
        // Jackson
    }

    public void setDefinisjon(AksjonspunktDefinisjon definisjon) {
        this.definisjon = definisjon;
    }

    public void setStatus(AksjonspunktStatus status) {
        this.status = status;
    }

    public void setToTrinnsBehandling(Boolean toTrinnsBehandling) {
        this.toTrinnsBehandling = toTrinnsBehandling;
    }

    public void setToTrinnsBehandlingGodkjent(Boolean toTrinnsBehandlingGodkjent) {
        this.toTrinnsBehandlingGodkjent = toTrinnsBehandlingGodkjent;
    }

    public void setVurderPaNyttArsaker(Set<VurderÅrsak> vurderPaNyttArsaker) {
        this.vurderPaNyttArsaker = vurderPaNyttArsaker;
    }

    public void setBesluttersBegrunnelse(String besluttersBegrunnelse) {
        this.besluttersBegrunnelse = besluttersBegrunnelse;
    }

    public void setAksjonspunktType(AksjonspunktType aksjonspunktType) {
        this.aksjonspunktType = aksjonspunktType;
    }

    public void setKanLoses(Boolean kanLoses) {
        this.kanLoses = kanLoses;
    }

    public void setErAktivt(Boolean erAktivt) {
        this.erAktivt = erAktivt;
    }

    public AksjonspunktDefinisjon getDefinisjon() {
        return definisjon;
    }

    public AksjonspunktStatus getStatus() {
        return status;
    }

    public Boolean getToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    public Set<VurderÅrsak> getVurderPaNyttArsaker() {
        return vurderPaNyttArsaker;
    }

    public String getBesluttersBegrunnelse() {
        return besluttersBegrunnelse;
    }

    public Boolean getToTrinnsBehandlingGodkjent() {
        return toTrinnsBehandlingGodkjent;
    }

    public AksjonspunktType getAksjonspunktType() {
        return aksjonspunktType;
    }

    public Boolean getKanLoses() {
        return kanLoses;
    }

    public Boolean getErAktivt() {
        return erAktivt;
    }

}
