package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@BehandlingStegRef(kode = "HENTGRUNNLAGSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class HentgrunnlagSteg implements BehandlingSteg {

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepository behandlingRepository;

    public HentgrunnlagSteg() {
        // CDI
    }

    @Inject
    public HentgrunnlagSteg(ProsessTaskRepository prosessTaskRepository, BehandlingRepository behandlingRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Long origBehandlingId = behandling.getBehandlingÅrsaker().get(0).getOriginalBehandling().orElseThrow().getId();

        // opprett prosess task for å hente grunnlag
        ProsessTaskData hentxmlTask = new ProsessTaskData(HentKravgrunnlagTask.TASKTYPE);
        hentxmlTask.setProperty(TaskProperty.PROPERTY_ORIGINAL_BEHANDLING_ID, String.valueOf(origBehandlingId));
        hentxmlTask.setBehandling(kontekst.getFagsakId(), kontekst.getBehandlingId(), kontekst.getAktørId().getId());
        prosessTaskRepository.lagre(hentxmlTask);

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }


}
