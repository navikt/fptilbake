package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordelling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

@Api(tags = { "fordel" })
@Path("/fordel")
@ApplicationScoped
@Transaction
public class FordelRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(FordelRestTjeneste.class);

    private static final String UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID = "I000114";

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
    @Produces("application/json; charset=UTF-8")
    @ApiOperation(value = "Ny journalpost skal behandles.", notes = ("Varsel om en ny journalpost som skal behandles i systemet."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void mottaJournalpost(@ApiParam("Krever saksnummer, journalpostId og behandlingstemaOffisiellKode") @Valid AbacJournalpostMottakDto mottattJournalpost) {

        Optional<String> dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode();
        Saksnummer saksnummer = new Saksnummer(mottattJournalpost.getSaksnummer());
        Optional<Behandling> behandlingForSaksnummer = hentAktivBehandlingForSaksnummer(saksnummer);
        UUID forsendelseId = mottattJournalpost.getForsendelseId().orElse(null);
        if (behandlingForSaksnummer.isPresent()) {
            logger.info("Behandling finnes for dokumentTypeId={},saksnummer={} og forsendelseId={}", dokumentTypeId.get(), saksnummer, forsendelseId);
            Behandling behandling = behandlingForSaksnummer.get();
            if (erDokumentTypeGyldig(dokumentTypeId)) {
                logger.info("Behandling={} skal fortsette på grunn av motta uttalse om tilbakekreving", behandling.getId());
                gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId()); //ta behandling av vent
            }
        }
    }

    private boolean erDokumentTypeGyldig(Optional<String> dokumentTypeId) {
        return dokumentTypeId.isPresent() && UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID.equals(dokumentTypeId.get());
    }

    private Optional<Behandling> hentAktivBehandlingForSaksnummer(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummer);
        if (!behandlinger.isEmpty()) {
            return behandlinger.stream()
                .filter(beh -> !beh.erAvsluttet())
                .filter(beh -> beh.isBehandlingPåVent()).findAny();
        }
        return Optional.empty();
    }
}
