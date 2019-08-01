package no.nav.foreldrepenger.tilbakekreving.simulering.tjeneste;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.simulering.klient.FpOppdragRestKlient;

public class SimuleringIntegrasjonTjenesteTest {

    private FpOppdragRestKlient restKlientMock = mock(FpOppdragRestKlient.class);

    private SimuleringIntegrasjonTjeneste integrasjonTjeneste = new SimuleringIntegrasjonTjeneste(restKlientMock);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final Long BEHANDLING_ID = 1L;


    @Test
    public void skal_hentResultat_VedBehandlingIdNull() {
        expectedException.expect(NullPointerException.class);
        integrasjonTjeneste.hentResultat(BEHANDLING_ID);
    }

    @Test
    public void skal_hentResultat_VedBehandlingIdIkkeNull() {
        integrasjonTjeneste.hentResultat(BEHANDLING_ID);
        verify(restKlientMock, atLeastOnce()).hentResultat(BEHANDLING_ID);
    }
}