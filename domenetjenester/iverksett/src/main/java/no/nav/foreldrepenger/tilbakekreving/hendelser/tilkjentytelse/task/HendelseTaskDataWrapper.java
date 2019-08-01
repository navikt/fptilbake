package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HendelseTaskDataWrapper {

    private static final String SAKSNUMMER = "saksnummer";
    private static final String FAGSAK_YTELSE_TYPE = "fagYtelseType";
    private static final String BEHANDLING_TYPE = "behandlingType";

    private ProsessTaskData prosessTaskData;

    public HendelseTaskDataWrapper(ProsessTaskData eksisterendeData) {
        this.prosessTaskData = eksisterendeData;
    }

    public ProsessTaskData getProsessTaskData() {
        return prosessTaskData;
    }

    public long getFagsakId() {
        return prosessTaskData.getFagsakId();
    }

    public long getBehandlingId() {
        return prosessTaskData.getBehandlingId();
    }

    public AktørId getAktørId() {
        String aktørId = prosessTaskData.getAktørId();
        return new AktørId(aktørId);
    }

    public void setSaksnummer(String saksnummer) {
        prosessTaskData.setProperty(SAKSNUMMER, saksnummer);
    }

    public Saksnummer getSaksnummer() {
        String saksnummer = prosessTaskData.getPropertyValue(SAKSNUMMER);
        return new Saksnummer(saksnummer);
    }

    public void setFagsakYtelseType(String fagsakYtelseType) {
        prosessTaskData.setProperty(FAGSAK_YTELSE_TYPE, fagsakYtelseType);
    }

    public FagsakYtelseType getFagsakYtelseType() {
        String kode = prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE);
        return FagsakYtelseType.fraKode(kode);
    }

    public void setBehandlingType(String behandlingType) {
        prosessTaskData.setProperty(BEHANDLING_TYPE, behandlingType);
    }

    public BehandlingType getBehandlingType() {
        String kode = prosessTaskData.getPropertyValue(BEHANDLING_TYPE);
        return BehandlingType.fraKode(kode);
    }

    public void validerTaskDataHåndterHendelse() {
        Objects.requireNonNull(prosessTaskData.getBehandlingId());
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getFagsakId());
    }

    public void validerTaskDataOpprettBehandling() {
        validerTaskDataHåndterHendelse();
        Objects.requireNonNull(getSaksnummer());
        Objects.requireNonNull(getFagsakYtelseType());
    }


    public static HendelseTaskDataWrapper lagWrapperForHendelseHåndtering(long fagsakId, long behandlingId, String aktørId) {
        ProsessTaskData td = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);
        td.setBehandling(fagsakId, behandlingId, aktørId);
        return new HendelseTaskDataWrapper(td);
    }

    public static HendelseTaskDataWrapper lagWrapperForOpprettBehandling(long fagsakId, long behandlingId, String aktørId) {
        ProsessTaskData td = new ProsessTaskData(OpprettBehandlingTask.TASKTYPE);
        td.setBehandling(fagsakId, behandlingId, aktørId);
        return new HendelseTaskDataWrapper(td);
    }

}
