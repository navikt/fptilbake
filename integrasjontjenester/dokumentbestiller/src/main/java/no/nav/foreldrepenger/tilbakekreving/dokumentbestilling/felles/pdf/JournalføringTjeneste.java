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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
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
import no.nav.journalpostapi.dto.sak.FagsakSystem;
import no.nav.journalpostapi.dto.sak.Sak;
import no.nav.vedtak.exception.IntegrasjonException;

@ApplicationScoped
public class JournalføringTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(JournalføringTjeneste.class);
    private static final Fagsystem appName = ApplicationName.hvilkenTilbake();

    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private JournalpostApiKlient journalpostApiKlient;
    private PersoninfoAdapter aktørConsumer;


    JournalføringTjeneste() {
        //for CDI proxy
    }

    @Inject
    public JournalføringTjeneste(BehandlingRepository behandlingRepository, VergeRepository vergeRepository, JournalpostApiKlient journalpostApiKlient, PersoninfoAdapter aktørConsumer) {
        this.behandlingRepository = behandlingRepository;
        this.vergeRepository = vergeRepository;
        this.journalpostApiKlient = journalpostApiKlient;
        this.aktørConsumer = aktørConsumer;
    }

    public JournalpostIdOgDokumentId journalførVedlegg(Long behandlingId, byte[] vedleggPdf) {
        logger.info("Starter journalføring av vedlegg for vedtaksbrev for behandlingId={}", behandlingId);

        boolean forsøkFerdigstill = true;
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
            .medTema(utledTema(behandling.getFagsak().getFagsakYtelseType()))
            .medBehandlingstema(BehandlingTema.TILBAKEBETALING)
            .medBruker(new Bruker(BrukerIdType.AktørId, behandling.getAktørId().getId()))
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
            throw uforventetAntallDokumenterIRespons(response.getDokumenter().size());
        }
        logger.info("Journalførte vedlegg for vedtaksbrev for behandlingId={} med journalpostid={}", behandlingId, journalpostId.getVerdi());
        return new JournalpostIdOgDokumentId(journalpostId, response.getDokumenter().get(0).getDokumentInfoId());
    }

    private AvsenderMottaker lagMottaker(Long behandlingId, BrevMottaker mottaker, BrevMetadata brevMetadata) {
        Adresseinfo adresseinfo = brevMetadata.getMottakerAdresse();
        return switch (mottaker) {
            case BRUKER -> AvsenderMottaker.builder()
                .medId(SenderMottakerIdType.NorskIdent, adresseinfo.getPersonIdent().getIdent())
                .medNavn(adresseinfo.getMottakerNavn())
                .build();
            case VERGE -> lagMottakerVerge(behandlingId);
            default -> throw new IllegalArgumentException("Ikke-støttet mottaker: " + mottaker);
        };
    }

    private AvsenderMottaker lagMottakerVerge(Long behandlingId) {
        VergeEntitet verge = vergeRepository.finnVergeInformasjon(behandlingId).orElseThrow();
        if (verge.getOrganisasjonsnummer() != null) {
            return AvsenderMottaker.builder()
                .medId(SenderMottakerIdType.Organisasjonsnummer, verge.getOrganisasjonsnummer())
                .medNavn(verge.getNavn())
                .build();
        } else {
            String fnrVerge = aktørConsumer.hentFnrForAktør(verge.getVergeAktørId()).map(PersonIdent::getIdent).orElseThrow();
            return AvsenderMottaker.builder()
                .medId(SenderMottakerIdType.NorskIdent, fnrVerge)
                .medNavn(verge.getNavn())
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
            throw uforventetAntallDokumenterIRespons(response.getDokumenter().size());
        }
        logger.info("Journalførte utgående {} til {} for behandlingId={} med journalpostid={}", dokumentkategori, brevMottaker, behandlingId, journalpostId.getVerdi());
        return new JournalpostIdOgDokumentId(journalpostId, response.getDokumenter().get(0).getDokumentInfoId());
    }

    private static Tema utledTema(FagsakYtelseType fagsakYtelseType) {
        return switch (fagsakYtelseType) {
            case ENGANGSTØNAD, FORELDREPENGER, SVANGERSKAPSPENGER -> Tema.FORELDREPENGER_SVANGERSKAPSPENGER;
            case FRISINN -> Tema.FRISINN;
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER, OPPLÆRINGSPENGER -> Tema.OMSORGSPENGER_PLEIEPENGER_OPPLÆRINGSPENGER;
            default -> throw new IllegalArgumentException("Ikke-støttet ytelseType: " + fagsakYtelseType);
        };
    }

    private Sak lagSaksreferanse(Fagsak fagsak) {
        return switch (appName) {
            case FPTILBAKE -> new Sak(fagsak.getSaksnummer().getVerdi(), FagsakSystem.FORELDREPENGELØSNINGEN);
            case K9TILBAKE -> new Sak(fagsak.getSaksnummer().getVerdi(), FagsakSystem.K9SAK);
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjonsnavn: " + appName);
        };
    }


    private static IntegrasjonException uforventetAntallDokumenterIRespons(Integer antallDokumenter) {
        return new IntegrasjonException("FPT-496149", String.format("Forsøkte å journalføre 1 dokument (vedlegg til vedtaksbrev), fikk %s dokumenter i respons fra dokarkiv", antallDokumenter));
    }



}
