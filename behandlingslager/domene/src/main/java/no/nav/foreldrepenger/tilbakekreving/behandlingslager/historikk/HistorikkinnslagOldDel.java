package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

@Entity(name = "HistorikkinnslagDel")
@Table(name = "HISTORIKKINNSLAG_DEL")
public class HistorikkinnslagOldDel extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_HISTORIKKINNSLAG_DEL")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historikkinnslag_id", nullable = false, updatable = false)
    @JsonBackReference
    private HistorikkinnslagOld historikkinnslag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "historikkinnslagDel")
    private List<HistorikkinnslagOldFelt> historikkinnslagFelt = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public HistorikkinnslagOld getHistorikkinnslag() {
        return historikkinnslag;
    }

    public List<HistorikkinnslagOldFelt> getHistorikkinnslagFelt() {
        return historikkinnslagFelt;
    }

    public Optional<String> getAarsak() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.AARSAK);
    }

    public Optional<HistorikkinnslagOldFelt> getAarsakFelt() {
        return finnFelt(HistorikkinnslagFeltType.AARSAK);
    }

    public Optional<HistorikkinnslagOldFelt> getTema() {
        return finnFelt(HistorikkinnslagFeltType.ANGÅR_TEMA);
    }


    public Optional<HistorikkinnslagOldFelt> getAvklartSoeknadsperiode() {
        return finnFelt(HistorikkinnslagFeltType.AVKLART_SOEKNADSPERIODE);
    }

    public Optional<String> getBegrunnelse() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.BEGRUNNELSE);
    }

    public Optional<HistorikkinnslagOldFelt> getBegrunnelseFelt() {
        return finnFelt(HistorikkinnslagFeltType.BEGRUNNELSE);
    }

    /**
     * Hent en hendelse
     *
     * @return Et HistorikkinnslagFelt fordi vi trenger navn (f.eks. BEH_VENT) og tilVerdi (f.eks. <fristDato>)
     */
    public Optional<HistorikkinnslagOldFelt> getHendelse() {
        return finnFelt(HistorikkinnslagFeltType.HENDELSE);
    }

    public Optional<String> getResultat() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.RESULTAT);
    }

    public Optional<HistorikkinnslagOldFelt> getResultatFelt() {
        return finnFelt(HistorikkinnslagFeltType.RESULTAT);
    }

    public Optional<String> getGjeldendeFra() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.GJELDENDE_FRA);
    }

    public Optional<HistorikkinnslagOldFelt> getGjeldendeFraFelt() {
        return finnFelt(HistorikkinnslagFeltType.GJELDENDE_FRA);
    }

    public Optional<String> getSkjermlenke() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.SKJERMLENKE);
    }

    public Optional<HistorikkinnslagOldFelt> getEndretFelt(HistorikkEndretFeltType endretFeltVerdiType) {
        List<HistorikkinnslagOldFelt> endredeFelt = getEndredeFelt();
        return endredeFelt
                .stream()
                .filter(felt -> Objects.equals(endretFeltVerdiType.getKode(), felt.getNavn()))
                .findFirst();
    }

    public List<HistorikkinnslagOldFelt> getEndredeFelt() {
        return finnFeltListe(HistorikkinnslagFeltType.ENDRET_FELT);
    }

    public Optional<HistorikkinnslagOldFelt> getOpplysning(HistorikkOpplysningType historikkOpplysningType) {
        List<HistorikkinnslagOldFelt> opplysninger = getOpplysninger();
        return opplysninger.stream()
                .filter(felt -> Objects.equals(historikkOpplysningType.getKode(), felt.getNavn()))
                .findFirst();
    }

    public List<HistorikkinnslagOldFelt> getOpplysninger() {
        return finnFeltListe(HistorikkinnslagFeltType.OPPLYSNINGER);
    }

    public List<HistorikkinnslagTotrinnsvurdering> getTotrinnsvurderinger() {
        List<HistorikkinnslagFeltType> aksjonspunktFeltTypeKoder = List.of(HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE,
                HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT,
                HistorikkinnslagFeltType.AKSJONSPUNKT_KODE);

        List<HistorikkinnslagOldFelt> alleAksjonspunktFelt = historikkinnslagFelt.stream()
                .filter(felt -> aksjonspunktFeltTypeKoder.contains(felt.getFeltType()))
                .collect(Collectors.toList());

        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = alleAksjonspunktFelt.stream()
                .collect(Collectors.groupingBy(HistorikkinnslagOldFelt::getSekvensNr))
                .entrySet()
                .stream()
                .map(entry -> lagHistorikkinnslagAksjonspunkt(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getSekvensNr))
                .collect(Collectors.toList());
        return totrinnsvurderinger;
    }

    private HistorikkinnslagTotrinnsvurdering lagHistorikkinnslagAksjonspunkt(Integer sekvensNr, List<HistorikkinnslagOldFelt> aksjonspunktFeltListe) {
        HistorikkinnslagTotrinnsvurdering aksjonspunkt = new HistorikkinnslagTotrinnsvurdering(sekvensNr);
        aksjonspunktFeltListe.forEach(felt -> {
            if (HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE.equals(felt.getFeltType())) {
                aksjonspunkt.setBegrunnelse(felt.getTilVerdi());
            } else if (HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT.equals(felt.getFeltType())) {
                aksjonspunkt.setGodkjent(Boolean.parseBoolean(felt.getTilVerdi()));
            } else if (HistorikkinnslagFeltType.AKSJONSPUNKT_KODE.equals(felt.getFeltType())) {
                var aksjonspunktDefinisjon = AksjonspunktDefinisjon.fraKode(felt.getTilVerdi());
                aksjonspunkt.setAksjonspunktDefinisjon(aksjonspunktDefinisjon);
            } else {
                throw new IllegalStateException("Uventet feltnavn " + felt.getFeltType().getKode());
            }
        });
        return aksjonspunkt;
    }

    private Optional<HistorikkinnslagOldFelt> finnFelt(HistorikkinnslagFeltType historikkinnslagFeltType) {
        return historikkinnslagFelt.stream()
                .filter(felt -> historikkinnslagFeltType.equals(felt.getFeltType()))
                .findFirst();
    }

    private Optional<String> finnFeltTilVerdi(HistorikkinnslagFeltType historikkinnslagFeltType) {
        return finnFelt(historikkinnslagFeltType)
                .map(HistorikkinnslagOldFelt::getTilVerdi);
    }

    private List<HistorikkinnslagOldFelt> finnFeltListe(HistorikkinnslagFeltType feltType) {
        return historikkinnslagFelt.stream()
                .filter(felt -> felt.getFeltType().equals(feltType))
                .sorted(Comparator.comparing(HistorikkinnslagOldFelt::getSekvensNr))
                .collect(Collectors.toList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HistorikkinnslagOldDel.Builder builder(HistorikkinnslagOldDel del) {
        return new Builder(del);
    }

    public static class Builder {
        private HistorikkinnslagOldDel kladd;


        private Builder() {
            this(new HistorikkinnslagOldDel());
        }

        public Builder(HistorikkinnslagOldDel del) {
            kladd = del;
        }

        public Builder leggTilFelt(HistorikkinnslagOldFelt felt) {
            kladd.historikkinnslagFelt.add(felt);
            felt.setHistorikkinnslagDel(kladd);
            return this;
        }

        public Builder medHistorikkinnslag(HistorikkinnslagOld historikkinnslag) {
            kladd.historikkinnslag = historikkinnslag;
            return this;
        }

        public boolean harFelt() {
            return !kladd.getHistorikkinnslagFelt().isEmpty();
        }

        public HistorikkinnslagOldDel build() {
            return kladd;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorikkinnslagOldDel)) {
            return false;
        }
        HistorikkinnslagOldDel that = (HistorikkinnslagOldDel) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
