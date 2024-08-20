package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VarselInfo")
@Table(name = "varsel")
public class VarselInfo extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VARSEL")
    private Long id;

    @Column(name = "behandling_id", nullable = false)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Column(name = "varsel_fritekst", nullable = false, updatable = false)
    private String varselTekst;

    @Column(name = "varsel_fritekst_utvidet")
    private String varselTekstUtvidet;

    @Column(name = "varsel_beloep")
    private Long varselBeløp;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    private VarselInfo() {
        // for hibernate
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public String getVarselTekst() {
        if (varselTekstUtvidet != null) {
            return varselTekstUtvidet;
        } else {
            return varselTekst;
        }
    }

    public Long getVarselBeløp() {
        return varselBeløp;
    }

    public void setVarselBeløp(Long varselBeløp) {
        this.varselBeløp = varselBeløp;
    }

    public void disable() {
        this.aktiv = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VarselInfo kladd = new VarselInfo();

        public Builder medBehandlingId(Long behandlingId) {
            kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medVarselTekst(String varselTekst) {
            kladd.varselTekstUtvidet = varselTekst;
            kladd.varselTekst = varselTekst;
            return this;
        }

        public Builder medVarselBeløp(Long varselBeløp) {
            kladd.varselBeløp = varselBeløp;
            return this;
        }

        public VarselInfo build() {
            Objects.requireNonNull(kladd.behandlingId);
            Objects.requireNonNull(kladd.varselTekst);
            return kladd;
        }
    }
}
