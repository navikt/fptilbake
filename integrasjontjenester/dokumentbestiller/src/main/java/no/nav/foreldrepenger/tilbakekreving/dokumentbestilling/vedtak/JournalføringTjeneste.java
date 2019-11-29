package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.journalpostapi.JournalpostApiKlient;
import no.nav.journalpostapi.dto.BehandlingTema;
import no.nav.journalpostapi.dto.Bruker;
import no.nav.journalpostapi.dto.BrukerIdType;
import no.nav.journalpostapi.dto.Journalposttype;
import no.nav.journalpostapi.dto.Tema;
import no.nav.journalpostapi.dto.Tilleggsopplysning;
import no.nav.journalpostapi.dto.dokument.Dokument;
import no.nav.journalpostapi.dto.dokument.Dokumentkategori;
import no.nav.journalpostapi.dto.dokument.Dokumentvariant;
import no.nav.journalpostapi.dto.dokument.Filtype;
import no.nav.journalpostapi.dto.dokument.Variantformat;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostRequest;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostResponse;
import no.nav.journalpostapi.dto.sak.Arkivsaksystem;
import no.nav.journalpostapi.dto.sak.Sak;
import no.nav.journalpostapi.dto.sak.Sakstype;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

@ApplicationScoped
public class JournalføringTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(JournalføringTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private JournalpostApiKlient journalpostApiKlient;

    JournalføringTjeneste() {
        //for CDI proxy
    }

    @Inject
    public JournalføringTjeneste(BehandlingRepository behandlingRepository, JournalpostApiKlient journalpostApiKlient) {
        this.behandlingRepository = behandlingRepository;
        this.journalpostApiKlient = journalpostApiKlient;
    }

    public JournalpostIdOgDokumentId journalførVedlegg(Long behandlingId, byte[] vedleggPdf) {
        logger.info("Starter journalføring av vedlegg for vedtaksbrev for behandlingId={}", behandlingId);

        boolean forsøkFerdigstill = true;
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
            .medTema(Tema.FORELDREPENGER_SVANGERSKAPSPENGER)
            .medBehandlingstema(BehandlingTema.TILBAKEBETALING)
            .medBruker(new Bruker(BrukerIdType.AktørId, behandling.getAktørId().getId()))
            .medEksternReferanseId(behandlingId.toString()) //FIXME bytt til uuid
            .medJournalførendeEnhet(behandling.getBehandlendeEnhetId())
            .medJournalposttype(Journalposttype.NOTAT)
            .medTittel("Oversikt over resultatet av tilbakebetalingssaken")
            .medSak(Sak.builder()
                .medSakstype(Sakstype.ARKIVSAK)
                .medArkivsak(Arkivsaksystem.GSAK, behandling.getFagsak().getSaksnummer().getVerdi())
                .build())
            .medHoveddokument(Dokument.builder()
                .medDokumentkategori(Dokumentkategori.Infobrev)
                .medTittel("Oversikt over resultatet av tilbakebetalingssaken")
                .medBrevkode("FP-TILB")
                .leggTilDokumentvariant(Dokumentvariant.builder()
                    .medFilnavn("vedlegg.pdf")
                    .medVariantformat(Variantformat.Arkiv)
                    .medDokument(vedleggPdf)
                    .medFiltype(Filtype.PDFA)
                    .build())
                .build())
            .leggTilTilleggsopplysning(new Tilleggsopplysning("bruksområde", "brukes i vedlegg til vedtaksbrev for tilbakekreving"))
            .build();

        OpprettJournalpostResponse response = journalpostApiKlient.opprettJournalpost(request, forsøkFerdigstill);
        JournalpostId journalpostId = new JournalpostId(response.getJournalpostId());
        if (response.getDokumenter().size() != 1) {
            throw JournalføringTjenesteFeil.FACTORY.uforventetAntallDokumenterIRespons(response.getDokumenter().size()).toException();
        }
        if (!response.isJournalpostFerdigstilt()) {
            throw JournalføringTjenesteFeil.FACTORY.journalføringIkkeFerdigstilt().toException();
        }
        logger.info("Journalførte vedlegg for vedtaksbrev for behandlingId={} med journalpostid={}", behandlingId, journalpostId.getVerdi());
        return new JournalpostIdOgDokumentId(journalpostId, response.getDokumenter().get(0).getDokumentInfoId());
    }

    interface JournalføringTjenesteFeil extends DeklarerteFeil {

        JournalføringTjenesteFeil FACTORY = FeilFactory.create(JournalføringTjenesteFeil.class);

        @IntegrasjonFeil(feilkode = "FPT-496149", feilmelding = "Forsøkte å journalføre 1 dokument (vedlegg til vedtaksbrev), fikk %s dokumenter i respons fra dokarkiv", logLevel = LogLevel.WARN)
        Feil uforventetAntallDokumenterIRespons(Integer antallDokumenter);

        @IntegrasjonFeil(feilkode = "FPT-954690", feilmelding = "Journalføring av vedlegget til vedtaksbrevet ble ikke ferdigstilt", logLevel = LogLevel.WARN)
        Feil journalføringIkkeFerdigstilt();

    }

}
