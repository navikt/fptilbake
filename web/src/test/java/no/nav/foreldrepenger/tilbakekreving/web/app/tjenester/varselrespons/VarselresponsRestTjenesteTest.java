package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;

public class VarselresponsRestTjenesteTest {

    private static Long BEHANDLING_ID = 1242364L;
    private static ResponsKanal RESPONS_KANAL = ResponsKanal.SELVBETJENING;
    private static Boolean AKSEPTERT_GRUNNLAG = true;

    private VarselresponsRestTjeneste restTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    private VarselresponsTjeneste varselresponsTjeneste;

    @BeforeEach
    public void setup() {
        varselresponsTjeneste = mock(VarselresponsTjeneste.class);
        gjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
        restTjeneste = new VarselresponsRestTjeneste(varselresponsTjeneste, gjenopptaBehandlingTjeneste);
    }

    @Test
    public void test_skal_kalle_gjenoppta_behandling() {
        VarselresponsDto input = new VarselresponsDto(BEHANDLING_ID, RESPONS_KANAL, AKSEPTERT_GRUNNLAG);
        restTjeneste.registrerBrukerrespons(input);

        verify(gjenopptaBehandlingTjeneste, atLeastOnce()).fortsettBehandling(BEHANDLING_ID);
    }

    @Test
    public void test_skal_hente_lagret_respons() {
        when(varselresponsTjeneste.hentRespons(anyLong())).thenReturn(Optional.of(Varselrespons.builder()
                .medBehandlingId(BEHANDLING_ID)
                .setKilde(RESPONS_KANAL.getDbKode())
                .setAkseptertFaktagrunnlag(AKSEPTERT_GRUNNLAG)
                .build()));

        Response result = restTjeneste.finnRespons(new BehandlingReferanse(BEHANDLING_ID));

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isInstanceOf(VarselresponsDto.class);
    }

}
