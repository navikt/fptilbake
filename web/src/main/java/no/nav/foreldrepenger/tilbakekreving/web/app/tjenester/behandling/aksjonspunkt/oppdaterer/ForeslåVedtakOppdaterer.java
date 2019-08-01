package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ForeslåVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslåVedtakDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@DtoTilServiceAdapter(dto = ForeslåVedtakDto.class, adapter = AksjonspunktOppdaterer.class)
public class ForeslåVedtakOppdaterer implements AksjonspunktOppdaterer<ForeslåVedtakDto> {

    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;

    @Inject
    public ForeslåVedtakOppdaterer(ForeslåVedtakTjeneste foreslåVedtakTjeneste, TotrinnTjeneste totrinnTjeneste, AksjonspunktRepository aksjonspunktRepository) {
        this.foreslåVedtakTjeneste = foreslåVedtakTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    @Override
    public void oppdater(ForeslåVedtakDto dto, Behandling behandling) {
        Long behandlingId = behandling.getId();

        foreslåVedtakTjeneste.lagreFriteksterFraSaksbehandler(behandlingId, lagOppsummeringstekst(behandlingId, dto), lagPerioderMedTekst(behandlingId, dto));

        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(behandlingId);

        opprettEllerReåpne(behandling, AksjonspunktDefinisjon.FATTE_VEDTAK);

        totrinnTjeneste.settNyttTotrinnsgrunnlag(behandling);
    }

    private Optional<List<VedtaksbrevPeriode>> lagPerioderMedTekst(Long behandlingId, ForeslåVedtakDto dto) {
        if (saksbehandlerHarLagtTilFritekst(dto)) {
            List<VedtaksbrevPeriode> vedtaksbrevPerioderTilDB = new ArrayList<>();
            List<PeriodeMedTekstDto> perioderMedFritekstFraSaksbehandler = dto.getPerioderMedTekst();

            for (PeriodeMedTekstDto periodeMedTekstDto : perioderMedFritekstFraSaksbehandler) {
                if (periodeMedTekstDto.getFaktaAvsnitt() != null) {
                    vedtaksbrevPerioderTilDB.add(lagPeriodeMedFakta(behandlingId, periodeMedTekstDto));
                } else if (periodeMedTekstDto.getVilkårAvsnitt() != null) {
                    vedtaksbrevPerioderTilDB.add(lagPeriodeMedVilkår(behandlingId, periodeMedTekstDto));
                } else if (periodeMedTekstDto.getSærligeGrunnerAvsnitt() != null) {
                    vedtaksbrevPerioderTilDB.add(lagPeriodeMedSærligeGrunner(behandlingId, periodeMedTekstDto));
                }
            }
            return Optional.of(vedtaksbrevPerioderTilDB);
        } else {
            return Optional.empty();
        }
    }

    private boolean saksbehandlerHarLagtTilFritekst(ForeslåVedtakDto dto) {
        List<PeriodeMedTekstDto> perioder = dto.getPerioderMedTekst();
        for (PeriodeMedTekstDto periode : perioder) {
            if (periode.getFaktaAvsnitt() != null || periode.getVilkårAvsnitt() != null || periode.getSærligeGrunnerAvsnitt() != null) {
                return true;
            }
        }
        return false;
    }

    private VedtaksbrevPeriode lagPeriodeMedFakta(Long behandlingId, PeriodeMedTekstDto periodeMedTekstDto) {
        return new VedtaksbrevPeriode.Builder()
            .medPeriode(Periode.of(periodeMedTekstDto.getFom(), periodeMedTekstDto.getTom()))
            .medBehandlingId(behandlingId)
            .medFritekstType(FritekstType.FAKTA_AVSNITT)
            .medFritekst(periodeMedTekstDto.getFaktaAvsnitt())
            .build();
    }

    private VedtaksbrevPeriode lagPeriodeMedVilkår(Long behandlingId, PeriodeMedTekstDto periodeMedTekstDto) {
        return new VedtaksbrevPeriode.Builder()
            .medPeriode(Periode.of(periodeMedTekstDto.getFom(), periodeMedTekstDto.getTom()))
            .medBehandlingId(behandlingId)
            .medFritekstType(FritekstType.VILKAAR_AVSNITT)
            .medFritekst(periodeMedTekstDto.getVilkårAvsnitt())
            .build();
    }

    private VedtaksbrevPeriode lagPeriodeMedSærligeGrunner(Long behandlingId, PeriodeMedTekstDto periodeMedTekstDto) {
        return new VedtaksbrevPeriode.Builder()
            .medPeriode(Periode.of(periodeMedTekstDto.getFom(), periodeMedTekstDto.getTom()))
            .medBehandlingId(behandlingId)
            .medFritekstType(FritekstType.SAERLIGE_GRUNNER_AVSNITT)
            .medFritekst(periodeMedTekstDto.getSærligeGrunnerAvsnitt())
            .build();
    }

    private Optional<VedtaksbrevOppsummering> lagOppsummeringstekst(Long behandlingId, ForeslåVedtakDto dto) {
        if (dto.getOppsummeringstekst() != null) {
            return Optional.of(new VedtaksbrevOppsummering.Builder()
                .medOppsummeringFritekst(dto.getOppsummeringstekst())
                .medBehandlingId(behandlingId).build());
        } else {
            return Optional.empty();
        }
    }

    private void opprettEllerReåpne(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Optional<Aksjonspunkt> aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);
        if (aksjonspunkt.isPresent()) {
            aksjonspunktRepository.setReåpnet(aksjonspunkt.get());
        } else {
            aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, BehandlingStegType.FORESLÅ_VEDTAK);
        }
    }
}
