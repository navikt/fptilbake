package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/feilutbetaling")
@Produces(APPLICATION_JSON)
@RequestScoped
@Transactional
public class FeilutbetalingSisteBehandlingRestTjeneste {

    private BehandlingRepository behandlingRepository;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;

    public FeilutbetalingSisteBehandlingRestTjeneste() {
        // For CDI
    }

    @Inject
    public FeilutbetalingSisteBehandlingRestTjeneste(BehandlingRepository behandlingRepository, KravgrunnlagTjeneste kravgrunnlagTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
    }

    @POST
    @Path("/siste-behandling")
    @Operation(
        tags = "feilutbetaling",
        description = "Henter feilutbetalte perioder og avsluttet dato for siste ordinære tilbakekrevingsbehandling. Brukes av k9-sak for å vurdere om ytelsesvedtak skal vente på tilbakekrevingsvedtak",
        responses = {
            @ApiResponse(responseCode = "200", description = "Feilutbetalte perioder og evt. avsluttet dato for siste ordinære tilbakekrevingsbehandling", content = @Content(schema = @Schema(implementation = BehandlingStatusOgFeilutbetalinger.class))),
            @ApiResponse(responseCode = "204", description = "Det finnes ingen tilbakekrevigsbehandlnig for saken")
        })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response hentInfoForSisteFørstegangsBehandling(@NotNull @Valid SaksnummerDto saksnummer) {
        Saksnummer saksnummeret = new Saksnummer(saksnummer.getVerdi());

        List<Behandling> behandlinger = behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummeret);
        Optional<Behandling> sisteBehandling = behandlinger.stream()
            .filter(b -> b.getType() == BehandlingType.TILBAKEKREVING)
            .max(Comparator.comparing(BaseEntitet::getOpprettetTidspunkt));

        if (sisteBehandling.isEmpty()) {
            return Response.noContent().build();
        }

        Behandling behandlingen = sisteBehandling.get();
        LocalDate avsluttetDato = behandlingen.getAvsluttetDato() != null ? behandlingen.getAvsluttetDato().toLocalDate() : null;
        List<LogiskPeriode> logiskePerioder = kravgrunnlagTjeneste.utledLogiskPeriode(behandlingen.getId());
        List<Periode> perioder = logiskePerioder.stream().map(LogiskPeriode::getPeriode).toList();
        return Response.ok(new BehandlingStatusOgFeilutbetalinger(avsluttetDato, perioder)).build();
    }

    public static class BehandlingStatusOgFeilutbetalinger {

        private LocalDate avsluttetDato;

        private List<Periode> feilutbetaltePerioder;

        @JsonCreator
        public BehandlingStatusOgFeilutbetalinger(LocalDate avsluttetDato, List<Periode> feilutbetaltePerioder) {
            this.avsluttetDato = avsluttetDato;
            this.feilutbetaltePerioder = feilutbetaltePerioder;
        }

        public LocalDate getAvsluttetDato() {
            return avsluttetDato;
        }

        public List<Periode> getFeilutbetaltePerioder() {
            return feilutbetaltePerioder;
        }
    }

}
