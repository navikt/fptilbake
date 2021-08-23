package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class VedtaksbrevPeriodeSammenslåer {

    private final List<VilkårVurderingPeriodeEntitet> vilkårsvurderinger;
    private final VurdertForeldelse foreldelse;

    public VedtaksbrevPeriodeSammenslåer(List<VilkårVurderingPeriodeEntitet> vilkårsvurderinger, VurdertForeldelse foreldelse) {
        this.vilkårsvurderinger = vilkårsvurderinger;
        this.foreldelse = foreldelse;
    }

    public List<Periode> utledPerioder(List<BeregningResultatPeriode> beregningResultatPerioder) {
        List<Periode> resultat = new ArrayList<>();

        BeregningResultatPeriode mal = beregningResultatPerioder.get(0);
        Periode periode = mal.getPeriode();
        for (BeregningResultatPeriode beregningResultatPeriode : beregningResultatPerioder) {
            if (harLikVurdering(mal.getPeriode(), beregningResultatPeriode.getPeriode())) {
                periode = Periode.omsluttende(periode, beregningResultatPeriode.getPeriode());
            } else {
                resultat.add(periode);
                mal = beregningResultatPeriode;
                periode = beregningResultatPeriode.getPeriode();
            }
        }
        resultat.add(periode);
        return resultat;
    }

    private boolean harLikVurdering(Periode periode1, Periode periode2) {
        return erBeggeForeldetPåSammeMåte(periode1, periode2)
            || likForeldelse(periode1, periode2) && likVilkårsvurdering(periode1, periode2);
    }

    private boolean erBeggeForeldetPåSammeMåte(Periode periode1, Periode periode2) {
        ForeldelseVurderingType f1 = finnForeldelseResultat(periode1);
        ForeldelseVurderingType f2 = finnForeldelseResultat(periode2);
        return f1 == f2 && (f1 == ForeldelseVurderingType.FORELDET || f1 == ForeldelseVurderingType.TILLEGGSFRIST);
    }

    private boolean likVilkårsvurdering(Periode periode1, Periode periode2) {
        VilkårVurderingPeriodeEntitet v1 = finnVilkårsvurdering(periode1);
        VilkårVurderingPeriodeEntitet v2 = finnVilkårsvurdering(periode2);
        return like(v1, v2);
    }

    private boolean like(VilkårVurderingPeriodeEntitet v1, VilkårVurderingPeriodeEntitet v2) {
        return v1.getVilkårResultat() == v2.getVilkårResultat()
            && v1.getNavOppfulgt() == v2.getNavOppfulgt()
            && like(v1.getAktsomhet(), v2.getAktsomhet())
            && like(v1.getGodTro(), v2.getGodTro());
    }

    private boolean like(VilkårVurderingGodTroEntitet gt1, VilkårVurderingGodTroEntitet gt2) {
        //trenger bare sjekke om de er satt, da innholdet ikke styrer ulike formuleringer i vedtaksbrevet
        return (gt1 == null) == (gt2 == null);
    }

    private boolean like(VilkårVurderingAktsomhetEntitet a1, VilkårVurderingAktsomhetEntitet a2) {
        if (a1 == null && a2 == null) {
            return true;
        }
        return a1 != null
            && a2 != null
            && a1.getAktsomhet() == a2.getAktsomhet()
            && like(a1.getSærligGrunner(), a2.getSærligGrunner())
            && Objects.equals(a1.getIleggRenter(), a2.getIleggRenter());
    }

    private boolean like(List<VilkårVurderingSærligGrunnEntitet> sg1, List<VilkårVurderingSærligGrunnEntitet> sg2) {
        Set<SærligGrunn> særligeGrunner1 = sg1.stream().map(VilkårVurderingSærligGrunnEntitet::getGrunn).collect(Collectors.toSet());
        Set<SærligGrunn> særligeGrunner2 = sg2.stream().map(VilkårVurderingSærligGrunnEntitet::getGrunn).collect(Collectors.toSet());
        return særligeGrunner1.equals(særligeGrunner2);
    }

    private VilkårVurderingPeriodeEntitet finnVilkårsvurdering(Periode periode) {
        return vilkårsvurderinger.stream()
            .filter(vp -> vp.getPeriode().overlapper(periode))
            .findFirst()
            .orElseThrow();
    }

    private boolean likForeldelse(Periode periode1, Periode periode2) {
        return finnForeldelseResultat(periode1) == finnForeldelseResultat(periode2);
    }

    private ForeldelseVurderingType finnForeldelseResultat(Periode periode1) {
        if (foreldelse == null){
            return null;
        }
        return foreldelse.getVurdertForeldelsePerioder().stream()
            .filter(f -> f.getPeriode().overlapper(periode1))
            .map(VurdertForeldelsePeriode::getForeldelseVurderingType)
            .findFirst()
            .orElse(ForeldelseVurderingType.UDEFINERT);
    }
}
