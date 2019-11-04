package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class VedtaksbrevFritekstTjeneste {

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BrevdataRepository brevdataRepository;

    VedtaksbrevFritekstTjeneste() {
        //for CDI proxy
    }

    @Inject
    public VedtaksbrevFritekstTjeneste(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository, VilkårsvurderingRepository vilkårsvurderingRepository, BrevdataRepository brevdataRepository) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.vilkårsvurderingRepository = vilkårsvurderingRepository;
        this.brevdataRepository = brevdataRepository;
    }

    public void lagreFriteksterFraSaksbehandler(Long behandlingId, VedtaksbrevOppsummering vedtaksbrevOppsummering, List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        validerAtPåkrevdeFriteksterErSatt(behandlingId, vedtaksbrevPerioder);

        brevdataRepository.slettOppsummering(behandlingId);
        brevdataRepository.slettPerioderMedFritekster(behandlingId);

        brevdataRepository.lagreVedtakPerioderOgTekster(vedtaksbrevPerioder);
        if (vedtaksbrevOppsummering != null) {
            brevdataRepository.lagreVedtaksbrevOppsummering(vedtaksbrevOppsummering);
        }
    }

    private void validerAtPåkrevdeFriteksterErSatt(Long behandlingId, List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .ifPresent(vilkårVurderingEntitet -> validerAtPåkrevdeFriteksterErSatt(vilkårVurderingEntitet, vedtaksbrevPerioder));

        FaktaFeilutbetaling faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId).orElseThrow();
        validerAtPåkrevdeFriteksterErSatt(faktaFeilutbetaling, vedtaksbrevPerioder);
    }

    private static void validerAtPåkrevdeFriteksterErSatt(VilkårVurderingEntitet vilkårVurdering, List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        List<Periode> perioderSomMåHaFritekst = vilkårVurdering.getPerioder().stream()
            .filter(p -> p.getAktsomhet() != null && p.getAktsomhet().getSærligGrunner().stream().map(VilkårVurderingSærligGrunnEntitet::getGrunn).anyMatch(SærligGrunn.ANNET::equals))
            .map(VilkårVurderingPeriodeEntitet::getPeriode)
            .collect(Collectors.toList());

        validerFritekstSatt(vedtaksbrevPerioder, perioderSomMåHaFritekst, FritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT);
    }

    private static void validerAtPåkrevdeFriteksterErSatt(FaktaFeilutbetaling fakta, List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        List<Periode> perioderSomMåHaFritekst = fakta.getFeilutbetaltPerioder().stream()
            .filter(p -> FellesUndertyper.ANNET_FRITEKST.equals(p.getHendelseUndertype()))
            .map(FaktaFeilutbetalingPeriode::getPeriode)
            .collect(Collectors.toList());

        validerFritekstSatt(vedtaksbrevPerioder, perioderSomMåHaFritekst, FritekstType.FAKTA_AVSNITT);
    }

    private static void validerFritekstSatt(List<VedtaksbrevPeriode> vedtaksbrevPerioder, List<Periode> perioderSomMåHaFritekst, FritekstType fritekstType) {
        for (Periode periode : perioderSomMåHaFritekst) {
            if (!finnesFritekst(vedtaksbrevPerioder, periode, fritekstType)) {
                throw FritekstFeil.FACTORY.manglerFritekst(periode, fritekstType.getKode()).toException();
            }
        }
    }

    private static boolean finnesFritekst(List<VedtaksbrevPeriode> perioder, Periode aktuellPeriode, FritekstType fritekstType) {
        return perioder.stream().anyMatch(p -> p.getPeriode().equals(aktuellPeriode)
            && fritekstType.equals(p.getFritekstType())
            && !p.getFritekst().isBlank());
    }

    interface FritekstFeil extends DeklarerteFeil {

        FritekstFeil FACTORY = FeilFactory.create(FritekstFeil.class);

        @TekniskFeil(feilkode = "FPT-022180", feilmelding = "Ugyldig input: Når ANNET er valgt er fritekst påkrevet. Mangler for periode %s og avsnitt %s", logLevel = WARN)
        Feil manglerFritekst(Periode periode, String fritekstType);

    }

}
