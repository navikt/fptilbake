package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.EsHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.LegacyUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.MedlemskapHendelseUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.ØkonomiUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HendelseUnderType implements Kodeverdi {
    public static final String KODEVERK = "HENDELSE_UNDERTYPE";

    private static final Logger logger = LoggerFactory.getLogger(HendelseUnderType.class);

    private static final Map<String, HendelseUnderType> KODER = new LinkedHashMap<>();

    private String kode;
    private String navn;
    private int sortering;

    static {
        registrerKoder(FpHendelseUnderTyper.ALLE);
        registrerKoder(FellesUndertyper.class);
        registrerKoder(MedlemskapHendelseUndertyper.class);
        registrerKoder(ØkonomiUndertyper.class);
        registrerKoder(LegacyUnderTyper.class);

        registrerKoder(SvpHendelseUnderTyper.class);
        registrerKoder(EsHendelseUnderTyper.class);
    }

    static void registrerKoder(Class<?> interfaceMedKoder) {
        Field[] konstanter = interfaceMedKoder.getFields();
        List<HendelseUnderType> undertyper = new ArrayList<>();
        for (Field f : konstanter) {
            if (HendelseUnderType.class.isAssignableFrom(f.getType())) {
                try {
                    HendelseUnderType ut = (HendelseUnderType) f.get(null);
                    if (ut == null){
                        System.out.println("hei");
                    }
                    undertyper.add(ut);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Utvikler-feil: klarer ikke å opprette HendeleUndertyper riktig", e);
                }
            }
            if (undertyper.isEmpty()) {
                throw new IllegalStateException("Utvikler-feil: klarte ikke å finne HendelseUndertyper i " + interfaceMedKoder.getName());
            }
        }
        System.out.println("registrer: " + undertyper);
        registrerKoder(undertyper);
        //logger.info("Lastet {} hendelse-undertyper fra {}", undertyper.size(), interfaceMedKoder.getName());
    }

    public static Map<String, HendelseUnderType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    static void registrerKoder(Collection<HendelseUnderType> undertyper) {
        for (var v : undertyper) {
            if (v == null) {
                throw new IllegalArgumentException(" Ikke lov å sette inn null");
            }
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    public HendelseUnderType(String kode, String kode2, String navn, int sortering) {
        if (!kode.equals(kode2)) {
            throw new IllegalArgumentException("Feil i hendelseUndertype (kopiert feil) " + kode + " og " + kode2);
        }
        this.kode = kode;
        this.navn = navn;
        this.sortering = sortering;
    }

    @JsonCreator
    public static HendelseUnderType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HendelseType: " + kode);
        }
        return ad;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return null;
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

    public int getSortering() {
        return sortering;
    }

    @Override
    public String toString() {
        return kode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HendelseUnderType that = (HendelseUnderType) o;
        return Objects.equals(kode, that.kode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<HendelseUnderType, String> {
        @Override
        public String convertToDatabaseColumn(HendelseUnderType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public HendelseUnderType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}


