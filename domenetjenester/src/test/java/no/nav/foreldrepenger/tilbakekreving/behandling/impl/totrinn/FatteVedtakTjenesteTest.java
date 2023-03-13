package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VedtakAksjonspunktData;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.VurderÅrsakTotrinnsvurdering;

class FatteVedtakTjenesteTest extends FellesTestOppsett {

    private TotrinnTjeneste totrinnTjeneste;
    private FatteVedtakTjeneste fatteVedtakTjeneste;

    @BeforeEach
    void setUp() {
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        totrinnTjeneste = new TotrinnTjeneste(totrinnRepository, repoProvider);
        fatteVedtakTjeneste = new FatteVedtakTjeneste(repoProvider, behandlingskontrollTjeneste, totrinnTjeneste);
    }

    @Test
    void opprettTotrinnsVurdering_nårAksjonspunktErGodkjent() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        VedtakAksjonspunktData vedtakAksjonspunktData = new VedtakAksjonspunktData(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, true, null, null);
        fatteVedtakTjeneste.opprettTotrinnsVurdering(behandling, Collections.singletonList(vedtakAksjonspunktData));

        List<Totrinnsvurdering> totrinnsvurderinger = List.copyOf(totrinnTjeneste.hentTotrinnsvurderinger(behandling));
        assertThat(totrinnsvurderinger).isNotEmpty();
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        Totrinnsvurdering totrinnsvurdering = totrinnsvurderinger.get(0);
        assertThat(totrinnsvurdering.getBehandling()).isEqualTo(behandling);
        assertThat(totrinnsvurdering.isGodkjent()).isTrue();
        assertThat(totrinnsvurdering.isAktiv()).isTrue();
        assertThat(totrinnsvurdering.getAksjonspunktDefinisjon().getKode()).isEqualTo(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING.getKode());
        assertThat(totrinnsvurdering.getVurderÅrsaker()).isEmpty();
        assertThat(totrinnsvurdering.getBegrunnelse()).isNull();

    }

    @Test
    void opprettTotrinnsVurdering_nårAksjonspunktErIkkeGodkjent() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        VedtakAksjonspunktData vedtakAksjonspunktData = new VedtakAksjonspunktData(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, false,
                "feil fakta", Collections.singleton(VurderÅrsak.FEIL_FAKTA.getKode()));
        fatteVedtakTjeneste.opprettTotrinnsVurdering(behandling, Collections.singletonList(vedtakAksjonspunktData));

        List<Totrinnsvurdering> totrinnsvurderinger = List.copyOf(totrinnTjeneste.hentTotrinnsvurderinger(behandling));
        assertThat(totrinnsvurderinger).isNotEmpty();
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        Totrinnsvurdering totrinnsvurdering = totrinnsvurderinger.get(0);
        assertThat(totrinnsvurdering.getBehandling()).isEqualTo(behandling);
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
