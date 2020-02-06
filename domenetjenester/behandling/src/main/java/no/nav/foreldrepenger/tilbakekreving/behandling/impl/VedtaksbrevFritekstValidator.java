package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class VedtaksbrevFritekstValidator {

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepository behandlingRepository;
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;

    public VedtaksbrevFritekstValidator() {
    }

    @Inject
    public VedtaksbrevFritekstValidator(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository,
                                        VilkårsvurderingRepository vilkårsvurderingRepository,
                                        BehandlingRepository behandlingRepository,
                                        VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.vilkårsvurderingRepository = vilkårsvurderingRepository;
        this.behandlingRepository = behandlingRepository;
        this.vedtaksbrevFritekstRepository = vedtaksbrevFritekstRepository;
    }

    public void validerAtPåkrevdeFriteksterErSatt(Long behandlingId, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .ifPresent(vilkårVurderingEntitet -> validerSærligeGrunnerAnnet(vilkårVurderingEntitet, vedtaksbrevFritekstPerioder));

        FaktaFeilutbetaling faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId).orElseThrow();
        validerFritekstFakta(faktaFeilutbetaling, vedtaksbrevFritekstPerioder);

        validerAtPåkrevdOppsummeringErSatt(behandlingId);
    }

    private void validerAtPåkrevdOppsummeringErSatt(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType())) {
            Optional<VedtaksbrevFritekstOppsummering> vedtaksbrevFritekstOppsummering = vedtaksbrevFritekstRepository.hentVedtaksbrevOppsummering(behandlingId);
            if (vedtaksbrevFritekstOppsummering.isEmpty()) {
                throw FritekstFeil.FACTORY.manglerPåkrevetOppsumering().toException();
            }
        }
    }

    private static void validerSærligeGrunnerAnnet(VilkårVurderingEntitet vilkårVurdering, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        vilkårVurdering.getPerioder().stream()
            .filter(p -> p.getAktsomhet() != null && p.getAktsomhet().getSærligGrunner().stream().map(VilkårVurderingSærligGrunnEntitet::getGrunn).anyMatch(SærligGrunn.ANNET::equals))
            .forEach(p -> validerFritekstSatt(vedtaksbrevFritekstPerioder, p.getPeriode(), "særlige grunner - annet", VedtaksbrevFritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT));
    }

    private static void validerFritekstFakta(FaktaFeilutbetaling fakta, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        for (HendelseUnderType hendelseUnderType : VedtaksbrevFritekstKonfigurasjon.UNDERTYPER_MED_PÅKREVD_FRITEKST) {
            validerFritekstFakta(fakta, hendelseUnderType, vedtaksbrevFritekstPerioder);
        }
    }

    private static void validerFritekstFakta(FaktaFeilutbetaling fakta, HendelseUnderType hendelseUnderType, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        fakta.getFeilutbetaltPerioder().stream()
            .filter(p -> hendelseUnderType.equals(p.getHendelseUndertype()))
            .forEach(p -> validerFritekstSatt(vedtaksbrevFritekstPerioder, p.getPeriode(), hendelseUnderType.getKode(), VedtaksbrevFritekstType.FAKTA_AVSNITT));
    }

    private static void validerFritekstSatt(List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder, Periode periodeSomMåHaFritekst, String hva, VedtaksbrevFritekstType fritekstType) {
        List<Periode> perioder = finnFritekstPerioder(vedtaksbrevFritekstPerioder, periodeSomMåHaFritekst, fritekstType);
        if (perioder.isEmpty()) {
            throw FritekstFeil.FACTORY.manglerFritekst(hva, periodeSomMåHaFritekst, fritekstType.getKode()).toException();
        }
        Periode førstePeriode = perioder.get(0);
        if (!periodeSomMåHaFritekst.getFom().equals(førstePeriode.getFom())) {
            throw FritekstFeil.FACTORY.manglerFritekst(hva, Periode.of(periodeSomMåHaFritekst.getFom(), førstePeriode.getFom().minusDays(1)), fritekstType.getKode()).toException();
        }
        for (int i = 1; i < perioder.size(); i++) {
            LocalDate forrigeSlutt = perioder.get(i - 1).getTom();
            LocalDate start = perioder.get(i).getFom();
            if (!forrigeSlutt.plusDays(1).equals(start)) {
                throw FritekstFeil.FACTORY.manglerFritekst(hva, Periode.of(forrigeSlutt.plusDays(1), start.minusDays(1)), fritekstType.getKode()).toException();
            }
        }
        Periode sistePeriode = perioder.get(perioder.size() - 1);
        if (!periodeSomMåHaFritekst.getTom().equals(sistePeriode.getTom())) {
            throw FritekstFeil.FACTORY.manglerFritekst(hva, Periode.of(sistePeriode.getTom().plusDays(1), periodeSomMåHaFritekst.getTom()), fritekstType.getKode()).toException();
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

    interface FritekstFeil extends DeklarerteFeil {

        FritekstFeil FACTORY = FeilFactory.create(FritekstFeil.class);

        @TekniskFeil(feilkode = "FPT-022180", feilmelding = "Ugyldig input: Når '%s' er valgt er fritekst påkrevet. Mangler for periode %s og avsnitt %s", logLevel = WARN)
        Feil manglerFritekst(String hendelseUnderType, Periode periode, String fritekstType);

        @TekniskFeil(feilkode = "FPT-063091", feilmelding = "Ugyldig input: Når det er revurdering, så er oppsummering fritekst påkrevet.", logLevel = WARN)
        Feil manglerPåkrevetOppsumering();
    }
}
