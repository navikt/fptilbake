package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;

import java.util.Arrays;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


public class Transisjoner {

    private static final List<StegTransisjon> ALLE_TRANSISJONER = Arrays.asList(
            new Startet(),
            new Utført(),
            new HenleggelseTransisjon(),
            new SettPåVent(),
            new TilbakeføringTransisjon(),
            new FremoverhoppTransisjon(FellesTransisjoner.FREMHOPP_TIL_FATTE_VEDTAK.getId(), BehandlingStegType.FATTE_VEDTAK),
            new FremoverhoppTransisjon(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK.getId(), BehandlingStegType.FORESLÅ_VEDTAK),
            new FremoverhoppTransisjon(FellesTransisjoner.FREMHOPP_TIL_IVERKSETT_VEDTAK.getId(), BehandlingStegType.IVERKSETT_VEDTAK)
    );

    private Transisjoner() {
        //skal ikke instansieres
    }

    public static StegTransisjon finnTransisjon(TransisjonIdentifikator transisjonIdentifikator) {
        for (StegTransisjon transisjon : ALLE_TRANSISJONER) {
            if (transisjon.getId().equals(transisjonIdentifikator.getId())) {
                return transisjon;
            }
        }
        throw new IllegalArgumentException("Ukjent transisjon: " + transisjonIdentifikator);
    }

}
