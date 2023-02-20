package no.nav.foreldrepenger.tilbakekreving.behandlingslager;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

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
        this.opprettetAv = finnBrukernavn();
        this.opprettetTidspunkt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        throw new IllegalStateException("Entiteten er stengt for oppdateringer");
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    private static String finnBrukernavn() {
        return Optional.ofNullable(KontekstHolder.getKontekst()).map(Kontekst::getKompaktUid)
            .orElse(BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES);
    }
}
