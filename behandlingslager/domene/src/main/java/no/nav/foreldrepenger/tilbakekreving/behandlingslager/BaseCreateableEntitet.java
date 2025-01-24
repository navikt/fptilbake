package no.nav.foreldrepenger.tilbakekreving.behandlingslager;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@MappedSuperclass
public class BaseCreateableEntitet implements Serializable {

    private static final String BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES = "VL";

    @Column(name = "opprettet_av", nullable = false)
    private String opprettetAv;

    @Column(name = "opprettet_tid", nullable = false)
    private LocalDateTime opprettetTidspunkt;

    @PrePersist
    protected void onCreate() {
        this.opprettetAv = opprettetAv != null ? opprettetAv : BaseCreateableEntitet.finnBrukernavn();
        this.opprettetTidspunkt = opprettetTidspunkt != null ? opprettetTidspunkt : LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        throw new IllegalStateException("Entiteten er stengt for oppdateringer");
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public void setOpprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    /**
     * Kan brukes til å eksplisitt sette opprettet tidspunkt, f.eks. ved migrering
     * av data fra et annet system. Ivaretar da opprinnelig tidspunkt istdf å sette
     * likt now().
     */
    public void setOpprettetTidspunkt(LocalDateTime opprettetTidspunkt) {
        this.opprettetTidspunkt = opprettetTidspunkt;
    }

    private static String finnBrukernavn() {
        return Optional.ofNullable(KontekstHolder.getKontekst()).map(Kontekst::getKompaktUid)
            .orElse(BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES);
    }
}
