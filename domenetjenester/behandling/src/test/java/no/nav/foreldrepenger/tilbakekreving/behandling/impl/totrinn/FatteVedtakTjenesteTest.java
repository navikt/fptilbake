package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VedtakAksjonspunktData;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.VurderÅrsakTotrinnsvurdering;

public class FatteVedtakTjenesteTest extends FellesTestOppsett {

    private TotrinnTjeneste totrinnTjeneste = new TotrinnTjeneste(totrinnRepository, repoProvider);
    private FatteVedtakTjeneste fatteVedtakTjeneste = new FatteVedtakTjeneste(repoProvider, totrinnTjeneste);

    @Test
    public void opprettTotrinnsVurdering_nårAksjonspunktErGodkjent() {
        repoProvider.getAksjonspunktRepository().leggTilAksjonspunkt(BEHANDLING, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING);
        VedtakAksjonspunktData vedtakAksjonspunktData = new VedtakAksjonspunktData(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, true, null, null);
        fatteVedtakTjeneste.opprettTotrinnsVurdering(BEHANDLING, Collections.singletonList(vedtakAksjonspunktData));

        List<Totrinnsvurdering> totrinnsvurderinger = List.copyOf(totrinnTjeneste.hentTotrinnsvurderinger(BEHANDLING));
        assertThat(totrinnsvurderinger).isNotEmpty();
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        Totrinnsvurdering totrinnsvurdering = totrinnsvurderinger.get(0);
        assertThat(totrinnsvurdering.getBehandling()).isEqualTo(BEHANDLING);
        assertThat(totrinnsvurdering.isGodkjent()).isTrue();
        assertThat(totrinnsvurdering.isAktiv()).isTrue();
        assertThat(totrinnsvurdering.getAksjonspunktDefinisjon().getKode()).isEqualTo(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING.getKode());
        assertThat(totrinnsvurdering.getVurderÅrsaker()).isEmpty();
        assertThat(totrinnsvurdering.getBegrunnelse()).isNull();

    }

    @Test
    public void opprettTotrinnsVurdering_nårAksjonspunktErIkkeGodkjent() {
        repoProvider.getAksjonspunktRepository().leggTilAksjonspunkt(BEHANDLING, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING);
        VedtakAksjonspunktData vedtakAksjonspunktData = new VedtakAksjonspunktData(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, false,
                "feil fakta", Collections.singleton(VurderÅrsak.FEIL_FAKTA.getKode()));
        fatteVedtakTjeneste.opprettTotrinnsVurdering(BEHANDLING, Collections.singletonList(vedtakAksjonspunktData));

        List<Totrinnsvurdering> totrinnsvurderinger = List.copyOf(totrinnTjeneste.hentTotrinnsvurderinger(BEHANDLING));
        assertThat(totrinnsvurderinger).isNotEmpty();
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        Totrinnsvurdering totrinnsvurdering = totrinnsvurderinger.get(0);
        assertThat(totrinnsvurdering.getBehandling()).isEqualTo(BEHANDLING);
        assertThat(totrinnsvurdering.isGodkjent()).isFalse();
        assertThat(totrinnsvurdering.isAktiv()).isTrue();
        assertThat(totrinnsvurdering.getAksjonspunktDefinisjon().getKode()).isEqualTo(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING.getKode());
        assertThat(totrinnsvurdering.getVurderÅrsaker()).isNotEmpty();
        assertThat(totrinnsvurdering.getVurderÅrsaker().size()).isEqualTo(1);
        VurderÅrsakTotrinnsvurdering årsak = totrinnsvurdering.getVurderÅrsaker().stream().findFirst().get();
        assertThat(årsak.getÅrsaksType()).isEqualByComparingTo(VurderÅrsak.FEIL_FAKTA);
        assertThat(totrinnsvurdering.getBegrunnelse()).isEqualTo("feil fakta");
    }
}
