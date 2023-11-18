package no.nav.foreldrepenger.tilbakekreving.behandling.steg.beregn.migrer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("start.korriger.beregningsresultat")
public class TriggKorrigerBeregningsresultatTask implements ProsessTaskHandler {
    private static final Logger logger = LoggerFactory.getLogger(TriggKorrigerBeregningsresultatTask.class);

    private EntityManager entityManager;

    public TriggKorrigerBeregningsresultatTask() {
        //for CDI proxy
    }

    @Inject
    public TriggKorrigerBeregningsresultatTask(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        if (prosessTaskData.getBehandlingId() != null) {
            throw new IllegalArgumentException(
                "Denne tasken tar ikke behandling som parameter. Mente du egentlig å starte 'migrer.beregningsresultat' ?");
        }
        Query query = entityManager.createNativeQuery(
            """
                insert into prosess_task (id, task_type, task_parametere)
                 select seq_prosess_task.nextval, 'korriger.beregningsresultat', 'behandlingId=' || b.id
                 from behandling b
                 where b.behandling_status = 'AVSLU'
                 and exists (select 1 from gr_beregningsresultat gr where gr.behandling_id = b.id and gr.aktiv = 'J')
                 and exists (select 1 from OKO_XML_SENDT oko where oko.behandling_id = b.id)
                """
        );
        int rader = query.executeUpdate();
        logger.info("Opprettet {} tasker av migrer.beregnignsresultat", rader);
    }
}