package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface FritekstbrevFeil extends DeklarerteFeil {
    FritekstbrevFeil FACTORY = FeilFactory.create(FritekstbrevFeil.class);

    @TekniskFeil(feilkode = "FPT-096507", feilmelding = "Feilet ved konvertering av brev til XML", logLevel = LogLevel.WARN)
    Feil feiletVedKonverteringTilXml(Exception e);

    @IntegrasjonFeil(feilkode = "FPT-227659", feilmelding = "Feilet ved sending av dokument til Dokumentproduksjon", logLevel = LogLevel.WARN)
    Feil feilFraDokumentProduksjon(Exception e);

    @IntegrasjonFeil(feilkode = "FPT-493305", feilmelding = "Feilet ved knytting av vedlegg %s til journalpost %s", logLevel = LogLevel.WARN)
    Feil feilVedTilknytningAvVedlegg(JournalpostIdOgDokumentId vedlegg, JournalpostId journalpost, Exception cause);

    @IntegrasjonFeil(feilkode = "FPT-694053", feilmelding = "Feilet ved ferdigstillelse av journalpost %s", logLevel = LogLevel.WARN)
    Feil feilVedFerdigstillelse(JournalpostId journalpostId, Exception e);

}
