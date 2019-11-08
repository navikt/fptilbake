package no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@Entity(name = "DokumentMalType")
@DiscriminatorValue(DokumentMalType.DISCRIMINATOR)
public class DokumentMalType extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_MAL_TYPE";

    public static final DokumentMalType INNHENT_DOK = new DokumentMalType("INNHEN");
    public static final DokumentMalType FRITEKST_DOK = new DokumentMalType("FRITKS");
    public static final DokumentMalType VARSEL_DOK = new DokumentMalType("VARS");
    public static final DokumentMalType KORRIGERT_VARSEL_DOK = new DokumentMalType("KORRIGVARS");

    private static Map<String, DokumentMalType> malTypeMap = new HashMap<>();

    static {
        malTypeMap.put(DokumentMalType.INNHENT_DOK.getKode(), DokumentMalType.INNHENT_DOK);
        malTypeMap.put(DokumentMalType.FRITEKST_DOK.getKode(), DokumentMalType.FRITEKST_DOK);
        malTypeMap.put(DokumentMalType.VARSEL_DOK.getKode(), DokumentMalType.VARSEL_DOK);
        malTypeMap.put(DokumentMalType.KORRIGERT_VARSEL_DOK.getKode(), DokumentMalType.KORRIGERT_VARSEL_DOK);
    }


    DokumentMalType() {
        // Hibernate trenger default konstruktør
    }

    DokumentMalType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static DokumentMalType fraKode(String kode) {
        if (malTypeMap.containsKey(kode)) {
            return malTypeMap.get(kode);
        }
        throw DokumentMalTypeFeil.FACTORY.ugyldigDokumentMalType(kode).toException();
    }

    interface DokumentMalTypeFeil extends DeklarerteFeil {
        DokumentMalTypeFeil FACTORY = FeilFactory.create(DokumentMalTypeFeil.class);

        @TekniskFeil(feilkode = "FPT-312922", feilmelding = "DokumentMalType '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigDokumentMalType(String kode);

    }

}
