package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface DokumentbestillingFeil extends DeklarerteFeil {
    DokumentbestillingFeil FACTORY = FeilFactory.create(DokumentbestillingFeil.class);

    @TekniskFeil(feilkode = "FPT-089912", feilmelding = "Fant ikke person med aktørId %s i tps", logLevel = LogLevel.WARN)
    Feil fantIkkeAdresseForAktørId(String aktørId);

    @TekniskFeil(feilkode = "FPT-841932", feilmelding = "Fant ikke behandling med saksnummer %s i fpsak", logLevel = LogLevel.WARN)
    Feil fantIkkeBehandlingIFpsak(String saksnummer);

    @TekniskFeil(feilkode = "FPT-748279", feilmelding = "Fant ikke behandling med behandlingId %s fpoppdrag", logLevel = LogLevel.WARN)
    Feil fantIkkeBehandlingIFpoppdrag(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-096507", feilmelding = "Feilet ved konvertering av brev til XML", logLevel = LogLevel.WARN)
    Feil feiletVedKonverteringTilXml(Exception e);

    @TekniskFeil(feilkode = "FPT-227659", feilmelding = "Feilet ved sending av dokument til Dokumentproduksjon", logLevel = LogLevel.WARN)
    Feil feilFraDokumentProduksjon(Exception e);

    @TekniskFeil(feilkode = "FPT-110800", feilmelding = "Feilet ved tekstgenerering til brev", logLevel = LogLevel.WARN)
    Feil feilVedTekstgenerering(Exception e);
}
