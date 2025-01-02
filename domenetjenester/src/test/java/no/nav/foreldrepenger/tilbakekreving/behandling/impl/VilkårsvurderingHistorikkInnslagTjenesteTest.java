package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class VilkårsvurderingHistorikkInnslagTjenesteTest extends FellesTestOppsett {

    private static final LocalDate PERIOD_FØRSTE_SISTE_DATO = LocalDate.of(2016, 3, 31);
    private static final LocalDate PERIODE_ANDRE_FØRSTE_DATO = LocalDate.of(2016, 4, 1);
    private static final String FØRSTE_PERIODE_BEGRUNNELSE = "1st periode begrunnelse";
    private static final String ANDRE_PERIODE_BEGRUNNELSE = "2nd periode begrunnelse";
    private static final String GOD_TRO_BEGRUNNELSE = "god tro begrunnelse";
    private static final String AKTSOMHET_BEGRUNNELSE = "aktsomhet begrunnelse";
    private static final String SÆRLIG_GRUNNER_BEGRUNNELSE = "særlig grunner begrunnelse";
    private static final String BELØP_TILBAKEKREVES = "2000";
    private static final String JA = "Ja";
    private static final String NEI = "Nei";

    @Test
    void lagHistorikkInnslag_nårForrigePeriodeFinnesIkke() {
        VilkårVurderingEntitet vurderingEntitet = new VilkårVurderingEntitet();
        vurderingEntitet.leggTilPeriode(formGodTroPeriode(vurderingEntitet, FOM, PERIOD_FØRSTE_SISTE_DATO));
        vurderingEntitet.leggTilPeriode(formAktsomhetPeriode(vurderingEntitet, PERIODE_ANDRE_FØRSTE_DATO, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, null, vurderingEntitet);
        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);

        flerePerioderAssert(historikkinnslag);

    }

    @Test
    void lagHistorikInnslag_medForrigePeriode_medIngenEndringer() {
        VilkårVurderingEntitet vurderingEntitet = new VilkårVurderingEntitet();
        vurderingEntitet.leggTilPeriode(formGodTroPeriode(vurderingEntitet, FOM, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, vurderingEntitet, vurderingEntitet);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        assertThat(historikkinnslager).isEmpty();
    }

    @Test
    void lagHistorikInnslag_medForrigePeriode_medEndringer() {
        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formAktsomhetPeriode(nyVurdering, FOM, TOM));

        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet forrigePeriodeEntitet = VilkårVurderingPeriodeEntitet.builder().medBegrunnelse(ANDRE_PERIODE_BEGRUNNELSE)
                .medPeriode(Periode.of(FOM, TOM))
                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER).build();
        VilkårVurderingAktsomhetEntitet forrigeAktsomhetEntitet = VilkårVurderingAktsomhetEntitet.builder().medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
                .medBegrunnelse("Gammel Aktsomhet Begrunnelse")
                .medPeriode(forrigePeriodeEntitet)
                .medSærligGrunnerTilReduksjon(true)
                .medIleggRenter(false)
                .medSærligGrunnerBegrunnelse(SÆRLIG_GRUNNER_BEGRUNNELSE).build();
        forrigeAktsomhetEntitet.leggTilSærligGrunn(formSærligGrunn(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, forrigeAktsomhetEntitet));
        forrigeAktsomhetEntitet.leggTilSærligGrunn(formSærligGrunn(SærligGrunn.TID_FRA_UTBETALING, forrigeAktsomhetEntitet));
        forrigePeriodeEntitet.setAktsomhet(forrigeAktsomhetEntitet);
        gammelVurdering.leggTilPeriode(forrigePeriodeEntitet);

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);

        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(1);
        HistorikkinnslagDel førsteDel = historikkinnslag.getHistorikkinnslagDeler().get(0);
        fellesHistorikkinnslagDelAssert(førsteDel, FOM, TOM, AKTSOMHET_BEGRUNNELSE, ANDRE_PERIODE_BEGRUNNELSE);
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn());
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER.getNavn());
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD)))
                .isEqualTo(Aktsomhet.SIMPEL_UAKTSOM.getNavn());
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD)))
                .isEqualTo(Aktsomhet.GROVT_UAKTSOM.getNavn());
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_SÆRLIGE_GRUNNER_TIL_REDUKSJON))).isEqualTo(formGrunnTekst(forrigeAktsomhetEntitet));
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_SÆRLIGE_GRUNNER_TIL_REDUKSJON))).isEqualTo(formGrunnTekst(nyVurdering.getPerioder().get(0).getAktsomhet()));
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES))).isNull();
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES))).isEqualTo(BELØP_TILBAKEKREVES);
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ILEGG_RENTER))).isEqualTo(NEI);
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ILEGG_RENTER))).isNull();
        assertThat(getTilVerdi(førsteDel.getOpplysning(HistorikkOpplysningType.SÆRLIG_GRUNNER_BEGRUNNELSE))).isEqualTo(SÆRLIG_GRUNNER_BEGRUNNELSE);

    }

    @Test
    void lagHistorikInnslag_nårPerioderErDeltOpp() {
        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        gammelVurdering.leggTilPeriode(formAktsomhetPeriode(gammelVurdering, FOM, TOM));

        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formGodTroPeriode(nyVurdering, FOM, PERIOD_FØRSTE_SISTE_DATO));
        nyVurdering.leggTilPeriode(formAktsomhetPeriode(nyVurdering, PERIODE_ANDRE_FØRSTE_DATO, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);

        flerePerioderAssert(historikkinnslag);
    }

    @Test
    void lagHistorikInnslag_nårVilkårResultatHarEndretFraGodTroTilAktsomhet() {
        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formAktsomhetPeriode(nyVurdering, FOM, TOM));

        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        gammelVurdering.leggTilPeriode(formGodTroPeriode(gammelVurdering, FOM, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(1);

        HistorikkinnslagDel historikkinnslagDel = historikkinnslag.getHistorikkinnslagDeler().get(0);
        fellesHistorikkinnslagDelAssert(historikkinnslagDel, FOM, TOM, AKTSOMHET_BEGRUNNELSE, ANDRE_PERIODE_BEGRUNNELSE);

        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn());
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.GOD_TRO.getNavn());
        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD)))
                .isEqualTo(Aktsomhet.GROVT_UAKTSOM.getNavn());
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD)))
                .isEqualTo(null);
        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES)))
                .isEqualTo(BELØP_TILBAKEKREVES);
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES)))
                .isEqualTo("1000");
    }

    @Test
    void lagHistorikInnslag_nårVilkårResultatHarEndretFraAktsomhetTilGodTro() {
        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formGodTroPeriode(nyVurdering, FOM, TOM));

        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        gammelVurdering.leggTilPeriode(formAktsomhetPeriode(gammelVurdering, FOM, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(1);

        HistorikkinnslagDel historikkinnslagDel = historikkinnslag.getHistorikkinnslagDeler().get(0);
        fellesHistorikkinnslagDelAssert(historikkinnslagDel, FOM, TOM, GOD_TRO_BEGRUNNELSE, FØRSTE_PERIODE_BEGRUNNELSE);

        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.GOD_TRO.getNavn());
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn());

        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES))).isEqualTo(String.valueOf(SUM_INNTREKK));
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES))).isEqualTo("2000");

        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.ER_BELØPET_BEHOLD))).isEqualTo(JA);
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.ER_BELØPET_BEHOLD))).isNull();
    }

    private Historikkinnslag fellesHistorikkInnslagAssert(List<Historikkinnslag> historikkinnslager) {
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.TILBAKEKREVING);
        return historikkinnslag;
    }

    private VilkårVurderingSærligGrunnEntitet formSærligGrunn(SærligGrunn grunn, VilkårVurderingAktsomhetEntitet aktsomhetEntitet) {
        return VilkårVurderingSærligGrunnEntitet.builder().medBegrunnelse("Annet begrunnelse")
                .medGrunn(grunn)
                .medVurdertAktsomhet(aktsomhetEntitet).build();
    }

    private VilkårVurderingPeriodeEntitet formAktsomhetPeriode(VilkårVurderingEntitet vurderingEntitet, LocalDate fom, LocalDate tom) {
        VilkårVurderingPeriodeEntitet andrePeriode = VilkårVurderingPeriodeEntitet.builder()
                .medPeriode(Periode.of(fom, tom))
                .medBegrunnelse(ANDRE_PERIODE_BEGRUNNELSE)
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medVurderinger(vurderingEntitet).build();
        VilkårVurderingAktsomhetEntitet aktsomhetEntitet = VilkårVurderingAktsomhetEntitet.builder()
                .medBegrunnelse(AKTSOMHET_BEGRUNNELSE)
                .medBeløpTilbakekreves(BigDecimal.valueOf(SUM_INNTREKK).add(BigDecimal.valueOf(1000l)))
                .medPeriode(andrePeriode)
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medSærligGrunnerTilReduksjon(false)
                .medSærligGrunnerBegrunnelse(SÆRLIG_GRUNNER_BEGRUNNELSE).build();
        aktsomhetEntitet.leggTilSærligGrunn(formSærligGrunn(SærligGrunn.STØRRELSE_BELØP, aktsomhetEntitet));
        aktsomhetEntitet.leggTilSærligGrunn(formSærligGrunn(SærligGrunn.ANNET, aktsomhetEntitet));
        andrePeriode.setAktsomhet(aktsomhetEntitet);
        return andrePeriode;
    }

    private VilkårVurderingPeriodeEntitet formGodTroPeriode(VilkårVurderingEntitet vurderingEntitet, LocalDate fom, LocalDate tom) {
        VilkårVurderingPeriodeEntitet førstePeriode = VilkårVurderingPeriodeEntitet.builder()
                .medPeriode(Periode.of(fom, tom))
                .medBegrunnelse(FØRSTE_PERIODE_BEGRUNNELSE)
                .medVilkårResultat(VilkårResultat.GOD_TRO)
                .medVurderinger(vurderingEntitet).build();
        VilkårVurderingGodTroEntitet godTroEntitet = VilkårVurderingGodTroEntitet.builder()
                .medBeløpTilbakekreves(BigDecimal.valueOf(SUM_INNTREKK))
                .medBeløpErIBehold(true)
                .medBegrunnelse(GOD_TRO_BEGRUNNELSE)
                .medPeriode(førstePeriode).build();
        førstePeriode.setGodTro(godTroEntitet);
        return førstePeriode;
    }

    private void fellesHistorikkinnslagDelAssert(HistorikkinnslagDel historikkinnslagDel, LocalDate fom, LocalDate tom, String vilkårBegrunnelse, String periodeBegrunnelse) {
        assertThat(historikkinnslagDel.getSkjermlenke().get()).isEqualTo(SkjermlenkeType.TILBAKEKREVING.getKode());
        assertThat(historikkinnslagDel.getBegrunnelse().get()).isEqualTo(vilkårBegrunnelse);

        assertThat(historikkinnslagDel.getOpplysning(HistorikkOpplysningType.TILBAKEKREVING_OPPFYLT_BEGRUNNELSE).get()
                .getTilVerdi()).isEqualTo(periodeBegrunnelse);
        assertThat(historikkinnslagDel.getOpplysning(HistorikkOpplysningType.PERIODE_FOM).get().getTilVerdi())
                .isEqualTo(formatDate(fom));
        assertThat(historikkinnslagDel.getOpplysning(HistorikkOpplysningType.PERIODE_TOM).get().getTilVerdi())
                .isEqualTo(formatDate(tom));
    }

    private String formGrunnTekst(VilkårVurderingAktsomhetEntitet aktsomhetEntitet) {
        List<String> grunnTekster = new ArrayList<>();
        StringBuilder grunnTekst = new StringBuilder();
        grunnTekst.append(aktsomhetEntitet.getSærligGrunnerTilReduksjon() ? JA : NEI);
        grunnTekst.append(": ");
        for (VilkårVurderingSærligGrunnEntitet særligGrunn : aktsomhetEntitet.getSærligGrunner()) {
            SærligGrunn grunn = særligGrunn.getGrunn();
            StringBuilder tekst = new StringBuilder(grunn.getNavn());
            if (SærligGrunn.ANNET.equals(grunn)) {
                tekst.append(": ");
                tekst.append(særligGrunn.getBegrunnelse());
            }
            grunnTekster.add(tekst.toString());
        }
        grunnTekst.append(String.join(", ", grunnTekster));
        return grunnTekst.toString();
    }

    private void flerePerioderAssert(Historikkinnslag historikkinnslag) {
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(2);
        HistorikkinnslagDel førsteDel = historikkinnslag.getHistorikkinnslagDeler().get(0);

        fellesHistorikkinnslagDelAssert(førsteDel, FOM, PERIOD_FØRSTE_SISTE_DATO, GOD_TRO_BEGRUNNELSE, FØRSTE_PERIODE_BEGRUNNELSE);

        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT)))
                .isEqualTo(VilkårResultat.GOD_TRO.getNavn());
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_BELØPET_BEHOLD)))
                .isEqualTo(JA);
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.ER_BELØPET_BEHOLD)))
                .isNull();
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES)))
                .isEqualTo(BigDecimal.valueOf(SUM_INNTREKK).toString());
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES)))
                .isNull();

        HistorikkinnslagDel andreDel = historikkinnslag.getHistorikkinnslagDeler().get(1);
        fellesHistorikkinnslagDelAssert(andreDel, PERIODE_ANDRE_FØRSTE_DATO, TOM, AKTSOMHET_BEGRUNNELSE, ANDRE_PERIODE_BEGRUNNELSE);

        assertThat(getTilVerdi(andreDel.getEndretFelt(HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD)))
                .isEqualTo(Aktsomhet.GROVT_UAKTSOM.getNavn());
        assertThat(getFraVerdi(andreDel.getEndretFelt(HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD)))
                .isNull();
        assertThat(getTilVerdi(andreDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES)))
                .isEqualTo(BELØP_TILBAKEKREVES);
        assertThat(getFraVerdi(andreDel.getEndretFelt(HistorikkEndretFeltType.BELØP_TILBAKEKREVES)))
                .isNull();
        assertThat(getTilVerdi(andreDel.getOpplysning(HistorikkOpplysningType.SÆRLIG_GRUNNER_BEGRUNNELSE))).isEqualTo(SÆRLIG_GRUNNER_BEGRUNNELSE);
    }


}
