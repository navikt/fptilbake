package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class AutomatiskSaksbehandlingVurderingTjeneste {

    private KravgrunnlagRepository grunnlagRepository;
    private VarselRepository varselRepository;

    AutomatiskSaksbehandlingVurderingTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AutomatiskSaksbehandlingVurderingTjeneste(KravgrunnlagRepository grunnlagRepository, VarselRepository varselRepository) {
        this.grunnlagRepository = grunnlagRepository;
        this.varselRepository = varselRepository;
    }

    public boolean lavFeilutbetalingKanVentePåAutomatiskBehandling(Behandling behandling) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlagOpt(behandling.getId()).orElse(null);
        return lavFeilutbetalingKanVentePåAutomatiskBehandling(behandling, kravgrunnlag);
    }

    public boolean lavFeilutbetalingKanVentePåAutomatiskBehandling(Behandling behandling, Kravgrunnlag431 kravgrunnlag) {
        return kravgrunnlag != null && varselRepository.finnVarsel(behandling.getId()).isEmpty() &&
            KravgrunnlagBeregningTjeneste.samletFeilutbetaltKanAutomatiskBehandles(kravgrunnlag, behandling.getOpprettetTidspunkt()) &&
            !harLavFeilutbetaltLiggetLengeNokForAutomatiskSaksbehandling(kravgrunnlag);
    }

    private boolean harLavFeilutbetaltLiggetLengeNokForAutomatiskSaksbehandling(Kravgrunnlag431 kravgrunnlag) {
        return ventefristForTilfelleSomKanAutomatiskSaksbehandles(kravgrunnlag).isBefore(LocalDateTime.now());
    }

    public LocalDateTime ventefristForTilfelleSomKanAutomatiskSaksbehandles(Long behandlingId) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        return ventefristForTilfelleSomKanAutomatiskSaksbehandles(kravgrunnlag);
    }

    public static LocalDateTime ventefristForTilfelleSomKanAutomatiskSaksbehandles(Kravgrunnlag431 kravgrunnlag) {
        return kravgrunnlag.getKontrollFeltAsLocalDate()
            .plus(AutomatiskSaksbehandlingRepository.getKravgrunnlagAlderNårGammel())
            .plusDays(1).atStartOfDay();
    }


}
