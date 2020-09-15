package no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktafeilutbetaling;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling.AutomatiskSaksbehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.AvklartFaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;

@BehandlingStegRef(kode = "FAKTFEILUTSTEG")
@BehandlingTypeRef
@ApplicationScoped
@Transactional
public class FaktaFeilutbetalingStegImpl implements FaktaFeilutbetalingSteg {

    private static final Logger logger = LoggerFactory.getLogger(FaktaFeilutbetalingStegImpl.class);

    private BehandlingRepository behandlingRepository;
    private AvklartFaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;

    FaktaFeilutbetalingStegImpl() {
        // for CDI
    }

    @Inject
    public FaktaFeilutbetalingStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                       AvklartFaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                       KravgrunnlagTjeneste kravgrunnlagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.isAutomatiskSaksbehandlet()) {
            utførStegAutomatisk(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        return BehandleStegResultat.utførtMedAksjonspunkter(
            Collections.singletonList(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
    }

    protected void utførStegAutomatisk(Behandling behandling) {
        long behandlingId = behandling.getId();
        logger.info("utfører fakta steg automatisk for behandling={}", behandlingId);
        List<KravgrunnlagPeriode432> feilutbetaltePerioder = kravgrunnlagTjeneste.finnKravgrunnlagPerioderMedFeilutbetaltPosteringer(behandlingId);
        List<UtbetaltPeriode> logiskePerioder = kravgrunnlagTjeneste.utledLogiskPeriode(feilutbetaltePerioder);
        HendelseTypeMedUndertypeDto hendelseTypeMedUndertypeDto = setHendelseTypeOgHendelseUndertype(behandling);

        List<FaktaFeilutbetalingDto> faktaFeilutbetalinger = logiskePerioder.stream()
            .map(periode -> new FaktaFeilutbetalingDto(periode.tilPeriode(), hendelseTypeMedUndertypeDto))
            .collect(Collectors.toList());
        faktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(behandling, faktaFeilutbetalinger,
            AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE);
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
