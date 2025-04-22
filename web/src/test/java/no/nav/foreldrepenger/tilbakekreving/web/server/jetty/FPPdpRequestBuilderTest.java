package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.FPPdpRequestBuilder;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.FpsakPipKlient;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

class FPPdpRequestBuilderTest {

    private static final String SAKSNUMMER = "5555";
    private static final UUID FPSAK_BEHANDLING_UUID = java.util.UUID.randomUUID();
    private static final UUID BEHANDLING_UUID = java.util.UUID.randomUUID();
    private static final Long BEHANDLING_ID = 1234L;
    private static final String PERSON1 = "8888888888888";
    private static final String PERSON2 = "4444444444444";
    private static final BehandlingStatus BEHANDLING_STATUS = BehandlingStatus.UTREDES;
    private static final String SAKSBEHANDLER = "Z12345";

    private final PipRepository pipRepository = mock(PipRepository.class);
    private final FpsakPipKlient fpsakPipKlient = mock(FpsakPipKlient.class);

    private final FPPdpRequestBuilder requestBuilder = new FPPdpRequestBuilder(pipRepository, fpsakPipKlient);

    @Test
    void skal_hente_behandling_og_fagsak_informasjon_når_input_er_behandling_id() {
        var attributter = AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID);

        when(pipRepository.hentBehandlingData(BEHANDLING_ID))
                .thenReturn(returnData(true, true));

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(request.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER).verdi()).isEqualTo(SAKSBEHANDLER);
        assertThat(request.getResource(ForeldrepengerDataKeys.FAGSAK_STATUS).verdi()).isEqualTo(PipFagsakStatus.UNDER_BEHANDLING.getVerdi());
        assertThat(request.getResource(ForeldrepengerDataKeys.BEHANDLING_STATUS).verdi()).isEqualTo(PipBehandlingStatus.UTREDES.getVerdi());
    }

    @Test
    void skal_hente_behandling_og_fagsak_informasjon_når_input_er_behandlinguuid() {
        var attributter = AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, BEHANDLING_UUID);

        when(pipRepository.hentBehandlingData(BEHANDLING_UUID))
                .thenReturn(returnData(true, true));

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(request.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER).verdi()).isEqualTo(SAKSBEHANDLER);
        assertThat(request.getResource(ForeldrepengerDataKeys.FAGSAK_STATUS).verdi()).isEqualTo(PipFagsakStatus.UNDER_BEHANDLING.getVerdi());
        assertThat(request.getResource(ForeldrepengerDataKeys.BEHANDLING_STATUS).verdi()).isEqualTo(PipBehandlingStatus.UTREDES.getVerdi());
    }

    @Test
    void skal_hente_behandlinginfo_fra_fpsak_når_input_er_fpsak_behandlingid() {
        var attributter = AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID);

        when(fpsakPipKlient.saksnummerForBehandling(FPSAK_BEHANDLING_UUID)).thenReturn(SAKSNUMMER);

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(request.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER)).isNull();
        assertThat(request.getResource(ForeldrepengerDataKeys.FAGSAK_STATUS).verdi()).isEqualTo(PipFagsakStatus.UNDER_BEHANDLING.getVerdi());
        assertThat(request.getResource(ForeldrepengerDataKeys.BEHANDLING_STATUS).verdi()).isEqualTo(PipBehandlingStatus.UTREDES.getVerdi());
    }

    @Test
    void skal_hente_behandlinginfo_fra_fpsak_når_input_er_fpsak_behandlingid_avsluttet_sak() {
        var attributter = AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID);

        when(fpsakPipKlient.saksnummerForBehandling(FPSAK_BEHANDLING_UUID)).thenReturn(SAKSNUMMER);

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(request.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER)).isNull();
        assertThat(request.getResource(ForeldrepengerDataKeys.FAGSAK_STATUS).verdi()).isEqualTo(PipFagsakStatus.UNDER_BEHANDLING.getVerdi());
        assertThat(request.getResource(ForeldrepengerDataKeys.BEHANDLING_STATUS).verdi()).isEqualTo(PipBehandlingStatus.UTREDES.getVerdi());
    }


    @Test
    void skal_ikke_hente_aktører_fra_fpsak_når_input_er_saksnummer() {
        var attributter = AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, SAKSNUMMER);

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(request.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER)).isNull();
    }

    @Test
    void skal_hente_behandling_og_fagsak_informasjon_når_saksnummer_er_input_uten_saksbehandler() {
        var attributter = AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID);

        when(pipRepository.hentBehandlingData(BEHANDLING_ID))
                .thenReturn(returnData(true, false));

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(request.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER)).isNull();
        assertThat(request.getResource(ForeldrepengerDataKeys.FAGSAK_STATUS).verdi()).isEqualTo(PipFagsakStatus.UNDER_BEHANDLING.getVerdi());
        assertThat(request.getResource(ForeldrepengerDataKeys.BEHANDLING_STATUS).verdi()).isEqualTo(PipBehandlingStatus.UTREDES.getVerdi());
    }

    @Test
    void skal_kaste_feil_ved_flere_behandlingIder() {
        var attributter = AbacDataAttributter.opprett()
                        .leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                        .leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, 85392L);

        assertThatThrownBy(() -> requestBuilder.lagAppRessursData(attributter))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FPT-426124");
    }

    @Test
    void skal_kaste_feil_ved_både_behandlingId_og_fpsak_behandlingUuid() {
        var attributter = AbacDataAttributter.opprett()
                        .leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                        .leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID);

        assertThatThrownBy(() -> requestBuilder.lagAppRessursData(attributter))
                .isInstanceOf(TekniskException.class)
                .hasMessageContainingAll("FPT-426124", BEHANDLING_ID.toString(), FPSAK_BEHANDLING_UUID.toString());
    }

    @Test
    void skal_kaste_feil_ved_både_behandlingId_og_behandlingUuid() {
        var attributter = AbacDataAttributter.opprett()
                        .leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                        .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, BEHANDLING_UUID);

        assertThatThrownBy(() -> requestBuilder.lagAppRessursData(attributter))
                .isInstanceOf(TekniskException.class)
                .hasMessageContainingAll("FPT-426124", BEHANDLING_ID.toString(), BEHANDLING_UUID.toString());
    }

    @Test
    void skal_kaste_feil_ved_både_behandlingUuid_og_fpsak_behandlingUuid() {
        var attributter = AbacDataAttributter.opprett()
                        .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, BEHANDLING_UUID)
                        .leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID);

        assertThatThrownBy(() -> requestBuilder.lagAppRessursData(attributter))
                .isInstanceOf(TekniskException.class)
                .hasMessageContainingAll("FPT-426124", BEHANDLING_UUID.toString(), FPSAK_BEHANDLING_UUID.toString());
    }

    private Optional<PipBehandlingData> returnData(boolean notEmpty, boolean mAnsvarligSaksbehandler) {
        if (notEmpty) {
            var bruksaksbehandler = mAnsvarligSaksbehandler ? SAKSBEHANDLER : null;
            var data = new PipBehandlingData(BEHANDLING_ID, BEHANDLING_UUID, new Saksnummer(SAKSNUMMER), new AktørId(PERSON1),
                BEHANDLING_STATUS, bruksaksbehandler);
            return Optional.of(data);
        }
        return Optional.empty();
    }

}
