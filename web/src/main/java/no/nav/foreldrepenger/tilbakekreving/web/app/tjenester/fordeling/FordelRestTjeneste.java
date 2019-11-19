package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Api(tags = {"fordel"})
@Path("/fordel")
@ApplicationScoped
@Transaction
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
    @ApiOperation(value = "Ny journalpost skal behandles.", notes = ("Varsel om en ny journalpost som skal behandles i systemet."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.UPDATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void mottaJournalpost(@ApiParam("Krever saksnummer, journalpostId og behandlingstemaOffisiellKode") @Valid AbacJournalpostMottakDto mottattJournalpost) {

        Optional<String> dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode();
        Saksnummer saksnummer = new Saksnummer(mottattJournalpost.getSaksnummer());
        Optional<Behandling> behandlingForSaksnummer = harBehandlingPåVent(saksnummer);
        UUID forsendelseId = mottattJournalpost.getForsendelseId().orElse(null);

        if (behandlingForSaksnummer.isPresent()) {
            logger.info("Behandling finnes for dokumentTypeId={},saksnummer={} og forsendelseId={}", dokumentTypeId.get(), saksnummer, forsendelseId);
            Behandling behandling = behandlingForSaksnummer.get();

            if (erTilbakemeldingFraBruker(dokumentTypeId)) {
                logger.info("Mottok dokument og tok behandlingId={} av vent. Saksnummer={} dokumentTypeId={} forsendelseId={}",
                    behandling.getId(), saksnummer, dokumentTypeId, forsendelseId);
                gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId()); //ta behandling av vent
            } else {
                logger.info("Mottok og ignorerte dokument pga dokumentTypeId. Saksnummer={} dokumentTypeId={} forsendelseId={}",
                    saksnummer, dokumentTypeId, forsendelseId);
            }

        } else {
            logger.info("Mottok og ignorerte dokument siden ingen behandling er på vent for saken. Saksnummer={} dokumentTypeId={} forsendelseId={}",
                saksnummer, dokumentTypeId, forsendelseId);
        }
    }

    private boolean erTilbakemeldingFraBruker(Optional<String> dokumentTypeId) {
        return dokumentTypeId.isPresent() && UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID.equals(dokumentTypeId.get());
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
