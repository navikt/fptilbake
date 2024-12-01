package no.nav.foreldrepenger.tilbakekreving.los.klient.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.los.klient.producer.LosKafkaProducerAiven;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.hendelser.behandling.Behandlingstype;
import no.nav.vedtak.hendelser.behandling.Hendelse;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.hendelser.behandling.Ytelse;
import no.nav.vedtak.hendelser.behandling.v1.BehandlingHendelseV1;

@ExtendWith(JpaExtension.class)
class FpLosPubliserEventTaskAivenTest {

    private BehandlingRepositoryProvider repositoryProvider;

    private LosKafkaProducerAiven mockKafkaProducerAiven = mock(LosKafkaProducerAiven.class);

    private FpLosPubliserEventTask losPubliserEventTask;

    private Behandling behandling;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @BeforeEach
    void setup(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        losPubliserEventTask = new FpLosPubliserEventTask(repositoryProvider, mockKafkaProducerAiven, Fagsystem.FPTILBAKE);

        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
    }

    @Test
    void skal_publisere_fplos_data_til_kafka() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));

        var prosessTaskData = lagProsessTaskData(Hendelse.VENTETILSTAND);
        losPubliserEventTask.doTask(prosessTaskData);

        var eventCaptor = ArgumentCaptor.forClass(BehandlingHendelseV1.class);
        verify(mockKafkaProducerAiven, atLeastOnce()).sendHendelseFplos(any(), eventCaptor.capture());
        var event = eventCaptor.getValue();


        assertThat(event.getHendelse()).isEqualTo(Hendelse.VENTETILSTAND);
        assertThat(event.getKildesystem()).isEqualTo(Kildesystem.FPTILBAKE);
        assertThat(event.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(event.getAktørId().getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelse()).isEqualTo(Ytelse.FORELDREPENGER);
        assertThat(event.getBehandlingstype()).isEqualTo(Behandlingstype.TILBAKEBETALING);
    }


    @Test
    void skal_publisere_fplos_data_til_kafka_for_henleggelse_når_kravgrunnlag_ikke_finnes() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.FAKTA_FEILUTBETALING, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));

        var prosessTaskData = lagProsessTaskData(Hendelse.AKSJONSPUNKT);
        losPubliserEventTask.doTask(prosessTaskData);

        var eventCaptor = ArgumentCaptor.forClass(BehandlingHendelseV1.class);
        verify(mockKafkaProducerAiven, atLeastOnce()).sendHendelseFplos(any(), eventCaptor.capture());
        var event = eventCaptor.getValue();


        assertThat(event.getHendelse()).isEqualTo(Hendelse.AKSJONSPUNKT);
        assertThat(event.getKildesystem()).isEqualTo(Kildesystem.FPTILBAKE);
        assertThat(event.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(event.getAktørId().getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelse()).isEqualTo(Ytelse.FORELDREPENGER);
        assertThat(event.getBehandlingstype()).isEqualTo(Behandlingstype.TILBAKEBETALING);

    }

    @Test
    void skal_publisere_fplos_data_til_kafka_for_opprettet_behandling() {
        var prosessTaskData = lagProsessTaskData(Hendelse.OPPRETTET);
        losPubliserEventTask.doTask(prosessTaskData);

        var eventCaptor = ArgumentCaptor.forClass(BehandlingHendelseV1.class);
        verify(mockKafkaProducerAiven, atLeastOnce()).sendHendelseFplos(any(), eventCaptor.capture());
        var event = eventCaptor.getValue();

        assertThat(event.getHendelse()).isEqualTo(Hendelse.OPPRETTET);
        assertThat(event.getKildesystem()).isEqualTo(Kildesystem.FPTILBAKE);
        assertThat(event.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(event.getAktørId().getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelse()).isEqualTo(Ytelse.FORELDREPENGER);
        assertThat(event.getBehandlingstype()).isEqualTo(Behandlingstype.TILBAKEBETALING);

    }


    private ProsessTaskData lagProsessTaskData(Hendelse hendelse) {
        var prosessTaskData = ProsessTaskData.forProsessTask(FpLosPubliserEventTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setProperty(FpLosPubliserEventTask.PROPERTY_EVENT_NAME, hendelse.name());
        return prosessTaskData;
    }

}
