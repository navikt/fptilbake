package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.UtvidetVilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakPeriode;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class VedtakOppsummeringTjenesteTest {

    private static final String ANSVARLIG_SAKSBEHANDLER = "Z13456";
    private static final String ANSVARLIG_BESLUTTER = "Z12456";

    private BehandlingRepositoryProvider repositoryProvider;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VurdertForeldelseRepository foreldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;

    private VedtakOppsummeringTjeneste vedtakOppsummeringTjeneste;

    private long behandlingId;
    private Behandling behandling;
    private final Periode periode = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 31));
    private Saksnummer saksnummer;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        EksternBehandlingRepository eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        foreldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste = new KravgrunnlagBeregningTjeneste(
            kravgrunnlagRepository);
        TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste = new TilbakekrevingBeregningTjeneste(
            repositoryProvider, kravgrunnlagBeregningTjeneste);
        vedtakOppsummeringTjeneste = new VedtakOppsummeringTjeneste(repositoryProvider, tilbakekrevingBeregningTjeneste);

        entityManager.setFlushMode(FlushModeType.AUTO);
        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);
        behandling.setAnsvarligBeslutter(ANSVARLIG_BESLUTTER);
        behandling.setBehandlendeEnhetId("8020");
        entityManager.persist(behandling);

        behandlingId = behandling.getId();
        saksnummer = behandling.getFagsak().getSaksnummer();
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
        lagKravgrunnlag();
        lagFakta();
    }

    @Test
    public void skal_hente_vedtak_oppsummering_for_foreldelse_perioder() {
        lagForeldelse();
        lagBehandlingVedtak();
        VedtakOppsummering vedtakOppsummering = vedtakOppsummeringTjeneste.hentVedtakOppsummering(behandlingId);
        fellesAssertVedtakOppsummering(vedtakOppsummering);
        List<VedtakPeriode> vedtakPerioder = vedtakOppsummering.getPerioder();
        VedtakPeriode vedtakPeriode = fellesAssertVedtakPeriode(vedtakPerioder);
        assertThat(vedtakPeriode.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(vedtakPeriode.getRenterBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vedtakPeriode.getTilbakekrevesBruttoBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vedtakPeriode.getAktsomhet()).isNull();
        assertThat(vedtakPeriode.getVilkårResultat()).isEqualByComparingTo(UtvidetVilkårResultat.FORELDET);
        assertThat(vedtakPeriode.isHarBruktSjetteLedd()).isFalse();
        assertThat(vedtakPeriode.getSærligeGrunner()).isNull();
    }

    @Test
    public void skal_hente_vedtak_oppsummering_for_perioder_med_god_tro() {
        lagVilkårMedGodTro();
        lagBehandlingVedtak();
        VedtakOppsummering vedtakOppsummering = vedtakOppsummeringTjeneste.hentVedtakOppsummering(behandlingId);
        fellesAssertVedtakOppsummering(vedtakOppsummering);
        List<VedtakPeriode> vedtakPerioder = vedtakOppsummering.getPerioder();
        VedtakPeriode vedtakPeriode = fellesAssertVedtakPeriode(vedtakPerioder);
        assertThat(vedtakPeriode.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(vedtakPeriode.getRenterBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vedtakPeriode.getTilbakekrevesBruttoBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(vedtakPeriode.getAktsomhet()).isNull();
        assertThat(vedtakPeriode.getVilkårResultat()).isEqualByComparingTo(UtvidetVilkårResultat.GOD_TRO);
        assertThat(vedtakPeriode.isHarBruktSjetteLedd()).isFalse();
        assertThat(vedtakPeriode.getSærligeGrunner()).isNull();
    }

    @Test
    public void skal_hente_vedtak_oppsummering_for_perioder_med_aktsomhet() {
        lagVilkårMedAktsomhet();
        lagBehandlingVedtak();
        VedtakOppsummering vedtakOppsummering = vedtakOppsummeringTjeneste.hentVedtakOppsummering(behandlingId);
        fellesAssertVedtakOppsummering(vedtakOppsummering);
        List<VedtakPeriode> vedtakPerioder = vedtakOppsummering.getPerioder();
        VedtakPeriode vedtakPeriode = fellesAssertVedtakPeriode(vedtakPerioder);
        assertThat(vedtakPeriode.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(vedtakPeriode.getRenterBeløp()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(vedtakPeriode.getTilbakekrevesBruttoBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(vedtakPeriode.getAktsomhet()).isEqualByComparingTo(no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.Aktsomhet.SIMPEL_UAKTSOM);
        assertThat(vedtakPeriode.getVilkårResultat()).isEqualByComparingTo(UtvidetVilkårResultat.FORSTO_BURDE_FORSTAATT);
        assertThat(vedtakPeriode.isHarBruktSjetteLedd()).isFalse();
        assertThat(vedtakPeriode.getSærligeGrunner()).isNotNull();
        assertThat(vedtakPeriode.getSærligeGrunner().isErSærligeGrunnerTilReduksjon()).isFalse();
        assertThat(vedtakPeriode.getSærligeGrunner().getSærligeGrunner()).isNotEmpty();
    }

    private void fellesAssertVedtakOppsummering(VedtakOppsummering vedtakOppsummering) {
        assertThat(vedtakOppsummering.getBehandlingUuid()).isNotNull();
        assertThat(vedtakOppsummering.getAnsvarligBeslutter()).isEqualTo(ANSVARLIG_BESLUTTER);
        assertThat(vedtakOppsummering.getAnsvarligSaksbehandler()).isEqualTo(ANSVARLIG_SAKSBEHANDLER);
        assertThat(vedtakOppsummering.getBehandlendeEnhetKode()).isNotEmpty();
        assertThat(vedtakOppsummering.getBehandlingOpprettetTid()).isNotNull();
        assertThat(vedtakOppsummering.getBehandlingType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);
        assertThat(vedtakOppsummering.isErBehandlingManueltOpprettet()).isFalse();
        assertThat(vedtakOppsummering.getReferertFagsakBehandlingUuid()).isNotNull();
        assertThat(vedtakOppsummering.getSaksnummer()).isEqualTo(saksnummer.getVerdi());
        assertThat(vedtakOppsummering.getVedtakFattetTid()).isNotNull();
        assertThat(vedtakOppsummering.getYtelseType()).isEqualByComparingTo(YtelseType.FP);
        assertThat(vedtakOppsummering.getForrigeBehandling()).isNull();
    }

    private VedtakPeriode fellesAssertVedtakPeriode(List<VedtakPeriode> vedtakPerioder) {
        assertThat(vedtakPerioder).isNotEmpty();
        assertThat(vedtakPerioder.size()).isEqualTo(1);
        VedtakPeriode vedtakPeriode = vedtakPerioder.get(0);
        assertThat(vedtakPeriode.getFom()).isEqualTo(periode.getFom());
        assertThat(vedtakPeriode.getTom()).isEqualTo(periode.getTom());
        assertThat(vedtakPeriode.getHendelseTypeTekst()).isEqualTo("§14-2 Medlemskap");
        assertThat(vedtakPeriode.getHendelseUndertypeTekst()).isEqualTo("Ikke bosatt");
        return vedtakPeriode;
    }

    private void lagFakta() {
        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();
        faktaFeilutbetaling.setBegrunnelse("fakta begrunnelse");
        FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder().medPeriode(periode)
            .medFeilutbetalinger(faktaFeilutbetaling)
            .medHendelseType(HendelseType.MEDLEMSKAP_TYPE)
            .medHendelseUndertype(HendelseUnderType.IKKE_BOSATT).build();
        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);
        faktaFeilutbetalingRepository.lagre(behandlingId, faktaFeilutbetaling);
    }

    private void lagForeldelse() {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        VurdertForeldelsePeriode foreldelsePeriode = VurdertForeldelsePeriode.builder().medPeriode(periode)
            .medForeldelseVurderingType(ForeldelseVurderingType.FORELDET)
            .medVurdertForeldelse(vurdertForeldelse)
            .medBegrunnelse("foreldelse begrunnelse")
            .medForeldelsesFrist(periode.getFom().plusMonths(8))
            .build();
        vurdertForeldelse.leggTilVurderForeldelsePerioder(foreldelsePeriode);
        foreldelseRepository.lagre(behandlingId, vurdertForeldelse);
    }

    private void lagVilkårMedAktsomhet() {
        VilkårVurderingEntitet vilkårVurderingEntitet = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet vilkårVurderingPeriodeEntitet = VilkårVurderingPeriodeEntitet.builder().medPeriode(periode)
            .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
            .medBegrunnelse("vilkår begrunnelse")
            .medVurderinger(vilkårVurderingEntitet).build();
        VilkårVurderingAktsomhetEntitet vilkårVurderingAktsomhetEntitet = VilkårVurderingAktsomhetEntitet.builder()
            .medPeriode(vilkårVurderingPeriodeEntitet)
            .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
            .medIleggRenter(true)
            .medSærligGrunnerTilReduksjon(false)
            .medBegrunnelse("aktsomhet begrunnelse").build();
        VilkårVurderingSærligGrunnEntitet særligGrunnEntitet = VilkårVurderingSærligGrunnEntitet.builder()
            .medGrunn(SærligGrunn.STØRRELSE_BELØP)
            .medVurdertAktsomhet(vilkårVurderingAktsomhetEntitet)
            .medBegrunnelse("særlig grunner begrunnelse").build();
        vilkårVurderingAktsomhetEntitet.leggTilSærligGrunn(særligGrunnEntitet);
        vilkårVurderingPeriodeEntitet.setAktsomhet(vilkårVurderingAktsomhetEntitet);
        vilkårVurderingEntitet.leggTilPeriode(vilkårVurderingPeriodeEntitet);
        vilkårsvurderingRepository.lagre(behandlingId, vilkårVurderingEntitet);
    }

    private void lagVilkårMedGodTro() {
        VilkårVurderingEntitet vilkårVurderingEntitet = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet vilkårVurderingPeriodeEntitet = VilkårVurderingPeriodeEntitet.builder().medPeriode(periode)
            .medVilkårResultat(VilkårResultat.GOD_TRO)
            .medBegrunnelse("vilkår begrunnelse")
            .medVurderinger(vilkårVurderingEntitet).build();
        VilkårVurderingGodTroEntitet vilkårVurderingGodTroEntitet = VilkårVurderingGodTroEntitet.builder()
            .medPeriode(vilkårVurderingPeriodeEntitet)
            .medBeløpTilbakekreves(BigDecimal.valueOf(1000))
            .medBeløpErIBehold(false)
            .medBegrunnelse("god tro begrunnelse").build();
        vilkårVurderingPeriodeEntitet.setGodTro(vilkårVurderingGodTroEntitet);
        vilkårVurderingEntitet.leggTilPeriode(vilkårVurderingPeriodeEntitet);
        vilkårsvurderingRepository.lagre(behandlingId, vilkårVurderingEntitet);
    }

    private void lagBehandlingVedtak() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.FULL_TILBAKEBETALING)
            .medBehandling(behandling).build();
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .medVedtaksdato(LocalDate.now())
            .medAnsvarligSaksbehandler("Z12345")
            .medBehandlingsresultat(behandlingsresultat).build();
        repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
        behandlingVedtakRepository.lagre(behandlingVedtak);
    }

    private void lagKravgrunnlag() {
        Kravgrunnlag431 kravgrunnlag431 = Kravgrunnlag431.builder().medEksternKravgrunnlagId("12345")
            .medVedtakId(12345l)
            .medBehandlendeEnhet("8020")
            .medBostedEnhet("8020")
            .medAnsvarligEnhet("8020")
            .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
            .medKravStatusKode(KravStatusKode.NYTT)
            .medUtbetalesTilId("1234567890")
            .medUtbetIdType(GjelderType.PERSON)
            .medGjelderVedtakId("1234567890")
            .medGjelderType(GjelderType.PERSON)
            .medFeltKontroll("2020")
            .medSaksBehId(ANSVARLIG_SAKSBEHANDLER)
            .medFagSystemId(saksnummer.getVerdi() + "100")
            .medReferanse(Henvisning.fraEksternBehandlingId(1L))
            .build();
        KravgrunnlagPeriode432 kravgrunnlagPeriode432 = KravgrunnlagPeriode432.builder().medPeriode(periode)
            .medKravgrunnlag431(kravgrunnlag431)
            .medBeløpSkattMnd(BigDecimal.valueOf(100)).build();
        KravgrunnlagBelop433 feilPostering = KravgrunnlagBelop433.builder().medKlasseKode(KlasseKode.FPATORD)
            .medKlasseType(KlasseType.FEIL)
            .medNyBelop(BigDecimal.valueOf(1000))
            .medSkattProsent(BigDecimal.valueOf(10))
            .medKravgrunnlagPeriode432(kravgrunnlagPeriode432).build();
        KravgrunnlagBelop433 ytelPostering = KravgrunnlagBelop433.builder().medKlasseKode(KlasseKode.FPATORD)
            .medKlasseType(KlasseType.YTEL)
            .medTilbakekrevesBelop(BigDecimal.valueOf(1000))
            .medOpprUtbetBelop(BigDecimal.valueOf(1000))
            .medSkattProsent(BigDecimal.valueOf(10))
            .medKravgrunnlagPeriode432(kravgrunnlagPeriode432).build();
        kravgrunnlagPeriode432.leggTilBeløp(feilPostering);
        kravgrunnlagPeriode432.leggTilBeløp(ytelPostering);
        kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

}
