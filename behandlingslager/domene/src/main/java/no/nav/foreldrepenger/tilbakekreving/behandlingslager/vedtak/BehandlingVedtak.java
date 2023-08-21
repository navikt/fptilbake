package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;

@Entity(name = "BehandlingVedtak")
@Table(name = "BEHANDLING_VEDTAK")
public class BehandlingVedtak extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_VEDTAK")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "VEDTAK_DATO", nullable = false)
    private LocalDate vedtaksdato;

    @Column(name = "ANSVARLIG_SAKSBEHANDLER", nullable = false)
    private String ansvarligSaksbehandler;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BEHANDLING_RESULTAT_ID", nullable = false, updatable = false, unique = true)
    private Behandlingsresultat behandlingsresultat;

    @Convert(converter = IverksettingStatus.KodeverdiConverter.class)
    @Column(name = "iverksetting_status", nullable = false)
    private IverksettingStatus iverksettingStatus = IverksettingStatus.UDEFINERT;

    private BehandlingVedtak() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public LocalDate getVedtaksdato() {
        return vedtaksdato;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public Behandlingsresultat getBehandlingsresultat() {
        return behandlingsresultat;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof BehandlingVedtak)) {
            return false;
        }
        BehandlingVedtak vedtak = (BehandlingVedtak) object;
        return Objects.equals(vedtaksdato, vedtak.getVedtaksdato())
                && Objects.equals(ansvarligSaksbehandler, vedtak.getAnsvarligSaksbehandler());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vedtaksdato, ansvarligSaksbehandler);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BehandlingVedtak.Builder builderEndreEksisterende(BehandlingVedtak behandlingVedtak) {
        return new BehandlingVedtak.Builder(behandlingVedtak, true);
    }

    public IverksettingStatus getIverksettingStatus() {
        return Objects.equals(IverksettingStatus.UDEFINERT, iverksettingStatus) ? null : iverksettingStatus;
    }

    public void setIverksettingStatus(IverksettingStatus iverksettingStatus) {
        this.iverksettingStatus = iverksettingStatus == null ? IverksettingStatus.UDEFINERT : iverksettingStatus;
    }

    public static class Builder {
        private BehandlingVedtak kladd = new BehandlingVedtak();

        public Builder() {
            // tom constructor
        }

        Builder(BehandlingVedtak gammeltResultat, boolean endreEksisterende) {
            if (endreEksisterende) {
                kladd = gammeltResultat;
            }
        }

        public Builder medVedtaksdato(LocalDate vedtaksdato) {
            this.kladd.vedtaksdato = vedtaksdato;
            return this;
        }

        public Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            this.kladd.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public Builder medBehandlingsresultat(Behandlingsresultat behandlingsresultat) {
            this.kladd.behandlingsresultat = behandlingsresultat;
            return this;
        }

        public Builder medIverksettingStatus(IverksettingStatus iverksettingStatus) {
            this.kladd.iverksettingStatus = iverksettingStatus;
            return this;
        }


        public BehandlingVedtak build() {
            verifyStateForBuild();
            return kladd;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(this.kladd.vedtaksdato, "vedtaksdato");
            Objects.requireNonNull(this.kladd.ansvarligSaksbehandler, "ansvarligSaksbehandler");
        }
    }

}
