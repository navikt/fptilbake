package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;

@ApplicationScoped
public class AutomatiskFaktaFastsettelseTjeneste {

    private AvklartFaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;

    AutomatiskFaktaFastsettelseTjeneste() {
        // for CDI
    }

    @Inject
    public AutomatiskFaktaFastsettelseTjeneste(AvklartFaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste, KravgrunnlagTjeneste kravgrunnlagTjeneste) {
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
    }

    public void fastsettFaktaAutomatisk(Behandling behandling, String begrunnelse) {
        List<KravgrunnlagPeriode432> feilutbetaltePerioder = kravgrunnlagTjeneste.finnKravgrunnlagPerioderMedFeilutbetaltPosteringer(behandling.getId());
        List<UtbetaltPeriode> logiskePerioder = kravgrunnlagTjeneste.utledLogiskPeriode(feilutbetaltePerioder);
        HendelseTypeMedUndertypeDto hendelseTypeMedUndertypeDto = setHendelseTypeOgHendelseUndertype(behandling);

        List<FaktaFeilutbetalingDto> faktaFeilutbetalinger = logiskePerioder.stream()
            .map(periode -> new FaktaFeilutbetalingDto(periode.getFom(), periode.getTom(), hendelseTypeMedUndertypeDto))
            .collect(Collectors.toList());
        faktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(behandling, faktaFeilutbetalinger, begrunnelse);
    }

    private HendelseTypeMedUndertypeDto setHendelseTypeOgHendelseUndertype(Behandling behandling) {
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        HendelseTypeMedUndertypeDto hendelseTypeMedUndertypeDto;
        HendelseUnderType hendelseUnderType = HendelseUnderType.ANNET_FRITEKST;
        if (FagsakYtelseType.FORELDREPENGER.equals(fagsakYtelseType)) {
            hendelseTypeMedUndertypeDto = new HendelseTypeMedUndertypeDto(HendelseType.FP_ANNET_HENDELSE_TYPE, hendelseUnderType);
        } else if (FagsakYtelseType.SVANGERSKAPSPENGER.equals(fagsakYtelseType)) {
            hendelseTypeMedUndertypeDto = new HendelseTypeMedUndertypeDto(HendelseType.SVP_ANNET_TYPE, hendelseUnderType);
        } else if (FagsakYtelseType.ENGANGSTØNAD.equals(fagsakYtelseType)) {
            hendelseTypeMedUndertypeDto = new HendelseTypeMedUndertypeDto(HendelseType.ES_ANNET_TYPE, hendelseUnderType);
        } else {
            hendelseTypeMedUndertypeDto = new HendelseTypeMedUndertypeDto(HendelseType.FRISINN_ANNET_TYPE, hendelseUnderType);
        }
        return hendelseTypeMedUndertypeDto;
    }
}
