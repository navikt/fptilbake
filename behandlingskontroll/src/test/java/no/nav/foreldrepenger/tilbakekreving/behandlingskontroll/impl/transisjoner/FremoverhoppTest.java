package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktType.INN;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktType.UT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.observer.BehandlingskontrollFremoverhoppTransisjonEventObserver;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.observer.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class FremoverhoppTest {

    private final List<StegTransisjon> transisjoner = new ArrayList<>();

    private BehandlingRepository behandlingRepository;
    private final BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepository();

    private BehandlingskontrollServiceProvider serviceProvider;
    private BehandlingRepositoryProvider repositoryProvider;

    private BehandlingStegType steg1;
    private BehandlingStegType steg2;
    private BehandlingStegType steg3;
    private BehandlingStegType steg4;
    private BehandlingskontrollFremoverhoppTransisjonEventObserver observer;

    private Behandling behandling;
    private BehandlingLås behandlingLås;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        serviceProvider = new BehandlingskontrollServiceProvider(entityManager, behandlingModellRepository, null);
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = serviceProvider.getBehandlingRepository();
        observer = new BehandlingskontrollFremoverhoppTransisjonEventObserver(serviceProvider) {
            @Override
            protected void hoppFramover(BehandlingStegModell stegModell, BehandlingTransisjonEvent transisjonEvent, BehandlingStegType sisteSteg,
                                        BehandlingStegType finalFørsteSteg) {
                transisjoner.add(new StegTransisjon(BehandlingSteg.TransisjonType.HOPP_OVER_FRAMOVER,
                        stegModell.getBehandlingStegType()));
            }
        };

        var modell = behandlingModellRepository.getModell(BehandlingType.TILBAKEKREVING);
        steg1 = BehandlingStegType.FORELDELSEVURDERINGSTEG;
        steg2 = modell.finnNesteSteg(steg1).getBehandlingStegType();
        steg3 = modell.finnNesteSteg(steg2).getBehandlingStegType();
        steg4 = modell.finnNesteSteg(steg3).getBehandlingStegType();
    }

    @Test
    void skal_avbryte_aksjonspunkt_som_skulle_vært_håndtert_i_mellomliggende_steg() {
        assertAPAvbrytesVedFremoverhopp(fra(steg1, UT), til(steg4), medAP(steg1, UT));
        assertAPAvbrytesVedFremoverhopp(fra(steg1, UT), til(steg4), medAP(steg1, UT));
    }

    @Test
    void skal_ikke_gjøre_noe_med_aksjonspunkt_som_oppsto_og_løstes_før_steget_det_hoppes_fra() {
        assertAPUendretVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg1, UT));
    }

    @Test
    void skal_ikke_gjøre_noe_med_aksjonspunkt_som_løstes_ved_inngang_til_steget_når_det_hoppes_fra_utgang_av_steget() {
        assertAPUendretVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg1, UT));
    }

    @Test
    void skal_avbryte_aksjonspunkt_i_utgang_av_frasteget_når_frasteget_ikke_er_ferdig() {
        assertAPAvbrytesVedFremoverhopp(fra(steg2, INN), til(steg4), medAP(steg2, UT));
        assertAPAvbrytesVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg2, UT));
    }

    @Test
    void skal_ikke_gjøre_noe_med_aksjonspunkt_som_skal_løses_i_steget_det_hoppes_til() {
        assertAPUendretVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg1, UT));
        assertAPUendretVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg1, UT));
        assertAPUendretVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg4, UT));
        assertAPUendretVedFremoverhopp(fra(steg2, UT), til(steg4), medAP(steg4, UT));
    }

    @Test
    void skal_kalle_transisjoner_på_steg_det_hoppes_over() throws Exception {
        assertThat(transisjonerVedFremoverhopp(fra(steg1, INN), til(steg4))).contains(
                StegTransisjon.hoppFremoverOver(steg1),
                StegTransisjon.hoppFremoverOver(steg2));
        assertThat(transisjonerVedFremoverhopp(fra(steg1, UT), til(steg4)))
                .contains(StegTransisjon.hoppFremoverOver(steg2));
        assertThat(transisjonerVedFremoverhopp(fra(steg2, INN), til(steg3)))
                .contains(StegTransisjon.hoppFremoverOver(steg2));
        assertThat(transisjonerVedFremoverhopp(fra(steg2, UT), til(steg3))).isEmpty();
    }

    private void assertAPAvbrytesVedFremoverhopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(AVBRUTT);
    }

    private void assertAPUendretVedFremoverhopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        var orginalStatus = ap.getStatus();
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(orginalStatus);
    }

    private List<StegTransisjon> transisjonerVedFremoverhopp(StegPort fra, BehandlingStegType til) {
        // skal ikke spille noen rolle for transisjoner hvilke aksjonspunkter som finnes
        var ap = medAP(steg1, UT);

        transisjoner.clear();
        utførFremoverhoppReturnerAksjonspunkt(fra, til, ap);
        return transisjoner;
    }

    private AbstractComparableAssert<?, AksjonspunktStatus> assertAPStatusEtterHopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        var aksjonspunkt = utførFremoverhoppReturnerAksjonspunkt(fra, til, ap);
        return Assertions.assertThat(aksjonspunkt.getStatus());
    }

    private Aksjonspunkt utførFremoverhoppReturnerAksjonspunkt(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {

        BehandlingStegStatus fraStatus;
        var fraPort = fra.port().getDbKode();
        if (fraPort.equals(VurderingspunktType.INN.getDbKode())) {
            fraStatus = BehandlingStegStatus.INNGANG;
        } else if (fraPort.equals(VurderingspunktType.UT.getDbKode())) {
            fraStatus = BehandlingStegStatus.UTGANG;
        } else {
            throw new IllegalStateException("BehandlingStegStatus " + fraPort + " ikke støttet i testen");
        }

        var fraTilstand = new BehandlingStegTilstand(behandling, fra.steg(), fraStatus);
        // BehandlingStegTilstand tilTilstand = new BehandlingStegTilstand(behandling,
        // til, BehandlingStegStatus.VENTER);
        var fagsak = behandling.getFagsak();
        var kontekst = new BehandlingskontrollKontekst(fagsak.getSaksnummer(), fagsak.getId(), behandlingLås);
        /*
         * BehandlingStegOvergangEvent.BehandlingStegOverhoppEvent behandlingEvent = new
         * BehandlingStegOvergangEvent.BehandlingStegOverhoppEvent(kontekst,
         * Optional.of(fraTilstand), Optional.of(tilTilstand));
         */
        var transisjonEvent = new BehandlingTransisjonEvent(kontekst, FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK, fraTilstand,
                til, true);

        // act
        observer.observerBehandlingSteg(transisjonEvent);

        return ap;
    }

    private Aksjonspunkt medAP(BehandlingStegType identifisertI, VurderingspunktType type) {
        return medAP(identifisertI, AksjonspunktStatus.OPPRETTET, identifisertI.getAksjonspunktDefinisjoner(type).get(0));
    }

    private Aksjonspunkt medAP(BehandlingStegType identifisertI, AksjonspunktStatus status, AksjonspunktDefinisjon ad) {

        var ytelseBehandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandling = Behandling.nyBehandlingFor(ytelseBehandling.getFagsak(), BehandlingType.TILBAKEKREVING).build();
        behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        var ap = serviceProvider.getAksjonspunktKontrollRepository().leggTilAksjonspunkt(behandling, ad, identifisertI);

        if (status.getKode().equals(UTFØRT.getKode())) {
            serviceProvider.getAksjonspunktKontrollRepository().setTilUtført(ap);
        } else if (status.getKode().equals(AksjonspunktStatus.OPPRETTET.getKode())) {
            // dette er default-status ved opprettelse
        } else {
            throw new IllegalArgumentException("Testen støtter ikke status " + status + " du må evt. utvide testen");
        }

        behandlingRepository.lagre(behandling, behandlingLås);

        return ap;
    }

    class TestSteg implements BehandlingSteg {

        private final BehandlingStegType behandlingStegType;

        protected TestSteg(BehandlingStegType behandlingStegType) {
            this.behandlingStegType = behandlingStegType;
        }

        @Override
        public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
            return null;
        }

        @Override
        public void vedHoppOverFramover(BehandlingskontrollKontekst kontekst, BehandlingStegModell modell, BehandlingStegType fraSteg,
                                        BehandlingStegType tilSteg) {
            transisjoner.add(new StegTransisjon(TransisjonType.HOPP_OVER_FRAMOVER, behandlingStegType));
        }

    }

    private BehandlingStegType til(BehandlingStegType steg) {
        return steg;
    }

    private StegPort fra(BehandlingStegType steg, VurderingspunktType port) {
        return new StegPort(steg, port);

    }

    private static record StegPort(BehandlingStegType steg, VurderingspunktType port) {
    }

}
