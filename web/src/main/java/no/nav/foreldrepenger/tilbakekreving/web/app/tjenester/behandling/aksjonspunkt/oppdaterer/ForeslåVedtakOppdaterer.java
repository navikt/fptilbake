package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ForeslåVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslåVedtakDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = ForeslåVedtakDto.class, adapter = AksjonspunktOppdaterer.class)
public class ForeslåVedtakOppdaterer implements AksjonspunktOppdaterer<ForeslåVedtakDto> {

    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private VedtaksbrevFritekstTjeneste vedtaksbrevFritekstTjeneste;

    @Inject
    public ForeslåVedtakOppdaterer(ForeslåVedtakTjeneste foreslåVedtakTjeneste, TotrinnTjeneste totrinnTjeneste, BehandlingskontrollTjeneste behandlingskontrollTjeneste, VedtaksbrevFritekstTjeneste vedtaksbrevFritekstTjeneste) {
        this.foreslåVedtakTjeneste = foreslåVedtakTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.vedtaksbrevFritekstTjeneste = vedtaksbrevFritekstTjeneste;
    }

    @Override
    public void oppdater(ForeslåVedtakDto dto, Behandling behandling) {
        Long behandlingId = behandling.getId();
        VedtaksbrevType brevType = behandling.utledVedtaksbrevType();
        vedtaksbrevFritekstTjeneste.lagreFriteksterFraSaksbehandler(behandlingId, lagOppsummeringstekst(behandlingId, dto), lagPerioderMedTekst(behandlingId, dto.getPerioderMedTekst()), brevType);
        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(behandlingId);

        opprettEllerReåpne(behandling, AksjonspunktDefinisjon.FATTE_VEDTAK);
        totrinnTjeneste.settNyttTotrinnsgrunnlag(behandling);
    }

    private List<VedtaksbrevFritekstPeriode> lagPerioderMedTekst(Long behandlingId, List<PeriodeMedTekstDto> perioderMedTekst) {
        List<VedtaksbrevFritekstPeriode> fritekstPerioder = new ArrayList<>();
        for (PeriodeMedTekstDto periodeDto : perioderMedTekst) {
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getFaktaAvsnitt, VedtaksbrevFritekstType.FAKTA_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getForeldelseAvsnitt, VedtaksbrevFritekstType.FORELDELSE_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getVilkårAvsnitt, VedtaksbrevFritekstType.VILKAAR_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getSærligeGrunnerAvsnitt, VedtaksbrevFritekstType.SAERLIGE_GRUNNER_AVSNITT).ifPresent(fritekstPerioder::add);
            lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getSærligeGrunnerAnnetAvsnitt, VedtaksbrevFritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT).ifPresent(fritekstPerioder::add);
        }
        return fritekstPerioder;
    }

    private Optional<VedtaksbrevFritekstPeriode> lagFritekstPeriode(Long behandlingId, PeriodeMedTekstDto dto, Function<PeriodeMedTekstDto, String> stringSupplier, VedtaksbrevFritekstType fritekstType) {
        String fritekst = stringSupplier.apply(dto);
        if (fritekst == null || fritekst.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(lagFritekstPeriode(behandlingId, fritekstType, dto.getPeriode(), fritekst));
    }

    private VedtaksbrevFritekstPeriode lagFritekstPeriode(Long behandlingId, VedtaksbrevFritekstType fritekstType, Periode periode, String fritekst) {
        return new VedtaksbrevFritekstPeriode.Builder()
                .medPeriode(periode)
                .medBehandlingId(behandlingId)
                .medFritekstType(fritekstType)
                .medFritekst(fritekst)
                .build();
    }

    private VedtaksbrevFritekstOppsummering lagOppsummeringstekst(Long behandlingId, ForeslåVedtakDto dto) {
        if (dto.getOppsummeringstekst() != null) {
            return new VedtaksbrevFritekstOppsummering.Builder()
                    .medOppsummeringFritekst(dto.getOppsummeringstekst())
                    .medBehandlingId(behandlingId).build();
        } else {
            return null;
        }
    }

    private void opprettEllerReåpne(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        Optional<Aksjonspunkt> aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);
        if (aksjonspunkt.isPresent()) {
            behandlingskontrollTjeneste.lagreAksjonspunkterReåpnet(kontekst, List.of(aksjonspunkt.get()), true, false);
        } else {
            behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.FORESLÅ_VEDTAK, List.of(aksjonspunktDefinisjon));
        }
    }
}
