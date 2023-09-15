package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/fordel")
@ApplicationScoped
@Transactional
public class FordelRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(FordelRestTjeneste.class);

    public static final String UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID = "I000114";

    private BehandlingRepository behandlingRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    public FordelRestTjeneste() {
        // for CDI
    }

    @Inject
    public FordelRestTjeneste(BehandlingRepository behandlingRepository,
                              GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
    }


    @POST
    @Path("/journalpost")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "fordel", description = "Ny journalpost skal behandles.", summary = "Varsel om en ny journalpost som skal behandles i systemet.")
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.FAGSAK)
    public void mottaJournalpost(@Parameter(description = "Krever saksnummer, journalpostId og behandlingstemaOffisiellKode") @Valid AbacJournalpostMottakDto mottattJournalpost) {

        String dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode().orElse(null);
        String saksnummer = mottattJournalpost.getSaksnummer();
        Optional<Behandling> behandlingForSaksnummer = harBehandlingPåVent(new Saksnummer(saksnummer));
        UUID forsendelseId = mottattJournalpost.getForsendelseId().orElse(null);

        if (behandlingForSaksnummer.isPresent()) {
            Behandling behandling = behandlingForSaksnummer.get();
            if (erTilbakemeldingFraBruker(dokumentTypeId)) {
                LOG.info("Mottok dokument og tok behandlingId={} av vent. Saksnummer={} dokumentTypeId={} forsendelseId={}", behandling.getId(), saksnummer, dokumentTypeId, forsendelseId);
                gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId(), HistorikkAktør.SØKER, ResponsKanal.SELVBETJENING); //ta behandling av vent
            } else {
                LOG.info("Mottok og ignorerte dokument pga dokumentTypeId. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
            }
        } else {
            LOG.info("Mottok og ignorerte dokument siden ingen behandling er på vent for saken. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
        }
    }

    private boolean erTilbakemeldingFraBruker(String dokumentTypeId) {
        return UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID.equals(dokumentTypeId);
    }

    private Optional<Behandling> harBehandlingPåVent(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummer);
        if (!behandlinger.isEmpty()) {
            return behandlinger.stream()
                    .filter(beh -> !beh.erAvsluttet())
                    .filter(Behandling::isBehandlingPåVent).findAny();
        }
        return Optional.empty();
    }
}
