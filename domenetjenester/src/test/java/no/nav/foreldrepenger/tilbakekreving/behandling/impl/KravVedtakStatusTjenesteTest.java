package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(MockitoExtension.class)
class KravVedtakStatusTjenesteTest {

    @Mock
    private KravVedtakStatusRepository kravVedtakStatusRepository;
    @Mock
    private BehandlingRepository behandlingRepository;
    @Mock
    private KravgrunnlagRepository grunnlagRepository;
    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;

    @BeforeEach
    void setUp() {
        kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository,
            new HalvtRettsgebyrTjeneste(grunnlagRepository, mock(VarselRepository.class)),
            null, behandlingRepository, grunnlagRepository, behandlingskontrollTjeneste);
    }

    @Test
    void exception_om_feil_status_kode_mottatt() {
        var behandlingId = 10L;
        var statusKode = KravStatusKode.ANNULLERT_OMG;
        var kravVedtakStatus437 = lagKravVedtakStatus(statusKode);

        var exception = assertThrows(TekniskException.class,
            () -> kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437));

        assertThat(exception.getMessage()).contains("Har fått ugyldig status kode " + statusKode.getKode() + " fra økonomisystem, kan ikke akseptere");
    }

    @Test
    void oppdater_status_hvis_SPER_uten_krav_grunnlag_koblet() {
        var behandlingId = 10L;
        var kravVedtakStatus437 = lagKravVedtakStatus(KravStatusKode.SPERRET);

        when(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).thenReturn(false);

        kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);

        verify(kravVedtakStatusRepository).lagre(behandlingId, kravVedtakStatus437);
        verify(grunnlagRepository, never()).sperrGrunnlag(anyLong());
        verifyNoInteractions(behandlingskontrollTjeneste);
    }

    @Test
    void oppdater_status_hvis_SPER_med_krav_grunnlag_koblet_allerede_sperret() {
        var behandlingId = 10L;
        var kravVedtakStatus437 = lagKravVedtakStatus(KravStatusKode.SPERRET);

        when(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).thenReturn(true);
        when(grunnlagRepository.erKravgrunnlagSperret(behandlingId)).thenReturn(true);

        kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);

        verify(kravVedtakStatusRepository).lagre(behandlingId, kravVedtakStatus437);
        verify(grunnlagRepository, never()).sperrGrunnlag(anyLong());
        verifyNoInteractions(behandlingskontrollTjeneste);
    }

    @Test
    void oppdater_status_hvis_SPER_med_krav_grunnlag_koblet_ikke_sperret() {
        var behandlingId = 10L;
        var kravVedtakStatus437 = lagKravVedtakStatus(KravStatusKode.SPERRET);

        var behandling = Behandling.nyBehandlingFor(
            Fagsak.opprettNy(Saksnummer.infotrygd("32423432"),
                NavBruker.opprettNy(new AktørId(233L), Språkkode.nn)), BehandlingType.TILBAKEKREVING).build();

        when(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).thenReturn(true);
        when(grunnlagRepository.erKravgrunnlagSperret(behandlingId)).thenReturn(false);
        when(behandlingRepository.hentBehandling(behandlingId)).thenReturn(behandling);

        kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);

        verify(kravVedtakStatusRepository).lagre(behandlingId, kravVedtakStatus437);
        verify(grunnlagRepository, times(1)).sperrGrunnlag(anyLong());
        verify(behandlingskontrollTjeneste).settBehandlingPåVent(eq(behandling), eq(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG),
            eq(BehandlingStegType.TBKGSTEG), any(), eq(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG));
    }

    @Test
    void oppdater_status_hvis_MANUELL_med_krav_grunnlag_koblet_ikke_sperret() {
        var behandlingId = 10L;
        var kravVedtakStatus437 = lagKravVedtakStatus(KravStatusKode.MANUELL);

        var behandling = Behandling.nyBehandlingFor(
            Fagsak.opprettNy(Saksnummer.infotrygd("32423432"),
                NavBruker.opprettNy(new AktørId(233L), Språkkode.nn)), BehandlingType.TILBAKEKREVING).build();

        when(grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)).thenReturn(true);
        when(grunnlagRepository.erKravgrunnlagSperret(behandlingId)).thenReturn(false);
        when(behandlingRepository.hentBehandling(behandlingId)).thenReturn(behandling);

        kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);

        verify(kravVedtakStatusRepository).lagre(behandlingId, kravVedtakStatus437);
        verify(grunnlagRepository, times(1)).sperrGrunnlag(anyLong());
        verify(behandlingskontrollTjeneste).settBehandlingPåVent(eq(behandling), eq(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG),
            eq(BehandlingStegType.TBKGSTEG), any(), eq(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG));
    }

    private static KravVedtakStatus437 lagKravVedtakStatus(KravStatusKode statusKode) {
        return KravVedtakStatus437.builder()
            .medKravStatusKode(statusKode)
            .medVedtakId(100L)
            .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
            .build();
    }
}
