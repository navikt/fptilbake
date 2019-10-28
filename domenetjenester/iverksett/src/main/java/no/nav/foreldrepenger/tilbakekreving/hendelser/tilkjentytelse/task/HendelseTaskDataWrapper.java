package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.BEHANDLING_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.EKSTERN_BEHANDLING_ID;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.SAKSNUMMER;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.TILBAKEKREVING_VALG;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.VARSEL_BELØP;
import static no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TaskProperties.VARSEL_TEKST;

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

    public String getVarselTekst() {
        return prosessTaskData.getPropertyValue(VARSEL_TEKST);
    }

    public String getVarselBeløp() {
        return prosessTaskData.getPropertyValue(VARSEL_BELØP);
    }

    public String getTilbakekrevingValg() {
        return prosessTaskData.getPropertyValue(TILBAKEKREVING_VALG);
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

    public void setVarselTekst(String varselTekst) {
        prosessTaskData.setProperty(VARSEL_TEKST, varselTekst);
    }

    public void setVarselBeløp(String varselBeløp) {
        prosessTaskData.setProperty(VARSEL_BELØP, varselBeløp);
    }

    public void setTilbakekrevingValg(String valg) {
        prosessTaskData.setProperty(TILBAKEKREVING_VALG, valg);
    }

    public void validerTaskDataHåndterHendelse() {
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(SAKSNUMMER));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(TILBAKEKREVING_VALG));
    }

    public void validerTaskDataOpprettBehandling() {
        validerTaskDataHåndterHendelse();
        Objects.requireNonNull(getSaksnummer());
        Objects.requireNonNull(getFagsakYtelseType());
    }

    public void validerTaskDataOppdaterBehandling() {
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(SAKSNUMMER));
    }


    public static HendelseTaskDataWrapper lagWrapperForHendelseHåndtering(TilkjentYtelseMelding melding) {
        ProsessTaskData td = lagProsessTaskDataMedFellesProperty(HåndterHendelseTask.TASKTYPE, melding.getAktørId(), melding.getBehandlingId(),
            melding.getBehandlingUuid().toString(), melding.getSaksnummer());
        td.setProperty(FAGSAK_YTELSE_TYPE, melding.getFagsakYtelseType());
        td.setProperty(VARSEL_TEKST,melding.getVarselTekst());
        td.setProperty(VARSEL_BELØP,String.valueOf(melding.getVarselBeløp()));
        td.setProperty(TILBAKEKREVING_VALG,melding.getTilbakekrevingValg());

        return new HendelseTaskDataWrapper(td);
    }

    public static HendelseTaskDataWrapper lagWrapperForOpprettBehandling(String behandlingUuid, long behandlingId, AktørId aktørId, Saksnummer saksnummer) {
        ProsessTaskData td = lagProsessTaskDataMedFellesProperty(OpprettBehandlingTask.TASKTYPE, aktørId, behandlingId,
            behandlingUuid, saksnummer);
        return new HendelseTaskDataWrapper(td);
    }

    public static HendelseTaskDataWrapper lagWrapperForOppdaterBehandling(String behandlingUuid, long behandlingId, AktørId aktørId, Saksnummer saksnummer) {
        ProsessTaskData td = lagProsessTaskDataMedFellesProperty(OppdaterBehandlingTask.TASKTYPE, aktørId, behandlingId,
            behandlingUuid, saksnummer);
        return new HendelseTaskDataWrapper(td);
    }

    private static ProsessTaskData lagProsessTaskDataMedFellesProperty(String taskType, AktørId aktørId, long behandlingId, String behandlingUuid, Saksnummer saksnummer) {
        ProsessTaskData td = new ProsessTaskData(taskType);
        td.setAktørId(aktørId.getId());
        td.setProperty(EKSTERN_BEHANDLING_ID, String.valueOf(behandlingId));
        td.setProperty(EKSTERN_BEHANDLING_UUID, behandlingUuid);
        td.setProperty(SAKSNUMMER, saksnummer.getVerdi());
        return td;
    }
}
