package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;

@Path("/fordel")
@ApplicationScoped
@Transactional
public class FordelRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(FordelRestTjeneste.class);

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
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.FAGSAK)
    public void mottaJournalpost(@Parameter(description = "Krever saksnummer, journalpostId og behandlingstemaOffisiellKode") @Valid AbacJournalpostMottakDto mottattJournalpost) {

        String dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode().orElse(null);
        String saksnummer = mottattJournalpost.getSaksnummer();
        Optional<Behandling> behandlingForSaksnummer = harBehandlingPåVent(new Saksnummer(saksnummer));
        UUID forsendelseId = mottattJournalpost.getForsendelseId().orElse(null);

        if (behandlingForSaksnummer.isPresent()) {
            Behandling behandling = behandlingForSaksnummer.get();
            if (erTilbakemeldingFraBruker(dokumentTypeId)) {
                logger.info("Mottok dokument og tok behandlingId={} av vent. Saksnummer={} dokumentTypeId={} forsendelseId={}", behandling.getId(), saksnummer, dokumentTypeId, forsendelseId);
                gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId(), HistorikkAktør.SØKER); //ta behandling av vent
            } else {
                logger.info("Mottok og ignorerte dokument pga dokumentTypeId. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
            }
        } else {
            logger.info("Mottok og ignorerte dokument siden ingen behandling er på vent for saken. Saksnummer={} dokumentTypeId={} forsendelseId={}", saksnummer, dokumentTypeId, forsendelseId);
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
                    .filter(beh -> beh.isBehandlingPåVent()).findAny();
        }
        return Optional.empty();
    }
}
