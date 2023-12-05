package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

import java.util.UUID;

class SendManueltVarselbrevTaskTest extends DokumentBestillerTestOppsett {

    private ManueltVarselBrevTjeneste mockManueltVarselBrevTjeneste = mock(ManueltVarselBrevTjeneste.class);

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private SendManueltVarselbrevTask varselbrevTask;

    @BeforeEach
    void setup() {
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));

        varselbrevTask = new SendManueltVarselbrevTask(repositoryProvider,
                mockManueltVarselBrevTjeneste,
                behandlingskontrollTjeneste
        );
    }

    @Test
    void skal_sende_manuelt_varselbrev_og_sett_behandling_på_vent() {
        var prosessTaskData = ProsessTaskData.forProsessTask(SendManueltVarselbrevTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Sender manuelt varsel brev");
        prosessTaskData.setProperty(SendManueltVarselbrevTask.MAL_TYPE, DokumentMalType.VARSEL_DOK.getKode());
        prosessTaskData.setProperty(SendManueltVarselbrevTask.BESTILLING_UUID, UUID.randomUUID().toString());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }

    @Test
    void skal_sende_korrigert_varselbrev_og_sett_behandling_på_vent() {
        var prosessTaskData = ProsessTaskData.forProsessTask(SendManueltVarselbrevTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Sender korrigert varsel brev");
        prosessTaskData.setProperty(SendManueltVarselbrevTask.MAL_TYPE, DokumentMalType.KORRIGERT_VARSEL_DOK.getKode());
        prosessTaskData.setProperty(SendManueltVarselbrevTask.BESTILLING_UUID, UUID.randomUUID().toString());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }

    @Test
    void skal_sende_manuelt_varselbrev_med_verge() {
        var prosessTaskData = ProsessTaskData.forProsessTask(SendManueltVarselbrevTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setPayload("Sender manuelt varsel brev");
        prosessTaskData.setProperty(SendManueltVarselbrevTask.MAL_TYPE, DokumentMalType.VARSEL_DOK.getKode());
        prosessTaskData.setProperty(SendManueltVarselbrevTask.BESTILLING_UUID, UUID.randomUUID().toString());

        vergeRepository.lagreVergeInformasjon(behandling.getId(), lagVerge());

        varselbrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        verify(mockManueltVarselBrevTjeneste, atLeast(2)).sendManueltVarselBrev(anyLong(), anyString(), any(BrevMottaker.class), any(UUID.class));
    }
}
