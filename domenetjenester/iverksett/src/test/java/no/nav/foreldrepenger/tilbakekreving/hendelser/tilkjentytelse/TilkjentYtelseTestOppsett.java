package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import static org.mockito.Mockito.spy;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HåndterHendelseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class TilkjentYtelseTestOppsett {

    protected static final Long EKSTERN_BEHANDLING_ID = 123L;
    protected static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(EKSTERN_BEHANDLING_ID);
    protected static final Saksnummer SAKSNUMMER = new Saksnummer("1234");
    protected static final AktørId AKTØR_ID = new AktørId("1234567898765");
    protected static final String IV_SYSTEM = "FPSAK";
    protected static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();
    protected static final FagsakYtelseType FAGSAK_YTELSE_TYPE = FagsakYtelseType.FORELDREPENGER;

    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected ProsessTaskRepository prosessTaskRepository = spy(new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null, null));

    protected HendelseTaskDataWrapper hendelseTaskDataWrapper;

    @Before
    public void init(){
        ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);

        prosessTaskData.setProperty(TaskProperties.EKSTERN_BEHANDLING_UUID, EKSTERN_BEHANDLING_UUID.toString());
        prosessTaskData.setProperty(TaskProperties.EKSTERN_BEHANDLING_ID,String.valueOf(EKSTERN_BEHANDLING_ID));
        prosessTaskData.setAktørId(AKTØR_ID.getId());
        prosessTaskData.setProperty(TaskProperties.SAKSNUMMER,SAKSNUMMER.getVerdi());
        prosessTaskData.setProperty(TaskProperties.FAGSAK_YTELSE_TYPE,FAGSAK_YTELSE_TYPE.getKode());
        hendelseTaskDataWrapper = new HendelseTaskDataWrapper(prosessTaskData);
    }

    protected TilkjentYtelseMelding opprettTilkjentYtelseMelding() {
        TilkjentYtelseMelding melding = new TilkjentYtelseMelding();
        melding.setAktørId(AKTØR_ID.getId());
        melding.setBehandlingId(EKSTERN_BEHANDLING_ID);
        melding.setIverksettingSystem(IV_SYSTEM);
        melding.setBehandlingUuid(EKSTERN_BEHANDLING_UUID);
        melding.setFagsakYtelseType(FagsakYtelseType.FORELDREPENGER.getKode());
        melding.setSaksnummer("1234");

        return melding;
    }
}
