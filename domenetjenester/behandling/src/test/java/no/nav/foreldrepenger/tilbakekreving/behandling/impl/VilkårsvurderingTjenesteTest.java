package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.RedusertBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.YtelseDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatGodTroDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

public class VilkårsvurderingTjenesteTest extends FellesTestOppsett {

    private static final String AKTIVITET_FISKER = "Fisker";
    private static final String AKTIVITET_ARBEIDSLEDIG = "Arbeidsledig";
    private static final String AKTIVITET_SJØMANN = "Sjømann";
    private static final LocalDate SISTE_DAG_I_FORELDELSE_PERIODE = LocalDate.of(2016, 4, 28);
    private static final LocalDate FØRSTE_DAG_I_FORELDELSE_PERIODE = LocalDate.of(2016, 4, 29);

    @Test
    public void hentDetaljertFeilutbetalingPerioder_nårPerioderErForeldet() {
        formGrunnlag();
        lagreFaktaTestdata();

        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Lists.newArrayList(
            new ForeldelsePeriodeDto(FOM, SISTE_DAG_I_FORELDELSE_PERIODE,
                ForeldelseVurderingType.FORELDET, "ABC"),
            new ForeldelsePeriodeDto(FØRSTE_DAG_I_FORELDELSE_PERIODE, TOM,
                ForeldelseVurderingType.IKKE_FORELDET, "CDE")));

        List<DetaljertFeilutbetalingPeriodeDto> perioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(internBehandlingId);
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(2);
        perioder.sort(Comparator.comparing(DetaljertFeilutbetalingPeriodeDto::getFom));

        DetaljertFeilutbetalingPeriodeDto førstePeriode = perioder.get(0);
        assertThat(førstePeriode.getOppfyltValg()).isEqualByComparingTo(VilkårResultat.UDEFINERT);
        assertThat(førstePeriode.getFom()).isEqualTo(FOM);
        assertThat(førstePeriode.getTom()).isEqualTo(SISTE_DAG_I_FORELDELSE_PERIODE);
        assertThat(førstePeriode.getHendelseType()).isEqualTo(HendelseType.FP_UTTAK_UTSETTELSE_TYPE);
        assertThat(førstePeriode.getHendelseUndertype()).isEqualTo(FpHendelseUnderTyper.ARBEID_HELTID);
        assertThat(førstePeriode.getFeilutbetaling()).isEqualByComparingTo(BigDecimal.valueOf(31000));
        assertThat(førstePeriode.isForeldet()).isTrue();

        assertThat(førstePeriode.getYtelser().size()).isEqualTo(2);
        førstePeriode.getYtelser().sort(Comparator.comparing(YtelseDto::getAktivitet));
        assertThat(førstePeriode.getYtelser().get(0).getAktivitet()).isEqualTo(AKTIVITET_ARBEIDSLEDIG);
        assertThat(førstePeriode.getYtelser().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(førstePeriode.getYtelser().get(1).getAktivitet()).isEqualTo(AKTIVITET_FISKER);
        assertThat(førstePeriode.getYtelser().get(1).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(førstePeriode.getRedusertBeloper().size()).isEqualTo(0);

        DetaljertFeilutbetalingPeriodeDto andrePeriode = perioder.get(1);
        assertThat(andrePeriode.getOppfyltValg()).isEqualByComparingTo(VilkårResultat.UDEFINERT);
        assertThat(andrePeriode.getFom()).isEqualTo(FØRSTE_DAG_I_FORELDELSE_PERIODE);
        assertThat(andrePeriode.getTom()).isEqualTo(TOM);
        assertThat(førstePeriode.getHendelseType()).isEqualTo(HendelseType.FP_UTTAK_UTSETTELSE_TYPE);
        assertThat(førstePeriode.getHendelseUndertype()).isEqualTo(FpHendelseUnderTyper.ARBEID_HELTID);
        assertThat(andrePeriode.getFeilutbetaling()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(andrePeriode.isForeldet()).isFalse();

        assertThat(andrePeriode.getYtelser().size()).isEqualTo(2);
        andrePeriode.getYtelser().sort(Comparator.comparing(YtelseDto::getAktivitet));
        assertThat(andrePeriode.getYtelser().get(0).getAktivitet()).isEqualTo(AKTIVITET_FISKER);
        assertThat(andrePeriode.getYtelser().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(andrePeriode.getYtelser().get(1).getAktivitet()).isEqualTo(AKTIVITET_SJØMANN);
        assertThat(andrePeriode.getYtelser().get(1).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(19000));
        assertThat(andrePeriode.getRedusertBeloper().size()).isEqualTo(0);
    }

    @Test
    public void hentDetaljertFeilutbetalingPerioder_nårPerioderErIkkeVurderesForForeldet() {
        formGrunnlag();
        lagreFaktaTestdata();

        List<DetaljertFeilutbetalingPeriodeDto> perioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(internBehandlingId);
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        DetaljertFeilutbetalingPeriodeDto periode = perioder.get(0);
        assertThat(periode.getFom()).isEqualTo(FOM);
        assertThat(periode.getTom()).isEqualTo(TOM);
        assertThat(periode.getOppfyltValg()).isEqualByComparingTo(VilkårResultat.UDEFINERT);
        assertThat(periode.getFeilutbetaling()).isEqualByComparingTo(BigDecimal.valueOf(51000));
        assertThat(periode.getHendelseType()).isEqualTo(HendelseType.FP_UTTAK_UTSETTELSE_TYPE);
        assertThat(periode.getHendelseUndertype()).isEqualTo(FpHendelseUnderTyper.ARBEID_HELTID);
        assertThat(periode.isForeldet()).isFalse();

        assertThat(periode.getYtelser().size()).isEqualTo(3);
        periode.getYtelser().sort(Comparator.comparing(YtelseDto::getAktivitet));
        assertThat(periode.getYtelser().get(0).getAktivitet()).isEqualTo(AKTIVITET_ARBEIDSLEDIG);
        assertThat(periode.getYtelser().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(periode.getYtelser().get(1).getAktivitet()).isEqualTo(AKTIVITET_FISKER);
        assertThat(periode.getYtelser().get(1).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(21000));
        assertThat(periode.getYtelser().get(2).getAktivitet()).isEqualTo(AKTIVITET_SJØMANN);
        assertThat(periode.getYtelser().get(2).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(19000));
        assertThat(periode.getRedusertBeloper().size()).isEqualTo(0);
    }

    @Test
    public void hentDetaljertFeilutbetalingPerioder_nårPerioderErIkkeVurderesForForeldet_medRedusertBeløp() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31), KlasseType.FEIL,
            BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);
        KravgrunnlagMock mockMedJustPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31),
            KlasseType.JUST, BigDecimal.valueOf(5000), BigDecimal.ZERO);
        mockMedJustPostering.setKlasseKode(KlasseKode.FPADATAL);

        KravgrunnlagMock mockMedFeilPostering1 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30), KlasseType.FEIL,
            BigDecimal.valueOf(21000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering1 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(21000));
        mockMedYtelPostering1.setKlasseKode(KlasseKode.FPADSNDFI);
        KravgrunnlagMock mockMedYtelPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30),
            KlasseType.YTEL, BigDecimal.valueOf(3000), BigDecimal.ZERO);
        mockMedYtelPostering2.setKlasseKode(KlasseKode.FPADSNDFI);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering1,
            mockMedYtelPostering, mockMedYtelPostering1, mockMedYtelPostering2, mockMedJustPostering));
        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);

        lagreFaktaTestdata();

        List<DetaljertFeilutbetalingPeriodeDto> perioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(internBehandlingId);
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        DetaljertFeilutbetalingPeriodeDto periode = perioder.get(0);
        assertThat(periode.getFeilutbetaling()).isEqualByComparingTo(BigDecimal.valueOf(32000));
        assertThat(periode.getHendelseType()).isEqualTo(HendelseType.FP_UTTAK_UTSETTELSE_TYPE);
        assertThat(periode.getHendelseUndertype()).isEqualTo(FpHendelseUnderTyper.ARBEID_HELTID);
        assertThat(periode.isForeldet()).isFalse();

        assertThat(periode.getRedusertBeloper().size()).isEqualTo(1);
        periode.getRedusertBeloper().sort(Comparator.comparing(RedusertBeløpDto::getBelop));

        RedusertBeløpDto førsteRedusertBeløp = periode.getRedusertBeloper().get(0);
        assertThat(førsteRedusertBeløp.getBelop()).isEqualByComparingTo(BigDecimal.valueOf(5000l));
        assertThat(førsteRedusertBeløp.isErTrekk()).isEqualTo(false);
    }

    @Test
    public void hentDetaljertFeilutbetalingPerioder_medFlereYtelserMedSammeInntekstKategori() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31), KlasseType.FEIL,
            BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        KravgrunnlagMock mockMedFeilPostering1 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30), KlasseType.FEIL,
            BigDecimal.valueOf(21000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering1 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(21000));
        mockMedYtelPostering1.setKlasseKode(KlasseKode.FPADATAL);
        KravgrunnlagMock mockMedYtelPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30),
            KlasseType.YTEL, BigDecimal.valueOf(3000), BigDecimal.ZERO);
        mockMedYtelPostering2.setKlasseKode(KlasseKode.FPADATAL);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering1,
            mockMedYtelPostering, mockMedYtelPostering1, mockMedYtelPostering2));
        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);

        lagreFaktaTestdata();

        List<DetaljertFeilutbetalingPeriodeDto> perioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(internBehandlingId);
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        DetaljertFeilutbetalingPeriodeDto periode = perioder.get(0);
        assertThat(periode.getYtelser().size()).isEqualTo(1);
        YtelseDto ytelseDto = periode.getYtelser().get(0);
        assertThat(ytelseDto.getAktivitet()).isEqualTo(kodeverkRepository.finn(Inntektskategori.class, Inntektskategori.ARBEIDSLEDIG).getNavn());
        assertThat(ytelseDto.getBelop()).isEqualTo(BigDecimal.valueOf(32000));
    }


    @Test
    public void lagreVilkårsvurdering_medGodTroOgForsettAktsomhet() {
        List<VilkårsvurderingPerioderDto> vilkårPerioder = Lists.newArrayList(
            formVilkårsvurderingPerioderDto(VilkårResultat.GOD_TRO, FOM, LocalDate.of(2016, 3, 31), null),
            formVilkårsvurderingPerioderDto(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, LocalDate.of(2016, 4, 1), TOM, Aktsomhet.FORSETT));
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        Optional<VilkårVurderingEntitet> aggregateEntitet = repoProvider.getVilkårsvurderingRepository().finnVilkårsvurdering(internBehandlingId);
        assertThat(aggregateEntitet).isNotEmpty();
        VilkårVurderingEntitet vilkårEntitet = aggregateEntitet.get();
        List<VilkårVurderingPeriodeEntitet> periodene = new ArrayList<>(vilkårEntitet.getPerioder());
        assertThat(periodene.size()).isEqualTo(2);
        periodene.sort(PERIODE_FOM_COMPARATOR);

        VilkårVurderingPeriodeEntitet førstePeriode = periodene.get(0);
        assertThat(førstePeriode.getPeriode()).isEqualTo(Periode.of(FOM, LocalDate.of(2016, 3, 31)));
        assertThat(førstePeriode.getAktsomhet()).isNull();
        assertThat(førstePeriode.getGodTro().isBeløpErIBehold()).isTrue();
        assertThat(førstePeriode.getGodTro().getBeløpTilbakekreves()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));

        VilkårVurderingPeriodeEntitet andrePeriode = periodene.get(1);
        assertThat(andrePeriode.getPeriode()).isEqualTo(Periode.of(LocalDate.of(2016, 4, 1), TOM));
        assertThat(andrePeriode.getGodTro()).isNull();
        assertThat(andrePeriode.getAktsomhet().getAktsomhet()).isEqualByComparingTo(Aktsomhet.FORSETT);
    }

    @Test
    public void lagreVilkårsvurdering_medSimpelOgGrøvtAktsomhet() {
        List<VilkårsvurderingPerioderDto> vilkårPerioder = Lists.newArrayList(
            formVilkårsvurderingPerioderDto(VilkårResultat.FORSTO_BURDE_FORSTÅTT, FOM, LocalDate.of(2016, 3, 31), Aktsomhet.SIMPEL_UAKTSOM),
            formVilkårsvurderingPerioderDto(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER, LocalDate.of(2016, 4, 1), TOM, Aktsomhet.GROVT_UAKTSOM));

        vilkårPerioder.stream()
            .filter(perioderDto -> VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER.equals(perioderDto.getVilkårResultat()))
            .map(perioderDto -> (VilkårResultatAnnetDto) perioderDto.getVilkarResultatInfo())
            .map(VilkårResultatAnnetDto::getAktsomhetInfo).forEach(aktsomhetDto -> {
            aktsomhetDto.setHarGrunnerTilReduksjon(true);
            aktsomhetDto.setAndelTilbakekreves(BigDecimal.TEN);
        });

        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        Optional<VilkårVurderingEntitet> aggregateEntitet = repoProvider.getVilkårsvurderingRepository().finnVilkårsvurdering(internBehandlingId);
        assertThat(aggregateEntitet).isNotEmpty();
        VilkårVurderingEntitet vilkårEntitet = aggregateEntitet.get();
        List<VilkårVurderingPeriodeEntitet> periodene = new ArrayList<>(vilkårEntitet.getPerioder());
        periodene.sort(PERIODE_FOM_COMPARATOR);

        assertThat(periodene.size()).isEqualTo(2);

        VilkårVurderingPeriodeEntitet førstePeriode = periodene.get(0);
        assertThat(førstePeriode.getPeriode()).isEqualTo(Periode.of(FOM, LocalDate.of(2016, 3, 31)));
        assertThat(førstePeriode.getGodTro()).isNull();
        assertThat(førstePeriode.getAktsomhet().getAktsomhet()).isEqualByComparingTo(Aktsomhet.SIMPEL_UAKTSOM);
        assertThat(førstePeriode.getAktsomhet().getManueltTilbakekrevesBeløp()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        assertThat(førstePeriode.getAktsomhet().getSærligGrunnerTilReduksjon()).isTrue();
        assertThat(førstePeriode.getAktsomhet().getSærligGrunner().size()).isEqualTo(2);

        VilkårVurderingPeriodeEntitet andrePeriode = periodene.get(1);
        assertThat(andrePeriode.getPeriode()).isEqualTo(Periode.of(LocalDate.of(2016, 4, 1), TOM));
        assertThat(andrePeriode.getGodTro()).isNull();
        assertThat(andrePeriode.getAktsomhet().getAktsomhet()).isEqualByComparingTo(Aktsomhet.GROVT_UAKTSOM);
        assertThat(andrePeriode.getAktsomhet().getSærligGrunnerTilReduksjon()).isTrue();
        assertThat(andrePeriode.getAktsomhet().getProsenterSomTilbakekreves()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(andrePeriode.getAktsomhet().getIleggRenter()).isTrue();
        assertThat(andrePeriode.getAktsomhet().getSærligGrunner().size()).isEqualTo(2);
    }

    @Test
    public void lagreVilkårsvurdering_medSimpelOgGrøvtAktsomhet_nårEnPeriodeErForeldet() {
        formGrunnlag();
        lagreFaktaTestdata();

        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Lists.newArrayList(
            new ForeldelsePeriodeDto(FOM, SISTE_DAG_I_FORELDELSE_PERIODE,
                ForeldelseVurderingType.FORELDET, "ABC"),
            new ForeldelsePeriodeDto(FØRSTE_DAG_I_FORELDELSE_PERIODE, TOM,
                ForeldelseVurderingType.IKKE_FORELDET, "CDE")));

        List<VilkårsvurderingPerioderDto> vilkårPerioder = Lists.newArrayList(
            formVilkårsvurderingPerioderDto(VilkårResultat.FORSTO_BURDE_FORSTÅTT, FOM, SISTE_DAG_I_FORELDELSE_PERIODE, Aktsomhet.SIMPEL_UAKTSOM),
            formVilkårsvurderingPerioderDto(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER, FØRSTE_DAG_I_FORELDELSE_PERIODE, TOM, Aktsomhet.GROVT_UAKTSOM));
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        Optional<VilkårVurderingEntitet> aggregateEntitet = repoProvider.getVilkårsvurderingRepository().finnVilkårsvurdering(internBehandlingId);
        assertThat(aggregateEntitet).isNotEmpty();
        VilkårVurderingEntitet vilkårEntitet = aggregateEntitet.get();
        assertThat(vilkårEntitet.getPerioder().size()).isEqualTo(1);

        VilkårVurderingPeriodeEntitet periode = vilkårEntitet.getPerioder().get(0);
        assertThat(periode.getPeriode()).isEqualTo(Periode.of(FØRSTE_DAG_I_FORELDELSE_PERIODE, TOM));
        assertThat(periode.getGodTro()).isNull();
        assertThat(periode.getAktsomhet().getAktsomhet()).isEqualByComparingTo(Aktsomhet.GROVT_UAKTSOM);
        assertThat(periode.getAktsomhet().getSærligGrunnerTilReduksjon()).isFalse();
        assertThat(periode.getAktsomhet().getProsenterSomTilbakekreves()).isNull();
        assertThat(periode.getAktsomhet().getIleggRenter()).isTrue();
        assertThat(periode.getAktsomhet().getSærligGrunner().size()).isEqualTo(2);
    }

    @Test
    public void hentVilkårsvurdering_medGodTroOgForsettAktsomhet() {
        List<VilkårsvurderingPerioderDto> vilkårPerioder = Lists.newArrayList(
            formVilkårsvurderingPerioderDto(VilkårResultat.GOD_TRO, FOM, LocalDate.of(2016, 3, 31), null),
            formVilkårsvurderingPerioderDto(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, LocalDate.of(2016, 4, 1), TOM, Aktsomhet.FORSETT));
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        formGrunnlag();

        List<VilkårsvurderingPerioderDto> perioder = vilkårsvurderingTjeneste.hentVilkårsvurdering(internBehandlingId);
        assertThat(perioder.size()).isEqualTo(2);
        perioder.sort(Comparator.comparing(VilkårsvurderingPerioderDto::getFom));

        VilkårsvurderingPerioderDto førstePeriode = perioder.get(0);
        assertThat(førstePeriode.getFom()).isEqualTo(FOM);
        assertThat(førstePeriode.getTom()).isEqualTo(LocalDate.of(2016, 3, 31));
        assertThat(førstePeriode.getVilkårResultat()).isEqualByComparingTo(VilkårResultat.GOD_TRO);
        assertThat(førstePeriode.getFeilutbetalingBelop()).isEqualByComparingTo(BigDecimal.valueOf(11000.00));

        VilkårResultatGodTroDto godTroDto = (VilkårResultatGodTroDto) førstePeriode.getVilkarResultatInfo();
        assertThat(godTroDto.getErBelopetIBehold()).isTrue();
        assertThat(godTroDto.getTilbakekrevesBelop()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));

        VilkårsvurderingPerioderDto andrePeriode = perioder.get(1);
        assertThat(andrePeriode.getFom()).isEqualTo(LocalDate.of(2016, 4, 1));
        assertThat(andrePeriode.getTom()).isEqualTo(TOM);
        assertThat(andrePeriode.getVilkårResultat()).isEqualByComparingTo(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER);
        assertThat(andrePeriode.getFeilutbetalingBelop()).isEqualByComparingTo(BigDecimal.valueOf(40000.00));

        VilkårResultatAnnetDto annetDto = (VilkårResultatAnnetDto) andrePeriode.getVilkarResultatInfo();
        assertThat(annetDto.getAktsomhet()).isEqualByComparingTo(Aktsomhet.FORSETT);
        assertThat(annetDto.getAktsomhetInfo()).isNotNull();
        assertThat(annetDto.getAktsomhetInfo().isHarGrunnerTilReduksjon()).isFalse();
        assertThat(annetDto.getAktsomhetInfo().getAndelTilbakekreves()).isNull();
        assertThat(annetDto.getAktsomhetInfo().isIleggRenter()).isNull();
        assertThat(annetDto.getAktsomhetInfo().getSærligeGrunner().size()).isEqualTo(0);
    }

    @Test
    public void hentVilkårsvurdering_medSimpelOgGrøvtAktsomhet() {
        List<VilkårsvurderingPerioderDto> vilkårPerioder = Lists.newArrayList(
            formVilkårsvurderingPerioderDto(VilkårResultat.FORSTO_BURDE_FORSTÅTT, FOM, LocalDate.of(2016, 3, 31), Aktsomhet.SIMPEL_UAKTSOM),
            formVilkårsvurderingPerioderDto(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER, LocalDate.of(2016, 4, 1), TOM, Aktsomhet.GROVT_UAKTSOM));
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        formGrunnlag();

        List<VilkårsvurderingPerioderDto> perioder = vilkårsvurderingTjeneste.hentVilkårsvurdering(internBehandlingId);
        assertThat(perioder.size()).isEqualTo(2);
        perioder.sort(Comparator.comparing(VilkårsvurderingPerioderDto::getFom));

        VilkårsvurderingPerioderDto førstePeriode = perioder.get(0);
        assertThat(førstePeriode.getFom()).isEqualTo(FOM);
        assertThat(førstePeriode.getTom()).isEqualTo(LocalDate.of(2016, 3, 31));
        assertThat(førstePeriode.getVilkårResultat()).isEqualByComparingTo(VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        assertThat(førstePeriode.getFeilutbetalingBelop()).isEqualByComparingTo(BigDecimal.valueOf(11000.00));

        VilkårResultatAnnetDto annetDto = (VilkårResultatAnnetDto) førstePeriode.getVilkarResultatInfo();
        assertThat(annetDto.getAktsomhet()).isEqualByComparingTo(Aktsomhet.SIMPEL_UAKTSOM);
        assertThat(annetDto.getAktsomhetInfo().getTilbakekrevesBelop()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        assertThat(annetDto.getAktsomhetInfo().isHarGrunnerTilReduksjon()).isTrue();
        assertThat(annetDto.getAktsomhetInfo().getSærligeGrunner().size()).isEqualTo(2);

        VilkårsvurderingPerioderDto andrePeriode = perioder.get(1);
        assertThat(andrePeriode.getFom()).isEqualTo(LocalDate.of(2016, 4, 1));
        assertThat(andrePeriode.getTom()).isEqualTo(TOM);
        assertThat(andrePeriode.getVilkårResultat()).isEqualByComparingTo(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER);
        assertThat(andrePeriode.getFeilutbetalingBelop()).isEqualByComparingTo(BigDecimal.valueOf(40000.00));

        annetDto = (VilkårResultatAnnetDto) andrePeriode.getVilkarResultatInfo();
        assertThat(annetDto.getAktsomhet()).isEqualByComparingTo(Aktsomhet.GROVT_UAKTSOM);
        assertThat(annetDto.getAktsomhetInfo().isHarGrunnerTilReduksjon()).isFalse();
        assertThat(annetDto.getAktsomhetInfo().getAndelTilbakekreves()).isNull();
        assertThat(annetDto.getAktsomhetInfo().isIleggRenter()).isTrue();
        assertThat(annetDto.getAktsomhetInfo().getSærligeGrunner().size()).isEqualTo(2);
    }

    @Test
    public void hentVilkårsvurdering_medForstBurdeForstattOgForsettAktsomhet_skalTilleggesRenter() {
        VilkårsvurderingPerioderDto vilkårsvurderingPerioderDto = formVilkårsvurderingPerioderDto(VilkårResultat.FORSTO_BURDE_FORSTÅTT, LocalDate.of(2016, 4, 1), TOM, Aktsomhet.FORSETT);
        VilkårResultatAnnetDto vilkarResultatInfo = (VilkårResultatAnnetDto) vilkårsvurderingPerioderDto.getVilkarResultatInfo();
        vilkarResultatInfo.getAktsomhetInfo().setIleggRenter(true);

        List<VilkårsvurderingPerioderDto> vilkårPerioder = Collections.singletonList(vilkårsvurderingPerioderDto);

        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        formGrunnlag();

        List<VilkårsvurderingPerioderDto> perioder = vilkårsvurderingTjeneste.hentVilkårsvurdering(internBehandlingId);
        assertThat(perioder.size()).isEqualTo(1);
        perioder.sort(Comparator.comparing(VilkårsvurderingPerioderDto::getFom));

        VilkårsvurderingPerioderDto andrePeriode = perioder.get(0);
        assertThat(andrePeriode.getVilkårResultat()).isEqualByComparingTo(VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        assertThat(andrePeriode.getFeilutbetalingBelop()).isEqualByComparingTo(BigDecimal.valueOf(40000.00));

        VilkårResultatAnnetDto annetDto = (VilkårResultatAnnetDto) andrePeriode.getVilkarResultatInfo();
        assertThat(annetDto.getAktsomhet()).isEqualByComparingTo(Aktsomhet.FORSETT);
        assertThat(annetDto.getAktsomhetInfo().isIleggRenter()).isNotNull();
        assertThat(annetDto.getAktsomhetInfo().isIleggRenter()).isTrue();
    }

    @Test
    public void hentVilkårsvurdering_medForstBurdeForstattOgForsettAktsomhet_skalIkkeTilleggesRenter() {
        VilkårsvurderingPerioderDto vilkårsvurderingPerioderDto = formVilkårsvurderingPerioderDto(VilkårResultat.FORSTO_BURDE_FORSTÅTT, LocalDate.of(2016, 4, 1), TOM, Aktsomhet.FORSETT);
        VilkårResultatAnnetDto vilkarResultatInfo = (VilkårResultatAnnetDto) vilkårsvurderingPerioderDto.getVilkarResultatInfo();
        vilkarResultatInfo.getAktsomhetInfo().setIleggRenter(false);

        List<VilkårsvurderingPerioderDto> vilkårPerioder = Collections.singletonList(vilkårsvurderingPerioderDto);

        vilkårsvurderingTjeneste.lagreVilkårsvurdering(internBehandlingId, vilkårPerioder);

        formGrunnlag();

        List<VilkårsvurderingPerioderDto> perioder = vilkårsvurderingTjeneste.hentVilkårsvurdering(internBehandlingId);
        assertThat(perioder.size()).isEqualTo(1);
        perioder.sort(Comparator.comparing(VilkårsvurderingPerioderDto::getFom));

        VilkårsvurderingPerioderDto andrePeriode = perioder.get(0);
        assertThat(andrePeriode.getVilkårResultat()).isEqualByComparingTo(VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        assertThat(andrePeriode.getFeilutbetalingBelop()).isEqualByComparingTo(BigDecimal.valueOf(40000.00));

        VilkårResultatAnnetDto annetDto = (VilkårResultatAnnetDto) andrePeriode.getVilkarResultatInfo();
        assertThat(annetDto.getAktsomhet()).isEqualByComparingTo(Aktsomhet.FORSETT);
        assertThat(annetDto.getAktsomhetInfo().isIleggRenter()).isNotNull();
        assertThat(annetDto.getAktsomhetInfo().isIleggRenter()).isFalse();
    }

    private void formGrunnlag() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31), KlasseType.FEIL,
            BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        KravgrunnlagMock mockMedFeilPostering1 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30), KlasseType.FEIL,
            BigDecimal.valueOf(21000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering1 = new KravgrunnlagMock(LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 30),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(21000));
        mockMedYtelPostering1.setKlasseKode(KlasseKode.FPADSNDFI);

        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 5, 1), TOM, KlasseType.FEIL,
            BigDecimal.valueOf(19000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 5, 1), TOM,
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(19000));
        mockMedYtelPostering2.setKlasseKode(KlasseKode.FPADATSJO);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering1, mockMedFeilPostering2,
            mockMedYtelPostering, mockMedYtelPostering1, mockMedYtelPostering2));
        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
    }

    private void lagreFaktaTestdata() {
        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();
        FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder()
            .medPeriode(FOM, TOM)
            .medHendelseType(HendelseType.FP_UTTAK_UTSETTELSE_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.ARBEID_HELTID)
            .medFeilutbetalinger(faktaFeilutbetaling)
            .build();
        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);
        faktaFeilutbetaling.setBegrunnelse("begrunnelse");

        faktaFeilutbetalingRepository.lagre(internBehandlingId, faktaFeilutbetaling);
    }

    private static Comparator<VilkårVurderingPeriodeEntitet> PERIODE_FOM_COMPARATOR = Comparator.comparing(o -> o.getPeriode().getFom());
}
