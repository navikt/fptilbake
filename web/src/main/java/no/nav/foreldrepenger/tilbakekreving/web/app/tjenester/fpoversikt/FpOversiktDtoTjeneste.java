package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fpoversikt;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;

@ApplicationScoped
public class FpOversiktDtoTjeneste {

    private VarselresponsTjeneste varselresponsTjeneste;
    private BrevSporingRepository brevSporingRepository;
    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;

    @Inject
    public FpOversiktDtoTjeneste(VarselresponsTjeneste varselresponsTjeneste,
                                 BrevSporingRepository brevSporingRepository,
                                 BehandlingRepository behandlingRepository,
                                 VergeRepository vergeRepository) {
        this.varselresponsTjeneste = varselresponsTjeneste;
        this.brevSporingRepository = brevSporingRepository;
        this.behandlingRepository = behandlingRepository;
        this.vergeRepository = vergeRepository;
    }

    FpOversiktDtoTjeneste() {
        //CDI
    }

    public Optional<Sak> hentSak(Saksnummer saksnummer) {
        var behandling = behandlingRepository.finnÅpenTilbakekrevingsbehandling(saksnummer);
        if (behandling.isEmpty()) {
            //Fpoversikt er bare opptatt av åpne tkb (enn så lenge)
            return Optional.empty();
        }
        var behandlingId = behandling.get().getId();
        var harVerge = vergeRepository.finnesVerge(behandlingId);
        var fagsak = behandling.get().getFagsak();

        var varsel = finnBrukerVarsel(behandlingId);
        return Optional.of(new Sak(fagsak.getSaksnummer().getVerdi(), varsel.orElse(null), harVerge));
    }

    private Optional<Sak.Varsel> finnBrukerVarsel(Long behandlingId) {
        return brevSporingRepository.hentSistSendtVarselbrev(behandlingId).map(v -> {
            var respons = varselresponsTjeneste.hentRespons(behandlingId)
                .filter(r -> ResponsKanal.getResponsKanal(r.getKilde()) == ResponsKanal.SELVBETJENING);
            return new Sak.Varsel(v.getOpprettetTidspunkt(), respons.isPresent());
        });
    }
}
