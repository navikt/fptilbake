package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BehandlingResultatType implements Kodeverdi {

    IKKE_FASTSATT("IKKE_FASTSATT", "Ikke fastsatt"),
    @Deprecated
    FASTSATT("FASTSATT", "Resultatet er fastsatt"), //Ikke bruk denne BehandlingResultatType.Blir fjernes senere
    HENLAGT_FEILOPPRETTET("HENLAGT_FEILOPPRETTET", "Henlagt, søknaden er feilopprettet"),
    HENLAGT_FEILOPPRETTET_MED_BREV("HENLAGT_FEILOPPRETTET_MED_BREV", "Feilaktig opprettet - med henleggelsesbrev"),
    HENLAGT_FEILOPPRETTET_UTEN_BREV("HENLAGT_FEILOPPRETTET_UTEN_BREV", "Feilaktig opprettet - uten henleggelsesbrev"),
    HENLAGT_KRAVGRUNNLAG_NULLSTILT("HENLAGT_KRAVGRUNNLAG_NULLSTILT", "Kravgrunnlaget er nullstilt"),
    HENLAGT_TEKNISK_VEDLIKEHOLD("HENLAGT_TEKNISK_VEDLIKEHOLD", "Teknisk vedlikehold"),

    HENLAGT("HENLAGT", "Henlagt"), // kun brukes i frontend
    INGEN_TILBAKEBETALING("INGEN_TILBAKEBETALING", "Ingen tilbakebetaling"),
    DELVIS_TILBAKEBETALING("DELVIS_TILBAKEBETALING", "Delvis tilbakebetaling"),
    FULL_TILBAKEBETALING("FULL_TILBAKEBETALING", "Tilbakebetaling");

    private String kode;
    private String navn;

    public static final String KODEVERK = "BEHANDLING_RESULTAT_TYPE";
    private static final Map<String, BehandlingResultatType> KODER = new LinkedHashMap<>();
    private static final Set<BehandlingResultatType> ALLE_HENLEGGELSESKODER = Collections.unmodifiableSet(new LinkedHashSet<>(
            List.of(HENLAGT_KRAVGRUNNLAG_NULLSTILT, HENLAGT_FEILOPPRETTET, HENLAGT_FEILOPPRETTET_MED_BREV,
                    HENLAGT_FEILOPPRETTET_UTEN_BREV, HENLAGT_TEKNISK_VEDLIKEHOLD)));

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    BehandlingResultatType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static Set<BehandlingResultatType> getAlleHenleggelseskoder() {
        return ALLE_HENLEGGELSESKODER;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BehandlingResultatType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BehandlingResultatType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BehandlingResultatType: " + kode);
        }
        return ad;
    }

    public static Map<String, BehandlingResultatType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static BehandlingResultatType fraVedtakResultatType(VedtakResultatType vedtakResultatType) {
        return switch (vedtakResultatType) {
            case INGEN_TILBAKEBETALING -> BehandlingResultatType.INGEN_TILBAKEBETALING;
            case FULL_TILBAKEBETALING -> BehandlingResultatType.FULL_TILBAKEBETALING;
            case DELVIS_TILBAKEBETALING -> BehandlingResultatType.DELVIS_TILBAKEBETALING;
            default -> throw new IllegalArgumentException("Ukjent vedtakResultatType :" + vedtakResultatType);
        };
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
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
