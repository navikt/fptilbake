package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.time.Period;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Definerer mulige Aksjonspunkter inkludert hvilket Vurderingspunkt de må løses i.
 * Inkluderer også konstanter for å enklere kunne referere til dem i eksisterende logikk.
 */
@Entity(name = "AksjonspunktDef")
@Table(name = "AKSJONSPUNKT_DEF")
public class AksjonspunktDefinisjon extends KodeverkTabell {

    /**
     * NB: Kun kodeverdi skal defineres på konstanter, ingen ekstra felter som skal ligge i databasen som frist eller
     * annet. Disse brukes kun til skriving.
     */
    public static final AksjonspunktDefinisjon SEND_VARSEL = new AksjonspunktDefinisjon("5001");
    public static final AksjonspunktDefinisjon VURDER_TILBAKEKREVING = new AksjonspunktDefinisjon("5002");
    public static final AksjonspunktDefinisjon VURDER_FORELDELSE = new AksjonspunktDefinisjon("5003");
    public static final AksjonspunktDefinisjon FORESLÅ_VEDTAK = new AksjonspunktDefinisjon("5004");
    public static final AksjonspunktDefinisjon FATTE_VEDTAK = new AksjonspunktDefinisjon("5005");
    public static final AksjonspunktDefinisjon VENT_PÅ_BRUKERTILBAKEMELDING = new AksjonspunktDefinisjon("7001");
    public static final AksjonspunktDefinisjon VENT_PÅ_TILBAKEKREVINGSGRUNNLAG = new AksjonspunktDefinisjon("7002");
    public static final AksjonspunktDefinisjon AVKLART_FAKTA_FEILUTBETALING = new AksjonspunktDefinisjon("7003");
    public static final AksjonspunktDefinisjon AVKLAR_VERGE = new AksjonspunktDefinisjon("5030");
    // kun brukes for å sende data til fplos når behandling venter på grunnlaget etter fristen
    public static final AksjonspunktDefinisjon VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG = new AksjonspunktDefinisjon("8001");


    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "aksjonspunkt_type", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + AksjonspunktType.DISCRIMINATOR
                    + "'"))})
    private AksjonspunktType aksjonspunktType = AksjonspunktType.UDEFINERT;

    /**
     * Definerer hvorvidt Aksjonspunktet default krever totrinnsbehandling. Dvs. Beslutter må godkjenne hva
     * Saksbehandler har utført.
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINN_BEHANDLING_DEFAULT", nullable = false)
    private boolean defaultTotrinnBehandling = false;

    /**
     * Definerer hvorvidt Aksjonspunktet skal lage historikk.
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "LAG_UTEN_HISTORIKK", nullable = false)
    private boolean lagUtenHistorikk = false;

    /**
     * Hvorvidt aksjonspunktet har en frist før det må være løst. Brukes i forbindelse med når Behandling er lagt til
     * Vent.
     */
    @Column(name = "frist_periode")
    private String fristPeriode;


    @ManyToOne()
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "skjermlenke_type", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + SkjermlenkeType.DISCRIMINATOR
                    + "'"))})
    private SkjermlenkeType skjermlenkeType = SkjermlenkeType.UDEFINERT;

    @ManyToOne
    @JoinColumn(name = "vurderingspunkt", nullable = false)
    private VurderingspunktDefinisjon vurderingspunktDefinisjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TILBAKEHOPP_VED_GJENOPPTAKELSE", nullable = false)
    private boolean tilbakehoppVedGjenopptakelse;

    @ManyToMany
    @JoinTable(name = "AKSJONSPUNKT_UTELUKKENDE",
        joinColumns = {@JoinColumn(name = "ap1")},
        inverseJoinColumns = {@JoinColumn(name = "ap2")})
    Set<AksjonspunktDefinisjon> utelukkendeUt = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "AKSJONSPUNKT_UTELUKKENDE",
            joinColumns = {@JoinColumn(name = "ap2")},
            inverseJoinColumns = {@JoinColumn(name = "ap1")})
    Set<AksjonspunktDefinisjon> utelukkendeInn = new HashSet<>();

    AksjonspunktDefinisjon() {
        // for hibernate
    }

    protected AksjonspunktDefinisjon(final String kode) {
        super(kode);
    }


    public SkjermlenkeType getSkjermlenkeType() {
        return skjermlenkeType;
    }

    public AksjonspunktType getAksjonspunktType() {
        return Objects.equals(AksjonspunktType.UDEFINERT, aksjonspunktType) ? null : aksjonspunktType;
    }

    public boolean getDefaultTotrinnBehandling() {
        return defaultTotrinnBehandling;
    }

    public boolean getLagUtenHistorikk() {
        return lagUtenHistorikk;
    }

    public String getFristPeriode() {
        return fristPeriode;
    }

    public Period getFristPeriod() {
        return fristPeriode == null ? null : Period.parse(fristPeriode);
    }

    public VurderingspunktDefinisjon getVurderingspunktDefinisjon() {
        return vurderingspunktDefinisjon;
    }

    public boolean tilbakehoppVedGjenopptakelse() {
        return tilbakehoppVedGjenopptakelse;
    }

    public Set<AksjonspunktDefinisjon> getUtelukkendeApdef() {
        Set<AksjonspunktDefinisjon> utelukkendeApdef = new HashSet<>();
        utelukkendeApdef.addAll(utelukkendeInn);
        utelukkendeApdef.addAll(utelukkendeUt);
        return Collections.unmodifiableSet(utelukkendeApdef);
    }
}
