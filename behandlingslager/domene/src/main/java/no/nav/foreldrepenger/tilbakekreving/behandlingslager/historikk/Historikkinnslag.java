package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

@Entity(name = "Historikkinnslag")
@Table(name = "HISTORIKKINNSLAG")
public class Historikkinnslag extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_HISTORIKKINNSLAG")
    private Long id;

    @Column(name = "tekst")
    private String tekst;

    @Column(name = "behandling_id")
    private Long behandlingId;

    @Column(name = "fagsak_id", nullable = false)
    private Long fagsakId;

    @Convert(converter = HistorikkAktør.KodeverdiConverter.class)
    @Column(name = "historikk_aktoer_id", nullable = false)
    private HistorikkAktør aktør = HistorikkAktør.UDEFINERT;

    @Convert(converter = HistorikkinnslagType.KodeverdiConverter.class)
    @Column(name = "historikkinnslag_type", nullable = false)
    private HistorikkinnslagType type = HistorikkinnslagType.UDEFINIERT;

    @OneToMany(mappedBy = "historikkinnslag", cascade = CascadeType.ALL)
    private List<HistorikkinnslagDokumentLink> dokumentLinker = new ArrayList<>();

    @OneToMany(mappedBy = "historikkinnslag")
    private List<HistorikkinnslagDel> historikkinnslagDeler = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @deprecated Erstattet med {@link #getHistorikkinnslagDeler}
     */
    @Deprecated
    public String getTekst() {
        return tekst;
    }

    /**
     * @deprecated Erstattet med {@link #setHistorikkinnslagDeler}
     */
    @Deprecated
    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandling(Behandling behandling) {
        this.behandlingId = behandling.getId();
        this.fagsakId = behandling.getFagsakId();
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public HistorikkAktør getAktør() {
        return Objects.equals(HistorikkAktør.UDEFINERT, aktør) ? null : aktør;
    }

    public void setAktør(HistorikkAktør aktør) {
        this.aktør = aktør == null ? HistorikkAktør.UDEFINERT : aktør;
    }

    public HistorikkinnslagType getType() {
        return type;
    }

    public void setType(HistorikkinnslagType type) {
        this.type = type;
    }

    public List<HistorikkinnslagDokumentLink> getDokumentLinker() {
        return dokumentLinker;
    }

    public void setDokumentLinker(List<HistorikkinnslagDokumentLink> dokumentLinker) {
        this.dokumentLinker = dokumentLinker;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public void setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
    }

    public List<HistorikkinnslagDel> getHistorikkinnslagDeler() {
        return historikkinnslagDeler;
    }

    public void setHistorikkinnslagDeler(List<HistorikkinnslagDel> historikkinnslagDeler) {
        historikkinnslagDeler.forEach(del -> HistorikkinnslagDel.builder(del).medHistorikkinnslag(this));
        this.historikkinnslagDeler = historikkinnslagDeler;
    }

    public static class Builder {
        private final Historikkinnslag historikkinnslag;

        public Builder() {
            historikkinnslag = new Historikkinnslag();
        }

        public Builder medBehandlingId(Long behandlingId) {
            historikkinnslag.behandlingId = behandlingId;
            return this;
        }

        public Builder medFagsakId(Long fagsakId) {
            historikkinnslag.fagsakId = fagsakId;
            return this;
        }

        public Builder medAktør(HistorikkAktør historikkAktør) {
            historikkinnslag.aktør = historikkAktør;
            return this;
        }

        public Builder medType(HistorikkinnslagType type) {
            historikkinnslag.type = type;
            return this;
        }

        public Builder medDokumentLinker(List<HistorikkinnslagDokumentLink> dokumentLinker) {
            if (historikkinnslag.dokumentLinker == null) {
                historikkinnslag.dokumentLinker = dokumentLinker;
            } else if (dokumentLinker != null) {
                historikkinnslag.dokumentLinker.addAll(dokumentLinker);
            }
            return this;
        }

        public Historikkinnslag build() {
            return historikkinnslag;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Historikkinnslag)) {
            return false;
        }
        Historikkinnslag that = (Historikkinnslag) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getBehandlingId(), that.getBehandlingId()) &&
                Objects.equals(getFagsakId(), that.getFagsakId()) &&
                Objects.equals(getAktør(), that.getAktør()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getDokumentLinker(), that.getDokumentLinker());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBehandlingId(), getFagsakId(), getAktør(), getType(), getDokumentLinker());
    }
}
