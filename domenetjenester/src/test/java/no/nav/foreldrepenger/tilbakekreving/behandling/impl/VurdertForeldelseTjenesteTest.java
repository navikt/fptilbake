package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeMedBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

public class VurdertForeldelseTjenesteTest extends FellesTestOppsett {

    public static final LocalDate FØRSTE_DATO = LocalDate.of(2019, 1, 31);
    private static final LocalDate FOM_1 = LocalDate.of(2016, 3, 10);
    private static final LocalDate TOM_1 = LocalDate.of(2016, 4, 06);

    @Test
    public void skal_lagreVurdertForeldelseGrunnlag() {
        LocalDate sisteDato = LocalDate.of(2019, 2, 19);
        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Collections.singletonList(
            new ForeldelsePeriodeDto(FØRSTE_DATO, sisteDato,
                ForeldelseVurderingType.FORELDET, FØRSTE_DATO.plusMonths(6), null, "ABC")));

        Optional<VurdertForeldelse> vurdertForeldelseOptional = vurdertForeldelseRepository.finnVurdertForeldelse(internBehandlingId);
        assertThat(vurdertForeldelseOptional).isPresent();
        List<VurdertForeldelsePeriode> vurdertForeldelsePerioder = Lists.newArrayList(vurdertForeldelseOptional.get().getVurdertForeldelsePerioder());
        assertThat(vurdertForeldelsePerioder).isNotEmpty();
        assertThat(vurdertForeldelsePerioder.size()).isEqualTo(1);
        assertThat(vurdertForeldelsePerioder.get(0).getForeldelseVurderingType()).isEqualByComparingTo(ForeldelseVurderingType.FORELDET);
        assertThat(vurdertForeldelsePerioder.get(0).getPeriode().getTom()).isEqualTo(LocalDate.of(2019, 2, 19));
        assertThat(vurdertForeldelsePerioder.get(0).getBegrunnelse()).isEqualTo("ABC");

        // test historikkinnslag
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert();
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(1);
        HistorikkinnslagDel historikkinnslagDel = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(getTilVerdi(historikkinnslagDel.getOpplysning(HistorikkOpplysningType.PERIODE_FOM))).isEqualTo(formatDate(FØRSTE_DATO));
        assertThat(getTilVerdi(historikkinnslagDel.getOpplysning(HistorikkOpplysningType.PERIODE_TOM))).isEqualTo(formatDate(sisteDato));
        assertThat(historikkinnslagDel.getBegrunnelse().get()).isEqualTo("ABC");
        assertThat(historikkinnslagDel.getSkjermlenke().get()).isEqualTo(SkjermlenkeType.FORELDELSE.getKode());
        assertThat(getTilVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.FORELDELSE)))
            .isEqualTo(ForeldelseVurderingType.FORELDET.getNavn());
        assertThat(getFraVerdi(historikkinnslagDel.getEndretFelt(HistorikkEndretFeltType.FORELDELSE)))
            .isEqualTo(null);
    }

    @Test
    public void skal_lagreVurdertForeldelseGrunnlag_medflereManueltPeriode() {
        LocalDate førstePeriodeSisteDato = LocalDate.of(2019, 2, 4);
        LocalDate andrePeriodeFørsteDato = LocalDate.of(2019, 2, 4);
        LocalDate andrePeriodeSisteDato = LocalDate.of(2019, 2, 11);

        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Lists.newArrayList(
            new ForeldelsePeriodeDto(FØRSTE_DATO, førstePeriodeSisteDato,
                ForeldelseVurderingType.FORELDET, FØRSTE_DATO.plusMonths(8), null, "ABC"),
            new ForeldelsePeriodeDto(andrePeriodeFørsteDato, andrePeriodeSisteDato,
                ForeldelseVurderingType.TILLEGGSFRIST, andrePeriodeFørsteDato.plusMonths(8), andrePeriodeFørsteDato.plusMonths(5), "CDE")));

        Optional<VurdertForeldelse> vurdertForeldelseOptional = vurdertForeldelseRepository.finnVurdertForeldelse(internBehandlingId);
        assertThat(vurdertForeldelseOptional).isPresent();
        List<VurdertForeldelsePeriode> vurdertForeldelsePerioder = Lists.newArrayList(vurdertForeldelseOptional.get().getVurdertForeldelsePerioder());
        assertThat(vurdertForeldelsePerioder).isNotEmpty();
        vurdertForeldelsePerioder.sort(Comparator.comparing(VurdertForeldelsePeriode::getFom));
        assertThat(vurdertForeldelsePerioder.size()).isEqualTo(2);

        assertThat(vurdertForeldelsePerioder.get(0).getForeldelseVurderingType()).isEqualByComparingTo(ForeldelseVurderingType.FORELDET);
        assertThat(vurdertForeldelsePerioder.get(0).getPeriode().getTom()).isEqualTo(LocalDate.of(2019, 2, 4));
        assertThat(vurdertForeldelsePerioder.get(0).getBegrunnelse()).isEqualTo("ABC");

        assertThat(vurdertForeldelsePerioder.get(1).getForeldelseVurderingType()).isEqualByComparingTo(ForeldelseVurderingType.TILLEGGSFRIST);
        assertThat(vurdertForeldelsePerioder.get(1).getBegrunnelse()).isEqualTo("CDE");

        // test historikkinnslag
        Historikkinnslag historikkinnslag = fellesHistorikkInnslagAssert();
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(2);

        HistorikkinnslagDel førsteDel = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(getTilVerdi(førsteDel.getOpplysning(HistorikkOpplysningType.PERIODE_FOM))).isEqualTo(formatDate(FØRSTE_DATO));
        assertThat(getTilVerdi(førsteDel.getOpplysning(HistorikkOpplysningType.PERIODE_TOM))).isEqualTo(formatDate(førstePeriodeSisteDato));
        assertThat(førsteDel.getBegrunnelse().get()).isEqualTo("ABC");
        assertThat(førsteDel.getSkjermlenke().get()).isEqualTo(SkjermlenkeType.FORELDELSE.getKode());
        assertThat(getTilVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.FORELDELSE)))
            .isEqualTo(ForeldelseVurderingType.FORELDET.getNavn());
        assertThat(getFraVerdi(førsteDel.getEndretFelt(HistorikkEndretFeltType.FORELDELSE)))
            .isEqualTo(null);

        HistorikkinnslagDel andreDel = historikkinnslag.getHistorikkinnslagDeler().get(1);
        assertThat(getTilVerdi(andreDel.getOpplysning(HistorikkOpplysningType.PERIODE_FOM))).isEqualTo(formatDate(andrePeriodeFørsteDato));
        assertThat(getTilVerdi(andreDel.getOpplysning(HistorikkOpplysningType.PERIODE_TOM))).isEqualTo(formatDate(andrePeriodeSisteDato));
        assertThat(andreDel.getBegrunnelse().get()).isEqualTo("CDE");
        assertThat(andreDel.getSkjermlenke().get()).isEqualTo(SkjermlenkeType.FORELDELSE.getKode());
        assertThat(getTilVerdi(andreDel.getEndretFelt(HistorikkEndretFeltType.FORELDELSE)))
            .isEqualTo(ForeldelseVurderingType.TILLEGGSFRIST.getNavn());
        assertThat(getFraVerdi(andreDel.getEndretFelt(HistorikkEndretFeltType.FORELDELSE)))
            .isEqualTo(null);
    }

    @Test
    public void henteVurdertForeldelse_nårForeldretPeriode_dekkerMellomToGrunnlagPeriode() {

        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM_1, LocalDate.of(2016, 3, 23), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 3, 24),
            TOM_1, KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(22000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Lists.newArrayList(
            new ForeldelsePeriodeDto(FOM_1, LocalDate.of(2016, 3, 28),
                ForeldelseVurderingType.FORELDET, FOM_1.plusYears(3), null, "ABC")));

        FeilutbetalingPerioderDto perioderDto = vurdertForeldelseTjeneste.henteVurdertForeldelse(internBehandlingId);

        assertThat(perioderDto.getPerioder().size()).isEqualTo(1);
        perioderDto.getPerioder().sort(Comparator.comparing(ForeldelsePeriodeMedBeløpDto::getFom));
        assertThat(perioderDto.getPerioder().get(0).getFom()).isEqualTo(FOM_1);
        assertThat(perioderDto.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 3, 28));
        assertThat(perioderDto.getPerioder().get(0).getForeldelseVurderingType()).isEqualTo(ForeldelseVurderingType.FORELDET);
        assertThat(perioderDto.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(13600.0));
    }

    @Test
    public void henteVurdertForeldelse_nårForeldretPeriode_dekkerMellomEnGrunnlagPeriode() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM_1, LocalDate.of(2016, 3, 23), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 3, 24),
            TOM_1, KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(22000));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2, mockMedYtelPostering));
        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag);
        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Lists.newArrayList(
            new ForeldelsePeriodeDto(FOM_1, LocalDate.of(2016, 3, 20),
                ForeldelseVurderingType.FORELDET, FOM_1.plusYears(3), null, "ABC")));

        FeilutbetalingPerioderDto perioderDto = vurdertForeldelseTjeneste.henteVurdertForeldelse(internBehandlingId);

        assertThat(perioderDto.getPerioder().size()).isEqualTo(1);
        perioderDto.getPerioder().sort(Comparator.comparing(ForeldelsePeriodeMedBeløpDto::getFom));
        assertThat(perioderDto.getPerioder().get(0).getFom()).isEqualTo(FOM_1);
        assertThat(perioderDto.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 3, 20));
        assertThat(perioderDto.getPerioder().get(0).getForeldelseVurderingType()).isEqualTo(ForeldelseVurderingType.FORELDET);
        assertThat(perioderDto.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(7000));
    }

    @Test
    public void henteVurdertForeldelse_medFlereForeldretPeriode() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM_1, LocalDate.of(2016, 3, 15), KlasseType.FEIL,
            BigDecimal.valueOf(4000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 3, 16),
            LocalDate.of(2016, 3, 24), KlasseType.FEIL, BigDecimal.valueOf(14000), BigDecimal.ZERO);

        KravgrunnlagMock mockMedFeilPostering3 = new KravgrunnlagMock(LocalDate.of(2016, 3, 26),
            LocalDate.of(2016, 4, 03), KlasseType.FEIL, BigDecimal.valueOf(5000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering4 = new KravgrunnlagMock(LocalDate.of(2016, 4, 4),
            TOM_1, KlasseType.FEIL, BigDecimal.valueOf(6000), BigDecimal.ZERO);

        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(29000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2,
            mockMedFeilPostering3, mockMedFeilPostering4, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(internBehandlingId, Lists.newArrayList(
            new ForeldelsePeriodeDto(FOM_1, LocalDate.of(2016, 3, 20),
                ForeldelseVurderingType.FORELDET, FOM_1.plusYears(3), null, "ABC"),
            new ForeldelsePeriodeDto(LocalDate.of(2016, 3, 21), LocalDate.of(2016, 3, 24),
                ForeldelseVurderingType.FORELDET, FOM_1.plusYears(3).plusMonths(2), null, "CDE"),
            new ForeldelsePeriodeDto(LocalDate.of(2016, 3, 26), TOM_1,
                ForeldelseVurderingType.TILLEGGSFRIST, FOM_1.plusYears(3).plusMonths(3), FOM_1.plusYears(2), "EFG")));

        FeilutbetalingPerioderDto perioderDto = vurdertForeldelseTjeneste.henteVurdertForeldelse(internBehandlingId);

        assertThat(perioderDto.getPerioder().size()).isEqualTo(3);
        perioderDto.getPerioder().sort(Comparator.comparing(ForeldelsePeriodeMedBeløpDto::getFom));
        assertThat(perioderDto.getPerioder().get(0).getFom()).isEqualTo(FOM_1);
        assertThat(perioderDto.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 3, 20));
        assertThat(perioderDto.getPerioder().get(0).getForeldelseVurderingType()).isEqualTo(ForeldelseVurderingType.FORELDET);
        assertThat(perioderDto.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(10000));

        assertThat(perioderDto.getPerioder().get(1).getFom()).isEqualTo(LocalDate.of(2016, 3, 21));
        assertThat(perioderDto.getPerioder().get(1).getTom()).isEqualTo(LocalDate.of(2016, 3, 24));
        assertThat(perioderDto.getPerioder().get(1).getForeldelseVurderingType()).isEqualTo(ForeldelseVurderingType.FORELDET);
        assertThat(perioderDto.getPerioder().get(1).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(8000));

        assertThat(perioderDto.getPerioder().get(2).getFom()).isEqualTo(LocalDate.of(2016, 3, 26));
        assertThat(perioderDto.getPerioder().get(2).getTom()).isEqualTo(TOM_1);
        assertThat(perioderDto.getPerioder().get(2).getForeldelseVurderingType()).isEqualTo(ForeldelseVurderingType.TILLEGGSFRIST);
        assertThat(perioderDto.getPerioder().get(2).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(11000));
    }

    @Test
    public void hentInitPerioder() {

        LocalDate sisteDagFørstePeriode = LocalDate.of(2016, 3, 28);
        LocalDate førsteDagAndrePeriode = LocalDate.of(2016, 3, 30);

        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM_1, LocalDate.of(2016, 3, 23), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(LocalDate.of(2016, 3, 24),
            sisteDagFørstePeriode, KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = new KravgrunnlagMock(førsteDagAndrePeriode,
            TOM_1, KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);

        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2, mockMedFeilPostering3, mockMedYtelPostering));
        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);

        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();

        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(lagPeriode(FOM_1, sisteDagFørstePeriode, HendelseType.FP_UTTAK_UTSETTELSE_TYPE, HendelseUnderType.ARBEID_HELTID, faktaFeilutbetaling));
        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(lagPeriode(førsteDagAndrePeriode, TOM_1, HendelseType.FP_UTTAK_UTSETTELSE_TYPE, HendelseUnderType.ARBEID_HELTID, faktaFeilutbetaling));
        faktaFeilutbetaling.setBegrunnelse("begrunnelse");

        faktaFeilutbetalingRepository.lagre(internBehandlingId, faktaFeilutbetaling);

        FeilutbetalingPerioderDto feilutbetalingPerioder = vurdertForeldelseTjeneste.hentFaktaPerioder(internBehandlingId);
        assertThat(feilutbetalingPerioder.getPerioder().size()).isEqualTo(2);

        List<ForeldelsePeriodeMedBeløpDto> perioder = feilutbetalingPerioder.getPerioder();
        perioder.sort(Comparator.comparing(ForeldelsePeriodeMedBeløpDto::getFom));

        assertThat(perioder.get(0).getBelop()).isEqualTo(BigDecimal.valueOf(22000));
        assertThat(perioder.get(0).getFom()).isEqualTo(FOM_1);
        assertThat(perioder.get(0).getTom()).isEqualTo(sisteDagFørstePeriode);

        assertThat(perioder.get(1).getBelop()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(perioder.get(1).getFom()).isEqualTo(førsteDagAndrePeriode);
        assertThat(perioder.get(1).getTom()).isEqualTo(TOM_1);
    }

    private FaktaFeilutbetalingPeriode lagPeriode(LocalDate fom, LocalDate tom, HendelseType årsak, HendelseUnderType underårsak, FaktaFeilutbetaling faktaFeilutbetaling) {
        return FaktaFeilutbetalingPeriode.builder()
            .medPeriode(fom, tom)
            .medHendelseType(årsak)
            .medHendelseUndertype(underårsak)
            .medFeilutbetalinger(faktaFeilutbetaling).build();
    }

    private Historikkinnslag fellesHistorikkInnslagAssert() {
        List<Historikkinnslag> historikkInnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        assertThat(historikkInnslager).isNotEmpty();
        assertThat(historikkInnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkInnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.FORELDELSE);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getBehandlingId()).isEqualTo(internBehandlingId);
        return historikkinnslag;
    }

}
