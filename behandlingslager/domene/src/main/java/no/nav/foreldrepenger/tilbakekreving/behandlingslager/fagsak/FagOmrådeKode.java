package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

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

@Entity(name = "FagOmraadeKode")
@DiscriminatorValue(FagOmrådeKode.DISCRIMINATOR)
public class FagOmrådeKode extends Kodeliste {

    public static final String DISCRIMINATOR = "FAG_OMRAADE_KODE";

    public static final FagOmrådeKode FORELDREPENGER = new FagOmrådeKode("FP");
    public static final FagOmrådeKode FORELDREPENGER_ARBEIDSGIVER = new FagOmrådeKode("FPREF");
    public static final FagOmrådeKode SYKEPENGER = new FagOmrådeKode("SP");
    public static final FagOmrådeKode SYKEPENGER_ARBEIDSGIVER = new FagOmrådeKode("SPREF");
    public static final FagOmrådeKode PLEIEPENGER = new FagOmrådeKode("OOP");
    public static final FagOmrådeKode PLEIEPENGER_ARBEIDSGIVER = new FagOmrådeKode("OOPREF");
    public static final FagOmrådeKode ENGANGSSTØNAD = new FagOmrådeKode("REFUTG");
    public static final FagOmrådeKode UDEFINERT = new FagOmrådeKode("-");

    private static final Map<String, FagOmrådeKode> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(FORELDREPENGER.getKode(), FORELDREPENGER);
        TILGJENGELIGE.put(FORELDREPENGER_ARBEIDSGIVER.getKode(), FORELDREPENGER_ARBEIDSGIVER);
        TILGJENGELIGE.put(SYKEPENGER.getKode(), SYKEPENGER);
        TILGJENGELIGE.put(SYKEPENGER_ARBEIDSGIVER.getKode(), SYKEPENGER_ARBEIDSGIVER);
        TILGJENGELIGE.put(PLEIEPENGER.getKode(), PLEIEPENGER);
        TILGJENGELIGE.put(PLEIEPENGER_ARBEIDSGIVER.getKode(), PLEIEPENGER);
        TILGJENGELIGE.put(ENGANGSSTØNAD.getKode(), ENGANGSSTØNAD);
    }

    FagOmrådeKode() {
        //Hibernate
    }

    private FagOmrådeKode(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static FagOmrådeKode fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        throw FagOmrådeKodeFeil.FEILFACTORY.ugyldigFagområdeKode(kode).toException();
    }

    interface FagOmrådeKodeFeil extends DeklarerteFeil {

        FagOmrådeKodeFeil FEILFACTORY = FeilFactory.create(FagOmrådeKodeFeil.class);

        @TekniskFeil(feilkode = "FPT-312905", feilmelding = "FagOmrådeKode '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigFagområdeKode(String fagOmrådeKode);
    }

}
