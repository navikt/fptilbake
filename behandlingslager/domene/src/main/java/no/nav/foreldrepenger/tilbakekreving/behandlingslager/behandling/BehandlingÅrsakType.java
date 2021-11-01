package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
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

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BehandlingÅrsakType implements Kodeverdi {

    RE_KLAGE_NFP("RE_KLAGE_NFP","Revurdering NFP omgjør vedtak basert på klage"),
    RE_KLAGE_KA("RE_KLAGE_KA","Revurdering etter KA-behandlet klage"),
    RE_OPPLYSNINGER_OM_VILKÅR("RE_VILKÅR","Nye opplysninger om vilkårsvurdering"),
    RE_OPPLYSNINGER_OM_FORELDELSE("RE_FORELDELSE","Nye opplysninger om foreldelse"),
    RE_FEILUTBETALT_BELØP_HELT_ELLER_DELVIS_BORTFALT("RE_FEILUTBETALT_BELØP_REDUSERT","Feilutbetalt beløp helt eller delvis bortfalt"),

    UDEFINERT("-","Ikke Definert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "BEHANDLING_AARSAK";
    private static final Map<String, BehandlingÅrsakType> KODER = new LinkedHashMap<>();
    public static final Set<BehandlingÅrsakType> KLAGE_ÅRSAKER = Set.of(BehandlingÅrsakType.RE_KLAGE_KA, BehandlingÅrsakType.RE_KLAGE_NFP);

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    BehandlingÅrsakType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BehandlingÅrsakType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BehandlingÅrsakType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InternBehandlingÅrsakType: " + kode);
        }
        return ad;
    }

    public static Map<String, BehandlingÅrsakType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
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

    @Override
    public String getNavn() {
        return navn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<BehandlingÅrsakType, String> {
        @Override
        public String convertToDatabaseColumn(BehandlingÅrsakType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BehandlingÅrsakType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}
