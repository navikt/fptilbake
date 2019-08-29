package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import static org.mockito.Mockito.spy;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HåndterHendelseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class TilkjentYtelseTestOppsett {

    protected static final Long BEHANDLING_ID = 123L;
    protected static final String SAKSNUMMER = "1234";
    protected static final String AKTØR_ID = "1234567898765";
    protected static final String IV_SYSTEM = "FPSAK";
    protected static final UUID BEHANDLING_UUID = UUID.randomUUID();
    protected static final FagsakYtelseType FAGSAK_YTELSE_TYPE = FagsakYtelseType.FORELDREPENGER;

    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected ProsessTaskRepository prosessTaskRepository = spy(new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null));

    protected HendelseTaskDataWrapper hendelseTaskDataWrapper;

    @Before
    public void init(){
        ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);

        prosessTaskData.setProperty(TaskProperties.BEHANDLING_UUID,BEHANDLING_UUID.toString());
        prosessTaskData.setProperty(TaskProperties.BEHANDLING_ID,String.valueOf(BEHANDLING_ID));
        prosessTaskData.setAktørId(AKTØR_ID);
        prosessTaskData.setProperty(TaskProperties.SAKSNUMMER,SAKSNUMMER);
        prosessTaskData.setProperty(TaskProperties.FAGSAK_YTELSE_TYPE,FAGSAK_YTELSE_TYPE.getKode());
        hendelseTaskDataWrapper = new HendelseTaskDataWrapper(prosessTaskData);
    }

    protected TilkjentYtelseMelding opprettTilkjentYtelseMelding() {
        TilkjentYtelseMelding melding = new TilkjentYtelseMelding();
        melding.setAktørId(AKTØR_ID);
        melding.setBehandlingId(BEHANDLING_ID);
        melding.setIverksettingSystem(IV_SYSTEM);
        melding.setBehandlingUuid(BEHANDLING_UUID);
        melding.setFagsakYtelseType(FagsakYtelseType.FORELDREPENGER.getKode());
        melding.setSaksnummer("1234");

        return melding;
    }
}
