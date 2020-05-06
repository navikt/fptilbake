package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class VergeTjenesteTest extends FellesTestOppsett {
    private VergeTjeneste vergeTjeneste = new VergeTjeneste(behandlingskontrollTjeneste, gjenopptaBehandlingTjeneste,
        repoProvider.getAksjonspunktRepository());
    private InternalManipulerBehandling manipulerBehandling = new InternalManipulerBehandlingImpl(repoProvider);

    @Test
    public void skal_opprette_verge_når_behandling_er_i_fakta_steg() {
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_FEILUTBETALING);
        assertThat(behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
    }
}
