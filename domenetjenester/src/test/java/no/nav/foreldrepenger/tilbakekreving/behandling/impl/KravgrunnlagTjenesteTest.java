package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.persistence.FlushModeType;
import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

class KravgrunnlagTjenesteTest extends FellesTestOppsett {

    private static final String SSN = "11112222333";
    private static final String ENHET = "8020";

    private final GjenopptaBehandlingMedGrunnlagTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingMedGrunnlagTjeneste.class);

    @Inject
    private SlettGrunnlagEventPubliserer eventPubliserer;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;

    private static final LocalDate fom = LocalDate.of(2016, 3, 15);
    private static final LocalDate tom = LocalDate.of(2016, 3, 18);

    @BeforeEach
    void setup() {
        entityManager.setFlushMode(FlushModeType.AUTO);
        when(mockTpsTjeneste.hentAktørForFnr(new PersonIdent(SSN))).thenReturn(Optional.of(aktørId));
        kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repoProvider, mockGjenopptaBehandlingTjeneste, behandlingskontrollTjeneste, eventPubliserer,
            new AutomatiskSaksbehandlingVurderingTjeneste(grunnlagRepository, varselRepository));
    }

    @Test
    void lagreTilbakekrevingsgrunnlagFraØkonomi() {
        Kravgrunnlag431 kravgrunnlag = lagKravgrunnlagDto(KravStatusKode.NYTT);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag, true);

        assertKravgrunnlag();
    }

    @Test
    void lagreTilbakekrevingsgrunnlagFraØkonomi_medEndretGrunnlag() {
        Kravgrunnlag431 kravgrunnlag = lagKravgrunnlagDto(KravStatusKode.ENDRET);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag, true);

        assertKravgrunnlag();
    }

    @Test
    void lagreTilbakekrevingsgrunnlagFraØkonomi_med_ugyldig_endret_grunnlag() {
        Kravgrunnlag431 kravgrunnlag = lagKravgrunnlagDto(KravStatusKode.ENDRET);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag, false);

        assertKravgrunnlag();
        verify(gjenopptaBehandlingTjeneste, never()).fortsettBehandlingMedGrunnlag(behandling.getId());
    }

    @Test
    void lagreTilbakekrevingsgrunnlagFraØkonomi_medEndretGrunnlag_med_allerede_har_grunnlag() {
        Kravgrunnlag431 kravgrunnlag = lagKravgrunnlagDto(KravStatusKode.NYTT);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag, true);

        FaktaFeilutbetaling faktaFeilutbetaling = lagFaktaFeilutbetaling();
        faktaFeilutbetalingRepository.lagre(internBehandlingId, faktaFeilutbetaling);

        VurdertForeldelse vurdertForeldelse = lagForeldelse();
        vurdertForeldelseRepository.lagre(internBehandlingId, vurdertForeldelse);

        VilkårVurderingEntitet vilkårEntitet = lagVilkårsVurdering();
        vilkårsvurderingRepository.lagre(internBehandlingId, vilkårEntitet);


        kravgrunnlag = lagKravgrunnlagDto(KravStatusKode.ENDRET);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag, true);

        assertKravgrunnlag();
        assertThat(faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(internBehandlingId)).isEmpty();
        assertThat(vurdertForeldelseRepository.finnVurdertForeldelse(internBehandlingId)).isEmpty();
        assertThat(vilkårsvurderingRepository.finnVilkårsvurdering(internBehandlingId)).isEmpty();
    }

    private VurdertForeldelse lagForeldelse() {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        VurdertForeldelsePeriode foreldelsePeriode = VurdertForeldelsePeriode.builder().medPeriode(fom, tom)
                .medVurdertForeldelse(vurdertForeldelse)
                .medForeldelseVurderingType(ForeldelseVurderingType.IKKE_FORELDET)
                .medBegrunnelse("ikke foreldet").build();
        vurdertForeldelse.leggTilVurderForeldelsePerioder(foreldelsePeriode);
        return vurdertForeldelse;
    }

    private VilkårVurderingEntitet lagVilkårsVurdering() {
        VilkårVurderingEntitet vilkårEntitet = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet periodeEntitet = VilkårVurderingPeriodeEntitet.builder().medPeriode(fom, tom)
                .medVurderinger(vilkårEntitet)
                .medVilkårResultat(VilkårResultat.GOD_TRO)
                .medBegrunnelse("vurdert").build();
        VilkårVurderingGodTroEntitet godTroEntitet = VilkårVurderingGodTroEntitet.builder().medPeriode(periodeEntitet)
                .medBegrunnelse("vurdert")
                .medBeløpErIBehold(true)
                .medBeløpTilbakekreves(BigDecimal.TEN).build();
        periodeEntitet.setGodTro(godTroEntitet);
        vilkårEntitet.leggTilPeriode(periodeEntitet);
        return vilkårEntitet;
    }

    private Kravgrunnlag431 lagKravgrunnlagDto(KravStatusKode kravStatusKode) {
        return Kravgrunnlag431.builder()
                .medEksternKravgrunnlagId("123")
                .medVedtakId(10000L)
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medKravStatusKode(kravStatusKode)
                .medGjelderVedtakId(aktørId.getId())
                .medGjelderType(GjelderType.PERSON)
                .medUtbetalesTilId(aktørId.getId())
                .medUtbetIdType(GjelderType.PERSON)
                .medFagSystemId("10000000000000000")
                .medAnsvarligEnhet(ENHET)
                .medBostedEnhet(ENHET)
                .medBehandlendeEnhet(ENHET)
                .medSaksBehId("Z991036")
                .medFeltKontroll("42354353453454")
                .build();
    }

    private void formPerioder(LocalDate fom, LocalDate tom, Kravgrunnlag431 kravgrunnlag) {
        IntStream.range(4, 6).forEach(j -> {
            KravgrunnlagPeriode432 periode = KravgrunnlagPeriode432.builder()
                    .medKravgrunnlag431(kravgrunnlag)
                    .medPeriode(fom.plusDays(j), tom.plusDays(j))
                    .build();
            kravgrunnlag.leggTilPeriode(periode);
            formBeløper(periode);
        });
    }

    private void formBeløper(KravgrunnlagPeriode432 periode) {
        IntStream.range(0, 2).forEach(i -> {
            KravgrunnlagBelop433 beløp = KravgrunnlagBelop433.builder()
                    .medKravgrunnlagPeriode432(periode)
                    .medKlasseKode(KlasseKode.FPATORD)
                    .medKlasseType(i == 0 ? KlasseType.FEIL : KlasseType.YTEL)
                    .medNyBelop(i == 0 ? BigDecimal.valueOf(11000) : BigDecimal.ZERO)
                    .medTilbakekrevesBelop(i > 0 ? BigDecimal.valueOf(11000) : BigDecimal.ZERO)
                    .medResultatKode("VED")
                    .build();
            periode.leggTilBeløp(beløp);
        });
    }

    private void assertKravgrunnlag() {
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(internBehandlingId);
        assertThat(kravgrunnlag).isNotNull();
        assertThat(kravgrunnlag.getGjelderVedtakId()).isEqualTo(aktørId.getId());
        assertThat(kravgrunnlag.getUtbetalesTilId()).isEqualTo(aktørId.getId());
        List<KravgrunnlagPeriode432> kravgrunnlagPerioder = new ArrayList<>(kravgrunnlag.getPerioder());

        assertThat(kravgrunnlagPerioder).isNotEmpty();
        kravgrunnlagPerioder.sort(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        assertThat(kravgrunnlagPerioder.get(0).getFom()).isEqualTo(fom.plusDays(4));
        List<KravgrunnlagBelop433> kravgrunnlagBeloper = new ArrayList<>(kravgrunnlagPerioder.get(0).getKravgrunnlagBeloper433());
        assertThat(kravgrunnlagBeloper).isNotEmpty();
        kravgrunnlagBeloper.sort(Comparator.comparing(KravgrunnlagBelop433::getId));
        assertThat(kravgrunnlagBeloper.get(0).getKlasseType()).isEqualTo(KlasseType.FEIL);
        assertThat(kravgrunnlagBeloper.get(1).getKlasseType()).isEqualTo(KlasseType.YTEL);
        assertThat(kravgrunnlagBeloper.get(0).getNyBelop()).isEqualByComparingTo(kravgrunnlagBeloper.get(1).getTilbakekrevesBelop());
    }


}
