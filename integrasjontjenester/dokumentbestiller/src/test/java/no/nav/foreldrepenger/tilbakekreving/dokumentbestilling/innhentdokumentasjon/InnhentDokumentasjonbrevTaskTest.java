package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Period;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InnhentDokumentasjonbrevTaskTest extends DokumentBestillerTestOppsett {

    private InnhentDokumentasjonbrevTjeneste mockInnhentDokumentasjonbrevTjeneste;
    private BehandlingModellRepository mockBehandlingModellRepository = mock(BehandlingModellRepository.class);

    private BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste;
    private InnhentDokumentasjonbrevTask innhentDokumentasjonBrevTask;

    @Before
    public void setup() {
        mockInnhentDokumentasjonbrevTjeneste = mock(InnhentDokumentasjonbrevTjeneste.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, mockBehandlingModellRepository, null);

        innhentDokumentasjonBrevTask = new InnhentDokumentasjonbrevTask(behandlingRepository,
            mockInnhentDokumentasjonbrevTjeneste,
            behandlingskontrollTjeneste,
            Period.ofWeeks(3));
    }

    @Test
    public void skal_sende_innhent_dokumentasjonbrev_og_sett_behandling_på_vent() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(InnhentDokumentasjonbrevTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Ber om flere opplysninger");

        innhentDokumentasjonBrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }
}
