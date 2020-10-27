package no.nav.foreldrepenger.tilbakekreving.poststed;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface KodeverkFeil extends DeklarerteFeil {

    KodeverkFeil FACTORY = FeilFactory.create(KodeverkFeil.class);

    @IntegrasjonFeil(feilkode = "FPT-868813", feilmelding = "Kodeverk ikke funnet", logLevel = LogLevel.ERROR)
    Feil hentKodeverkKodeverkIkkeFunnet(HentKodeverkHentKodeverkKodeverkIkkeFunnet ex);

    @IntegrasjonFeil(feilkode = "FPT-402870", feilmelding = "Kodeverktype ikke støttet: %s", logLevel = LogLevel.ERROR)
    Feil hentKodeverkKodeverkTypeIkkeStøttet(String kodeverkType);

    @IntegrasjonFeil(feilkode = "FPT-563155", feilmelding = "Synkronisering med kodeverk feilet", logLevel = LogLevel.WARN)
    Feil synkronoseringAvKodeverkFeilet(IntegrasjonException e);

}
