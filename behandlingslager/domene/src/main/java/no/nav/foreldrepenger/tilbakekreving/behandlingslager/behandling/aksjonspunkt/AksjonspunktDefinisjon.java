package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon.ENTRINN;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon.FORBLI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon.TOTRINN;

import java.time.Period;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

/**
 * Definerer mulige Aksjonspunkter inkludert hvilket Vurderingspunkt de må løses i.
 * Inkluderer også konstanter for å enklere kunne referere til dem i eksisterende logikk.
 */
public enum AksjonspunktDefinisjon implements Kodeverdi {

    // Manuelle

    VURDER_TILBAKEKREVING(AksjonspunktKodeDefinisjon.VURDER_TILBAKEKREVING, AksjonspunktType.MANUELL,
            "Vurder tilbakekreving", BehandlingStegType.VTILBSTEG, VurderingspunktType.UT, TOTRINN),
    VURDER_FORELDELSE(AksjonspunktKodeDefinisjon.VURDER_FORELDELSE, AksjonspunktType.MANUELL,
            "Vurder foreldelse", BehandlingStegType.FORELDELSEVURDERINGSTEG, VurderingspunktType.UT, TOTRINN),
    FORESLÅ_VEDTAK(AksjonspunktKodeDefinisjon.FORESLÅ_VEDTAK, AksjonspunktType.MANUELL,
            "Foreslå vedtak", BehandlingStegType.FORESLÅ_VEDTAK, VurderingspunktType.UT, TOTRINN),
    FATTE_VEDTAK(AksjonspunktKodeDefinisjon.FATTE_VEDTAK, AksjonspunktType.MANUELL,
            "Fatte vedtak", BehandlingStegType.FATTE_VEDTAK, VurderingspunktType.INN, ENTRINN),
    AVKLAR_VERGE(AksjonspunktKodeDefinisjon.AVKLAR_VERGE, AksjonspunktType.MANUELL,
            "Avklar verge", BehandlingStegType.FAKTA_VERGE, VurderingspunktType.UT, ENTRINN),
    AVKLART_FAKTA_FEILUTBETALING(AksjonspunktKodeDefinisjon.AVKLART_FAKTA_FEILUTBETALING, AksjonspunktType.MANUELL,
            "Avklar fakta for feilutbetaling", BehandlingStegType.FAKTA_FEILUTBETALING, VurderingspunktType.UT, TOTRINN),

    // Autopunkter

    VENT_PÅ_BRUKERTILBAKEMELDING(AksjonspunktKodeDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktType.AUTOPUNKT,
            "Venter på tilbakemelding fra bruker", BehandlingStegType.VARSEL, VurderingspunktType.UT, ENTRINN,
            FORBLI, "P4W"),
    VENT_PÅ_TILBAKEKREVINGSGRUNNLAG(AksjonspunktKodeDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, AksjonspunktType.AUTOPUNKT,
            "Venter på tilbakekrevingsgrunnlag fra økonomi", BehandlingStegType.TBKGSTEG, VurderingspunktType.UT, ENTRINN,
            FORBLI, "P4W"),


    UNDEFINED,


    ;

    static final String KODEVERK = "AKSJONSPUNKT_DEF";

    private static final Map<String, AksjonspunktDefinisjon> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private AksjonspunktType aksjonspunktType = AksjonspunktType.UDEFINERT;

    /**
     * Definerer hvorvidt Aksjonspunktet default krever totrinnsbehandling. Dvs. Beslutter må godkjenne hva
     * Saksbehandler har utført.
     */
    private boolean defaultTotrinnBehandling = false;

    /**
     * Hvorvidt aksjonspunktet har en frist før det må være løst. Brukes i forbindelse med når Behandling er lagt til
     * Vent.
     */
    private String fristPeriode;

    private boolean tilbakehoppVedGjenopptakelse;

    private BehandlingStegType behandlingStegType;

    private String navn;

    private VurderingspunktType vurderingspunktType;

    private boolean erUtgått = false;

    private String kode;

    AksjonspunktDefinisjon() {
        // for CDI
    }

    // Bruk for ordinære aksjonspunkt og overstyring
    private AksjonspunktDefinisjon(String kode,
                                   AksjonspunktType aksjonspunktType,
                                   String navn,
                                   BehandlingStegType behandlingStegType,
                                   VurderingspunktType vurderingspunktType,
                                   boolean defaultTotrinnBehandling) {
        this.kode = Objects.requireNonNull(kode);
        this.navn = navn;
        this.aksjonspunktType = aksjonspunktType;
        this.behandlingStegType = behandlingStegType;
        this.vurderingspunktType = vurderingspunktType;
        this.defaultTotrinnBehandling = defaultTotrinnBehandling;
        this.tilbakehoppVedGjenopptakelse = false;
        this.fristPeriode = null;
    }

    // Bruk for autopunkt i 7nnn serien
    private AksjonspunktDefinisjon(String kode,
                                   AksjonspunktType aksjonspunktType,
                                   String navn,
                                   BehandlingStegType behandlingStegType,
                                   VurderingspunktType vurderingspunktType,
                                   boolean defaultTotrinnBehandling,
                                   boolean tilbakehoppVedGjenopptakelse,
                                   String fristPeriode) {
        this.kode = Objects.requireNonNull(kode);
        this.navn = navn;
        this.aksjonspunktType = aksjonspunktType;
        this.behandlingStegType = behandlingStegType;
        this.vurderingspunktType = vurderingspunktType;
        this.defaultTotrinnBehandling = defaultTotrinnBehandling;
        this.tilbakehoppVedGjenopptakelse = tilbakehoppVedGjenopptakelse;
        this.fristPeriode = fristPeriode;
    }

    public AksjonspunktType getAksjonspunktType() {
        return Objects.equals(AksjonspunktType.UDEFINERT, aksjonspunktType) ? null : aksjonspunktType;
    }

    public boolean erAutopunkt() {
        return AksjonspunktType.AUTOPUNKT.equals(getAksjonspunktType());
    }

    public boolean getDefaultTotrinnBehandling() {
        return defaultTotrinnBehandling;
    }

    public String getFristPeriode() {
        return fristPeriode;
    }

    public Period getFristPeriod() {
        return (fristPeriode == null ? null : Period.parse(fristPeriode));
    }

    public boolean tilbakehoppVedGjenopptakelse() {
        return tilbakehoppVedGjenopptakelse;
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonValue
    @Override
    public String getKode() {
        return kode;
    }

    public BehandlingStegType getBehandlingSteg() {
        return behandlingStegType;
    }

    public VurderingspunktType getVurderingspunktType() {
        return vurderingspunktType;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    /**
     * Aksjonspunkt tidligere brukt, nå utgått (kan ikke gjenoppstå).
     */
    public boolean erUtgått() {
        return erUtgått;
    }

    // Beholder JsonCreator annotasjon her enn så lenge for å støtte deserialisering av UNDEFINED når denne har blitt serialisert til objekt.
    // Dette er nødvendig sidan UNDEFINED ikkje har nokon kode verdi satt, så property blir null ved serialisering til objekt.
    // Kan fjernast når spesialserialisering til objekt er fjerna, eller viss ein legger til feks kode = "-" på UNDEFINED.
    @JsonCreator
    public static AksjonspunktDefinisjon fraKode(String kode) {
        if (kode == null) {
            return UNDEFINED;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AksjonspunktDefinisjon: " + kode);
        }
        return ad;
    }

    public static Map<String, AksjonspunktDefinisjon> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static List<AksjonspunktDefinisjon> finnAksjonspunktDefinisjoner(BehandlingStegType behandlingStegType, VurderingspunktType vurderingspunktType) {
        return KODER.values().stream()
                .filter(ad -> Objects.equals(ad.getBehandlingSteg(), behandlingStegType) && Objects.equals(ad.getVurderingspunktType(), vurderingspunktType))
                .toList();
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<AksjonspunktDefinisjon, String> {
        @Override
        public String convertToDatabaseColumn(AksjonspunktDefinisjon attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public AksjonspunktDefinisjon convertToEntityAttribute(String dbData) {
            return dbData == null ? null : AksjonspunktDefinisjon.fraKode(dbData);
        }
    }
}
