package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.HentKorrigertKravgrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@CdiDbAwareTest
class ForvaltningKravgrunnlagRestTjenesteTest {

    ForvaltningKravgrunnlagRestTjeneste forvaltningKravgrunnlagRestTjeneste;

    @Mock
    KravgrunnlagRepository kravgrunnlagRepository;

    @Mock
    KravgrunnlagHenter kravgrunnlagHenter;

    @Inject
    BehandlingRepositoryProvider repositoryProvider;

    @Inject
    ØkonomiMottattXmlRepository mottattXmlRepository;

    ScenarioSimple scenario = ScenarioSimple.simple();
    Behandling behandling;

    @BeforeEach
    void setup() {
        var forvaltningTjeneste = new ForvaltningTjeneste(mock(ProsessTaskTjeneste.class), mottattXmlRepository,
            repositoryProvider.getBehandlingRepository(), kravgrunnlagRepository, kravgrunnlagHenter);

        forvaltningKravgrunnlagRestTjeneste = new ForvaltningKravgrunnlagRestTjeneste(repositoryProvider.getBehandlingRepository(), forvaltningTjeneste, kravgrunnlagRepository);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    void skal_hente_korrigert_kravgrunnlag() {
        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningKravgrunnlagRestTjeneste.hentKorrigertKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    void skal_ikke_hente_korrigert_kravgrunnlag_når_behandling_er_avsluttet() {
        behandling.avsluttBehandling();
        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningKravgrunnlagRestTjeneste.hentKorrigertKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void annuler_krav_grunnlag_ok() {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(kravgrunnlagRepository.hentIsAktivFor(behandling.getId())).thenReturn(grunnlag);
        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningKravgrunnlagRestTjeneste.annullerKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(kravgrunnlagHenter).annullerKravgrunnlag(behandling.getId(), grunnlag);
    }

    @Test
    void annuler_krav_grunnlag_500_kravgrunnlag_finnes_ikke() {
        doThrow(new IllegalStateException("Finnes ikke kravgrunnlag")).when(kravgrunnlagRepository).hentIsAktivFor(behandling.getId());

        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningKravgrunnlagRestTjeneste.annullerKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

}
