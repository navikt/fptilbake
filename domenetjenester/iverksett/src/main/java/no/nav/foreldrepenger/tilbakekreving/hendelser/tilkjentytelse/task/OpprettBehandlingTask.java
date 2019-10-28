package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(OpprettBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OpprettBehandlingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "hendelser.opprettBehandling";

    private VarselRepository varselRepository;
    private BehandlingTjeneste behandlingTjeneste;


    OpprettBehandlingTask() {
        // CDI
    }

    @Inject
    public OpprettBehandlingTask(VarselRepository varselRepository, BehandlingTjeneste behandlingTjeneste) {
        this.varselRepository = varselRepository;
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData taskData) {
        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataOpprettBehandling();

        String externBehandlingUuid = dataWrapper.getBehandlingUuid();
        long eksternBehandlingId = dataWrapper.getBehandlingId();
        Saksnummer saksnummer = dataWrapper.getSaksnummer();
        AktørId aktørId = dataWrapper.getAktørId();
        BehandlingType behandlingType = dataWrapper.getBehandlingType();
        FagsakYtelseType fagsakYtelseType = dataWrapper.getFagsakYtelseType();
        String varselTekst = dataWrapper.getVarselTekst();
        Long varselBeløp = dataWrapper.getVarselBeløp() != null ? Long.valueOf(dataWrapper.getVarselBeløp()) : 0l;

        Long behandlingId = opprettBehandling(saksnummer, UUID.fromString(externBehandlingUuid), eksternBehandlingId, aktørId, fagsakYtelseType, behandlingType);
        if (StringUtils.isNotEmpty(varselTekst)) { // lagres varsel bare når varselTekst finnes
            lagreVarselData(behandlingId, varselTekst, varselBeløp);
        }
    }

    private Long opprettBehandling(Saksnummer saksnummer, UUID eksternUuid, long eksternBehandlingId,
                                   AktørId aktørId, FagsakYtelseType fagsakYtelseType,
                                   BehandlingType behandlingType) {
        return behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternUuid, eksternBehandlingId, aktørId, fagsakYtelseType, behandlingType);
    }

    private void lagreVarselData(Long behandlingId, String varselTekst, Long varselBeløp) {
        varselRepository.lagre(behandlingId, varselTekst, varselBeløp);
    }
}
