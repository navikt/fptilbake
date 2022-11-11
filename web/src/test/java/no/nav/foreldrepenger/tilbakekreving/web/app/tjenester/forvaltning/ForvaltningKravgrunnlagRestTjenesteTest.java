package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.HentKorrigertKravgrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@CdiDbAwareTest
class ForvaltningKravgrunnlagRestTjenesteTest {

    ForvaltningKravgrunnlagRestTjeneste forvaltningKravgrunnlagRestTjeneste;

    @Mock
    KravgrunnlagRepository kravgrunnlagRepository;

    @Inject
    BehandlingRepositoryProvider repositoryProvider;

    @Inject
    ØkonomiMottattXmlRepository mottattXmlRepository;

    ScenarioSimple scenario = ScenarioSimple.simple();
    Behandling behandling;

    @BeforeEach
    void setup() {
        var forvaltningTjeneste = new ForvaltningTjeneste(mock(ProsessTaskTjeneste.class), mottattXmlRepository, repositoryProvider.getBehandlingRepository(), kravgrunnlagRepository, mock(ØkonomiConsumer.class));

        forvaltningKravgrunnlagRestTjeneste = new ForvaltningKravgrunnlagRestTjeneste(repositoryProvider.getBehandlingRepository(), forvaltningTjeneste);
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
        when(kravgrunnlagRepository.hentIsAktivFor(behandling.getId())).thenReturn(
            Kravgrunnlag431.builder()
                .medVedtakId(13434L)
                .medEksternKravgrunnlagId("1234")
                .medKravStatusKode(KravStatusKode.NYTT)
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medUtbetalesTilId("bruker")
                .medUtbetIdType(GjelderType.APPBRUKER)
                .medFagSystemId("sfsdf")
                .build());

        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningKravgrunnlagRestTjeneste.annulerKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    void annuler_krav_grunnlag_500_kravgrunnlag_finnes_ikke() {
        when(kravgrunnlagRepository.hentIsAktivFor(behandling.getId())).thenReturn(null);

        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningKravgrunnlagRestTjeneste.annulerKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

}