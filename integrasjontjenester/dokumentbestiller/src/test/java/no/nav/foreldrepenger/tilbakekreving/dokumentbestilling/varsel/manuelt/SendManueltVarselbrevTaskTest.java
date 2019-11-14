package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class SendManueltVarselbrevTaskTest extends DokumentBestillerTestOppsett {

    private ManueltVarselBrevTjeneste mockManueltVarselBrevTjeneste = mock(ManueltVarselBrevTjeneste.class);
    private BehandlingModellRepository mockBehandlingModellRepository = mock(BehandlingModellRepository.class);

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, mockBehandlingModellRepository, null);
    private SendManueltVarselbrevTask varselbrevTask = new SendManueltVarselbrevTask(behandlingRepository,
        mockManueltVarselBrevTjeneste,
        behandlingskontrollTjeneste,
        Period.ofWeeks(3));

    @Test
    public void skal_sende_manuelt_varselbrev_og_sett_behandling_på_vent() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(TaskProperty.FRITEKST, "Sender manuelt varsel brev");
        prosessTaskData.setProperty(TaskProperty.MAL_TYPE, DokumentMalType.VARSEL_DOK.getKode());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }

    @Test
    public void skal_sende_korrigert_varselbrev_og_sett_behandling_på_vent() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(TaskProperty.FRITEKST, "Sender korrigert varsel brev");
        prosessTaskData.setProperty(TaskProperty.MAL_TYPE, DokumentMalType.KORRIGERT_VARSEL_DOK.getKode());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }
}
