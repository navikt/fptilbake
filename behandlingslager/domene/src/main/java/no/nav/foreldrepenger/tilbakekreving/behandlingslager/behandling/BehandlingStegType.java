package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum BehandlingStegType implements Kodeverdi {

    // Tilbakekreving
    INOPPSTEG("INOPPSTEG", "Innhent opplysninger", BehandlingStatus.UTREDES),
    VARSEL("VARSELSTEG", "Varsel om tilbakekreving", BehandlingStatus.UTREDES),
    TBKGSTEG("TBKGSTEG", "Motatt kravgrunnlag fra økonomi", BehandlingStatus.UTREDES),

    // Tilbakekreving - revurdering
    HENTGRUNNLAGSTEG("HENTGRUNNLAGSTEG", "Hent grunnlag fra økonomi", BehandlingStatus.UTREDES),

    // Felles for behandlingene
    FAKTA_VERGE("FAKTAVERGESTEG", "Fakta om verge", BehandlingStatus.UTREDES),
    FAKTA_FEILUTBETALING("FAKTFEILUTSTEG", "Fakta om Feilutbetaling", BehandlingStatus.UTREDES),
    FORELDELSEVURDERINGSTEG("VFORELDETSTEG", "Vurder foreldelse", BehandlingStatus.UTREDES),
    VTILBSTEG("VTILBSTEG", "Vurder tilbakekreving", BehandlingStatus.UTREDES),
    FORESLÅ_VEDTAK("FORVEDSTEG", "Foreslå vedtak", BehandlingStatus.UTREDES),
    FATTE_VEDTAK( "FVEDSTEG", "Fatte Vedtak", BehandlingStatus.FATTER_VEDTAK),
    IVERKSETT_VEDTAK("IVEDSTEG", "Iverksett Vedtak", BehandlingStatus.IVERKSETTER_VEDTAK);

    static final String KODEVERK = "BEHANDLING_STEG_TYPE";

    private static final Map<String, BehandlingStegType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    /**
     * Definisjon av hvilken status behandlingen skal rapporteres som når dette steget er aktivt.
     */
    @JsonIgnore
    private BehandlingStatus definertBehandlingStatus;

    @JsonIgnore
    private String navn;

    private String kode;

    private BehandlingStegType(String kode) {
        this.kode = kode;
    }

    private BehandlingStegType(String kode, String navn, BehandlingStatus definertBehandlingStatus) {
        this.kode = kode;
        this.navn = navn;
        this.definertBehandlingStatus = definertBehandlingStatus;
    }

    public BehandlingStatus getDefinertBehandlingStatus() {
        return definertBehandlingStatus;
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjonerInngang() {
        return AksjonspunktDefinisjon.finnAksjonspunktDefinisjoner(this, VurderingspunktType.INN);
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjonerUtgang() {
        return AksjonspunktDefinisjon.finnAksjonspunktDefinisjoner(this, VurderingspunktType.UT);
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjoner(VurderingspunktType type) {
        return AksjonspunktDefinisjon.finnAksjonspunktDefinisjoner(this, type);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonCreator
    public static BehandlingStegType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BehandlingStegType: " + kode);
        }
        return ad;
    }

    @Override
    public String toString() {
        return super.toString() + "('" + getKode() + "')";
    }

    public static Map<String, BehandlingStegType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<BehandlingStegType, String> {
        @Override
        public String convertToDatabaseColumn(BehandlingStegType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BehandlingStegType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : BehandlingStegType.fraKode(dbData);
        }
    }
}
