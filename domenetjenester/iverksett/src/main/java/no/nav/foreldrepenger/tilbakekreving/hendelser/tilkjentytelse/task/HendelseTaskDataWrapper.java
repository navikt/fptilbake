package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.BEHANDLING_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.EKSTERN_BEHANDLING_ID;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.EKSTERN_BEHANDLING_UUID;
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
        return Long.valueOf(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID));
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
        return prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID);
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        prosessTaskData.setProperty(FAGSAK_YTELSE_TYPE, fagsakYtelseType.getKode());
    }

    public void setSaksnummer(Saksnummer saksnummer) {
        prosessTaskData.setProperty(SAKSNUMMER, saksnummer.getVerdi());
    }


    public void setBehandlingType(BehandlingType behandlingType) {
        prosessTaskData.setProperty(BEHANDLING_TYPE, behandlingType.getKode());
    }

    public void validerTaskDataHåndterHendelse() {
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID));
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
        td.setAktørId(melding.getAktørId().getId());
        td.setProperty(EKSTERN_BEHANDLING_ID,String.valueOf(melding.getBehandlingId()));
        td.setProperty(EKSTERN_BEHANDLING_UUID, melding.getBehandlingUuid().toString());
        td.setProperty(SAKSNUMMER,melding.getSaksnummer().getVerdi());
        td.setProperty(FAGSAK_YTELSE_TYPE,melding.getFagsakYtelseType());

        return new HendelseTaskDataWrapper(td);
    }

    public static HendelseTaskDataWrapper lagWrapperForOpprettBehandling(String behandlingUuid, long behandlingId, AktørId aktørId) {
        ProsessTaskData td = new ProsessTaskData(OpprettBehandlingTask.TASKTYPE);
        td.setAktørId(aktørId.getId());
        td.setProperty(EKSTERN_BEHANDLING_ID,String.valueOf(behandlingId));
        td.setProperty(EKSTERN_BEHANDLING_UUID, behandlingUuid);
        return new HendelseTaskDataWrapper(td);
    }

}
