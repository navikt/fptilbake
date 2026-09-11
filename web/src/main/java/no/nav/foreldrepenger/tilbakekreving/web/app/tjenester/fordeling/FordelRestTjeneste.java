package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
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
    private FagsakRepository fagsakRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private VarselresponsTjeneste varselresponsTjeneste;

    public FordelRestTjeneste() {
        // for CDI
    }

    @Inject
    public FordelRestTjeneste(BehandlingRepository behandlingRepository,
                              FagsakRepository fagsakRepository,
                              GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                              VarselresponsTjeneste varselresponsTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.fagsakRepository = fagsakRepository;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.varselresponsTjeneste = varselresponsTjeneste;
    }


    @POST
    @Path("/journalpost")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "fordel", description = "Ny journalpost skal behandles.", summary = "Varsel om en ny journalpost som skal behandles i systemet.")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public void mottaJournalpost(@TilpassetAbacAttributt(supplierClass = JournalpostMottakDtoAbacDataSupplier.class)
                                     @Parameter(description = "Krever saksnummer, journalpostId og behandlingstemaOffisiellKode")
                                     @NotNull @Valid JournalpostMottakDto mottattJournalpost) {

        String dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode().orElse(null);
        String saksnummer = mottattJournalpost.getSaksnummer();
        UUID forsendelseId = mottattJournalpost.getForsendelseId().orElse(null);

        if (!erTilbakemeldingFraBruker(dokumentTypeId)) {
            LOG.info("Mottok og ignorerte dokument pga dokumentTypeId. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
            return;
        }

        var åpenBehandling = hentÅpenBehandling(saksnummer);
        if (åpenBehandling.isPresent()) {
            var behandling = åpenBehandling.get();
            LOG.info("Mottok dokument og tok behandlingId={} av vent. Saksnummer={} dokumentTypeId={} forsendelseId={}", behandling.getId(), saksnummer, dokumentTypeId, forsendelseId);
            varselresponsTjeneste.lagreRespons(behandling.getId(), ResponsKanal.SELVBETJENING);
            gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId(), behandling.getFagsakId(), HistorikkAktør.VEDTAKSLØSNINGEN);
            return;
        }

        // Uttalelsen kan komme inn etter at behandlingen er avsluttet. Den skal fortsatt være synlig i
        // fagsakhistorikken, og lagres derfor uten behandling.
        var fagsak = fagsakRepository.hentSakGittSaksnummer(new Saksnummer(saksnummer));
        if (fagsak.isPresent()) {
            LOG.info("Mottok dokument uten åpen behandling for saken. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
            varselresponsTjeneste.opprettHistorikkinnslagForUttalelseUtenBehandling(fagsak.get().getId());
        } else {
            LOG.info("Mottok og ignorerte dokument siden fagsaken ikke finnes. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
        }
    }

    public static class JournalpostMottakDtoAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (JournalpostMottakDto) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
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
