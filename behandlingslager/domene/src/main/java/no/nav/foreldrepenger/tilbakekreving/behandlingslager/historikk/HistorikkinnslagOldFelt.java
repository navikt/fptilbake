package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@Entity(name = "HistorikkinnslagFelt")
@Table(name = "HISTORIKKINNSLAG_FELT")
public class HistorikkinnslagOldFelt extends BaseEntitet implements IndexKey {

    private static final String DISCRIMINATOR = "HISTORIKKINNSLAG_FELT_TYPE";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_HISTORIKKINNSLAG_FELT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historikkinnslag_del_id", nullable = false, updatable = false)
    @JsonBackReference
    private HistorikkinnslagOldDel historikkinnslagDel;

    @Convert(converter = HistorikkinnslagFeltType.KodeverdiConverter.class)
    @Column(name = "historikkinnslag_felt_type", nullable = false)
    private HistorikkinnslagFeltType feltType;

    @Column(name = "navn")
    private String navn;

    @Column(name = "navn_verdi")
    private String navnVerdi;

    @Column(name = "fra_verdi")
    private String fraVerdi;

    @Column(name = "til_verdi")
    private String tilVerdi;

    @Column(name = "fra_verdi_kode")
    private String fraVerdiKode;

    @Column(name = "til_verdi_kode")
    private String tilVerdiKode;

    @Column(name = "kl_fra_verdi")
    private String klFraVerdi;

    @Column(name = "kl_til_verdi")
    private String klTilVerdi;

    @Column(name = "kl_navn")
    private String klNavn;

    @Column(name = "sekvens_nr")
    private Integer sekvensNr;

    protected HistorikkinnslagOldFelt() {

    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(this.sekvensNr, this.feltType, this.navn, this.navnVerdi);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public HistorikkinnslagOldDel getHistorikkinnslagDel() {
        return historikkinnslagDel;
    }

    void setHistorikkinnslagDel(HistorikkinnslagOldDel historikkinnslagDel) {
        this.historikkinnslagDel = historikkinnslagDel;
    }

    public HistorikkinnslagFeltType getFeltType() {
        return feltType;
    }

    public String getNavn() {
        return navn;
    }

    public String getNavnVerdi() {
        return navnVerdi;
    }

    public String getKlNavn() {
        return klNavn;
    }

    public String getFraVerdi() {
        return fraVerdi == null ? fraVerdiKode : fraVerdi;
    }

    public String getFraVerdiKode() {
        return fraVerdiKode;
    }

    public String getTilVerdi() {
        return tilVerdi == null ? tilVerdiKode : tilVerdi;
    }

    public String getTilVerdiKode() {
        return tilVerdiKode;
    }

    public String getKlFraVerdi() {
        return klFraVerdi;
    }

    public String getKlTilVerdi() {
        return klTilVerdi;
    }

    public Integer getSekvensNr() {
        return sekvensNr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorikkinnslagOldFelt)) {
            return false;
        }
        HistorikkinnslagOldFelt that = (HistorikkinnslagOldFelt) o;
        return Objects.equals(feltType, that.feltType)
                && Objects.equals(navn, that.navn)
                && Objects.equals(navnVerdi, that.navnVerdi)
                && Objects.equals(klNavn, that.klNavn)
                && Objects.equals(klFraVerdi, that.klFraVerdi)
                && Objects.equals(klTilVerdi, that.klFraVerdi)
                && Objects.equals(sekvensNr, that.sekvensNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feltType, navn, navnVerdi, klNavn, klFraVerdi, klTilVerdi, sekvensNr);
    }

    public static class Builder {
        private HistorikkinnslagOldFelt kladd;

        private Builder() {
            kladd = new HistorikkinnslagOldFelt();
        }


        public Builder medFeltType(HistorikkinnslagFeltType feltType) {
            kladd.feltType = feltType;
            return this;
        }

        public Builder medNavnVerdi(String navnVerdi) {
            kladd.navnVerdi = navnVerdi;
            return this;
        }

        public Builder medNavn(Kodeverdi kodeverdi) {
            kladd.navn = kodeverdi.getKode();
            kladd.klNavn = kodeverdi.getKodeverk();
            return this;
        }

        public Builder medFraVerdi(String fraVerdi) {
            kladd.fraVerdi = fraVerdi;
            return this;
        }

        public Builder medFraVerdi(Kodeverdi fraVerdi) {
            if (fraVerdi != null) {
                kladd.fraVerdiKode = fraVerdi.getKode();
                kladd.klFraVerdi = fraVerdi.getKodeverk();
            } else {
                kladd.fraVerdiKode = null;
            }
            return this;
        }

        public Builder medTilVerdi(String tilVerdi) {
            kladd.tilVerdi = tilVerdi;
            return this;
        }

        public <K extends Kodeverdi> Builder medTilVerdi(K tilVerdi) {
            if (tilVerdi != null) {
                kladd.tilVerdiKode = tilVerdi.getKode();
                kladd.klTilVerdi = tilVerdi.getKodeverk();
            }
            return this;
        }

        public Builder medSekvensNr(Integer sekvensNr) {
            kladd.sekvensNr = sekvensNr;
            return this;
        }

        public HistorikkinnslagOldFelt build(HistorikkinnslagOldDel.Builder historikkinnslagDelBuilder) {
            historikkinnslagDelBuilder.leggTilFelt(kladd);
            return kladd;
        }
    }
}
