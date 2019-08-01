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

@Entity(name = "SærligGrunn")
@DiscriminatorValue(SærligGrunn.DISCRIMINATOR)
public class SærligGrunn extends Kodeliste {

    public static final String DISCRIMINATOR = "SAERLIG_GRUNN";

    public static final SærligGrunn GRAD_AV_UAKTSOMHET = new SærligGrunn("GRAD_UAKTSOMHET");
    public static final SærligGrunn HELT_ELLER_DELVIS_NAVS_FEIL = new SærligGrunn("HELT_ELLER_DELVIS_NAVS_FEIL");
    public static final SærligGrunn STØRRELSE_BELØP = new SærligGrunn("STOERRELSE_BELOEP");
    public static final SærligGrunn TID_FRA_UTBETALING = new SærligGrunn("TID_FRA_UTBETALING");
    public static final SærligGrunn ANNET = new SærligGrunn("ANNET");

    private static final Map<String, SærligGrunn> særligGrunnerMap = new HashMap<>();

    static {
        særligGrunnerMap.put(GRAD_AV_UAKTSOMHET.getKode(), GRAD_AV_UAKTSOMHET);
        særligGrunnerMap.put(HELT_ELLER_DELVIS_NAVS_FEIL.getKode(), HELT_ELLER_DELVIS_NAVS_FEIL);
        særligGrunnerMap.put(STØRRELSE_BELØP.getKode(), STØRRELSE_BELØP);
        særligGrunnerMap.put(TID_FRA_UTBETALING.getKode(), TID_FRA_UTBETALING);
        særligGrunnerMap.put(ANNET.getKode(), ANNET);
    }

    SærligGrunn(String kode) {
        super(kode, DISCRIMINATOR);
    }

    SærligGrunn() {
        // For hibernate
    }

    public static SærligGrunn fraKode(String kode) {
        if (særligGrunnerMap.containsKey(kode)) {
            return særligGrunnerMap.get(kode);
        }
        throw SærligGrunnFeil.FEILFACTORY.ugyldigSærligGrunn(kode).toException();
    }

    interface SærligGrunnFeil extends DeklarerteFeil {

        SærligGrunnFeil FEILFACTORY = FeilFactory.create(SærligGrunnFeil.class);

        @TekniskFeil(feilkode = "FPT-312925", feilmelding = "SærligGrunn '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigSærligGrunn(String særligGrunn);
    }
}
