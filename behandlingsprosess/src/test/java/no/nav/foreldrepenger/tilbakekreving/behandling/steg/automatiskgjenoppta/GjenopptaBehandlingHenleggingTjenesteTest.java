package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
class GjenopptaBehandlingHenleggingTjenesteTest {

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    @Mock
    private ProsessTaskTjeneste mockProsessTaskTjeneste;
    @Mock
    private KravgrunnlagRepository mockGrunnlagRepository;
    @Mock
    private BehandlingRepositoryProvider mockRepositoryProvider;

    @BeforeEach
    void setUp() {
        when(mockRepositoryProvider.getGrunnlagRepository()).thenReturn(mockGrunnlagRepository);
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjeneste(mockProsessTaskTjeneste, null, null, mockRepositoryProvider);
    }

    @Test
    void skal_henlegge_behandling_nar_kravgrunnlag_mangler() {
        var argCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);

        // Arrange
        var behandlingMock = mockBehandlingUtenKravgrunnlag();

        // Act
        gjenopptaBehandlingTjeneste.gjenopptaBehandlingOmMulig("testCallId", behandlingMock);

        // Assert
        verify(mockProsessTaskTjeneste).lagre(argCaptor.capture());
        var taskData = argCaptor.getValue();
        assertThat(taskData.taskType()).isEqualTo(TaskType.forProsessTask(HenleggBehandlingTask.class));
        assertThat(taskData.getBehandlingIdAsLong()).isEqualTo(behandlingMock.getId());
    }

    private Behandling mockBehandlingUtenKravgrunnlag() {
        var behandlingMock = mock(Behandling.class);
        final var behandlingId = behandlingMock.getId();

        var ventPåGrunnlagAksjonspunktMock = mock(Aksjonspunkt.class);
        when(ventPåGrunnlagAksjonspunktMock.getFristTid()).thenReturn(LocalDateTime.now().minusDays(1));
        when(ventPåGrunnlagAksjonspunktMock.erOpprettet()).thenReturn(true);

        when(behandlingMock.getSaksnummer()).thenReturn(Saksnummer.infotrygd("123456"));
        when(behandlingMock.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).thenReturn(
            Optional.of(ventPåGrunnlagAksjonspunktMock));

        when(mockGrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).thenReturn(false);
        return behandlingMock;
    }
}
