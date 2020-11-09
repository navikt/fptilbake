package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum AdresseType implements Kodeverdi {

    BOSTEDSADRESSE("BOSTEDSADRESSE"),
    POSTADRESSE("POSTADRESSE"),
    POSTADRESSE_UTLAND("POSTADRESSE_UTLAND"),
    MIDLERTIDIG_POSTADRESSE_NORGE("MIDLERTIDIG_POSTADRESSE_NORGE"),
    MIDLERTIDIG_POSTADRESSE_UTLAND("MIDLERTIDIG_POSTADRESSE_UTLAND"),
    UKJENT_ADRESSE("UKJENT_ADRESSE");

    public static final String KODEVERK = "ADRESSE_TYPE";
    public static final List<AdresseType> kjentePostadressetyper = Collections.unmodifiableList(
        Arrays.asList(
            AdresseType.BOSTEDSADRESSE,
            AdresseType.POSTADRESSE,
            AdresseType.POSTADRESSE_UTLAND,
            AdresseType.MIDLERTIDIG_POSTADRESSE_NORGE,
            AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND,
            AdresseType.UKJENT_ADRESSE
        )
    );
    private static final Map<String, AdresseType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    AdresseType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AdresseType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AdresseType.class, node, "kode");
        var ad = KODER.get(kode);

        return ad;
    }

    public static Map<String, AdresseType> kodeMap() {
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

    @Override
    public String getNavn() {
        return null;
    }
}
