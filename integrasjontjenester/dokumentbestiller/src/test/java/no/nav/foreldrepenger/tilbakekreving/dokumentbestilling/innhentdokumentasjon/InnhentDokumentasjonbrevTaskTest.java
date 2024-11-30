package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

class InnhentDokumentasjonbrevTaskTest extends DokumentBestillerTestOppsett {

    private InnhentDokumentasjonbrevTjeneste mockInnhentDokumentasjonbrevTjeneste;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private InnhentDokumentasjonbrevTask innhentDokumentasjonBrevTask;

    @BeforeEach
    void setup() {
        mockInnhentDokumentasjonbrevTjeneste = mock(InnhentDokumentasjonbrevTjeneste.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));

        innhentDokumentasjonBrevTask = new InnhentDokumentasjonbrevTask(repositoryProvider,
                mockInnhentDokumentasjonbrevTjeneste,
                behandlingskontrollTjeneste
        );
    }

    @Test
    void skal_sende_innhent_dokumentasjonbrev_og_sett_behandling_på_vent() {
        var prosessTaskData = ProsessTaskData.forProsessTask(InnhentDokumentasjonbrevTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setPayload("Ber om flere opplysninger");
        prosessTaskData.setProperty(TaskProperties.BESTILLING_UUID, UUID.randomUUID().toString());

        innhentDokumentasjonBrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }

    @Test
    void skal_sende_innhent_dokumentasjonbrev_og_sett_behandling_på_vent_når_verge_finnes() {
        var prosessTaskData = ProsessTaskData.forProsessTask(InnhentDokumentasjonbrevTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setProperty(TaskProperties.BESTILLING_UUID, UUID.randomUUID().toString());
        prosessTaskData.setPayload("Ber om flere opplysninger");

        vergeRepository.lagreVergeInformasjon(behandling.getId(), lagVerge());

        innhentDokumentasjonBrevTask.doTask(prosessTaskData);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        verify(mockInnhentDokumentasjonbrevTjeneste, atLeast(2)).sendInnhentDokumentasjonBrev(anyLong(), anyString(),
            any(BrevMottaker.class), any(UUID.class));
    }

    @Test
    void skal_feile_om_bestilling_uuid_mangler() {
        var prosessTaskData = ProsessTaskData.forProsessTask(InnhentDokumentasjonbrevTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setPayload("Ber om flere opplysninger");

        assertThrows(NullPointerException.class, () -> innhentDokumentasjonBrevTask.doTask(prosessTaskData));
    }
}
