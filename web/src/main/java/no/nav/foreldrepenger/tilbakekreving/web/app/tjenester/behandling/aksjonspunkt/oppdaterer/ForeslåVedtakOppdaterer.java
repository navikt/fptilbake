package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.cxf.common.util.CollectionUtils;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ForeslåVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
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
    private VedtaksbrevFritekstTjeneste vedtaksbrevFritekstTjeneste;

    @Inject
    public ForeslåVedtakOppdaterer(ForeslåVedtakTjeneste foreslåVedtakTjeneste, TotrinnTjeneste totrinnTjeneste, AksjonspunktRepository aksjonspunktRepository, VedtaksbrevFritekstTjeneste vedtaksbrevFritekstTjeneste) {
        this.foreslåVedtakTjeneste = foreslåVedtakTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.vedtaksbrevFritekstTjeneste = vedtaksbrevFritekstTjeneste;
    }

    @Override
    public void oppdater(ForeslåVedtakDto dto, Behandling behandling) {
        Long behandlingId = behandling.getId();
        BrevType brevType = BrevType.VEDTAK_BREV;
        if(behandling.erBehandlingRevurderingOgHarÅrsakFeilutbetalingBortfalt()){
            brevType = BrevType.FRITEKST_VEDTAK_BREV;
        }
        vedtaksbrevFritekstTjeneste.lagreFriteksterFraSaksbehandler(behandlingId, lagOppsummeringstekst(behandlingId, dto, brevType), lagPerioderMedTekst(behandlingId, dto.getPerioderMedTekst()));
        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(behandlingId);

        opprettEllerReåpne(behandling, AksjonspunktDefinisjon.FATTE_VEDTAK);
        totrinnTjeneste.settNyttTotrinnsgrunnlag(behandling);
    }

    private List<VedtaksbrevFritekstPeriode> lagPerioderMedTekst(Long behandlingId, List<PeriodeMedTekstDto> perioderMedTekst) {
        List<VedtaksbrevFritekstPeriode> fritekstPerioder = new ArrayList<>();
        if (!CollectionUtils.isEmpty(perioderMedTekst)) {
            for (PeriodeMedTekstDto periodeDto : perioderMedTekst) {
                lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getFaktaAvsnitt, VedtaksbrevFritekstType.FAKTA_AVSNITT).ifPresent(fritekstPerioder::add);
                lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getVilkårAvsnitt, VedtaksbrevFritekstType.VILKAAR_AVSNITT).ifPresent(fritekstPerioder::add);
                lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getSærligeGrunnerAvsnitt, VedtaksbrevFritekstType.SAERLIGE_GRUNNER_AVSNITT).ifPresent(fritekstPerioder::add);
                lagFritekstPeriode(behandlingId, periodeDto, PeriodeMedTekstDto::getSærligeGrunnerAnnetAvsnitt, VedtaksbrevFritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT).ifPresent(fritekstPerioder::add);
            }
        }
        return fritekstPerioder;
    }

    private Optional<VedtaksbrevFritekstPeriode> lagFritekstPeriode(Long behandlingId, PeriodeMedTekstDto dto, Function<PeriodeMedTekstDto, String> stringSupplier, VedtaksbrevFritekstType fritekstType) {
        String fritekst = stringSupplier.apply(dto);
        if (fritekst == null) {
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

    private VedtaksbrevFritekstOppsummering lagOppsummeringstekst(Long behandlingId, ForeslåVedtakDto dto, BrevType brevType) {
        if (dto.getOppsummeringstekst() != null) {
            return new VedtaksbrevFritekstOppsummering.Builder()
                .medOppsummeringFritekst(dto.getOppsummeringstekst())
                .medBrevType(brevType.getKode())
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
