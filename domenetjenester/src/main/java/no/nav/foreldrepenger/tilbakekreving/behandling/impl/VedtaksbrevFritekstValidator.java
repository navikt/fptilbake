package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering.maxFritekstLengde;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.exception.TekniskException;

@Dependent
public class VedtaksbrevFritekstValidator {

    public static Set<HendelseUnderType> UNDERTYPER_MED_PÅKREVD_FRITEKST = Set.of(
        HendelseUnderType.ANNET_FRITEKST,
        HendelseUnderType.ENDRING_GRUNNLAG,
        HendelseUnderType.SVP_ENDRING_GRUNNLAG
    );

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepository behandlingRepository;

    @Inject
    public VedtaksbrevFritekstValidator(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository,
                                        VilkårsvurderingRepository vilkårsvurderingRepository,
                                        BehandlingRepository behandlingRepository) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.vilkårsvurderingRepository = vilkårsvurderingRepository;
        this.behandlingRepository = behandlingRepository;
    }

    public void validerAtPåkrevdeFriteksterErSatt(Long behandlingId,
                                                  List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder,
                                                  VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering,
                                                  VedtaksbrevType brevType) {

        if (brevType == VedtaksbrevType.ORDINÆR) {
            var vurderingEntitet = vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId);
            vurderingEntitet.ifPresent(vurdering -> validerSærligeGrunnerAnnet(vurdering, vedtaksbrevFritekstPerioder));

            var faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId).orElseThrow();
            validerFritekstFakta(faktaFeilutbetaling, vedtaksbrevFritekstPerioder);
        }

        validerFritekstLengde(vedtaksbrevFritekstOppsummering, brevType);
        validerAtPåkrevdOppsummeringErSatt(behandlingId, vedtaksbrevFritekstOppsummering);
    }

    private void validerFritekstLengde(VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering, VedtaksbrevType brevType) {
        if (vedtaksbrevFritekstOppsummering != null
                && vedtaksbrevFritekstOppsummering.getOppsummeringFritekst() != null
                && vedtaksbrevFritekstOppsummering.getOppsummeringFritekst().length() >= maxFritekstLengde(brevType)) {
            throw fritekstOppsumeringForLang();
        }
    }

    private void validerAtPåkrevdOppsummeringErSatt(Long behandlingId, VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean erRevurderingEtterKlage = behandling.getBehandlingÅrsaker().stream()
                .anyMatch(ba -> ba.getBehandlingÅrsakType() == BehandlingÅrsakType.RE_KLAGE_KA || ba.getBehandlingÅrsakType() == BehandlingÅrsakType.RE_KLAGE_NFP);
        if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType()) && !erRevurderingEtterKlage &&
                (vedtaksbrevFritekstOppsummering == null || vedtaksbrevFritekstOppsummering.getOppsummeringFritekst() == null || vedtaksbrevFritekstOppsummering.getOppsummeringFritekst().isEmpty())) {
            throw manglerPåkrevetOppsumering();
        }
    }

    private void validerSærligeGrunnerAnnet(VilkårVurderingEntitet vilkårVurdering, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        validerSærligeGrunnerAnnetTimelineImpl(vilkårVurdering, vedtaksbrevFritekstPerioder);
    }

    private void validerFritekstFakta(FaktaFeilutbetaling fakta, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        validerFritekstFaktaTimelineImpl(fakta, vedtaksbrevFritekstPerioder);
    }

    static TekniskException manglerPåkrevetOppsumering() {
        return new TekniskException("FPT-063091", "Ugyldig input: Når det er revurdering, så er oppsummering fritekst påkrevet.");
    }

    public static TekniskException fritekstOppsumeringForLang() {
        return new TekniskException("FPT-063092", "Ugyldig input: Oppsummeringstekst er for lang.");
    }


    private static void validerFritekstFaktaTimelineImpl(FaktaFeilutbetaling fakta, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        LocalDateTimeline<HendelseUnderType> perioderSomSkalHaFritekst = new LocalDateTimeline<>(
                fakta.getFeilutbetaltPerioder().stream()
                        .filter(f -> UNDERTYPER_MED_PÅKREVD_FRITEKST.contains(f.getHendelseUndertype()))
                        .map(f -> new LocalDateSegment<HendelseUnderType>(f.getPeriode().getFom(), f.getPeriode().getTom(), f.getHendelseUndertype()))
                        .toList()
        );
        LocalDateTimeline<?> perioderSomHarFritekst = perioderSomHarFritekst(vedtaksbrevFritekstPerioder, VedtaksbrevFritekstType.FAKTA_AVSNITT);
        LocalDateTimeline<?> perioderSomManglerFritekst = perioderSomSkalHaFritekst.disjoint(perioderSomHarFritekst);
        if (!perioderSomManglerFritekst.isEmpty()) {
            Set<LocalDateInterval> periodene = perioderSomManglerFritekst.getLocalDateIntervals();
            throw new IllegalArgumentException("Noen fakta-valg medfører påkrevet fritekst. Det mangler fritekst for " + String.join(", ", periodene.stream().map(p -> Periode.of(p.getFomDato(), p.getTomDato())).map(Periode::toString).toList()) + " i fakta-avsnittet");
        }
    }

    private static void validerSærligeGrunnerAnnetTimelineImpl(VilkårVurderingEntitet vilkårVurdering, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        LocalDateTimeline<?> perioderSomSkalHaFritekst = new LocalDateTimeline<>(vilkårVurdering.getPerioder().stream()
                .filter(p -> p.getAktsomhet() != null && p.getAktsomhet().getSærligGrunner().stream().map(VilkårVurderingSærligGrunnEntitet::getGrunn).anyMatch(SærligGrunn.ANNET::equals))
                .map(VilkårVurderingPeriodeEntitet::getPeriode)
                .map(VedtaksbrevFritekstValidator::toSegment)
                .toList());
        LocalDateTimeline<?> perioderSomHarFritekst = perioderSomHarFritekst(vedtaksbrevFritekstPerioder, VedtaksbrevFritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT);
        LocalDateTimeline<?> perioderSomManglerFritekst = perioderSomSkalHaFritekst.disjoint(perioderSomHarFritekst);
        if (!perioderSomManglerFritekst.isEmpty()) {
            Set<LocalDateInterval> periodene = perioderSomManglerFritekst.getLocalDateIntervals();
            throw new IllegalArgumentException("Særlige grunner - Annet medfører påkrevet fritekst. Det mangler fritekst for " + String.join(", ", periodene.stream().map(p -> Periode.of(p.getFomDato(), p.getTomDato())).map(Periode::toString).toList()) + " i avsnittet om særlige grunner");
        }
    }

    private static LocalDateTimeline<?> perioderSomHarFritekst(List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder, VedtaksbrevFritekstType fritekstType) {
        return new LocalDateTimeline<>(vedtaksbrevFritekstPerioder.stream()
                .filter(f -> f.getFritekstType() == fritekstType && f.getFritekst() != null && !f.getFritekst().isBlank())
                .map(VedtaksbrevFritekstPeriode::getPeriode)
                .map(VedtaksbrevFritekstValidator::toSegment)
                .toList());
    }

    private static LocalDateSegment<Void> toSegment(Periode p) {
        return new LocalDateSegment<>(p.getFom(), p.getTom(), null);
    }

}
