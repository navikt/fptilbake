package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.time.LocalDateTime;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface GrunnlagStegFeil extends DeklarerteFeil {

    GrunnlagStegFeil FACTORY = FeilFactory.create(GrunnlagStegFeil.class);

    @TekniskFeil(feilkode = "FPT-783523",
            feilmelding = "Grunnlag fra Økonomi har ikke blitt mottatt innen fristen: %s, behandlingen kan ikke fortsette uten grunnlaget." +
                    " Kontroller at økonomisystemet er tilgjengelig og har grunnlag for behandling med saksnummer: %s . [ behandlingId: %s ]",
            logLevel = LogLevel.ERROR)
    Feil harIkkeMottattGrunnlagFraØkonomi(LocalDateTime frist, String saksnummer, Long behandlingId);

}
