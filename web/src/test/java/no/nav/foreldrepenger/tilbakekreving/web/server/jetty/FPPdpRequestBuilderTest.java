package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.TilbakekrevingAbacAttributtType;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.CommonAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.AbacBehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.AbacFagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.FPPdpRequestBuilder;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.FpAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.FpsakPipKlient;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp.PipDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class FPPdpRequestBuilderTest {

    private static final String DUMMY_ID_TOKEN = "dummyheader.dymmypayload.dummysignaturee";
    private static final String SAKSNUMMER = "5555";
    private static final UUID FPSAK_BEHANDLING_UUID = java.util.UUID.randomUUID();
    private static final UUID BEHANDLING_UUID = java.util.UUID.randomUUID();
    private static final Long BEHANDLING_ID = 1234L;
    private static final String SAK_STATUS = "OPPR";
    private static final String PERSON1 = "8888888";
    private static final String PERSON2 = "4444444";
    private static final String BEHANDLING_STATUS = "UTRED";
    private static final String SAKSBEHANDLER = "Z12345";

    private final PipRepository pipRepository = mock(PipRepository.class);
    private final FpsakPipKlient fpsakPipKlient = mock(FpsakPipKlient.class);

    private final FPPdpRequestBuilder requestBuilder = new FPPdpRequestBuilder(pipRepository, fpsakPipKlient);

    @Test
    public void skal_hente_behandling_og_fagsak_informasjon_når_input_er_behandling_id() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID));

        when(pipRepository.hentBehandlingData(BEHANDLING_ID))
            .thenReturn(returnData(true, true));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON1, PERSON2);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isEqualTo(SAKSBEHANDLER);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS)).isEqualTo(AbacFagsakStatus.OPPRETTET.getEksternKode());
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS)).isEqualTo(AbacBehandlingStatus.UTREDES.getEksternKode());
        assertThat(request.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }

    @Test
    public void skal_hente_behandling_og_fagsak_informasjon_når_input_er_behandlinguuid() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, BEHANDLING_UUID));

        when(pipRepository.hentBehandlingData(BEHANDLING_UUID))
            .thenReturn(returnData(true, true));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON1, PERSON2);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isEqualTo(SAKSBEHANDLER);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS)).isEqualTo(AbacFagsakStatus.OPPRETTET.getEksternKode());
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS)).isEqualTo(AbacBehandlingStatus.UTREDES.getEksternKode());
        assertThat(request.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }

    @Test
    public void skal_hente_behandlinginfo_fra_fpsak_når_input_er_fpsak_behandlingid() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID));

        PipDto pipDto = new PipDto();
        pipDto.setAktørIder(Set.of(new AktørId(PERSON1)));
        pipDto.setBehandlingStatus(BehandlingStatus.OPPRETTET.getKode());
        pipDto.setFagsakStatus(FagsakStatus.UNDER_BEHANDLING.getKode());
        when(fpsakPipKlient.hentPipdataForFpsakBehandling(FPSAK_BEHANDLING_UUID)).thenReturn(pipDto);

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON1);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isNull();
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS)).isEqualTo(AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS)).isEqualTo(AbacBehandlingStatus.OPPRETTET.getEksternKode());
        assertThat(request.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }


    @Test
    public void skal_hente_aktører_fra_fpsak_når_input_er_saksnummer() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, SAKSNUMMER));

        when(fpsakPipKlient.hentAktørIderSomString(new Saksnummer(SAKSNUMMER))).thenReturn(Set.of(PERSON2));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON2);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isNull();
        assertThat(request.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }

    @Test
    public void skal_hente_behandling_og_fagsak_informasjon_når_saksnummer_er_input_uten_saksbehandler() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID));

        when(pipRepository.hentBehandlingData(BEHANDLING_ID))
            .thenReturn(returnData(true, false));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON1, PERSON2);
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isNullOrEmpty();
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS)).isEqualTo(AbacFagsakStatus.OPPRETTET.getEksternKode());
        assertThat(request.getString(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS)).isEqualTo(AbacBehandlingStatus.UTREDES.getEksternKode());
        assertThat(request.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }

    @Test
    public void skal_kaste_feil_ved_flere_behandlingIder() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 85392L));

        assertThatThrownBy(() -> requestBuilder.lagPdpRequest(attributter))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("FPT-426124");
    }

    @Test
    public void skal_kaste_feil_ved_både_behandlingId_og_fpsak_behandlingUuid() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                .leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID));

        assertThatThrownBy(() -> requestBuilder.lagPdpRequest(attributter))
            .isInstanceOf(TekniskException.class)
            .hasMessageContainingAll("FPT-317633", BEHANDLING_ID.toString(), FPSAK_BEHANDLING_UUID.toString());
    }

    @Test
    public void skal_kaste_feil_ved_både_behandlingId_og_behandlingUuid() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, BEHANDLING_UUID));

        assertThatThrownBy(() -> requestBuilder.lagPdpRequest(attributter))
            .isInstanceOf(TekniskException.class)
            .hasMessageContainingAll("FPT-317633", BEHANDLING_ID.toString(), BEHANDLING_UUID.toString());
    }

    @Test
    public void skal_kaste_feil_ved_både_behandlingUuid_og_fpsak_behandlingUuid() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, BEHANDLING_UUID)
                .leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, FPSAK_BEHANDLING_UUID));

        assertThatThrownBy(() -> requestBuilder.lagPdpRequest(attributter))
            .isInstanceOf(TekniskException.class)
            .hasMessageContainingAll("FPT-317634", BEHANDLING_UUID.toString(), FPSAK_BEHANDLING_UUID.toString());
    }

    private AbacAttributtSamling byggAbacAttributtsamling() {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN);
        attributtSamling.setActionType(BeskyttetRessursActionAttributt.READ);
        attributtSamling.setResource(AbacProperty.FAGSAK);
        return attributtSamling;
    }

    private Optional<PipBehandlingData> returnData(boolean notEmpty, boolean mAnsvarligSaksbehandler) {
        Optional<PipBehandlingData> behandlingData = Optional.empty();
        if (notEmpty) {
            PipBehandlingData data = new PipBehandlingData();
            data.setSaksnummer(SAKSNUMMER);
            data.setBehandlingId(BEHANDLING_ID);
            data.setFagsakstatus(SAK_STATUS);
            data.leggTilAktørId(new AktørId(PERSON1));
            data.leggTilAktørId(new AktørId(PERSON2));
            data.setStatusForBehandling(BEHANDLING_STATUS);
            if (mAnsvarligSaksbehandler) {
                data.setAnsvarligSaksbehandler(SAKSBEHANDLER);
            }
            behandlingData = Optional.of(data);
        }
        return behandlingData;
    }

}
