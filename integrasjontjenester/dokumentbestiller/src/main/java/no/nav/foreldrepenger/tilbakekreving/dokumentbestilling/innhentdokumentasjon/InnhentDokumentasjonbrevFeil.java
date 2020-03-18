package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InnhentDokumentasjonbrevFeil extends DeklarerteFeil {

    InnhentDokumentasjonbrevFeil FACTORY = FeilFactory.create(InnhentDokumentasjonbrevFeil.class);

    @TekniskFeil(feilkode = "FPT-612901", feilmelding = "Kravgrunnlag finnes ikke for behandling=%s, kan ikke sende innhent-dokumentasjonbrev", logLevel = LogLevel.WARN)
    Feil kanIkkeSendeBrevForGrunnlagFinnesIkke(Long behandlingId);
}
