package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

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

@Entity(name = "VilkårResultat")
@DiscriminatorValue(VilkårResultat.DISCRIMINATOR)
public class VilkårResultat extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAAR_RESULTAT";

    public static final VilkårResultat FORSTO_BURDE_FORSTÅTT = new VilkårResultat("FORSTO_BURDE_FORSTAATT");
    public static final VilkårResultat FEIL_OPPLYSNINGER_FRA_BRUKER = new VilkårResultat("FEIL_OPPLYSNINGER");
    public static final VilkårResultat MANGELFULLE_OPPLYSNINGER_FRA_BRUKER = new VilkårResultat("MANGELFULL_OPPLYSNING");
    public static final VilkårResultat GOD_TRO = new VilkårResultat("GOD_TRO");

    public static final VilkårResultat UDEFINERT = new VilkårResultat("-");

    private static Map<String, VilkårResultat> vilkårResultatMap = new HashMap<>();

    static {
        vilkårResultatMap.put(FORSTO_BURDE_FORSTÅTT.getKode(), FORSTO_BURDE_FORSTÅTT);
        vilkårResultatMap.put(FEIL_OPPLYSNINGER_FRA_BRUKER.getKode(), FEIL_OPPLYSNINGER_FRA_BRUKER);
        vilkårResultatMap.put(MANGELFULLE_OPPLYSNINGER_FRA_BRUKER.getKode(), MANGELFULLE_OPPLYSNINGER_FRA_BRUKER);
        vilkårResultatMap.put(GOD_TRO.getKode(), GOD_TRO);
    }

    private VilkårResultat(String kode) {
        super(kode, DISCRIMINATOR);
    }

    VilkårResultat() {
        // For hibernate
    }

    public static VilkårResultat fraKode(String kode) {
        if (vilkårResultatMap.containsKey(kode)) {
            return vilkårResultatMap.get(kode);
        }
        throw VilkårResultatFeil.FEILFACTORY.ugyldigVilkårResultat(kode).toException();
    }

    interface VilkårResultatFeil extends DeklarerteFeil {

        VilkårResultatFeil FEILFACTORY = FeilFactory.create(VilkårResultatFeil.class);

        @TekniskFeil(feilkode = "FPT-312923", feilmelding = "VilkårResultat '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigVilkårResultat(String vilkårResultat);
    }

}
