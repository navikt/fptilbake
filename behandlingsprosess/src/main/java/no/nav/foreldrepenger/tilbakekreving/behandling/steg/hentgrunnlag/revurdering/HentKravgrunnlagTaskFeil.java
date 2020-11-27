package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface HentKravgrunnlagTaskFeil extends DeklarerteFeil {

    HentKravgrunnlagTaskFeil FACTORY = FeilFactory.create(HentKravgrunnlagTaskFeil.class);

    @TekniskFeil(feilkode = "FPT-587169",
        feilmelding = "Hentet et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i Fagsaksystemet for saksnummer=%s, henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
        logLevel = LogLevel.WARN)
    Feil behandlingFinnesIkkeIFagsaksystemet(String saksnummer, Henvisning henvisning);
}
