package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskTjenesteImpl;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class HåndterGamleKravgrunnlagBatchTaskTest {

    private ProsessTaskTjeneste taskTjeneste;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private final Clock clock = Clock.fixed(Instant.parse(getDateString()), ZoneId.systemDefault());
    private HåndterGamleKravgrunnlagBatchTask gamleKravgrunnlagBatchTjeneste;
    Long mottattXmlId = null;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        entityManager.setFlushMode(FlushModeType.AUTO);
        mottattXmlRepository = new ØkonomiMottattXmlRepository(entityManager);
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML());
        taskTjeneste = new ProsessTaskTjenesteImpl(new ProsessTaskRepositoryImpl(entityManager, null, null));
        gamleKravgrunnlagBatchTjeneste = new HåndterGamleKravgrunnlagBatchTask(mottattXmlRepository,
            taskTjeneste, clock, Period.ofWeeks(-1));
    }

    @Test
    public void skal_ikke_kjøre_batch_i_helgen() {
        Clock clock = Clock.fixed(Instant.parse("2020-05-03T12:00:00.00Z"), ZoneId.systemDefault());
        HåndterGamleKravgrunnlagBatchTask gamleKravgrunnlagBatchTjeneste = new HåndterGamleKravgrunnlagBatchTask(mottattXmlRepository,
            taskTjeneste, clock, Period.ofWeeks(-1));
        gamleKravgrunnlagBatchTjeneste.doTask(lagProsessTaskData());
        assertThat(taskTjeneste.finnAlle(ProsessTaskStatus.KLAR)).isEmpty();
    }

    @Test
    public void skal_ikke_kjøre_batch_på_helligdager() {
        Clock clock = Clock.fixed(Instant.parse("2020-12-25T12:00:00.00Z"), ZoneId.systemDefault());
        HåndterGamleKravgrunnlagBatchTask gamleKravgrunnlagBatchTjeneste = new HåndterGamleKravgrunnlagBatchTask(mottattXmlRepository,
            taskTjeneste, clock, Period.ofWeeks(-1));
        gamleKravgrunnlagBatchTjeneste.doTask(lagProsessTaskData());
        assertThat(taskTjeneste.finnAlle(ProsessTaskStatus.KLAR)).isEmpty();
    }

    @Test
    public void skal_kjøre_batch_og_opprette_prosess_task_for_grunnlag(){
        gamleKravgrunnlagBatchTjeneste.doTask(lagProsessTaskData());
        List<ProsessTaskData> prosessTasker = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty().hasSize(1);
        ProsessTaskData prosessTaskData = prosessTasker.get(0);
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(HåndterGamleKravgrunnlagTask.class));
        assertThat(prosessTaskData.getPropertyValue("mottattXmlId")).isEqualTo(String.valueOf(mottattXmlId));
        assertThat(prosessTaskData.getGruppe()).contains("gammel-kravgrunnlag");
    }

    private String getInputXML() {
        try {
            Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("xml/kravgrunnlag_periode_YTEL.xml")).toURI());
            return Files.readString(path);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }

    private String getDateString() {
        return (LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY || LocalDate.now().getDayOfWeek() == DayOfWeek.SATURDAY) ?
            Instant.now().plus(2, ChronoUnit.DAYS).toString() :
            Instant.now().toString();
    }

    private ProsessTaskData lagProsessTaskData() {
        return ProsessTaskData.forProsessTask(HåndterGamleKravgrunnlagTask.class);
    }

}
