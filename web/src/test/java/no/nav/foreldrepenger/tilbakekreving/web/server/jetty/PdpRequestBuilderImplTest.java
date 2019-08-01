package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.abac.xacml.NavAttributter;
import no.nav.abac.xacml.StandardAttributter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.pip.fpinfo.intern.FpsakPipKlient;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.PdpRequestBuilderImpl;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class PdpRequestBuilderImplTest {

    private static final String DUMMY_ID_TOKEN = "dummyheader.dymmypayload.dummysignaturee";
    private static final String SAKSNUMMER = "5555";
    private static final Long BEHANDLING_ID = 1234L;
    private static final String SAK_STATUS = "OPPR";
    private static final String PERSON1 = "8888888";
    private static final String PERSON2 = "4444444";
    private static final String BEHANDLING_STATUS = "UTRED";
    private static final String SAKSBEHANDLER = "Z12345";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PipRepository pipRepository = mock(PipRepository.class);
    private FpsakPipKlient fpsakPipKlient = mock(FpsakPipKlient.class);

    private PdpRequestBuilderImpl requestBuilder = new PdpRequestBuilderImpl(pipRepository, fpsakPipKlient);

    @Test
    public void skal_hente_behandling_og_fagsak_informasjon_når_saksnummer_er_input_med_saksbehandler() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID));

        when(pipRepository.hentBehandlingData(BEHANDLING_ID))
            .thenReturn(returnData(true, true));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON1, PERSON2);
        assertThat(request.getString(NavAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isEqualTo(SAKSBEHANDLER);
        assertThat(request.getString(NavAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS)).isEqualTo(AbacFagsakStatus.OPPRETTET.getEksternKode());
        assertThat(request.getString(NavAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS)).isEqualTo(AbacBehandlingStatus.UTREDES.getEksternKode());
        assertThat(request.getString(StandardAttributter.ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }

    @Test
    public void skal_hente_behandling_og_fagsak_informasjon_når_saksnummer_er_input_uten_saksbehandler() {
        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID));

        when(pipRepository.hentBehandlingData(BEHANDLING_ID))
            .thenReturn(returnData(true, false));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(PERSON1, PERSON2);
        assertThat(request.getString(NavAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER)).isNullOrEmpty();
        assertThat(request.getString(NavAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS)).isEqualTo(AbacFagsakStatus.OPPRETTET.getEksternKode());
        assertThat(request.getString(NavAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS)).isEqualTo(AbacBehandlingStatus.UTREDES.getEksternKode());
        assertThat(request.getString(StandardAttributter.ACTION_ID)).isEqualTo(BeskyttetRessursActionAttributt.READ.getEksternKode());
    }

    @Test
    public void skal_kaste_feil_ved_flere_behandlingIder() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-426124");

        AbacAttributtSamling attributter = byggAbacAttributtsamling().leggTil(
            AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, BEHANDLING_ID)
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 85392L));

        requestBuilder.lagPdpRequest(attributter);
    }

    private AbacAttributtSamling byggAbacAttributtsamling() {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN);
        attributtSamling.setActionType(BeskyttetRessursActionAttributt.READ);
        attributtSamling.setResource(BeskyttetRessursResourceAttributt.FAGSAK);
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
