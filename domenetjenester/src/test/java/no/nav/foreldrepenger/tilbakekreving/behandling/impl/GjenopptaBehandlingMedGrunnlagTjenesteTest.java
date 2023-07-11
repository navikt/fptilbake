package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.InternalAksjonspunktManipulator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@CdiDbAwareTest
class GjenopptaBehandlingMedGrunnlagTjenesteTest {

    private GjenopptaBehandlingMedGrunnlagTjeneste gjenopptaBehandlingTjeneste;

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Inject
    private BehandlingVenterRepository behandlingVenterRepository;

    @Mock
    private ProsessTaskTjeneste mockTaskTjeneste;

    private InternalAksjonspunktManipulator internalAksjonspunktManipulator = new InternalAksjonspunktManipulator();


    @BeforeEach
    void setup() {
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingMedGrunnlagTjeneste(mockTaskTjeneste, behandlingVenterRepository);
    }

    @Test
    void skal_ikke_fortsette_behandling_med_grunnlag_for_behandling_i_varsel_steg_og_fristen_ikke_gått_ut() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(20));
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandling.getId());
        verifyNoInteractions(mockTaskTjeneste);
    }

    @Test
    void skal_fortsette_behandling_med_grunnlag_for_behandling_i_tbk_steg() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(20));
        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandling.getId());
        verify(mockTaskTjeneste).lagre(any(ProsessTaskData.class));
    }

    @Test
    void skal_fortsette_behandling_med_grunnlag_for_behandling_i_fakta_steg() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(20));
        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandling.getId());
        verify(mockTaskTjeneste).lagre(any(ProsessTaskData.class));
    }

    private Behandling lagBehandling() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().minusDays(10));
        BehandlingLås lås = behandlingRepositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        return behandling;
    }
}
