package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.DATE_FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeType;
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
        var historikkinnslager = historikkinnslagRepository.hent(saksnummer);
        var historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);

        flerePerioderAssert(historikkinnslag);
    }

    @Test
    void lagHistorikInnslag_medForrigePeriode_medIngenEndringer() {
        VilkårVurderingEntitet vurderingEntitet = new VilkårVurderingEntitet();
        vurderingEntitet.leggTilPeriode(formGodTroPeriode(vurderingEntitet, FOM, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, vurderingEntitet, vurderingEntitet);

        var historikkinnslager = historikkinnslagRepository.hent(saksnummer);
        var tilbakekrevingHistorikkinnslag = tilbakekrevingHistorikkinnslag(historikkinnslager);
        assertThat(tilbakekrevingHistorikkinnslag).isEmpty();
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

        var historikkinnslager = historikkinnslagRepository.hent(saksnummer);
        var historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);
        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(SkjermlenkeType.TILBAKEKREVING);
        assertThat(historikkinnslag.getLinjer().get(0).getTekst()).contains("__Vurdering__ av perioden", DATE_FORMATTER.format(FOM), DATE_FORMATTER.format(TOM));
        assertThat(historikkinnslag.getLinjer().get(1).getTekst()).contains("Beløp som skal tilbakekreves", "satt til", BELØP_TILBAKEKREVES);
        assertThat(historikkinnslag.getLinjer().get(2).getTekst()).contains("Er vilkårene for tilbakekreving oppfylt?", "endret fra", VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn(), VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER.getNavn());
        assertThat(historikkinnslag.getLinjer().get(3).getTekst()).contains("I hvilken grad har mottaker handlet uaktsomt?", "endret fra", Aktsomhet.SIMPEL_UAKTSOM.getNavn(), Aktsomhet.GROVT_UAKTSOM.getNavn());
        assertThat(historikkinnslag.getLinjer().get(4).getTekst()).contains("Skal det tilegges renter?", NEI, "er fjernet");
        assertThat(historikkinnslag.getLinjer().get(5).getTekst()).contains("Er det særlige grunner til reduksjon?", "endret fra", formGrunnTekst(forrigeAktsomhetEntitet), formGrunnTekst(nyVurdering.getPerioder().get(0).getAktsomhet()));
        // assertThat(historikkinnslag.getLinjer().get(6).getTekst()).contains(ANDRE_PERIODE_BEGRUNNELSE); Lik som forrige og derfor ikke inkludert
        assertThat(historikkinnslag.getLinjer().get(6).getTekst()).contains(AKTSOMHET_BEGRUNNELSE);
        // assertThat(historikkinnslag.getLinjer().get(7).getTekst()).contains(SÆRLIG_GRUNNER_BEGRUNNELSE); lik
    }

    @Test
    void lagHistorikInnslag_nårPerioderErDeltOpp() {
        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        gammelVurdering.leggTilPeriode(formAktsomhetPeriode(gammelVurdering, FOM, TOM));

        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formGodTroPeriode(nyVurdering, FOM, PERIOD_FØRSTE_SISTE_DATO));
        nyVurdering.leggTilPeriode(formAktsomhetPeriode(nyVurdering, PERIODE_ANDRE_FØRSTE_DATO, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        var historikkinnslager = historikkinnslagRepository.hent(saksnummer);
        var historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);

        flerePerioderAssert(historikkinnslag);
    }

    @Test
    void lagHistorikInnslag_nårVilkårResultatHarEndretFraGodTroTilAktsomhet() {
        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formAktsomhetPeriode(nyVurdering, FOM, TOM));

        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        gammelVurdering.leggTilPeriode(formGodTroPeriode(gammelVurdering, FOM, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        var historikkinnslager = historikkinnslagRepository.hent(saksnummer);
        var historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);
        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(SkjermlenkeType.TILBAKEKREVING);
        assertThat(historikkinnslag.getLinjer().get(0).getTekst()).contains("__Vurdering__ av perioden", DATE_FORMATTER.format(FOM), DATE_FORMATTER.format(TOM));
        assertThat(historikkinnslag.getLinjer().get(1).getTekst()).contains("Beløp som skal tilbakekreves", "endret fra", BELØP_TILBAKEKREVES, "1000");
        assertThat(historikkinnslag.getLinjer().get(2).getTekst()).contains("Er beløpet i behold?", "er fjernet");
        assertThat(historikkinnslag.getLinjer().get(3).getTekst()).contains("Er vilkårene for tilbakekreving oppfylt?", "endret fra", VilkårResultat.GOD_TRO.getNavn(), VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn());
        assertThat(historikkinnslag.getLinjer().get(4).getTekst()).contains("I hvilken grad har mottaker handlet uaktsomt?", "er satt til ", Aktsomhet.GROVT_UAKTSOM.getNavn());
        assertThat(historikkinnslag.getLinjer().get(5).getTekst()).contains("Er det særlige grunner til reduksjon?", "er satt til ", formGrunnTekst(nyVurdering.getPerioder().get(0).getAktsomhet()));
        assertThat(historikkinnslag.getLinjer().get(6).getTekst()).contains(ANDRE_PERIODE_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(7).getTekst()).contains(AKTSOMHET_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(8).getTekst()).contains(SÆRLIG_GRUNNER_BEGRUNNELSE);
    }

    @Test
    void lagHistorikInnslag_nårVilkårResultatHarEndretFraAktsomhetTilGodTro() {
        VilkårVurderingEntitet nyVurdering = new VilkårVurderingEntitet();
        nyVurdering.leggTilPeriode(formGodTroPeriode(nyVurdering, FOM, TOM));

        VilkårVurderingEntitet gammelVurdering = new VilkårVurderingEntitet();
        gammelVurdering.leggTilPeriode(formAktsomhetPeriode(gammelVurdering, FOM, TOM));

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandling, gammelVurdering, nyVurdering);

        var historikkinnslager = historikkinnslagRepository.hent(saksnummer);
        var historikkinnslag = fellesHistorikkInnslagAssert(historikkinnslager);

        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(SkjermlenkeType.TILBAKEKREVING);
        assertThat(historikkinnslag.getLinjer().get(0).getTekst()).contains("__Vurdering__ av perioden", DATE_FORMATTER.format(FOM), DATE_FORMATTER.format(TOM));
        assertThat(historikkinnslag.getLinjer().get(1).getTekst()).contains("Beløp som skal tilbakekreves", "endret fra", "2000", String.valueOf(SUM_INNTREKK));
        assertThat(historikkinnslag.getLinjer().get(2).getTekst()).contains("Er beløpet i behold?", "satt til", JA);
        assertThat(historikkinnslag.getLinjer().get(3).getTekst()).contains("Er vilkårene for tilbakekreving oppfylt?", "endret fra", VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn(), VilkårResultat.GOD_TRO.getNavn());
        assertThat(historikkinnslag.getLinjer().get(4).getTekst()).contains("I hvilken grad har mottaker handlet uaktsomt?", Aktsomhet.GROVT_UAKTSOM.getNavn(), "er fjernet");
        assertThat(historikkinnslag.getLinjer().get(5).getTekst()).contains("Er det særlige grunner til reduksjon?", "Nei", SærligGrunn.STØRRELSE_BELØP.getNavn(), SærligGrunn.ANNET.getNavn(), "Annet begrunnelse", "er fjernet");
        assertThat(historikkinnslag.getLinjer().get(6).getTekst()).contains(FØRSTE_PERIODE_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(7).getTekst()).contains(GOD_TRO_BEGRUNNELSE);
    }

    private Historikkinnslag fellesHistorikkInnslagAssert(List<Historikkinnslag> historikkinnslager) {
        assertThat(historikkinnslager).hasSize(2);
        assertThat(historikkinnslager).extracting(Historikkinnslag::getSkjermlenke).containsOnlyOnce(SkjermlenkeType.TILBAKEKREVING);
        var historikkinnslagTilbakekreving = tilbakekrevingHistorikkinnslag(historikkinnslager);
        assertThat(historikkinnslagTilbakekreving).isPresent();
        assertThat(historikkinnslagTilbakekreving.get().getBehandlingId()).isEqualTo(internBehandlingId);
        return historikkinnslagTilbakekreving.get();
    }

    private static Optional<Historikkinnslag> tilbakekrevingHistorikkinnslag(List<Historikkinnslag> historikkinnslager) {
        return historikkinnslager.stream()
            .filter(h -> SkjermlenkeType.TILBAKEKREVING.equals(h.getSkjermlenke()))
            .findFirst();
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
        assertThat(historikkinnslag.getLinjer()).hasSizeGreaterThan(0);
        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(SkjermlenkeType.TILBAKEKREVING);

        // Periode 1
        assertThat(historikkinnslag.getLinjer().get(0).getTekst()).contains("__Vurdering__ av perioden", DATE_FORMATTER.format(FOM), DATE_FORMATTER.format(PERIOD_FØRSTE_SISTE_DATO));
        assertThat(historikkinnslag.getLinjer().get(1).getTekst()).contains("Beløp som skal tilbakekreves", "satt til", BigDecimal.valueOf(SUM_INNTREKK).toString());
        assertThat(historikkinnslag.getLinjer().get(2).getTekst()).contains("Er beløpet i behold?", "satt til", JA);
        assertThat(historikkinnslag.getLinjer().get(3).getTekst()).contains("Er vilkårene for tilbakekreving oppfylt?", "satt til", VilkårResultat.GOD_TRO.getNavn());
        assertThat(historikkinnslag.getLinjer().get(4).getTekst()).contains(FØRSTE_PERIODE_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(5).getTekst()).contains(GOD_TRO_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(6).getType()).isEqualTo(HistorikkinnslagLinjeType.LINJESKIFT);

        // Periode 2
        assertThat(historikkinnslag.getLinjer().get(7).getTekst()).contains("__Vurdering__ av perioden", DATE_FORMATTER.format(PERIODE_ANDRE_FØRSTE_DATO), DATE_FORMATTER.format(TOM));
        assertThat(historikkinnslag.getLinjer().get(8).getTekst()).contains("Beløp som skal tilbakekreves", "satt til", BELØP_TILBAKEKREVES);
        assertThat(historikkinnslag.getLinjer().get(9).getTekst()).contains("Er vilkårene for tilbakekreving oppfylt?", "satt til", VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER.getNavn());
        assertThat(historikkinnslag.getLinjer().get(10).getTekst()).contains("I hvilken grad har mottaker handlet uaktsomt?", "satt til", Aktsomhet.GROVT_UAKTSOM.getNavn());
        assertThat(historikkinnslag.getLinjer().get(11).getTekst()).contains("Er det særlige grunner til reduksjon?", "satt til", "Nei", SærligGrunn.STØRRELSE_BELØP.getNavn(), SærligGrunn.ANNET.getNavn(), "Annet begrunnelse");
        assertThat(historikkinnslag.getLinjer().get(12).getTekst()).contains(ANDRE_PERIODE_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(13).getTekst()).contains(AKTSOMHET_BEGRUNNELSE);
        assertThat(historikkinnslag.getLinjer().get(14).getTekst()).contains(SÆRLIG_GRUNNER_BEGRUNNELSE);
    }


}
