package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class HalvtRettsgebyrTjeneste {

    private KravgrunnlagRepository grunnlagRepository;
    private VarselRepository varselRepository;

    HalvtRettsgebyrTjeneste() {
        //for CDI proxy
    }

    @Inject
    public HalvtRettsgebyrTjeneste(KravgrunnlagRepository grunnlagRepository, VarselRepository varselRepository) {
        this.grunnlagRepository = grunnlagRepository;
        this.varselRepository = varselRepository;
    }

    public boolean samletUnderHalvtRettsgebyrKanVentePåAutomatiskBehandling(Long behandlingId) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlagOpt(behandlingId).orElse(null);
        return samletUnderHalvtRettsgebyrKanVentePåAutomatiskBehandling(behandlingId, kravgrunnlag);
    }

    public boolean samletUnderHalvtRettsgebyrKanVentePåAutomatiskBehandling(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        return kravgrunnlag != null && varselRepository.finnVarsel(behandlingId).isEmpty() &&
            KravgrunnlagBeregningTjeneste.samletFeilutbetaltUnderHalvtRettsgebyr(kravgrunnlag) &&
            !harFeilutbetaltUnderHalvtRettsgebyrLiggetLengeNok(kravgrunnlag);
    }

    private static boolean harFeilutbetaltUnderHalvtRettsgebyrLiggetLengeNok(Kravgrunnlag431 kravgrunnlag) {
        return ventefristForTilfelleUnderHalvtRettsgebyr(kravgrunnlag).isBefore(LocalDateTime.now());
    }

    public LocalDateTime ventefristForTilfelleUnderHalvtRettsgebyr(Long behandlingId) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        return ventefristForTilfelleUnderHalvtRettsgebyr(kravgrunnlag);
    }

    public static LocalDateTime ventefristForTilfelleUnderHalvtRettsgebyr(Kravgrunnlag431 kravgrunnlag) {
        return kravgrunnlag.getKontrollFeltAsLocalDate()
            .plus(AutomatiskSaksbehandlingRepository.getKravgrunnlagAlderNårGammel())
            .plusDays(1).atStartOfDay();
    }


}
