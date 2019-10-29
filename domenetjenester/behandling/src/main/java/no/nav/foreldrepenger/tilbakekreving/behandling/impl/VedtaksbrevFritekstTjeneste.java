package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;

@ApplicationScoped
public class VedtaksbrevFritekstTjeneste {
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BrevdataRepository brevdataRepository;

    VedtaksbrevFritekstTjeneste() {
        //for CDI proxy
    }

    @Inject
    public VedtaksbrevFritekstTjeneste(VilkårsvurderingRepository vilkårsvurderingRepository, BrevdataRepository brevdataRepository) {
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
    }

    private static void validerAtPåkrevdeFriteksterErSatt(VilkårVurderingEntitet vilkårVurdering, List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        for (VilkårVurderingPeriodeEntitet vilkårPeriode : vilkårVurdering.getPerioder()) {
            validerAtPåkrevdeFriteksterErSatt(vilkårPeriode, vedtaksbrevPerioder);
        }
    }

    private static void validerAtPåkrevdeFriteksterErSatt(VilkårVurderingPeriodeEntitet vilkårPeriode, List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        boolean særligeGrunnerAnnetErValgt = vilkårPeriode.getAktsomhet() != null && vilkårPeriode.getAktsomhet().getSærligGrunner().stream()
            .map(VilkårVurderingSærligGrunnEntitet::getGrunn)
            .anyMatch(SærligGrunn.ANNET::equals);
        if (særligeGrunnerAnnetErValgt && !finnesFritekstForAnnenSærligGrunn(vedtaksbrevPerioder, vilkårPeriode.getPeriode())) {
            throw FritekstFeil.FACTORY.manglerFritekstForSærligGrunn(vilkårPeriode.getPeriode()).toException();
        }
    }

    private static boolean finnesFritekstForAnnenSærligGrunn(List<VedtaksbrevPeriode> perioder, Periode aktuellPeriode) {
        return perioder.stream().anyMatch(p -> p.getPeriode().equals(aktuellPeriode)
            && FritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT.equals(p.getFritekstType())
            && !p.getFritekst().isBlank());
    }

    interface FritekstFeil extends DeklarerteFeil {

        FritekstFeil FACTORY = FeilFactory.create(FritekstFeil.class);

        @FunksjonellFeil(feilkode = "FPT-022180", feilmelding = "Ugyldig input: Når SærligGrunn.ANNET er valgt er fritekst for denne påkrevet, men finnes ikke for periode: %s", løsningsforslag = "Skriv inn fritekst", logLevel = WARN)
        Feil manglerFritekstForSærligGrunn(Periode periode);
    }

}
