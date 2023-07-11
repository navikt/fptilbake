package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("henvisning.korrigert")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class KorrigertHenvisningTask implements ProsessTaskHandler {

    public static final String PROPERTY_EKSTERN_UUID = "eksternUuid";
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private FagsystemKlient fagsystemKlient;

    KorrigertHenvisningTask() {
        // for CDI
    }

    @Inject
    public KorrigertHenvisningTask(BehandlingRepositoryProvider repositoryProvider,
                                   FagsystemKlient fagsystemKlient) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.fagsystemKlient = fagsystemKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = Long.parseLong(prosessTaskData.getBehandlingId());
        String eksternUuid = prosessTaskData.getPropertyValue(PROPERTY_EKSTERN_UUID);
        UUID eksternBehandlingUuid = UUID.fromString(eksternUuid);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<SamletEksternBehandlingInfo> samletEksternBehandlingInfo = fagsystemKlient.hentBehandlingsinfoOpt(eksternBehandlingUuid, Tillegsinformasjon.FAGSAK);
        if (samletEksternBehandlingInfo.isEmpty()) {
            throw new TekniskException("FPT-7728492", String.format("Fant ikke eksternBehandlingUuid %s i fagsystemet. Kan ikke korrigere henvisningen for behandling %s", eksternUuid, behandlingId));
        }
        SamletEksternBehandlingInfo eksternBehandlingInfo = samletEksternBehandlingInfo.get();
        Saksnummer eksternBehandlingSaksnummer = eksternBehandlingInfo.getSaksnummer();
        if (!eksternBehandlingSaksnummer.equals(behandling.getFagsak().getSaksnummer())) {
            throw new TekniskException("FPT-7728493", String.format("EksternBehandlingUuid %s har ikke samme saksnummer %s som behandling. Kan ikke korrigere henvisningen for behandling %s", eksternUuid, eksternBehandlingSaksnummer, behandlingId));
        }

        Henvisning henvisning = eksternBehandlingInfo.getGrunninformasjon().getHenvisning();
        // Hvis EksternBehandling allerede har en annen henvisning for behandlingen,
        // skal det deaktiveres og en ny rad med ny henvisning skal legges til
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, henvisning, eksternBehandlingUuid);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }
}
