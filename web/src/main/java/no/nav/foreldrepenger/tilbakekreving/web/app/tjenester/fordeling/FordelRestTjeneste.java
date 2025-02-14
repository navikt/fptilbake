package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/fordel")
@ApplicationScoped
@Transactional
public class FordelRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(FordelRestTjeneste.class);

    public static final String UTTALELSE_TILBAKEKREVING_DOKUMENT_TYPE_ID = "I000114";
    public static final String UTTALELSE_TILBAKEBETALING_DOKUMENT_TYPE_ID = "I000119";

    private BehandlingRepository behandlingRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private VarselresponsTjeneste varselresponsTjeneste;

    public FordelRestTjeneste() {
        // for CDI
    }

    @Inject
    public FordelRestTjeneste(BehandlingRepository behandlingRepository,
                              GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                              VarselresponsTjeneste varselresponsTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.varselresponsTjeneste = varselresponsTjeneste;
    }


    @POST
    @Path("/journalpost")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "fordel", description = "Ny journalpost skal behandles.", summary = "Varsel om en ny journalpost som skal behandles i systemet.")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public void mottaJournalpost(@Parameter(description = "Krever saksnummer, journalpostId og behandlingstemaOffisiellKode") @Valid AbacJournalpostMottakDto mottattJournalpost) {

        String dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode().orElse(null);
        String saksnummer = mottattJournalpost.getSaksnummer();
        UUID forsendelseId = mottattJournalpost.getForsendelseId().orElse(null);

        var åpenBehandling = hentÅpenBehandling(saksnummer);
        if (åpenBehandling.isPresent()) {
            var behandling = åpenBehandling.get();
            if (erTilbakemeldingFraBruker(dokumentTypeId)) {
                LOG.info("Mottok dokument og tok behandlingId={} av vent. Saksnummer={} dokumentTypeId={} forsendelseId={}", behandling.getId(), saksnummer, dokumentTypeId, forsendelseId);
                varselresponsTjeneste.lagreRespons(behandling.getId(), ResponsKanal.SELVBETJENING);
                gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId(), behandling.getFagsakId(), HistorikkAktør.SØKER);
            } else {
                LOG.info("Mottok og ignorerte dokument pga dokumentTypeId. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
            }
        } else {
            LOG.info("Mottok og ignorerte dokument siden ingen behandling er på vent for saken. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
        }
    }

    private Optional<Behandling> hentÅpenBehandling(String saksnummer) {
        return behandlingRepository.hentAlleBehandlingerForSaksnummer(new Saksnummer(saksnummer))
            .stream()
            .filter(b -> !b.erAvsluttet())
            .findAny();
    }

    private boolean erTilbakemeldingFraBruker(String dokumentTypeId) {
        return UTTALELSE_TILBAKEKREVING_DOKUMENT_TYPE_ID.equals(dokumentTypeId) || UTTALELSE_TILBAKEBETALING_DOKUMENT_TYPE_ID.equals(dokumentTypeId);
    }
}
