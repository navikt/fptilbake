package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.hibernate.jpa.HibernateHints;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskStatusUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskEvent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe.Entry;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEntitet;

@ApplicationScoped
public class FagsakProsessTaskRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FagsakProsessTaskRepository.class);

    private EntityManager entityManager;
    private ProsessTaskTjeneste taskTjeneste;

    FagsakProsessTaskRepository() {
        // for proxy
    }

    @Inject
    public FagsakProsessTaskRepository(EntityManager entityManager, ProsessTaskTjeneste taskTjeneste) {
        this.entityManager = entityManager;
        this.taskTjeneste = taskTjeneste;
    }

    public void lagre(FagsakProsessTask fagsakProsessTask) {
        ProsessTaskData ptData = taskTjeneste.finn(fagsakProsessTask.getProsessTaskId());
        LOG.debug("Linker fagsak[{}] -> prosesstask[{}], {} gruppeSekvensNr=[{}]", fagsakProsessTask.getSaksnummer(), fagsakProsessTask.getProsessTaskId(), ptData.taskType(), fagsakProsessTask.getGruppeSekvensNr());
        EntityManager em = getEntityManager();
        em.persist(fagsakProsessTask);
        em.flush();
    }

    public Optional<FagsakProsessTask> hent(Long prosessTaskId, boolean lås) {
        TypedQuery<FagsakProsessTask> query = getEntityManager().createQuery("from FagsakProsessTask fpt where fpt.prosessTaskId=:prosessTaskId",
                FagsakProsessTask.class);
        query.setParameter("prosessTaskId", prosessTaskId);
        if (lås) {
            query.setLockMode(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        }
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public void fjern(Saksnummer saksnummer, Long prosessTaskId, Long gruppeSekvensNr) {
        ProsessTaskData ptData = taskTjeneste.finn(prosessTaskId);
        LOG.debug("Fjerner link fagsak[{}] -> prosesstask[{}], {} gruppeSekvensNr=[{}]", saksnummer, prosessTaskId, ptData.taskType(), gruppeSekvensNr);
        EntityManager em = getEntityManager();
        Query query = em.createQuery("Delete from FagsakProsessTask Where prosessTaskId = :prosessTaskId and saksnummer=:saksnummer");
        query.setParameter("prosessTaskId", prosessTaskId);
        query.setParameter("saksnummer", saksnummer.getVerdi());
        query.executeUpdate();
        em.flush();
    }

    public void fjernForAvsluttedeBehandlinger() {
        var em = getEntityManager();
        var query = em.createNativeQuery("delete from FAGSAK_PROSESS_TASK fp where fp.id in ( select fpt.id from FAGSAK_PROSESS_TASK fpt join BEHANDLING b on fpt.behandling_id=b.id where behandling_status = :avsluttet )");
        query.setParameter("avsluttet", BehandlingStatus.AVSLUTTET.getKode());
        query.executeUpdate();
        em.flush();
    }

    public void fjernForAvsluttedeTasks() {
        var em = getEntityManager();
        var query = em.createNativeQuery("delete from FAGSAK_PROSESS_TASK fp where fp.prosess_task_id in ( select pt.id from FAGSAK_PROSESS_TASK fpt join PROSESS_TASK pt on fpt.prosess_task_id=pt.id where pt.status = :ferdig )");
        query.setParameter("ferdig", ProsessTaskStatus.FERDIG.getDbKode());
        query.executeUpdate();
        em.flush();
    }

    public List<ProsessTaskData> finnAlleForAngittSøk(Saksnummer saksnummer, Long behandlingId, String gruppeId, Collection<ProsessTaskStatus> statuser,
                                                      LocalDateTime nesteKjoeringFraOgMed,
                                                      LocalDateTime nesteKjoeringTilOgMed) {

        List<String> statusNames = statuser.stream().map(ProsessTaskStatus::getDbKode).collect(Collectors.toList());

        // native sql for å håndtere join og subselect,
        // samt cast til hibernate spesifikk håndtering av parametere som kan være NULL
        @SuppressWarnings("unchecked")
        NativeQuery<ProsessTaskEntitet> query = (NativeQuery<ProsessTaskEntitet>) entityManager
                .createNativeQuery(
                        "SELECT pt.* FROM PROSESS_TASK pt"
                                + " INNER JOIN FAGSAK_PROSESS_TASK fpt ON fpt.prosess_task_id = pt.id"
                                + " WHERE pt.status IN (:statuses)"
                                + " AND pt.task_gruppe = coalesce(:gruppe, pt.task_gruppe)"
                                + " AND (pt.neste_kjoering_etter IS NULL"
                                + "      OR ("
                                + "           pt.neste_kjoering_etter >= cast(:nesteKjoeringFraOgMed as timestamp(0)) AND pt.neste_kjoering_etter <= cast(:nesteKjoeringTilOgMed as timestamp(0))"
                                + "      ))"
                                + " AND fpt.saksnummer = :saksnummer AND fpt.behandling_id = coalesce(:behandlingId, fpt.behandling_id)",
                        ProsessTaskEntitet.class);

        query.setParameter("statuses", statusNames)
                .setParameter("gruppe", gruppeId, StandardBasicTypes.STRING)
                .setParameter("nesteKjoeringFraOgMed", nesteKjoeringFraOgMed) // max oppløsning på neste_kjoering_etter er sekunder
                .setParameter("nesteKjoeringTilOgMed", nesteKjoeringTilOgMed)
                .setParameter("saksnummer", saksnummer.getVerdi())
                .setParameter("behandlingId", behandlingId, StandardBasicTypes.LONG)
                .setHint(HibernateHints.HINT_READ_ONLY, "true");

        List<ProsessTaskEntitet> resultList = query.getResultList();
        return tilProsessTask(resultList);
    }

    public String lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(Saksnummer saksnummer, Long behandlingId, ProsessTaskGruppe gruppe) {
        // oppretter nye tasks hvis gamle har feilet og matcher angitt gruppe, eller tidligere er FERDIG. Ignorerer hvis tidligere gruppe fortsatt
        // er KLAR
        List<Entry> nyeTasks = gruppe.getTasks();
        List<ProsessTaskData> eksisterendeTasks = sjekkStatusProsessTasks(saksnummer, behandlingId, null);

        List<ProsessTaskData> matchedTasks = eksisterendeTasks;

        if (matchedTasks.isEmpty()) {
            // legg inn nye
            return taskTjeneste.lagre(gruppe);
        } else {

            // hvis noen er FEILET så oppretter vi ikke ny
            Optional<ProsessTaskData> feilet = matchedTasks.stream().filter(t -> t.getStatus().equals(ProsessTaskStatus.FEILET)).findFirst();

            var nyeTaskTyper = nyeTasks.stream().map(t -> t.task().taskType()).collect(Collectors.toSet());
            var eksisterendeTaskTyper = eksisterendeTasks.stream().map(ProsessTaskData::taskType).collect(Collectors.toSet());

            if (feilet.isEmpty()) {
                if (eksisterendeTaskTyper.containsAll(nyeTaskTyper)) {
                    return eksisterendeTasks.get(0).getGruppe();
                } else {
                    return taskTjeneste.lagre(gruppe);
                }
            } else {
                return feilet.get().getGruppe();
            }
        }

    }

    public List<ProsessTaskData> sjekkStatusProsessTasks(Saksnummer saksnummer, Long behandlingId, String gruppe) {
        Objects.requireNonNull(saksnummer, "saksnummer");

        LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);

        // et tidsrom for neste kjøring vi kan ta hensyn til. Det som er lenger ut i fremtiden er ikke relevant her, kun det vi kan forvente
        // kjøres i umiddelbar fremtid. tar i tillegg hensyn til alt som skulle ha vært kjørt tilbake i tid (som har stoppet av en eller annen
        // grunn).
        LocalDateTime fom = now.minusWeeks(2);
        LocalDateTime tom = now.plusMinutes(10); // kun det som forventes kjørt om kort tid.

        EnumSet<ProsessTaskStatus> statuser = EnumSet.allOf(ProsessTaskStatus.class);
        List<ProsessTaskData> tasks = Collections.emptyList();
        if (gruppe != null) {
            tasks = finnAlleForAngittSøk(saksnummer, behandlingId, gruppe, new ArrayList<>(statuser), fom, tom);
        }

        if (tasks.isEmpty()) {
            // ignorerer alle ferdig, suspendert, veto når vi søker blant alle grupper
            statuser.removeAll(ProsessTaskStatusUtil.FERDIG_STATUSER);
            statuser.remove(ProsessTaskStatus.SUSPENDERT);
            tasks = finnAlleForAngittSøk(saksnummer, behandlingId, null, new ArrayList<>(statuser), fom, tom);
        }

        return tasks;
    }

    public Optional<FagsakProsessTask> sjekkTillattKjøreFagsakProsessTask(ProsessTaskData ptData) {
        var saksnummer = Optional.ofNullable(ptData.getSaksnummer()).map(Saksnummer::new).orElse(null);
        Long prosessTaskId = ptData.getId();
        Optional<FagsakProsessTask> fagsakProsessTaskOpt = hent(prosessTaskId, true);

        if (fagsakProsessTaskOpt.isPresent()) {
            FagsakProsessTask fagsakProsessTask = fagsakProsessTaskOpt.get();
            if (fagsakProsessTask.getGruppeSekvensNr() == null) {
                // Dersom gruppe_sekvensnr er NULL er task alltid tillatt.
                return Optional.empty();
            }
            // Dersom den ikke er NULL er den kun tillatt dersom den matcher laveste gruppe_sekvensnr for Fagsak i
            // FAGSAK_PROSESS_TASK tabell.
            TypedQuery<FagsakProsessTask> query = getEntityManager().createQuery("from FagsakProsessTask fpt " +
                            "where fpt.saksnummer=:saksnummer and gruppeSekvensNr is not null " +
                            "order by gruppeSekvensNr ",
                    FagsakProsessTask.class);
            query.setParameter("saksnummer", saksnummer);

            FagsakProsessTask førsteFagsakProsessTask = query.getResultList().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Skal ikke være mulig å havne her, da eksistens av matchende fagsaktask er verifisert ovenfor."));
            if (!førsteFagsakProsessTask.getGruppeSekvensNr().equals(fagsakProsessTask.getGruppeSekvensNr())) {
                // Den første tasken (sortert på gruppe_sekvensnr) er ikke samme som gjeldende => en blokkerende task
                return Optional.of(førsteFagsakProsessTask);
            }
            return Optional.empty();
        } else {
            // fallback, er ikke knyttet til Fagsak - så har ingen formening om hvilken rekkefølge det skal kjøres
            return Optional.empty();
        }

    }

    /**
     * Observerer og vedlikeholder relasjon mellom fagsak og prosess task for enklere søk (dvs. fjerner relasjon når FERDIG eller KJOERT).
     */
    public void observeProsessTask(@Observes ProsessTaskEvent ptEvent) {

        Long prosessTaskId = ptEvent.getId();
        if (ptEvent.getSaksnummer() == null) {
            return; // do nothing, er ikke relatert til fagsak/behandling
        }

        var saksnummer = new Saksnummer(ptEvent.getSaksnummer());
        ProsessTaskStatus status = ptEvent.getNyStatus();

        Optional<FagsakProsessTask> fagsakProsessTaskOpt = hent(prosessTaskId, true);

        if (fagsakProsessTaskOpt.isPresent()) {
            if (ProsessTaskStatusUtil.FERDIG_STATUSER.contains(status)) {
                // fjern link
                fjern(saksnummer, ptEvent.getId(), fagsakProsessTaskOpt.get().getGruppeSekvensNr());
            }
        }

    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    private List<ProsessTaskData> tilProsessTask(List<ProsessTaskEntitet> resultList) {
        return resultList.stream().map(ProsessTaskEntitet::tilProsessTask).collect(Collectors.toList());
    }

}
