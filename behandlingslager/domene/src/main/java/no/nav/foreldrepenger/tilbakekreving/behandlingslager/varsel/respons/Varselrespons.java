package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Varselrespons")
@Table(name = "mottaker_varsel_respons")
public class Varselrespons extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOTTAKER_VARSEL_RESPONS")
    private Long id;

    @Column(name = "behandling_id", nullable = false)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "akseptert_faktagrunnlag")
    private Boolean akseptertFaktagrunnlag;

    @Column(name = "kilde", nullable = false)
    private String kilde;

    private Varselrespons() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Boolean getAkseptertFaktagrunnlag() {
        return akseptertFaktagrunnlag;
    }

    public void setAkseptertFaktagrunnlag(boolean akseptertFaktagrunnlag) {
        this.akseptertFaktagrunnlag = akseptertFaktagrunnlag;
    }

    public String getKilde() {
        return kilde;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Varselrespons kladd;

        public Builder() {
            kladd = new Varselrespons();
        }

        public Builder medBehandlingId(Long behandlingId) {
            kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder setAkseptertFaktagrunnlag(Boolean akseptertFaktagrunnlag) {
            kladd.akseptertFaktagrunnlag = akseptertFaktagrunnlag;
            return this;
        }

        public Builder setKilde(String kilde) {
            kladd.kilde = kilde;
            return this;
        }

        public Varselrespons build() {
            Objects.requireNonNull(kladd.behandlingId);
            Objects.requireNonNull(kladd.kilde);
            return kladd;
        }
    }

}
