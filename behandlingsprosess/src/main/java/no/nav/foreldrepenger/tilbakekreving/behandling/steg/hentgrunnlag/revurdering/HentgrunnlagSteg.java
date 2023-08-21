package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@BehandlingStegRef(BehandlingStegType.HENTGRUNNLAGSTEG)
@BehandlingTypeRef
@ApplicationScoped
public class HentgrunnlagSteg implements BehandlingSteg {

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingRepository behandlingRepository;

    public HentgrunnlagSteg() {
        // CDI
    }

    @Inject
    public HentgrunnlagSteg(ProsessTaskTjeneste taskTjeneste, BehandlingRepository behandlingRepository) {
        this.taskTjeneste = taskTjeneste;
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Long origBehandlingId = behandling.getBehandlingÅrsaker().get(0).getOriginalBehandling().orElseThrow().getId();

        // opprett prosess task for å hente grunnlag
        ProsessTaskData hentxmlTask = ProsessTaskData.forProsessTask(HentKravgrunnlagTask.class);
        hentxmlTask.setProperty(TaskProperties.PROPERTY_ORIGINAL_BEHANDLING_ID, String.valueOf(origBehandlingId));
        hentxmlTask.setBehandling(kontekst.getFagsakId(), kontekst.getBehandlingId(), kontekst.getAktørId().getId());
        taskTjeneste.lagre(hentxmlTask);

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }


}
