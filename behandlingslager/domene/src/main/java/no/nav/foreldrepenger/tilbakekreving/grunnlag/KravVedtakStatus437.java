package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@Entity(name = "KravVedtakStatus437")
@Table(name = "KRAV_VEDTAK_STATUS_437")
public class KravVedtakStatus437 extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KRAV_VEDTAK_STATUS_437")
    private Long id;

    @Column(name = "vedtak_id", nullable = false, updatable = false)
    private Long vedtakId;

    @Convert(converter = KravStatusKode.KodeverdiConverter.class)
    @Column(name = "krav_status_kode", nullable = false, updatable = false)
    private KravStatusKode kravStatusKode;

    @Convert(converter = FagOmrådeKode.KodeverdiConverter.class)
    @Column(name = "fag_omraade_kode", nullable = false)
    private FagOmrådeKode fagOmrådeKode;

    @Column(name = "fagsystem_id", nullable = false, updatable = false)
    private String fagSystemId;

    @Column(name = "gjelder_vedtak_id", nullable = false, updatable = false)
    private String gjelderVedtakId;

    @Convert(converter = GjelderType.KodeverdiConverter.class)
    @Column(name = "gjelder_type", nullable = false, updatable = false)
    private GjelderType gjelderType;

    @AttributeOverrides({
        @AttributeOverride(name = "henvisning", column = @Column(name = "referanse"))
    })
    private Henvisning referanse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVedtakId() {
        return vedtakId;
    }

    public KravStatusKode getKravStatusKode() {
        return kravStatusKode;
    }

    public FagOmrådeKode getFagOmrådeKode() {
        return fagOmrådeKode;
    }

    public String getFagSystemId() {
        return fagSystemId;
    }

    public String getGjelderVedtakId() {
        return gjelderVedtakId;
    }

    public GjelderType getGjelderType() {
        return gjelderType;
    }

    public Henvisning getReferanse() {
        return referanse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KravVedtakStatus437 that = (KravVedtakStatus437) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(vedtakId, that.vedtakId) &&
                Objects.equals(kravStatusKode, that.kravStatusKode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vedtakId, kravStatusKode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private KravVedtakStatus437 kladd = new KravVedtakStatus437();

        public Builder medVedtakId(Long vedtakId) {
            this.kladd.vedtakId = vedtakId;
            return this;
        }

        public Builder medKravStatusKode(KravStatusKode kravStatusKode) {
            this.kladd.kravStatusKode = kravStatusKode;
            return this;
        }

        public Builder medFagomraadeKode(FagOmrådeKode fagOmrådeKode) {
            this.kladd.fagOmrådeKode = fagOmrådeKode;
            return this;
        }

        public Builder medFagSystemId(String fagSystemId) {
            this.kladd.fagSystemId = fagSystemId;
            return this;
        }

        public Builder medGjelderVedtakId(String gjelderVedtakId) {
            this.kladd.gjelderVedtakId = gjelderVedtakId;
            return this;
        }

        public Builder medGjelderType(GjelderType gjelderType) {
            this.kladd.gjelderType = gjelderType;
            return this;
        }

        public Builder medReferanse(Henvisning referanse) {
            this.kladd.referanse = referanse;
            return this;
        }

        public KravVedtakStatus437 build() {
            Objects.requireNonNull(this.kladd.vedtakId, "vedtakId");
            Objects.requireNonNull(this.kladd.kravStatusKode, "kravStatusKode");
            Objects.requireNonNull(this.kladd.fagOmrådeKode, "fagOmrådeKode");
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "vedtakId=" + vedtakId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "kravStatusKode='" + kravStatusKode //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

}
