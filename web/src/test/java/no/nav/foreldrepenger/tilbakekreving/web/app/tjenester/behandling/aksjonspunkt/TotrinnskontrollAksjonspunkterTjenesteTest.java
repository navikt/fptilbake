package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn.TotrinnskontrollAksjonspunkterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn.TotrinnskontrollSkjermlenkeContextDto;

@CdiDbAwareTest
public class TotrinnskontrollAksjonspunkterTjenesteTest {

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollAksjonspunkterTjeneste;

    private Behandling behandling;

    @BeforeEach
    public void setup() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, BehandlingStegType.TBKGSTEG);
        scenario.medBehandlingStegStart(BehandlingStegType.FATTE_VEDTAK);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void hentTotrinnsSkjermlenkeContext() {
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medBehandling(behandling)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING)
                .medGodkjent(true)
                .build();
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, Collections.singletonList(totrinnsvurdering));

        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContexter = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
        assertThat(skjermlenkeContexter).isNotEmpty();
        assertThat(skjermlenkeContexter.size()).isEqualTo(1);

        TotrinnskontrollSkjermlenkeContextDto skjermlenkeContext = skjermlenkeContexter.get(0);
        assertThat(skjermlenkeContext.getSkjermlenkeType()).isEqualTo(SkjermlenkeType.TILBAKEKREVING.getKode());
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().size()).isEqualTo(1);
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().get(0).getAksjonspunktKode()).isEqualTo(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING.getKode());
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().get(0).getTotrinnskontrollGodkjent()).isTrue();
    }

    @Test
    public void hentTotrinnsSkjermlenkeContext_nårIkkegodkjent() {
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medBehandling(behandling)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING)
                .medGodkjent(false)
                .medBegrunnelse("annet")
                .build();
        totrinnsvurdering.leggTilVurderÅrsakTotrinnsvurdering(VurderÅrsak.ANNET);
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, Collections.singletonList(totrinnsvurdering));

        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContexter = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
        assertThat(skjermlenkeContexter).isNotEmpty();
        assertThat(skjermlenkeContexter.size()).isEqualTo(1);

        TotrinnskontrollSkjermlenkeContextDto skjermlenkeContext = skjermlenkeContexter.get(0);
        assertThat(skjermlenkeContext.getSkjermlenkeType()).isEqualTo(SkjermlenkeType.TILBAKEKREVING.getKode());
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().size()).isEqualTo(1);

        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = skjermlenkeContext.getTotrinnskontrollAksjonspunkter().get(0);
        assertThat(totrinnskontrollAksjonspunkterDto.getAksjonspunktKode()).isEqualTo(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING.getKode());
        assertThat(totrinnskontrollAksjonspunkterDto.getTotrinnskontrollGodkjent()).isNull();
        assertThat(totrinnskontrollAksjonspunkterDto.getBesluttersBegrunnelse()).isEqualTo("annet");
        assertThat(totrinnskontrollAksjonspunkterDto.getArsaker()).contains(VurderÅrsak.ANNET);
    }

    @Test
    public void hentTotrinnsvurderingSkjermlenkeContext(){
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medBehandling(behandling)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING)
                .medGodkjent(true)
                .build();
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, Collections.singletonList(totrinnsvurdering));

        List<TotrinnskontrollSkjermlenkeContextDto> skjermlenkeContexter = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling);
        assertThat(skjermlenkeContexter).isNotEmpty();
        assertThat(skjermlenkeContexter.size()).isEqualTo(1);

        TotrinnskontrollSkjermlenkeContextDto skjermlenkeContext = skjermlenkeContexter.get(0);
        assertThat(skjermlenkeContext.getSkjermlenkeType()).isEqualTo(SkjermlenkeType.TILBAKEKREVING.getKode());
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().size()).isEqualTo(1);
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().get(0).getAksjonspunktKode()).isEqualTo(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING.getKode());
        assertThat(skjermlenkeContext.getTotrinnskontrollAksjonspunkter().get(0).getTotrinnskontrollGodkjent()).isTrue();
    }
}
