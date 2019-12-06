package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "IverksattVedtak")
@Table(name = "IVERKSATT_VEDTAK")
public class IverksattVedtak extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IVERKSATT_VEDTAK")
    private Long id;

    @Column(name = "behandling_id", nullable = false, unique = true)
    private Long behandlingId;

    @Column(name = "iverksatt_dato", nullable = false, updatable = false)
    private LocalDate iverksattDato;

    @Column(name = "oko_vedtak_id")
    private String økonomiVedtakId;

    @Column(name = "brutto_tilbakekreves")
    private BigDecimal tilbakekrevesBruttoUtenRenter;

    @Column(name = "netto_tilbakekreves")
    private BigDecimal tilbakekrevesNettoUtenRenter;

    @Column(name = "renter")
    private BigDecimal renter;

    @Column(name = "skatt")
    private BigDecimal skatt;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean brukt;
        private IverksattVedtak kladd = new IverksattVedtak();

        public Builder medBehandlingId(Long behandlingId) {
            kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medIverksattDato(LocalDate iverksattDato) {
            kladd.iverksattDato = iverksattDato;
            return this;
        }

        public Builder medØkonomiVedtakId(BigInteger økonomiVedtakId) {
            kladd.økonomiVedtakId = økonomiVedtakId.toString();
            return this;
        }

        public Builder medTilbakekrevesBruttoUtenRenter(BigDecimal tilbakekrevesBruttoUtenRenter) {
            kladd.tilbakekrevesBruttoUtenRenter = tilbakekrevesBruttoUtenRenter;
            return this;
        }

        public Builder medTilbakekrevesNettoUtenRenter(BigDecimal tilbakekrevesNettoUtenRenter) {
            kladd.tilbakekrevesNettoUtenRenter = tilbakekrevesNettoUtenRenter;
            return this;
        }

        public Builder medRenter(BigDecimal renter) {
            kladd.renter = renter;
            return this;
        }

        public Builder medSkatt(BigDecimal skatt) {
            kladd.skatt = skatt;
            return this;
        }

        public IverksattVedtak build() {
            if (brukt) {
                throw new IllegalArgumentException("this.build() er allerede brukt, lag ny builder!");
            }
            Objects.requireNonNull(kladd.behandlingId, "mangler behandlingId");
            Objects.requireNonNull(kladd.iverksattDato, "mangler iverksattDato");
            Objects.requireNonNull(kladd.økonomiVedtakId, "mangler økonomiVedtakId");
            Objects.requireNonNull(kladd.tilbakekrevesBruttoUtenRenter, "mangler tilbakekrevesBruttoUtenRenter");
            Objects.requireNonNull(kladd.tilbakekrevesNettoUtenRenter, "mangler tilbakekrevesNettoUtenRenter");
            Objects.requireNonNull(kladd.renter, "mangler renter");
            Objects.requireNonNull(kladd.skatt, "mangler skatt");
            brukt = true;
            return kladd;
        }
    }

}
