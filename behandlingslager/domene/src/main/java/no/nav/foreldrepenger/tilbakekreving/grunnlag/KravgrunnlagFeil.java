package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KravgrunnlagFeil extends DeklarerteFeil {

    KravgrunnlagFeil FEILFACTORY = FeilFactory.create(KravgrunnlagFeil.class);

    @TekniskFeil(feilkode = "FPT-312901", feilmelding = "Klassetype '%s' er ugyldig", logLevel = LogLevel.WARN)
    Feil ugyldigKlasseType(String klassetype);

    @TekniskFeil(feilkode = "FPT-312902", feilmelding = "KravStatusKode '%s' er ugyldig", logLevel = LogLevel.WARN)
    Feil ugyldigKravStatusKode(String kravStatusKode);

    @TekniskFeil(feilkode = "FPT-312903", feilmelding = "GjelderType '%s' er ugyldig", logLevel = LogLevel.WARN)
    Feil ugyldigGjelderType(String gjelderType);

    @FunksjonellFeil(feilkode = "FPT-313924", feilmelding = "Kravgrunnlaget kan ikke brukes lenger",
        exceptionClass = AktivKravgrunnlagException.class, logLevel = LogLevel.WARN, løsningsforslag = "")
    Feil kravgrunnlagetKanIkkeBrukes(long behandlingId);
}
