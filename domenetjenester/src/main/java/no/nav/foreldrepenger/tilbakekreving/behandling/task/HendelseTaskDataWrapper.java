package no.nav.foreldrepenger.tilbakekreving.behandling.task;


import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.BEHANDLING_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_ID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.HENVISNING;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.SAKSNUMMER;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HendelseTaskDataWrapper {

    private ProsessTaskData prosessTaskData;

    public HendelseTaskDataWrapper(ProsessTaskData eksisterendeData) {
        this.prosessTaskData = eksisterendeData;
    }

    public ProsessTaskData getProsessTaskData() {
        return prosessTaskData;
    }

    public Henvisning getHenvisning() {
        String henvisningValue = prosessTaskData.getPropertyValue(HENVISNING);
        if (henvisningValue != null) {
            return new Henvisning(henvisningValue);
        }
        //TODO k9-tilbake, denne koden er for å tåle prosesstasker som er opprettet før endring til henvisning
        long eksternBehandlingId = Long.parseLong(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID));
        return Henvisning.fraEksternBehandlingId(eksternBehandlingId);
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
        if (prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID) == null && prosessTaskData.getPropertyValue(HENVISNING) == null) {
            throw new IllegalArgumentException("Trenger minst en av henvisning og ekstern behandling id, manglet begge.");
        }
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(SAKSNUMMER));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE));
    }

    public void validerTaskDataOpprettBehandling() {
        validerTaskDataHåndterHendelse();
        Objects.requireNonNull(getSaksnummer());
        Objects.requireNonNull(getFagsakYtelseType());
    }

    public void validerTaskDataOppdaterBehandling() {
        if (prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID) == null && prosessTaskData.getPropertyValue(HENVISNING) == null) {
            throw new IllegalArgumentException("Trenger minst en av henvisning og ekstern behandling id, manglet begge.");
        }
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(SAKSNUMMER));
    }

    public static HendelseTaskDataWrapper lagWrapperForOpprettBehandling(String behandlingUuid, Henvisning henvisning, AktørId aktørId, Saksnummer saksnummer) {
        ProsessTaskData td = ProsessTaskData.forProsessTask(OpprettBehandlingTask.class);
        td.setAktørId(aktørId.getId());
        td.setProperty(EKSTERN_BEHANDLING_UUID, behandlingUuid);
        td.setProperty(EKSTERN_BEHANDLING_ID, henvisning.getVerdi()); //TODO k9-tilbake fjern når transisjon til henvisning er ferdig
        td.setProperty(HENVISNING, henvisning.getVerdi());
        td.setProperty(SAKSNUMMER, saksnummer.getVerdi());
        return new HendelseTaskDataWrapper(td);
    }
}
