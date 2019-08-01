package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnresultatgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

public class TotrinnTjenesteTest extends FellesTestOppsett {

    private TotrinnTjeneste totrinnTjeneste = new TotrinnTjeneste(totrinnRepository, repoProvider);

    @Test
    public void settNyttTotrinnsgrunnlag() {

        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, TOM, KlasseType.FEIL,
                BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, TOM,
                KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder()
                .medGrunnlagØkonomi(kravgrunnlag431)
                .medBehandlingId(INTERN_BEHANDLING_ID).build();
        grunnlagRepository.lagre(kravgrunnlagAggregate);

        repoProvider.getFeilutbetalingRepository().lagre(formFeilutbetalingAggregate());
        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(INTERN_BEHANDLING_ID, Collections.singletonList(
                new ForeldelsePeriodeDto(FOM, TOM,
                        ForeldelseVurderingType.FORELDET, "ABC")));
        List<VilkårsvurderingPerioderDto> vilkårPerioder = Lists.newArrayList(
                formVilkårsvurderingPerioderDto(VilkårResultat.GOD_TRO, FOM, TOM, Aktsomhet.FORSETT));
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(INTERN_BEHANDLING_ID, vilkårPerioder);

        totrinnTjeneste.settNyttTotrinnsgrunnlag(BEHANDLING);

        Optional<Totrinnresultatgrunnlag> totrinnresultatgrunnlag = totrinnTjeneste.hentTotrinngrunnlagHvisEksisterer(BEHANDLING);
        assertThat(totrinnresultatgrunnlag).isNotEmpty();
        Totrinnresultatgrunnlag resultat = totrinnresultatgrunnlag.get();
        assertThat(resultat.getBehandling().getId()).isEqualTo(INTERN_BEHANDLING_ID);
        assertThat(resultat.getFaktaFeilutbetalingId()).isEqualTo(repoProvider.getFeilutbetalingRepository().finnFeilutbetaling(INTERN_BEHANDLING_ID).get().getId());
        assertThat(resultat.getVurderForeldelseId()).isEqualTo(repoProvider.getVurdertForeldelseRepository().finnVurdertForeldelseForBehandling(INTERN_BEHANDLING_ID).get().getId());
        assertThat(resultat.getVurderVilkårId()).isEqualTo(repoProvider.getVilkårsvurderingRepository().finnVilkårsvurderingForBehandlingId(INTERN_BEHANDLING_ID).get().getId());
    }

    @Test
    public void settNyeTotrinnaksjonspunktvurderinger() {
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medGodkjent(true)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING)
                .medBehandling(BEHANDLING)
                .build();
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(BEHANDLING, Collections.singletonList(totrinnsvurdering));

        List<Totrinnsvurdering> totrinnsvurderinger = (List<Totrinnsvurdering>) totrinnTjeneste.hentTotrinnsvurderinger(BEHANDLING);
        assertThat(totrinnsvurderinger).isNotEmpty();
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        totrinnsvurdering = totrinnsvurderinger.get(0);
        assertThat(totrinnsvurdering.getVurderÅrsaker()).isEmpty();
        assertThat(totrinnsvurdering.isGodkjent()).isTrue();
        assertThat(totrinnsvurdering.getAksjonspunktDefinisjon()).isEqualToComparingFieldByField(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING);
        assertThat(totrinnsvurdering.getBehandling()).isEqualToComparingFieldByField(BEHANDLING);
        assertThat(totrinnsvurdering.getBegrunnelse()).isNull();
    }
}
