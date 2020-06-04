package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Period;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class SendManueltVarselbrevTaskTest extends DokumentBestillerTestOppsett {

    private ManueltVarselBrevTjeneste mockManueltVarselBrevTjeneste = mock(ManueltVarselBrevTjeneste.class);
    private BehandlingModellRepository mockBehandlingModellRepository = mock(BehandlingModellRepository.class);

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private SendManueltVarselbrevTask varselbrevTask;

    @Inject
    private VergeRepository vergeRepository;

    @Before
    public void setup() {
        behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, mockBehandlingModellRepository, null);

        varselbrevTask = new SendManueltVarselbrevTask(repositoryProvider,
            mockManueltVarselBrevTjeneste,
            behandlingskontrollTjeneste,
            Period.ofWeeks(3));
    }

    @Test
    public void skal_sende_manuelt_varselbrev_og_sett_behandling_på_vent() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Sender manuelt varsel brev");
        prosessTaskData.setProperty(TaskProperty.MAL_TYPE, DokumentMalType.VARSEL_DOK.getKode());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }

    @Test
    public void skal_sende_korrigert_varselbrev_og_sett_behandling_på_vent() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Sender korrigert varsel brev");
        prosessTaskData.setProperty(TaskProperty.MAL_TYPE, DokumentMalType.KORRIGERT_VARSEL_DOK.getKode());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }

    @Test
    public void skal_sende_manuelt_varselbrev_med_verge() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Sender manuelt varsel brev");
        prosessTaskData.setProperty(TaskProperty.MAL_TYPE, DokumentMalType.VARSEL_DOK.getKode());
        vergeRepository.lagreVergeInformasjon(behandling.getId(), lagVerge());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        verify(mockManueltVarselBrevTjeneste, atLeast(2)).sendManueltVarselBrev(anyLong(), anyString(), any(BrevMottaker.class));
    }
}
