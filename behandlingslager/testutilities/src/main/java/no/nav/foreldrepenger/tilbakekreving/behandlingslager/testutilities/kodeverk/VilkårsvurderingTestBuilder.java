package no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk;

import java.math.BigDecimal;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.NavOppfulgt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class VilkårsvurderingTestBuilder {
    private VilkårsvurderingRepository repository;

    private VilkårsvurderingTestBuilder(VilkårsvurderingRepository repository) {
        this.repository = repository;
    }

    public static VilkårsvurderingTestBuilder medRepo(VilkårsvurderingRepository repository) {
        return new VilkårsvurderingTestBuilder(repository);
    }

    public void lagre(Long behandingId, Map<Periode, VVurdering> vurderinger) {
        VilkårVurderingEntitet vurderingEntitet = new VilkårVurderingEntitet();
        for (Map.Entry<Periode, VVurdering> entry : vurderinger.entrySet()) {
            VilkårVurderingPeriodeEntitet periodeEntitet = VilkårVurderingPeriodeEntitet.builder()
                .medPeriode(entry.getKey())
                .medNavOppfulgt(NavOppfulgt.UDEFINERT)
                .medVilkårResultat(entry.getValue().getVilkårResultat())
                .medVurderinger(vurderingEntitet)
                .medBegrunnelse("foo")
                .build();
            entry.getValue().leggPåVurdering(periodeEntitet);
            vurderingEntitet.leggTilPeriode(periodeEntitet);
        }

        repository.lagre(behandingId, vurderingEntitet);
    }

    public static class VVurdering {
        private VilkårResultat vilkårResultat = VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER;
        private VilkårVurderingGodTroEntitet.Builder godTro;
        private VilkårVurderingAktsomhetEntitet.Builder aktsomhet;

        private VVurdering() {
        }

        public static VVurdering godTro() {
            VVurdering vurdering = new VVurdering();
            vurdering.vilkårResultat = VilkårResultat.GOD_TRO;
            vurdering.godTro = VilkårVurderingGodTroEntitet.builder()
                .medBegrunnelse("foo");
            return vurdering;
        }

        public static VVurdering simpelUaktsom() {
            VVurdering vurdering = new VVurdering();
            vurdering.aktsomhet = VilkårVurderingAktsomhetEntitet.builder()
                .medBegrunnelse("foo")
                .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
                .medIleggRenter(false)
                .medSærligGrunnerTilReduksjon(false);
            return vurdering;
        }

        public static VVurdering forsett() {
            VVurdering vurdering = new VVurdering();
            vurdering.aktsomhet = VilkårVurderingAktsomhetEntitet.builder()
                .medBegrunnelse("foo")
                .medAktsomhet(Aktsomhet.FORSETT);
            return vurdering;
        }

        public VVurdering setMedRenter(boolean renter) {
            if (aktsomhet != null) {
                aktsomhet.medIleggRenter(renter);
                return this;
            }
            throw new IllegalArgumentException("Kan ikke ilegge renter når god tro");
        }

        public VVurdering setManueltBeløp(int beløp) {
            return setManueltBeløp(BigDecimal.valueOf(beløp));
        }

        public VVurdering setManueltBeløp(BigDecimal beløp) {
            if (aktsomhet != null) {
                aktsomhet.medBeløpTilbakekreves(beløp);
            } else {
                godTro.medBeløpErIBehold(beløp.signum() != 0);
                godTro.medBeløpTilbakekreves(beløp);
            }
            return this;
        }

        public VVurdering setProsenterTilbakekreves(BigDecimal prosentandel) {
            if (aktsomhet != null) {
                aktsomhet.medProsenterSomTilbakekreves(prosentandel);
                aktsomhet.medSærligGrunnerTilReduksjon(BigDecimal.valueOf(100).compareTo(prosentandel) != 0);
                return this;
            }
            throw new IllegalArgumentException("Kan ikke bruke andel når god tro");
        }

        public VilkårResultat getVilkårResultat() {
            return vilkårResultat;
        }

        public void leggPåVurdering(VilkårVurderingPeriodeEntitet periodeEntitet) {
            if (godTro != null) {
                periodeEntitet.setGodTro(godTro.medPeriode(periodeEntitet).build());
            } else {
                periodeEntitet.setAktsomhet(aktsomhet.medPeriode(periodeEntitet).build());
            }
        }
    }
}
