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
public class HistorikkinnslagDel extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_HISTORIKKINNSLAG_DEL")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historikkinnslag_id", nullable = false, updatable = false)
    @JsonBackReference
    private Historikkinnslag historikkinnslag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "historikkinnslagDel")
    private List<HistorikkinnslagFelt> historikkinnslagFelt = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Historikkinnslag getHistorikkinnslag() {
        return historikkinnslag;
    }

    public List<HistorikkinnslagFelt> getHistorikkinnslagFelt() {
        return historikkinnslagFelt;
    }

    public Optional<String> getAarsak() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.AARSAK);
    }

    public Optional<HistorikkinnslagFelt> getAarsakFelt() {
        return finnFelt(HistorikkinnslagFeltType.AARSAK);
    }

    public Optional<HistorikkinnslagFelt> getTema() {
        return finnFelt(HistorikkinnslagFeltType.ANGÅR_TEMA);
    }


    public Optional<HistorikkinnslagFelt> getAvklartSoeknadsperiode() {
        return finnFelt(HistorikkinnslagFeltType.AVKLART_SOEKNADSPERIODE);
    }

    public Optional<String> getBegrunnelse() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.BEGRUNNELSE);
    }

    public Optional<HistorikkinnslagFelt> getBegrunnelseFelt() {
        return finnFelt(HistorikkinnslagFeltType.BEGRUNNELSE);
    }

    /**
     * Hent en hendelse
     *
     * @return Et HistorikkinnslagFelt fordi vi trenger navn (f.eks. BEH_VENT) og tilVerdi (f.eks. <fristDato>)
     */
    public Optional<HistorikkinnslagFelt> getHendelse() {
        return finnFelt(HistorikkinnslagFeltType.HENDELSE);
    }

    public Optional<String> getResultat() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.RESULTAT);
    }

    public Optional<String> getGjeldendeFra() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.GJELDENDE_FRA);
    }

    public Optional<HistorikkinnslagFelt> getGjeldendeFraFelt() {
        return finnFelt(HistorikkinnslagFeltType.GJELDENDE_FRA);
    }

    public Optional<String> getSkjermlenke() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.SKJERMLENKE);
    }

    public Optional<HistorikkinnslagFelt> getEndretFelt(HistorikkEndretFeltType endretFeltVerdiType) {
        List<HistorikkinnslagFelt> endredeFelt = getEndredeFelt();
        return endredeFelt
                .stream()
                .filter(felt -> Objects.equals(endretFeltVerdiType.getKode(), felt.getNavn()))
                .findFirst();
    }

    public List<HistorikkinnslagFelt> getEndredeFelt() {
        return finnFeltListe(HistorikkinnslagFeltType.ENDRET_FELT);
    }

    public Optional<HistorikkinnslagFelt> getOpplysning(HistorikkOpplysningType historikkOpplysningType) {
        List<HistorikkinnslagFelt> opplysninger = getOpplysninger();
        return opplysninger.stream()
                .filter(felt -> Objects.equals(historikkOpplysningType.getKode(), felt.getNavn()))
                .findFirst();
    }

    public List<HistorikkinnslagFelt> getOpplysninger() {
        return finnFeltListe(HistorikkinnslagFeltType.OPPLYSNINGER);
    }

    public List<HistorikkinnslagTotrinnsvurdering> getTotrinnsvurderinger() {
        List<HistorikkinnslagFeltType> aksjonspunktFeltTypeKoder = List.of(HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE,
                HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT,
                HistorikkinnslagFeltType.AKSJONSPUNKT_KODE);

        List<HistorikkinnslagFelt> alleAksjonspunktFelt = historikkinnslagFelt.stream()
                .filter(felt -> aksjonspunktFeltTypeKoder.contains(felt.getFeltType()))
                .collect(Collectors.toList());

        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = alleAksjonspunktFelt.stream()
                .collect(Collectors.groupingBy(HistorikkinnslagFelt::getSekvensNr))
                .entrySet()
                .stream()
                .map(entry -> lagHistorikkinnslagAksjonspunkt(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getSekvensNr))
                .collect(Collectors.toList());
        return totrinnsvurderinger;
    }

    private HistorikkinnslagTotrinnsvurdering lagHistorikkinnslagAksjonspunkt(Integer sekvensNr, List<HistorikkinnslagFelt> aksjonspunktFeltListe) {
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

    private Optional<HistorikkinnslagFelt> finnFelt(HistorikkinnslagFeltType historikkinnslagFeltType) {
        return historikkinnslagFelt.stream()
                .filter(felt -> historikkinnslagFeltType.equals(felt.getFeltType()))
                .findFirst();
    }

    private Optional<String> finnFeltTilVerdi(HistorikkinnslagFeltType historikkinnslagFeltType) {
        return finnFelt(historikkinnslagFeltType)
                .map(HistorikkinnslagFelt::getTilVerdi);
    }

    private List<HistorikkinnslagFelt> finnFeltListe(HistorikkinnslagFeltType feltType) {
        return historikkinnslagFelt.stream()
                .filter(felt -> felt.getFeltType().equals(feltType))
                .sorted(Comparator.comparing(HistorikkinnslagFelt::getSekvensNr))
                .collect(Collectors.toList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HistorikkinnslagDel.Builder builder(HistorikkinnslagDel del) {
        return new Builder(del);
    }

    public static class Builder {
        private HistorikkinnslagDel kladd;


        private Builder() {
            this(new HistorikkinnslagDel());
        }

        public Builder(HistorikkinnslagDel del) {
            kladd = del;
        }

        public Builder leggTilFelt(HistorikkinnslagFelt felt) {
            kladd.historikkinnslagFelt.add(felt);
            felt.setHistorikkinnslagDel(kladd);
            return this;
        }

        public Builder medHistorikkinnslag(Historikkinnslag historikkinnslag) {
            kladd.historikkinnslag = historikkinnslag;
            return this;
        }

        public boolean harFelt() {
            return !kladd.getHistorikkinnslagFelt().isEmpty();
        }

        public HistorikkinnslagDel build() {
            return kladd;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorikkinnslagDel)) {
            return false;
        }
        HistorikkinnslagDel that = (HistorikkinnslagDel) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
