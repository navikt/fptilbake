package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.journalpostapi.JournalpostApiKlient;
import no.nav.journalpostapi.dto.AvsenderMottaker;
import no.nav.journalpostapi.dto.BehandlingTema;
import no.nav.journalpostapi.dto.Bruker;
import no.nav.journalpostapi.dto.BrukerIdType;
import no.nav.journalpostapi.dto.Journalposttype;
import no.nav.journalpostapi.dto.SenderMottakerIdType;
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
import no.nav.journalpostapi.dto.sak.FagsakSystem;
import no.nav.journalpostapi.dto.sak.Sak;
import no.nav.journalpostapi.dto.sak.Sakstype;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class JournalføringTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(JournalføringTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private JournalpostApiKlient journalpostApiKlient;
    private AktørConsumer aktørConsumer;
    private String appName;

    JournalføringTjeneste() {
        //for CDI proxy
    }

    @Inject
    public JournalføringTjeneste(BehandlingRepository behandlingRepository, VergeRepository vergeRepository, JournalpostApiKlient journalpostApiKlient, AktørConsumer aktørConsumer, @KonfigVerdi(value = "app.name") String appName) {
        this.behandlingRepository = behandlingRepository;
        this.vergeRepository = vergeRepository;
        this.journalpostApiKlient = journalpostApiKlient;
        this.aktørConsumer = aktørConsumer;
        this.appName = appName;
    }

    public JournalpostIdOgDokumentId journalførVedlegg(Long behandlingId, byte[] vedleggPdf) {
        logger.info("Starter journalføring av vedlegg for vedtaksbrev for behandlingId={}", behandlingId);

        boolean forsøkFerdigstill = true;
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
            .medTema(utledTema(behandling.getFagsak().getFagsakYtelseType()))
            .medBehandlingstema(BehandlingTema.TILBAKEBETALING)
            .medBruker(new Bruker(BrukerIdType.AktørId, behandling.getAktørId().getId()))
            .medEksternReferanseId(behandling.getUuid().toString())
            .medJournalførendeEnhet(behandling.getBehandlendeEnhetId())
            .medJournalposttype(Journalposttype.NOTAT)
            .medTittel("Oversikt over resultatet av tilbakebetalingssaken")
            .medSak(lagSaksreferanse(behandling.getFagsak()))
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
        logger.info("Journalførte vedlegg for vedtaksbrev for behandlingId={} med journalpostid={}", behandlingId, journalpostId.getVerdi());
        return new JournalpostIdOgDokumentId(journalpostId, response.getDokumenter().get(0).getDokumentInfoId());
    }

    private AvsenderMottaker lagMottaker(Long behandlingId, BrevMottaker mottaker, BrevMetadata brevMetadata) {
        Adresseinfo adresseinfo = brevMetadata.getMottakerAdresse();
        switch (mottaker) {
            case BRUKER:
                return AvsenderMottaker.builder()
                    .medId(SenderMottakerIdType.NorskIdent, adresseinfo.getPersonIdent().getIdent())
                    .medNavn(adresseinfo.getMottakerNavn())
                    .medLand(adresseinfo.getLand())
                    .build();
            case VERGE:
                return lagMottakerVerge(behandlingId, adresseinfo);
            default:
                throw new IllegalArgumentException("Ikke-støttet mottaker: " + mottaker);
        }
    }

    private AvsenderMottaker lagMottakerVerge(Long behandlingId, Adresseinfo adresseinfo) {
        VergeEntitet verge = vergeRepository.finnVergeInformasjon(behandlingId).orElseThrow();
        if (verge.getOrganisasjonsnummer() != null) {
            return AvsenderMottaker.builder()
                .medId(SenderMottakerIdType.Organisasjonsnummer, verge.getOrganisasjonsnummer())
                .medNavn(verge.getNavn())
                .medLand(adresseinfo.getLand())
                .build();
        } else {
            String fnrVerge = aktørConsumer.hentPersonIdentForAktørId(verge.getVergeAktørId().getId()).orElseThrow();
            return AvsenderMottaker.builder()
                .medId(SenderMottakerIdType.NorskIdent, fnrVerge)
                .medNavn(verge.getNavn())
                .medLand(adresseinfo.getLand())
                .build();
        }
    }

    public JournalpostIdOgDokumentId journalførUtgåendeBrev(Long behandlingId, Dokumentkategori dokumentkategori, BrevMetadata brevMetadata, BrevMottaker brevMottaker, byte[] vedleggPdf) {
        logger.info("Starter journalføring av {} til {} for behandlingId={}", dokumentkategori, brevMottaker, behandlingId);

        boolean forsøkFerdigstill = true;
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
            .medTema(utledTema(behandling.getFagsak().getFagsakYtelseType()))
            .medBehandlingstema(BehandlingTema.TILBAKEBETALING)
            .medBruker(new Bruker(BrukerIdType.AktørId, behandling.getAktørId().getId()))
            .medAvsenderMottaker(lagMottaker(behandlingId, brevMottaker, brevMetadata))
            .medEksternReferanseId(behandling.getUuid().toString())
            .medJournalførendeEnhet(behandling.getBehandlendeEnhetId())
            .medJournalposttype(Journalposttype.UTGÅENDE)
            .medTittel(brevMetadata.getTittel())
            .medSak(lagSaksreferanse(behandling.getFagsak()))
            .medHoveddokument(Dokument.builder()
                .medDokumentkategori(dokumentkategori)
                .medTittel(brevMetadata.getTittel())
                .medBrevkode(brevMetadata.getFagsaktype().getKode() + "-TILB")
                .leggTilDokumentvariant(Dokumentvariant.builder()
                    .medFilnavn(dokumentkategori == Dokumentkategori.Vedtaksbrev ? "vedtak.pdf" : "brev.pdf")
                    .medVariantformat(Variantformat.Arkiv)
                    .medDokument(vedleggPdf)
                    .medFiltype(Filtype.PDFA)
                    .build())
                .build())
            .build();

        OpprettJournalpostResponse response = journalpostApiKlient.opprettJournalpost(request, forsøkFerdigstill);
        JournalpostId journalpostId = new JournalpostId(response.getJournalpostId());
        if (response.getDokumenter().size() != 1) {
            throw JournalføringTjenesteFeil.FACTORY.uforventetAntallDokumenterIRespons(response.getDokumenter().size()).toException();
        }
        logger.info("Journalførte utgående {} til {} for behandlingId={} med journalpostid={}", dokumentkategori, brevMottaker, behandlingId, journalpostId.getVerdi());
        return new JournalpostIdOgDokumentId(journalpostId, response.getDokumenter().get(0).getDokumentInfoId());
    }

    private static Tema utledTema(FagsakYtelseType fagsakYtelseType) {
        switch (fagsakYtelseType) {

            case ENGANGSTØNAD:
            case FORELDREPENGER:
            case SVANGERSKAPSPENGER:
                return Tema.FORELDREPENGER_SVANGERSKAPSPENGER;
            case FRISINN:
                return Tema.FRISINN;
            case PLEIEPENGER_SYKT_BARN:
            case PLEIEPENGER_NÆRSTÅENDE:
            case OMSORGSPENGER:
            case OPPLÆRINGSPENGER:
                return Tema.OMSORGSPENGER_PLEIEPENGER_OPPLÆRINGSPENGER;
            default:
                throw new IllegalArgumentException("Ikke-støttet ytelseType: " + fagsakYtelseType);
        }
    }

    private Sak lagSaksreferanse(Fagsak fagsak) {
        switch (appName) {
            case "fptilbake":
                return lagReferanseTilGsakSak(fagsak.getSaksnummer());
            case "k9-tilbake":
                return lagReferanseTilK9FagsystemSak(fagsak.getSaksnummer());
            default:
                throw new IllegalArgumentException("Ikke-støttet app.name: " + appName);
        }
    }

    private Sak lagReferanseTilK9FagsystemSak(Saksnummer saksnummer) {
        return Sak.builder()
            .medSakstype(Sakstype.FAGSAK)
            .medFagsak(FagsakSystem.K9SAK, saksnummer.getVerdi())
            .build();
    }

    private Sak lagReferanseTilGsakSak(Saksnummer saksnummer) {
        return Sak.builder()
            .medSakstype(Sakstype.ARKIVSAK)
            .medArkivsak(Arkivsaksystem.GSAK, saksnummer.getVerdi())
            .build();
    }

    interface JournalføringTjenesteFeil extends DeklarerteFeil {

        JournalføringTjenesteFeil FACTORY = FeilFactory.create(JournalføringTjenesteFeil.class);

        @IntegrasjonFeil(feilkode = "FPT-496149", feilmelding = "Forsøkte å journalføre 1 dokument (vedlegg til vedtaksbrev), fikk %s dokumenter i respons fra dokarkiv", logLevel = LogLevel.WARN)
        Feil uforventetAntallDokumenterIRespons(Integer antallDokumenter);

    }

}
