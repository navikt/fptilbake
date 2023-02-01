package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering.maxFritekstLengde;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

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
        vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
                .ifPresent(vilkårVurderingEntitet -> validerSærligeGrunnerAnnet(vilkårVurderingEntitet, vedtaksbrevFritekstPerioder));

        FaktaFeilutbetaling faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId).orElseThrow();
        if (brevType == VedtaksbrevType.ORDINÆR) {
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

    private static void validerFritekstFakta(FaktaFeilutbetaling fakta, HendelseUnderType hendelseUnderType, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        fakta.getFeilutbetaltPerioder().stream()
                .filter(p -> hendelseUnderType.equals(p.getHendelseUndertype()))
                .forEach(p -> validerFritekstSatt(vedtaksbrevFritekstPerioder, p.getPeriode(), hendelseUnderType.getKode(), VedtaksbrevFritekstType.FAKTA_AVSNITT));
    }

    private static void validerFritekstSatt(List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder, Periode periodeSomMåHaFritekst, String hva, VedtaksbrevFritekstType fritekstType) {
        List<Periode> perioder = finnFritekstPerioder(vedtaksbrevFritekstPerioder, periodeSomMåHaFritekst, fritekstType);
        if (perioder.isEmpty()) {
            throw manglerFritekst(hva, periodeSomMåHaFritekst, fritekstType.getKode());
        }
        Periode førstePeriode = perioder.get(0);
        if (!periodeSomMåHaFritekst.getFom().equals(førstePeriode.getFom())) {
            throw manglerFritekst(hva, Periode.of(periodeSomMåHaFritekst.getFom(), førstePeriode.getFom().minusDays(1)), fritekstType.getKode());
        }
        for (int i = 1; i < perioder.size(); i++) {
            LocalDate forrigeSlutt = perioder.get(i - 1).getTom();
            LocalDate start = perioder.get(i).getFom();
            if (!forrigeSlutt.plusDays(1).equals(start)) {
                throw manglerFritekst(hva, Periode.of(forrigeSlutt.plusDays(1), start.minusDays(1)), fritekstType.getKode());
            }
        }
        Periode sistePeriode = perioder.get(perioder.size() - 1);
        if (!periodeSomMåHaFritekst.getTom().equals(sistePeriode.getTom())) {
            throw manglerFritekst(hva, Periode.of(sistePeriode.getTom().plusDays(1), periodeSomMåHaFritekst.getTom()), fritekstType.getKode());
        }
    }

    private static List<Periode> finnFritekstPerioder(List<VedtaksbrevFritekstPeriode> perioder, Periode periodeSomMåHaFritekst, VedtaksbrevFritekstType fritekstType) {
        return perioder.stream()
                .filter(p -> fritekstType.equals(p.getFritekstType()))
                .filter(p -> !p.getFritekst().isBlank())
                .map(VedtaksbrevFritekstPeriode::getPeriode)
                .filter(periodeSomMåHaFritekst::omslutter)
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
    }


    static TekniskException manglerFritekst(String hendelseUnderType, Periode periode, String fritekstType) {
        return new TekniskException("FPT-022180", String.format("Ugyldig input: Når '%s' er valgt er fritekst påkrevet. Mangler for periode %s og avsnitt %s", hendelseUnderType, periode, fritekstType));
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
                        .filter(f -> VedtaksbrevFritekstKonfigurasjon.UNDERTYPER_MED_PÅKREVD_FRITEKST.contains(f.getHendelseUndertype()))
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
