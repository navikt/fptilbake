package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    public boolean lavFeilutbetalingKanVentePåAutomatiskBehandling(Long behandlingId) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlagOpt(behandlingId).orElse(null);
        return lavFeilutbetalingKanVentePåAutomatiskBehandling(behandlingId, kravgrunnlag);
    }

    public boolean lavFeilutbetalingKanVentePåAutomatiskBehandling(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        return kravgrunnlag != null && varselRepository.finnVarsel(behandlingId).isEmpty() &&
            KravgrunnlagBeregningTjeneste.samletFeilutbetaltKanAutomatiskBehandles(kravgrunnlag) &&
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
