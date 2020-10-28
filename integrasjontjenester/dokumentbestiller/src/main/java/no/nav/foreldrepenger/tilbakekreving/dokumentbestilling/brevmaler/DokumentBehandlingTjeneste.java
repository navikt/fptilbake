package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevToggle;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevTask;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.ManueltVarselBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.ManueltVarselBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.SendManueltVarselbrevTask;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendBeskjedUtsendtVarselTilSelvbetjeningTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class DokumentBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private ProsessTaskRepository prosessTaskRepository;

    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;
    private InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste;


    DokumentBehandlingTjeneste() {
        // for cdi proxy
    }

    @Inject
    public DokumentBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                      ProsessTaskRepository prosessTaskRepository,
                                      HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                      ManueltVarselBrevTjeneste manueltVarselBrevTjeneste,
                                      InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.prosessTaskRepository = prosessTaskRepository;

        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.manueltVarselBrevTjeneste = manueltVarselBrevTjeneste;
        this.innhentDokumentasjonBrevTjeneste = innhentDokumentasjonBrevTjeneste;
    }

    public List<BrevmalDto> hentBrevmalerFor(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<DokumentMalType> gyldigBrevMaler = new ArrayList<>();

        gyldigBrevMaler.add(DokumentMalType.INNHENT_DOK);

        leggTilVarselBrevmaler(behandlingId, gyldigBrevMaler);

        return tilBrevmalDto(behandling, gyldigBrevMaler);
    }

    public void bestillBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (DokumentMalType.VARSEL_DOK.equals(malType) || DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            håndteresManueltSendVarsel(behandling, malType, fritekst);
        } else if (DokumentMalType.INNHENT_DOK.equals(malType)) {
            System.out.println("Bestiller dokument type " + malType.getKode());
            håndteresInnhentDokumentasjon(behandling, malType, fritekst);
        }
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevBestilt(behandling, malType);
    }

    public byte[] forhåndsvisBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        byte[] dokument = new byte[0];
        if (DokumentMalType.VARSEL_DOK.equals(malType) || DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            dokument = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, malType, fritekst);
        } else if (DokumentMalType.INNHENT_DOK.equals(malType)) {
            dokument = innhentDokumentasjonBrevTjeneste.hentForhåndsvisningInnhentDokumentasjonBrev(behandlingId, fritekst);
        }
        return dokument;
    }

    private void leggTilVarselBrevmaler(Long behandlingId, List<DokumentMalType> gyldigBrevMaler) {
        if (!brevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId)) {
            gyldigBrevMaler.add(DokumentMalType.VARSEL_DOK);
        } else {
            gyldigBrevMaler.add(DokumentMalType.KORRIGERT_VARSEL_DOK);
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

        ProsessTaskData sendVarselbrev = new ProsessTaskData(SendManueltVarselbrevTask.TASKTYPE);
        sendVarselbrev.setProperty(TaskProperty.MAL_TYPE, malType.getKode());
        sendVarselbrev.setPayload(fritekst);
        sendVarselbrev.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        ProsessTaskData sendBeskjedUtsendtVarsel = new ProsessTaskData(SendBeskjedUtsendtVarselTilSelvbetjeningTask.TASKTYPE);
        sendBeskjedUtsendtVarsel.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(sendVarselbrev);
        if (BrevToggle.brukDokprod(BrevType.VARSEL_BREV)) {
            taskGruppe.addNesteSekvensiell(sendBeskjedUtsendtVarsel);
        }
        prosessTaskRepository.lagre(taskGruppe);
    }

    private void håndteresInnhentDokumentasjon(Behandling behandling, DokumentMalType malType, String fritekst) {
        Long behandlingId = behandling.getId();
        if (!grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            throw InnhentDokumentasjonbrevFeil.FACTORY.kanIkkeSendeBrevForGrunnlagFinnesIkke(behandlingId).toException();
        }

        ProsessTaskData sendInnhentDokumentasjonBrev = new ProsessTaskData(InnhentDokumentasjonbrevTask.TASKTYPE);
        sendInnhentDokumentasjonBrev.setPayload(fritekst);
        sendInnhentDokumentasjonBrev.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        prosessTaskRepository.lagre(sendInnhentDokumentasjonBrev);
    }
}
