package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevSporingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class GjenopptaBehandlingHenleggingTjenesteTest {

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    @Mock
    private ProsessTaskTjeneste mockProsessTaskTjeneste;
    @Mock
    private KravgrunnlagRepository mockGrunnlagRepository;
    @Mock
    private BrevSporingTjeneste mockBrevSporingTjeneste;
    @Mock
    private BehandlingRepositoryProvider mockRepositoryProvider;

    @BeforeEach
    void setUp() {
        when(mockRepositoryProvider.getGrunnlagRepository()).thenReturn(mockGrunnlagRepository);
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjeneste(mockProsessTaskTjeneste, null, null, mockRepositoryProvider, mockBrevSporingTjeneste);
    }

    @Test
    void skal_henlegge_behandling_nar_kravgrunnlag_mangler_og_varselbrev_ikke_sendt() {
        // Arrange
        var behandlingMock = mockBehandlingUtenKravgrunnlag(false);

        // Act
        gjenopptaBehandlingTjeneste.gjenopptaBehandlingOmMulig("testCallId", behandlingMock);

        // Assert
        verify(mockProsessTaskTjeneste).lagre(any(ProsessTaskData.class));
        verify(mockBrevSporingTjeneste).erVarselBrevSendtFor(behandlingMock.getId());
    }

    @Test
    void skal_ikke_henlegge_behandling_nar_kravgrunnlag_mangler_og_varselbrev_er_sendt() {
        // Arrange
        var behandlingMock = mockBehandlingUtenKravgrunnlag(true);

        // Act
        gjenopptaBehandlingTjeneste.gjenopptaBehandlingOmMulig("testCallId", behandlingMock);

        // Assert
        verify(mockProsessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
        verify(mockBrevSporingTjeneste).erVarselBrevSendtFor(behandlingMock.getId());
    }

    private Behandling mockBehandlingUtenKravgrunnlag(boolean erVarselSendt) {
        var behandlingMock = mock(Behandling.class);
        final var behandlingId = behandlingMock.getId();

        var ventPåGrunnlalAksjonspunktMock = mock(Aksjonspunkt.class);
        when(ventPåGrunnlalAksjonspunktMock.getFristTid()).thenReturn(LocalDateTime.now().minusDays(1));
        when(ventPåGrunnlalAksjonspunktMock.erOpprettet()).thenReturn(true);

        when(behandlingMock.getSaksnummer()).thenReturn(Saksnummer.infotrygd("123456"));
        when(behandlingMock.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).thenReturn(
            Optional.of(ventPåGrunnlalAksjonspunktMock));

        when(mockGrunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).thenReturn(false);
        when(mockBrevSporingTjeneste.erVarselBrevSendtFor(behandlingId)).thenReturn(erVarselSendt);

        return behandlingMock;
    }


}
