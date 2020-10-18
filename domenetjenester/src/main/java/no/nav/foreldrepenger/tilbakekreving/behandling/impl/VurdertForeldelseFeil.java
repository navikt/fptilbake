package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface VurdertForeldelseFeil extends DeklarerteFeil {
    VurdertForeldelseFeil FACTORY = FeilFactory.create(VurdertForeldelseFeil.class);

    @TekniskFeil(feilkode = "FPT-793210",
            feilmelding = "Finner ikke grunnlag fra økonomi for behandlingId: %s", logLevel = WARN)
    Feil kanIkkeFinneGrunnlagFraØkonomi(Long behandlingId);
}

