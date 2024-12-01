package no.nav.foreldrepenger.tilbakekreving.behandling.task;


import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.BEHANDLING_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_ID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.HENVISNING;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        return Optional.ofNullable(prosessTaskData.getAktørId()).map(AktørId::new).orElse(null);
    }

    public Saksnummer getSaksnummer() {
        return Optional.ofNullable(prosessTaskData.getSaksnummer()).map(Saksnummer::new).orElse(null);
    }

    public FagsakYtelseType getFagsakYtelseType() {
        String kode = prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE);
        return FagsakYtelseType.fraKode(kode);
    }

    public BehandlingType getBehandlingType() {
        String kode = prosessTaskData.getPropertyValue(BEHANDLING_TYPE);
        return BehandlingType.fraKode(kode);
    }

    public UUID getBehandlingUuid() {
        return UUID.fromString(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        prosessTaskData.setProperty(FAGSAK_YTELSE_TYPE, fagsakYtelseType.getKode());
    }

    public void setSaksnummer(Saksnummer saksnummer) {
        Optional.ofNullable(saksnummer).map(Saksnummer::getVerdi).ifPresent(prosessTaskData::setSaksnummer);
    }


    public void setBehandlingType(BehandlingType behandlingType) {
        prosessTaskData.setProperty(BEHANDLING_TYPE, behandlingType.getKode());
    }

    public void validerTaskDataHåndterHendelse() {
        if (prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_ID) == null && prosessTaskData.getPropertyValue(HENVISNING) == null) {
            throw new IllegalArgumentException("Trenger minst en av henvisning og ekstern behandling id, manglet begge.");
        }
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getSaksnummer());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE));
    }

    public void validerTaskDataHåndterVedtakFattet() {
        Objects.requireNonNull(prosessTaskData.getAktørId());
        Objects.requireNonNull(prosessTaskData.getSaksnummer());
        Objects.requireNonNull(prosessTaskData.getPropertyValue(EKSTERN_BEHANDLING_UUID));
        Objects.requireNonNull(prosessTaskData.getPropertyValue(FAGSAK_YTELSE_TYPE));
    }

    public void validerTaskDataOpprettBehandling() {
        validerTaskDataHåndterHendelse();
        Objects.requireNonNull(getSaksnummer());
        Objects.requireNonNull(getFagsakYtelseType());
    }

    public static HendelseTaskDataWrapper lagWrapperForOpprettBehandling(UUID behandlingUuid, Henvisning henvisning, AktørId aktørId, Saksnummer saksnummer) {
        ProsessTaskData td = ProsessTaskData.forProsessTask(OpprettBehandlingTask.class);
        td.setAktørId(aktørId.getId());
        td.setSaksnummer(saksnummer.getVerdi());
        td.setProperty(EKSTERN_BEHANDLING_UUID, behandlingUuid.toString());
        td.setProperty(EKSTERN_BEHANDLING_ID, henvisning.getVerdi()); //TODO k9-tilbake fjern når transisjon til henvisning er ferdig
        td.setProperty(HENVISNING, henvisning.getVerdi());
        return new HendelseTaskDataWrapper(td);
    }
}
