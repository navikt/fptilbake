package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FagsakYtelseType implements Kodeverdi {


    ENGANGSTØNAD("ES", "Engangsstønad","Eingongsstønad"),
    FORELDREPENGER("FP", "Foreldrepenger","Foreldrepengar"),
    SVANGERSKAPSPENGER("SVP", "Svangerskapspenger","Svangerskapspengar"),

    //K9
    FRISINN("FRISINN", "Kompensasjonsytelse for selvstendig næringsdrivende og frilansere","Kompensasjonsytelse for selvstendig næringsdrivende og frilansere"),
    PLEIEPENGER_SYKT_BARN("PSB", "Pleiepenger","Pleiepengar"), //TODO: Heter egentlig Pleiepenger sykt barn, men brukes direkte i varselbrev - og der skal det stå "Pleiepenger"
    PLEIEPENGER_NÆRSTÅENDE("PPN", "Pleiepenger nærstående",""),
    OMSORGSPENGER("OMP", "Omsorgspenger","Omsorgspengar"),
    OPPLÆRINGSPENGER("OLP", "Opplæringspenger",""),

    //K9-rammevedtak (kan ignoreres for tilbakekreving, men må være i listen for at VedtakHendelse skal parse alle vedtak)
    OMSORGSPENGER_KS("OMP_KS", "Ekstra omsorgsdager kronisk syk", ""),
    OMSORGSPENGER_MA("OMP_MA", "Ekstra omsorgsdager midlertidig alene", ""),


    UDEFINERT("-", "Ikke definert","Ikke Definert"); //$NON-NLS-1$

    private String kode;
    private String navn; //på bøkmål som standard
    private String navnPåNynorsk;

    public static final String KODEVERK = "FAGSAK_YTELSE"; //$NON-NLS-1$
    private static final Map<String, FagsakYtelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private FagsakYtelseType(String kode, String navn, String navnPåNynorsk) {
        this.kode = kode;
        this.navn = navn;
        this.navnPåNynorsk = navnPåNynorsk;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FagsakYtelseType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(FagsakYtelseType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FagsakYtelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, FagsakYtelseType> kodeMap() {
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

    @JsonProperty
    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty
    public String getNavnPåNynorsk() {
        return navnPåNynorsk;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<FagsakYtelseType, String> {
        @Override
        public String convertToDatabaseColumn(FagsakYtelseType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public FagsakYtelseType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

    public static String finnFagsaktypenavnPåAngittSpråk(FagsakYtelseType fagsakYtelseType, Språkkode språkkode){
        return Språkkode.nn.equals(språkkode) ? fagsakYtelseType.getNavnPåNynorsk() : fagsakYtelseType.getNavn();
    }

}
