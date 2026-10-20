package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagEndretEvent;

class TotrinnTjenesteTest extends FellesTestOppsett {

    private TotrinnTjeneste totrinnTjeneste;

    @BeforeEach
    void setUp() {
        totrinnTjeneste = new TotrinnTjeneste(totrinnRepository, repoProvider);
    }


    @Test
    void settNyeTotrinnaksjonspunktvurderinger() {
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medGodkjent(true)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING)
                .medBehandling(behandling)
                .build();
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, Collections.singletonList(totrinnsvurdering));

        List<Totrinnsvurdering> totrinnsvurderinger = (List<Totrinnsvurdering>) totrinnTjeneste.hentTotrinnsvurderinger(behandling);
        assertThat(totrinnsvurderinger).isNotEmpty();
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        totrinnsvurdering = totrinnsvurderinger.get(0);
        assertThat(totrinnsvurdering.getVurderÅrsaker()).isEmpty();
        assertThat(totrinnsvurdering.isGodkjent()).isTrue();
        assertThat(totrinnsvurdering.getAksjonspunktDefinisjon()).isEqualToComparingFieldByField(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING);
        assertThat(totrinnsvurdering.getBehandling()).isEqualToComparingFieldByField(behandling);
        assertThat(totrinnsvurdering.getBegrunnelse()).isNull();
    }

    @Test
    void slettGammelTotrinndata() {
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medGodkjent(true)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING)
                .medBehandling(behandling)
                .build();
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, Collections.singletonList(totrinnsvurdering));

        List<Totrinnsvurdering> totrinnsvurderinger = (List<Totrinnsvurdering>) totrinnTjeneste.hentTotrinnsvurderinger(behandling);
        assertThat(totrinnsvurderinger).isNotEmpty();

        KravgrunnlagEndretEvent kravgrunnlagEndretEvent = new KravgrunnlagEndretEvent(behandling.getId());
        totrinnTjeneste.slettGammelTotrinnData(kravgrunnlagEndretEvent);

        totrinnsvurderinger = (List<Totrinnsvurdering>) totrinnTjeneste.hentTotrinnsvurderinger(behandling);
        assertThat(totrinnsvurderinger).isEmpty();
    }
}
