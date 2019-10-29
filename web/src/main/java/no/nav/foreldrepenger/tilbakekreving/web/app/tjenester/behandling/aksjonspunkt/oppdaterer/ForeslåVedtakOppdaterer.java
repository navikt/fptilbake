package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    private List<VedtaksbrevPeriode> lagPerioderMedTekst(Long behandlingId, ForeslåVedtakDto dto) {
        List<VedtaksbrevPeriode> fritekstPerioder = new ArrayList<>();
        for (PeriodeMedTekstDto periodeDto : dto.getPerioderMedTekst()) {
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getFaktaAvsnitt, FritekstType.FAKTA_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getVilkårAvsnitt, FritekstType.VILKAAR_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getSærligeGrunnerAvsnitt, FritekstType.SAERLIGE_GRUNNER_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getSærligeGrunnerAnnetAvsnitt, FritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT).ifPresent(fritekstPerioder::add);
        }
        return fritekstPerioder;
    }

    private Optional<VedtaksbrevPeriode> lagFritekstPeriode(Long behandlingId, PeriodeMedTekstDto dto, Function<PeriodeMedTekstDto, String> stringSupplier, FritekstType fritekstType) {
        String fritekst = stringSupplier.apply(dto);
        if (fritekst == null) {
            return Optional.empty();
        }
        return Optional.of(lagFritekstPeriode(behandlingId, fritekstType, dto.getPeriode(), fritekst));
    }

    private VedtaksbrevPeriode lagFritekstPeriode(Long behandlingId, FritekstType fritekstType, Periode periode, String fritekst) {
        return new VedtaksbrevPeriode.Builder()
            .medPeriode(periode)
            .medBehandlingId(behandlingId)
            .medFritekstType(fritekstType)
            .medFritekst(fritekst)
            .build();
    }

    private VedtaksbrevOppsummering lagOppsummeringstekst(Long behandlingId, ForeslåVedtakDto dto) {
        if (dto.getOppsummeringstekst() != null) {
            return new VedtaksbrevOppsummering.Builder()
                .medOppsummeringFritekst(dto.getOppsummeringstekst())
                .medBehandlingId(behandlingId).build();
        } else {
            return null;
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
