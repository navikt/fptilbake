package no.nav.foreldrepenger.tilbakekreving.web.app.exceptions;


import java.util.List;

import no.nav.vedtak.exception.FunksjonellException;

public class FeltValideringFeil {

    static FunksjonellException feltverdiKanIkkeValideres(List<String> feltnavn) {
        return new FunksjonellException("FPT-328673", String.format("Det oppstod en valideringsfeil på felt %s. Vennligst kontroller at alle feltverdier er korrekte.", feltnavn), "Kontroller at alle feltverdier er korrekte");
    }
    static FunksjonellException feltverdiKanIkkeValideres(List<String> feltnavn, List<String> aksjonspunktKoder) {
        return new FunksjonellException("FPT-328673", String.format("Det oppstod en valideringsfeil på felt %s for aksjonspunktkode %s. Vennligst kontroller at alle feltverdier er korrekte.", feltnavn, aksjonspunktKoder), "Kontroller at alle feltverdier er korrekte");
    }
}
