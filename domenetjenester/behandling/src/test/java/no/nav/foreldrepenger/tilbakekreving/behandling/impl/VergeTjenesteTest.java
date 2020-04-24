package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

public class VergeTjenesteTest extends FellesTestOppsett {
    private VergeTjeneste vergeTjeneste = new VergeTjeneste(behandlingskontrollTjeneste, gjenopptaBehandlingTjeneste,
        repoProvider.getAksjonspunktRepository(), historikkTjenesteAdapter);
    private InternalManipulerBehandling manipulerBehandling = new InternalManipulerBehandlingImpl(repoProvider);

    @Test
    public void skal_opprette_verge_når_behandling_er_i_fakta_steg() {
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_FEILUTBETALING);
        assertThat(behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(internBehandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.FAKTA_VERGE);
        List<HistorikkinnslagDel> historikkinnslagDeler = historikkinnslager.get(0).getHistorikkinnslagDeler();
        assertThat(historikkinnslagDeler).isNotEmpty();
        assertThat(historikkinnslagDeler.size()).isEqualTo(1);
        assertThat(historikkinnslagDeler.get(0).getSkjermlenke()).isNotEmpty();
        assertThat(historikkinnslagDeler.get(0).getSkjermlenke().get()).isEqualTo(SkjermlenkeType.VERGE.getKode());
        assertThat(historikkinnslagDeler.get(0).getHendelse()).isNotEmpty();
    }

}
