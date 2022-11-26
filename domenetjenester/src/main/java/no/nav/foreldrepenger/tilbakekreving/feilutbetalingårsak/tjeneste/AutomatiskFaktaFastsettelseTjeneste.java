package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;

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
        List<LogiskPeriode> logiskePerioder = kravgrunnlagTjeneste.utledLogiskPeriode(behandling.getId());
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
        hendelseTypeMedUndertypeDto = switch (fagsakYtelseType) {
            case FORELDREPENGER -> new HendelseTypeMedUndertypeDto(HendelseType.FP_ANNET_HENDELSE_TYPE, hendelseUnderType);
            case SVANGERSKAPSPENGER -> new HendelseTypeMedUndertypeDto(HendelseType.SVP_ANNET_TYPE, hendelseUnderType);
            case ENGANGSTØNAD -> new HendelseTypeMedUndertypeDto(HendelseType.ES_ANNET_TYPE, hendelseUnderType);
            case FRISINN -> new HendelseTypeMedUndertypeDto(HendelseType.FRISINN_ANNET_TYPE, hendelseUnderType);
            case OMSORGSPENGER -> new HendelseTypeMedUndertypeDto(HendelseType.OMP_ANNET_TYPE, hendelseUnderType);
            case PLEIEPENGER_SYKT_BARN -> new HendelseTypeMedUndertypeDto(HendelseType.PSB_ANNET_TYPE, hendelseUnderType);
            case PLEIEPENGER_NÆRSTÅENDE -> new HendelseTypeMedUndertypeDto(HendelseType.PPN_ANNET_TYPE, hendelseUnderType);
            case OPPLÆRINGSPENGER -> new HendelseTypeMedUndertypeDto(HendelseType.OLP_ANNET_TYPE, hendelseUnderType);
            default -> throw new IllegalArgumentException("Ikke-støttet fagsak ytelse type: " + fagsakYtelseType);
        };
        return hendelseTypeMedUndertypeDto;
    }
}
