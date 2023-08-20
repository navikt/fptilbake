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
import no.nav.journalpostapi.DokArkivKlient;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.DokArkiv;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.AvsenderMottaker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.DokumentInfoOpprett;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Sak;

@ApplicationScoped
public class JournalføringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(JournalføringTjeneste.class);
    private static final Fagsystem appName = ApplicationName.hvilkenTilbake();

    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private DokArkiv dokArkivKlient;
    private PersoninfoAdapter aktørConsumer;


    JournalføringTjeneste() {
        //for CDI proxy
    }

    @Inject
    public JournalføringTjeneste(BehandlingRepository behandlingRepository, VergeRepository vergeRepository,
                                 DokArkiv dokArkivKlient, PersoninfoAdapter aktørConsumer) {
        this.behandlingRepository = behandlingRepository;
        this.vergeRepository = vergeRepository;
        this.dokArkivKlient = dokArkivKlient;
        this.aktørConsumer = aktørConsumer;
    }

    private AvsenderMottaker lagMottaker(Long behandlingId, BrevMottaker mottaker, BrevMetadata brevMetadata) {
        Adresseinfo adresseinfo = brevMetadata.getMottakerAdresse();
        return switch (mottaker) {
            case BRUKER -> new AvsenderMottaker(adresseinfo.getPersonIdent().getIdent(), AvsenderMottaker.AvsenderMottakerIdType.FNR, adresseinfo.getMottakerNavn());
            case VERGE -> lagMottakerVerge(behandlingId);
        };
    }

    private AvsenderMottaker lagMottakerVerge(Long behandlingId) {
        VergeEntitet verge = vergeRepository.finnVergeInformasjon(behandlingId).orElseThrow();
        if (verge.getOrganisasjonsnummer() != null) {
            return new AvsenderMottaker(verge.getOrganisasjonsnummer(), AvsenderMottaker.AvsenderMottakerIdType.ORGNR, verge.getNavn());
        } else {
            String fnrVerge = aktørConsumer.hentFnrForAktør(verge.getVergeAktørId()).map(PersonIdent::getIdent).orElseThrow();
            return new AvsenderMottaker(fnrVerge, AvsenderMottaker.AvsenderMottakerIdType.FNR, verge.getNavn());
        }
    }

    public JournalpostIdOgDokumentId journalførUtgåendeBrev(Long behandlingId, String dokumentkategori, BrevMetadata brevMetadata, BrevMottaker brevMottaker, byte[] vedleggPdf) {
        LOG.info("Starter journalføring av {} til {} for behandlingId={}", dokumentkategori, brevMottaker, behandlingId);

        boolean forsøkFerdigstill = true;
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        var request = OpprettJournalpostRequest.nyUtgående()
                .medTema(utledTema(behandling.getFagsak().getFagsakYtelseType()))
                .medBehandlingstema(DokArkivKlient.BEHANDLINGTEMA_TILBAKEBETALING)
                .medBruker(new Bruker(behandling.getAktørId().getId(), Bruker.BrukerIdType.AKTOERID))
                .medAvsenderMottaker(lagMottaker(behandlingId, brevMottaker, brevMetadata))
                .medJournalfoerendeEnhet(behandling.getBehandlendeEnhetId())
                .medTittel(brevMetadata.getTittel())
                .medSak(lagSaksreferanse(behandling.getFagsak()))
                .leggTilDokument(DokumentInfoOpprett.builder()
                        .medDokumentkategori(dokumentkategori)
                        .medTittel(brevMetadata.getTittel())
                        .medBrevkode(brevMetadata.getFagsaktype().getKode() + "-TILB")
                        .leggTilDokumentvariant(new Dokumentvariant(Dokumentvariant.Variantformat.ARKIV, Dokumentvariant.Filtype.PDFA, vedleggPdf)))
                .build();

        var response = dokArkivKlient.opprettJournalpost(request, forsøkFerdigstill);
        JournalpostId journalpostId = new JournalpostId(response.journalpostId());
        if (response.dokumenter().size() != 1) {
            throw uforventetAntallDokumenterIRespons(response.dokumenter().size());
        }
        LOG.info("Journalførte utgående {} til {} for behandlingId={} med journalpostid={}", dokumentkategori, brevMottaker, behandlingId, journalpostId.getVerdi());
        return new JournalpostIdOgDokumentId(journalpostId, response.dokumenter().get(0).dokumentInfoId());
    }

    private static String utledTema(FagsakYtelseType fagsakYtelseType) {
        return switch (fagsakYtelseType) {
            case ENGANGSTØNAD, FORELDREPENGER, SVANGERSKAPSPENGER -> DokArkivKlient.TEMA_FORELDREPENGER_SVANGERSKAPSPENGER;
            case FRISINN -> DokArkivKlient.TEMA_FRISINN;
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER, OPPLÆRINGSPENGER -> DokArkivKlient.TEMA_OMSORGSPENGER_PLEIEPENGER_OPPLÆRINGSPENGER;
            default -> throw new IllegalArgumentException("Ikke-støttet ytelseType: " + fagsakYtelseType);
        };
    }

    private Sak lagSaksreferanse(Fagsak fagsak) {
        return switch (appName) {
            case FPTILBAKE -> new Sak(fagsak.getSaksnummer().getVerdi(), Fagsystem.FPSAK.getOffisiellKode(), Sak.Sakstype.FAGSAK);
            case K9TILBAKE -> new Sak(fagsak.getSaksnummer().getVerdi(), Fagsystem.K9SAK.getOffisiellKode(), Sak.Sakstype.FAGSAK);
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjonsnavn: " + appName);
        };
    }


    private static IntegrasjonException uforventetAntallDokumenterIRespons(Integer antallDokumenter) {
        return new IntegrasjonException("FPT-496149", String.format("Forsøkte å journalføre 1 dokument (vedlegg til vedtaksbrev), fikk %s dokumenter i respons fra dokarkiv", antallDokumenter));
    }


}
