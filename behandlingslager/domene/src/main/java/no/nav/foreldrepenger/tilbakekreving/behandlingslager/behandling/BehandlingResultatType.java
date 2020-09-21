package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BehandlingResultatType implements Kodeverdi {

    IKKE_FASTSATT("IKKE_FASTSATT", "Ikke fastsatt"),
    FASTSATT("FASTSATT", "Resultatet er fastsatt"),
    HENLAGT_FEILOPPRETTET("HENLAGT_FEILOPPRETTET", "Henlagt, søknaden er feilopprettet"),
    HENLAGT_FEILOPPRETTET_MED_BREV("HENLAGT_FEILOPPRETTET_MED_BREV", "Feilaktig opprettet - med henleggelsesbrev"),
    HENLAGT_FEILOPPRETTET_UTEN_BREV("HENLAGT_FEILOPPRETTET_UTEN_BREV", "Feilaktig opprettet - uten henleggelsesbrev"),
    HENLAGT_KRAVGRUNNLAG_NULLSTILT("HENLAGT_KRAVGRUNNLAG_NULLSTILT", "Kravgrunnlaget er nullstilt"),
    HENLAGT_TEKNISK_VEDLIKEHOLD("HENLAGT_TEKNISK_VEDLIKEHOLD", "Teknisk vedlikehold"),
    ENDRET("ENDRET", "Resultatet er endret i revurderingen"),
    INGEN_ENDRING("INGEN_ENDRING", "Ingen endring"),

    HENLAGT("HENLAGT", "Henlagt"), // kun brukes for å vise behandling resultat i frontend
    INGEN_TILBAKEKREVING("INGEN_TILBAKEKREVING", "Ingen Tilbakekreving"), // kun brukes for å vise behandling resultat i frontend
    DELVIS_TILBAKEKREVING("DELVIS_TILBAKEKREVING", "Delvis Tilbakekreving"), // kun brukes for å vise behandling resultat i frontend
    FULL_TILBAKEKREVING("FULL_TILBAKEKREVING", "Full Tilbakekreving"); // kun brukes for å vise behandling resultat i frontend

    private String kode;
    private String navn;

    public static final String KODEVERK = "BEHANDLING_RESULTAT_TYPE";
    private static final Map<String, BehandlingResultatType> KODER = new LinkedHashMap<>();
    private static final Set<BehandlingResultatType> ALLE_HENLEGGELSESKODER = Collections.unmodifiableSet(new LinkedHashSet<>(
        Arrays.asList(HENLAGT_KRAVGRUNNLAG_NULLSTILT, HENLAGT_FEILOPPRETTET, HENLAGT_FEILOPPRETTET_MED_BREV,
            HENLAGT_FEILOPPRETTET_UTEN_BREV, HENLAGT_TEKNISK_VEDLIKEHOLD)));

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private BehandlingResultatType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static Set<BehandlingResultatType> getAlleHenleggelseskoder() {
        return ALLE_HENLEGGELSESKODER;
    }

    @JsonCreator
    public static BehandlingResultatType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BehandlingResultatType: " + kode);
        }
        return ad;
    }

    public static Map<String, BehandlingResultatType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getNavn() {
        return navn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<BehandlingResultatType, String> {
        @Override
        public String convertToDatabaseColumn(BehandlingResultatType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BehandlingResultatType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
