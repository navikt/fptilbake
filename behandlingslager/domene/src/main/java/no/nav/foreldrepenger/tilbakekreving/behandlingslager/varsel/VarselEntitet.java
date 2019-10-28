package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VarselEntitet")
@Table(name = "varsel")
public class VarselEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOTTAKER_VARSEL_RESPONS")
    private Long id;

    @Column(name = "behandling_id", nullable = false)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Column(name = "varsel_tekst")
    private String varselTekst;

    @Column(name = "varsel_beloep")
    private Long varselBeløp;

    private VarselEntitet() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public String getVarselTekst() {
        return varselTekst;
    }

    public Long getVarselBeløp() {
        return varselBeløp;
    }

    public void disable() {
        this.aktiv = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VarselEntitet kladd = new VarselEntitet();

        public Builder medBehandlingId(Long behandlingId) {
            kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medAktiv(boolean aktiv) {
            kladd.aktiv = aktiv;
            return this;
        }

        public Builder medVarselTekst(String varselTekst) {
            kladd.varselTekst = varselTekst;
            return this;
        }

        public Builder medVarselBeløp(Long varselBeløp) {
            kladd.varselBeløp = varselBeløp;
            return this;
        }

        public VarselEntitet build() {
            Objects.requireNonNull(kladd.behandlingId);
            return kladd;
        }
    }
}
