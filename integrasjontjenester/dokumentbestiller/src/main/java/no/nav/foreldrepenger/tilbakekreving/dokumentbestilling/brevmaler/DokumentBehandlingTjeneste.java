package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.ManueltVarselBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.SendManueltVarselbrevTask;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class DokumentBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private BrevdataRepository brevdataRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private ProsessTaskRepository prosessTaskRepository;

    private HistorikkinnslagTjeneste historikkinnslagTjeneste;


    DokumentBehandlingTjeneste() {
        // for cdi proxy
    }

    @Inject
    public DokumentBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                      BrevdataRepository brevdataRepository,
                                      ProsessTaskRepository prosessTaskRepository,
                                      HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.brevdataRepository = brevdataRepository;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.prosessTaskRepository = prosessTaskRepository;

        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public List<BrevmalDto> hentBrevmalerFor(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<DokumentMalType> gyldigBrevMaler = new ArrayList<>();

        gyldigBrevMaler.add(kodeverkRepository.finn(DokumentMalType.class, DokumentMalType.INNHENT_DOK));
        gyldigBrevMaler.add(kodeverkRepository.finn(DokumentMalType.class, DokumentMalType.FRITEKST_DOK));

        leggTilVarselBrevmaler(behandlingId, gyldigBrevMaler);

        return tilBrevmalDto(behandling, gyldigBrevMaler);
    }

    public void bestillBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        malType = kodeverkRepository.finn(DokumentMalType.class, malType);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (DokumentMalType.VARSEL_DOK.equals(malType) || DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            håndteresManueltSendVarsel(behandling, malType, fritekst);
        }
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevBestilt(behandling, malType);
    }

    private void leggTilVarselBrevmaler(Long behandlingId, List<DokumentMalType> gyldigBrevMaler) {
        if (!brevdataRepository.harVarselBrevSendtForBehandlingId(behandlingId)) {
            gyldigBrevMaler.add(kodeverkRepository.finn(DokumentMalType.class, DokumentMalType.VARSEL_DOK));
        } else {
            gyldigBrevMaler.add(kodeverkRepository.finn(DokumentMalType.class, DokumentMalType.KORRIGERT_VARSEL_DOK));
        }
    }

    private List<BrevmalDto> tilBrevmalDto(Behandling behandling, List<DokumentMalType> dmtList) {
        List<BrevmalDto> brevmalDtoList = new ArrayList<>(dmtList.size());
        for (DokumentMalType dmt : dmtList) {
            boolean tilgjengelig = sjekkOmTilgjengelig(behandling, dmt);
            brevmalDtoList.add(new BrevmalDto(dmt.getKode(), dmt.getNavn(), tilgjengelig));
        }
        return brevmalDtoList;
    }

    private boolean sjekkOmTilgjengelig(Behandling behandling, DokumentMalType mal) {
        if (DokumentMalType.VARSEL_DOK.getKode().equals(mal.getKode()) || DokumentMalType.KORRIGERT_VARSEL_DOK.getKode().equals(mal.getKode())) {
            return !behandling.erSaksbehandlingAvsluttet() && !behandling.erAvsluttet();
        }
        return true;
    }

    private void håndteresManueltSendVarsel(Behandling behandling, DokumentMalType malType, String fritekst) {
        Long behandlingId = behandling.getId();
        if (!grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            throw ManueltVarselBrevFeil.FACTORY.kanIkkeSendeVarselForGrunnlagFinnesIkke(behandlingId).toException();
        }

        ProsessTaskData prosessTaskData = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        prosessTaskData.setProperty(TaskProperty.MAL_TYPE, malType.getKode());
        prosessTaskData.setProperty(TaskProperty.FRITEKST, fritekst);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        prosessTaskRepository.lagre(prosessTaskData);
    }

}
