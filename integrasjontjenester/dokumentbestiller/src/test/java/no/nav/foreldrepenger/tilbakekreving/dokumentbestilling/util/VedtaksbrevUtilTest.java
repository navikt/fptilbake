package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.PeriodeMedBrevtekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class VedtaksbrevUtilTest {

    private final LocalDate JANUAR_1_2019 = LocalDate.of(2019, 1, 1);
    private final LocalDate JANUAR_30_2019 = LocalDate.of(2019, 1, 30);
    private final LocalDate FEBRUAR_1_2019 = LocalDate.of(2019, 2, 1);
    private final LocalDate FEBRUAR_15_2019 = LocalDate.of(2019, 2, 15);
    private final LocalDate FEBRUAR_20_2019 = LocalDate.of(2019, 2, 20);
    private final LocalDate MARS_15_2019 = LocalDate.of(2019, 3, 15);
    private final LocalDate FEBRUAR_20_2018 = LocalDate.of(2018, 2, 20);
    private final LocalDate FEBUAR_22_2018 = LocalDate.of(2018, 2, 22);

    private static final Long BEHANDLING_ID = 12345678L;


    @Test
    public void skal_sette_fritekst_på_tilsvarende_periode() {
        PeriodeMedTekstDto fritekstDto1 = new PeriodeMedTekstDto();
        fritekstDto1.setFom(FEBRUAR_1_2019);
        fritekstDto1.setFaktaAvsnitt("SAKSBEHANDLERS TEKST OM FAKTA TIL PERIODEN 1");
        fritekstDto1.setVilkårAvsnitt("SAKSBEHANDLERS TEKST OM VILKÅR FOR PERIODEN 1");
        fritekstDto1.setSærligeGrunnerAvsnitt("SAKSBEHANDLERS TEKST OM SÆRLIGE GRUNNER 1");

        PeriodeMedTekstDto fritekstDto2 = new PeriodeMedTekstDto();
        fritekstDto2.setFom(JANUAR_1_2019);
        fritekstDto2.setFaktaAvsnitt("SAKSBEHANDLERS TEKST OM FAKTA TIL PERIODEN 2");
        fritekstDto2.setVilkårAvsnitt("SAKSBEHANDLERS TEKST OM VILKÅR FOR PERIODEN 2");
        fritekstDto2.setSærligeGrunnerAvsnitt("SAKSBEHANDLERS TEKST OM SÆRLIGE GRUNNER 2");

        VilkårsvurderingPerioderDto vilkårsperiode1 = lagVilkårvurderingperiodeMedVilkår(JANUAR_1_2019, JANUAR_30_2019,
            Aktsomhet.GROVT_UAKTSOM, SærligGrunn.ANNET, VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        VilkårsvurderingPerioderDto vilkårsperiode2 = lagVilkårvurderingperiodeMedVilkår(FEBRUAR_1_2019, FEBRUAR_15_2019,
            Aktsomhet.FORSETT, SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, VilkårResultat.GOD_TRO);

        List<PeriodeMedBrevtekst> perioderMedFritekster = VedtaksbrevUtil.lagSortertePerioderMedTekst(
            List.of(vilkårsperiode1, vilkårsperiode2),
            List.of(fritekstDto1, fritekstDto2));

        Assertions.assertThat(perioderMedFritekster.get(0).getFritekstFakta()).isEqualTo("SAKSBEHANDLERS TEKST OM FAKTA TIL PERIODEN 2");
        Assertions.assertThat(perioderMedFritekster.get(0).getFritekstVilkår()).isEqualTo("SAKSBEHANDLERS TEKST OM VILKÅR FOR PERIODEN 2");
        Assertions.assertThat(perioderMedFritekster.get(0).getFritekstSærligeGrunner()).isEqualTo("SAKSBEHANDLERS TEKST OM SÆRLIGE GRUNNER 2");

        Assertions.assertThat(perioderMedFritekster.get(1).getFritekstFakta()).isEqualTo("SAKSBEHANDLERS TEKST OM FAKTA TIL PERIODEN 1");
        Assertions.assertThat(perioderMedFritekster.get(1).getFritekstVilkår()).isEqualTo("SAKSBEHANDLERS TEKST OM VILKÅR FOR PERIODEN 1");
        Assertions.assertThat(perioderMedFritekster.get(1).getFritekstSærligeGrunner()).isEqualTo("SAKSBEHANDLERS TEKST OM SÆRLIGE GRUNNER 1");
    }

    @Test
    public void skal_ikke_sette_fritekst_dersom_ingen_tilsvarende_perioder() {
        PeriodeMedTekstDto fritekstPeriodeForFebruar = new PeriodeMedTekstDto();
        fritekstPeriodeForFebruar.setFom(FEBRUAR_1_2019);
        fritekstPeriodeForFebruar.setFaktaAvsnitt("SAKSBEHANDLERS TEKST OM FAKTA TIL PERIODEN 1");
        fritekstPeriodeForFebruar.setVilkårAvsnitt("SAKSBEHANDLERS TEKST OM VILKÅR FOR PERIODEN 1");
        fritekstPeriodeForFebruar.setSærligeGrunnerAvsnitt("SAKSBEHANDLERS TEKST OM SÆRLIGE GRUNNER 1");

        VilkårsvurderingPerioderDto vilkårsperiodeForJanuar = lagVilkårvurderingperiodeMedVilkår(JANUAR_1_2019, JANUAR_30_2019,
            Aktsomhet.GROVT_UAKTSOM, SærligGrunn.ANNET, VilkårResultat.FORSTO_BURDE_FORSTÅTT);

        List<PeriodeMedBrevtekst> perioderMedFritekster = VedtaksbrevUtil.lagSortertePerioderMedTekst(
            List.of(vilkårsperiodeForJanuar),
            List.of(fritekstPeriodeForFebruar));

        Assertions.assertThat(perioderMedFritekster.get(0).getFritekstFakta()).isNull();
        Assertions.assertThat(perioderMedFritekster.get(0).getFritekstVilkår()).isNull();
        Assertions.assertThat(perioderMedFritekster.get(0).getFritekstSærligeGrunner()).isNull();
    }

    @Test
    public void skal_finne_aktsomhet_og_særlige_grunner_og_vilkårresultat_og_samle_de_til_en_tekst() {
        VilkårsvurderingPerioderDto vilkårsvurderingPeriode = lagVilkårvurderingperiodeMedVilkår(JANUAR_1_2019, JANUAR_30_2019, Aktsomhet.GROVT_UAKTSOM, SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        List<PeriodeMedBrevtekst> perioderMedTekst = VedtaksbrevUtil.lagSortertePerioderMedTekst(Arrays.asList(vilkårsvurderingPeriode), null);
        Assertions.assertThat(perioderMedTekst.get(0).getGenerertVilkårAvsnitt()).isEqualTo("Vurderingen av denne perioden er: FORSTO_BURDE_FORSTAATT");
        Assertions.assertThat(perioderMedTekst.get(0).getGenerertSærligeGrunnerAvsnitt()).isEqualTo("Bruker har utvist GROVT_UAKTSOM med følgende særlige grunner: HELT_ELLER_DELVIS_NAVS_FEIL, ");
    }

    @Test
    public void skal_sortere_periodene_kronologisk() {
        List<VilkårsvurderingPerioderDto> vilkårsvurderingPerioder = lagVilkårvurderingperioder();
        List<PeriodeMedBrevtekst> perioderMedTekst = VedtaksbrevUtil.lagSortertePerioderMedTekst(vilkårsvurderingPerioder, null);
        Assertions.assertThat(perioderMedTekst.get(0).getFom()).isEqualTo(FEBRUAR_20_2018);
        Assertions.assertThat(perioderMedTekst.get(perioderMedTekst.size() - 1).getFom()).isEqualTo(FEBRUAR_20_2019);
    }

    @Test
    public void skal_håndtere_ingen_fritekstperioder() {
        VedtaksbrevUtil.lagSortertePerioderMedTekst(lagVilkårvurderingperioder(), null);
    }

    @Test
    public void skal_slå_sammen_vurderingsvilkårperioder_dersom_de_har_samme_vilkår_og_er_rett_etter_hverandre() {
        //TODO (Trine): implement. PFP-7975
    }

    private List<VilkårsvurderingPerioderDto> lagVilkårvurderingperioder() {
        VilkårsvurderingPerioderDto periode1 = lagVilkårvurderingperiodeMedVilkår(JANUAR_1_2019, JANUAR_30_2019,
            Aktsomhet.GROVT_UAKTSOM, SærligGrunn.ANNET, VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        VilkårsvurderingPerioderDto periode2 = lagVilkårvurderingperiodeMedVilkår(FEBRUAR_1_2019, FEBRUAR_15_2019,
            Aktsomhet.FORSETT, SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, VilkårResultat.GOD_TRO);
        VilkårsvurderingPerioderDto periode3 = lagVilkårvurderingperiodeMedVilkår(FEBRUAR_20_2019, MARS_15_2019,
            Aktsomhet.SIMPEL_UAKTSOM, SærligGrunn.GRAD_AV_UAKTSOMHET, VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER);
        VilkårsvurderingPerioderDto periode4 = lagVilkårvurderingperiodeMedVilkår(FEBRUAR_20_2018, FEBUAR_22_2018,
            Aktsomhet.FORSETT, SærligGrunn.STØRRELSE_BELØP, VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER);
        return Arrays.asList(periode1, periode2, periode3, periode4);
    }

    private VilkårsvurderingPerioderDto lagVilkårvurderingperiodeMedVilkår(
        LocalDate fom, LocalDate tom, Aktsomhet aktsomhet, SærligGrunn særligGrunn, VilkårResultat vilkårResultat) {

        VilkårResultatAktsomhetDto aktsomhetInfo = new VilkårResultatAktsomhetDto();
        aktsomhetInfo.setSærligeGrunner(Arrays.asList(særligGrunn));
        VilkårResultatAnnetDto vilkårResultatAnnetDto = new VilkårResultatAnnetDto("begrunnelsen", aktsomhet, aktsomhetInfo);

        VilkårsvurderingPerioderDto periode = new VilkårsvurderingPerioderDto();
        periode.setFom(fom);
        periode.setTom(tom);
        periode.setVilkårResultat(vilkårResultat);
        periode.setVilkarResultatInfo(vilkårResultatAnnetDto);
        return periode;
    }

    @Test
    public void skal_mappe_perioder_med_fritekst_fra_db_til_dtoer() {

        //Arrange
        VedtaksbrevPeriode eksisterendePeriode1 = new VedtaksbrevPeriode.Builder()
            .medBehandlingId(BEHANDLING_ID)
            .medFritekstType(FritekstType.FAKTA_AVSNITT)
            .medFritekst("Fakta for 1. februar til 15. februar")
            .medPeriode(Periode.of(FEBRUAR_1_2019, FEBRUAR_15_2019))
            .build();

        VedtaksbrevPeriode eksisterendePeriode2 = new VedtaksbrevPeriode.Builder()
            .medBehandlingId(BEHANDLING_ID)
            .medFritekstType(FritekstType.VILKAAR_AVSNITT)
            .medFritekst("Vilkår for 1. februar til 15. februar")
            .medPeriode(Periode.of(FEBRUAR_1_2019, FEBRUAR_15_2019))
            .build();

        VedtaksbrevPeriode eksisterendePeriode3 = new VedtaksbrevPeriode.Builder()
            .medBehandlingId(BEHANDLING_ID)
            .medFritekstType(FritekstType.FAKTA_AVSNITT)
            .medFritekst("Fakta for 15. februar til 25. februar")
            .medPeriode(Periode.of(FEBRUAR_15_2019, MARS_15_2019))
            .build();

        //Act
        List<PeriodeMedTekstDto> perioderMedTeksterMappet = VedtaksbrevUtil.mapFritekstFraDb(List.of(eksisterendePeriode1, eksisterendePeriode2, eksisterendePeriode3));

        //Assert
        Assertions.assertThat(perioderMedTeksterMappet)
            .hasSize(2)
            .extracting("faktaAvsnitt")
            .contains("Fakta for 15. februar til 25. februar")
            .contains("Fakta for 1. februar til 15. februar");

        Assertions.assertThat(perioderMedTeksterMappet)
            .extracting("vilkårAvsnitt")
            .contains("Vilkår for 1. februar til 15. februar");
    }
}
