package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface FinnGrunnlagTaskFeil extends DeklarerteFeil {

    FinnGrunnlagTaskFeil FACTORY = FeilFactory.create(FinnGrunnlagTaskFeil.class);

    @TekniskFeil(feilkode = "FPT-783524",
        feilmelding = "Grunnlag fra Økonomi har mottatt med feil referanse for behandlingId=%s. Den finnes ikke i fpsak for saksnummer=%s",
        logLevel = LogLevel.WARN)
    Feil grunnlagHarFeilReferanse(Long behandlingId, String saksnummer);

}
