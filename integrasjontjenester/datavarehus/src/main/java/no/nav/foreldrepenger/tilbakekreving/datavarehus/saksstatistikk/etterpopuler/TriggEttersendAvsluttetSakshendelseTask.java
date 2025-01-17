package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.etterpopuler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "start.ettersend.sakshendelser", prioritet = 4)
public class TriggEttersendAvsluttetSakshendelseTask implements ProsessTaskHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TriggEttersendAvsluttetSakshendelseTask.class);

    private EntityManager entityManager;

    public TriggEttersendAvsluttetSakshendelseTask() {
        //for CDI proxy
    }

    @Inject
    public TriggEttersendAvsluttetSakshendelseTask(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        if (prosessTaskData.getBehandlingIdAsLong() != null) {
            throw new IllegalArgumentException(
                "Denne tasken tar ikke behandling som parameter. Mente du egentlig å starte 'migrer.beregningsresultat' ?");
        }
        Query query = entityManager.createNativeQuery(
            """
                insert into prosess_task (id, task_type, task_parametere)
                 select seq_prosess_task.nextval, 'dvh.ettersend.sakshendelser', 'behandlingId=' || b.id
                 from behandling b
                 where b.behandling_status = 'AVSLU'
                """
        );
        int rader = query.executeUpdate();
        LOG.info("Opprettet {} tasker av migrer.beregnignsresultat", rader);
    }
}
