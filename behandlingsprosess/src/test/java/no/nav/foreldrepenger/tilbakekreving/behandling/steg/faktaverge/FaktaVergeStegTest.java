package no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktaverge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktTestSupport;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;

class FaktaVergeStegTest extends FellesTestOppsett {

    private FaktaVergeSteg faktaVergeSteg;

    @BeforeEach
    void setUp() {
        faktaVergeSteg = new FaktaVergeSteg(behandlingRepository);
    }

    @Test
    void skal_utføre_steg_hvis_verge_aksjonspunkt_finnes() {
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        AksjonspunktTestSupport.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(behandling.getSaksnummer(), behandling.getFagsakId(), behandlingLås);
        BehandleStegResultat stegResultat = faktaVergeSteg.utførSteg(kontekst);
        assertThat(stegResultat.getAksjonspunktListe()).isNotEmpty();
        assertThat(stegResultat.getAksjonspunktListe().contains(AksjonspunktDefinisjon.AVKLAR_VERGE)).isTrue();
        assertThat(stegResultat.getTransisjon()).isNotNull();
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
    }

    @Test
    void skal_ikke_utføre_steg_hvis_verge_aksjonspunkt_ikke_finnes() {
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(behandling.getSaksnummer(), behandling.getFagsakId(), behandlingLås);
        BehandleStegResultat stegResultat = faktaVergeSteg.utførSteg(kontekst);
        assertThat(stegResultat.getAksjonspunktListe()).isEmpty();
    }
}
