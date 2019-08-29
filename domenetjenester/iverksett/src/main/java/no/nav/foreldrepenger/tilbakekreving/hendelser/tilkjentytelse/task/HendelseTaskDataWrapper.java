package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.BEHANDLING_ID;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.BEHANDLING_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.SAKSNUMMER;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseMelding;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HendelseTaskDataWrapper {

    private ProsessTaskData prosessTaskData;

    public HendelseTaskDataWrapper(ProsessTaskData eksisterendeData) {
        this.prosessTaskData = eksisterendeData;
    }

    public ProsessTaskData getProsessTaskData() {
        return prosessTaskData;
    }

    public long getBehandlingId() {
        return prosessTaskData.getBehandlingId();
    }

    public AktørId getAktørId() {
        String aktørId = prosessTaskData.getAktørId();
        return new AktørId(aktørId);
    }

    public Saksnummer getSaksnummer() {
        String saksnummer = prosessTaskData.getPropertyValue(SAKSNUMMER);
        return new Saksnummer(saksnummer);
    }

    public FagsakYtelseType getFagsakYtelseType() {
        String kode = prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE);
        return FagsakYtelseType.fraKode(kode);
    }

    public BehandlingType getBehandlingType() {
        String kode = prosessTaskData.getPropertyValue(BEHANDLING_TYPE);
        return BehandlingType.fraKode(kode);
    }

    public String getBehandlingUuid() {
        return prosessTaskData.getPropertyValue(BEHANDLING_UUID);
    }

    public void setFagsakYtelseType(String fagsakYtelseType) {
        prosessTaskData.setProperty(FAGSAK_YTELSE_TYPE, fagsakYtelseType);
    }

    public void setSaksnummer(String saksnummer) {
        prosessTaskData.setProperty(SAKSNUMMER, saksnummer);
    }


    public void setBehandlingType(String behandlingType) {
        prosessTaskData.setProperty(BEHANDLING_TYPE, behandlingType);
    }

    public void validerTaskDataHåndterHendelse() {
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getBehandlingId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(SAKSNUMMER));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE));
    }

    public void validerTaskDataOpprettBehandling() {
        validerTaskDataHåndterHendelse();
        Objects.requireNonNull(getSaksnummer());
        Objects.requireNonNull(getFagsakYtelseType());
    }


    public static HendelseTaskDataWrapper lagWrapperForHendelseHåndtering(TilkjentYtelseMelding melding) {
        ProsessTaskData td = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);
        td.setAktørId(melding.getAktørId());
        td.setProperty(BEHANDLING_ID,String.valueOf(melding.getBehandlingId()));
        td.setProperty(BEHANDLING_UUID, melding.getBehandlingUuid().toString());
        td.setProperty(SAKSNUMMER,melding.getSaksnummer());
        td.setProperty(FAGSAK_YTELSE_TYPE,melding.getFagsakYtelseType());

        return new HendelseTaskDataWrapper(td);
    }

    public static HendelseTaskDataWrapper lagWrapperForOpprettBehandling(String behandlingUuid, long behandlingId, String aktørId) {
        ProsessTaskData td = new ProsessTaskData(OpprettBehandlingTask.TASKTYPE);
        td.setAktørId(aktørId);
        td.setProperty(BEHANDLING_ID,String.valueOf(behandlingId));
        td.setProperty(BEHANDLING_UUID, behandlingUuid);
        return new HendelseTaskDataWrapper(td);
    }

}
