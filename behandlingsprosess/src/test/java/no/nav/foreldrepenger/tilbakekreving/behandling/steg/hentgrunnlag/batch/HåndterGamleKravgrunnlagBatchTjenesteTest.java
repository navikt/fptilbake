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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.EmptyBatchArguments;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class HåndterGamleKravgrunnlagBatchTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(),null, null);
    private final ØkonomiMottattXmlRepository mottattXmlRepository = new ØkonomiMottattXmlRepository(repositoryRule.getEntityManager());
    private final Clock clock = Clock.fixed(Instant.parse(getDateString()), ZoneId.systemDefault());
    private final HåndterGamleKravgrunnlagBatchTjeneste gamleKravgrunnlagBatchTjeneste = new HåndterGamleKravgrunnlagBatchTjeneste(mottattXmlRepository,
        prosessTaskRepository, clock, Period.ofWeeks(-1));
    Long mottattXmlId = null;
    private final BatchArguments emptyBatchArguments = new EmptyBatchArguments(Collections.EMPTY_MAP);

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML());
    }

    @Test
    public void skal_ikke_kjøre_batch_i_helgen() {
        Clock clock = Clock.fixed(Instant.parse("2020-05-03T12:00:00.00Z"), ZoneId.systemDefault());
        HåndterGamleKravgrunnlagBatchTjeneste gamleKravgrunnlagBatchTjeneste = new HåndterGamleKravgrunnlagBatchTjeneste(mottattXmlRepository,
            prosessTaskRepository, clock, Period.ofWeeks(-1));
        gamleKravgrunnlagBatchTjeneste.launch(emptyBatchArguments);
        assertThat(prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR)).isEmpty();
    }

    @Test
    public void skal_kjøre_batch_og_opprette_prosess_task_for_grunnlag(){
        gamleKravgrunnlagBatchTjeneste.launch(emptyBatchArguments);
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty().hasSize(1);
        ProsessTaskData prosessTaskData = prosessTasker.get(0);
        assertThat(prosessTaskData.getTaskType()).isEqualTo(HåndterGamleKravgrunnlagTask.TASKTYPE);
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

}
