package no.nav.foreldrepenger.tilbakekreving.behandling;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public interface BehandlingTjeneste {

    List<Behandling> hentBehandlinger(Saksnummer saksnummer);

    void settBehandlingPaVent(Long behandlingsId, LocalDate frist, Venteårsak ventearsak);

    void endreBehandlingPåVent(Long behandlingId, LocalDate frist, Venteårsak venteårsak);

    Long opprettBehandlingManuell(Saksnummer saksnummer, UUID eksternUuid, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType);

    Long opprettBehandlingAutomatisk(Saksnummer saksnummer, UUID eksternUuid, long eksternbehandlingId,
                                     AktørId aktørId, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType);

    void kanEndreBehandling(Long behandlingId, Long versjon);

    Behandling hentBehandling(Long behandlingId);

    boolean erBehandlingHenlagt(Behandling behandling);

    boolean kanOppretteBehandling(Saksnummer saksnummer, UUID eksternUuid);

    void oppdaterBehandlingMedEksternReferanse(Saksnummer saksnummer, long eksternBehandlingId, UUID eksternUuid);

}
