package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevTask;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.ManueltVarselBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.SendManueltVarselbrevTask;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendBeskjedUtsendtVarselTilSelvbetjeningTask;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class DokumentBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private ProsessTaskTjeneste taskTjeneste;

    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;
    private InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste;


    DokumentBehandlingTjeneste() {
        // for cdi proxy
    }

    @Inject
    public DokumentBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                      ProsessTaskTjeneste taskTjeneste,
                                      HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                      ManueltVarselBrevTjeneste manueltVarselBrevTjeneste,
                                      InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.taskTjeneste = taskTjeneste;

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
            throw new TekniskException("FPT-612900", String.format("Kravgrunnlag finnes ikke for behandling=%s, kan ikke sende varsel", behandlingId));
        }

        ProsessTaskData sendVarselbrev = ProsessTaskData.forProsessTask(SendManueltVarselbrevTask.class);
        sendVarselbrev.setProperty(TaskProperty.MAL_TYPE, malType.getKode());
        sendVarselbrev.setPayload(fritekst);
        sendVarselbrev.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        ProsessTaskData sendBeskjedUtsendtVarsel = ProsessTaskData.forProsessTask(SendBeskjedUtsendtVarselTilSelvbetjeningTask.class);
        sendBeskjedUtsendtVarsel.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(sendVarselbrev);
        // TODO(sladek) mangler det en slik eller håndteres det nedstrøms i sendvarselbrevtask? taskGruppe.addNesteSekvensiell(sendBeskjedUtsendtVarsel);
        taskTjeneste.lagre(taskGruppe);
    }

    private void håndteresInnhentDokumentasjon(Behandling behandling, DokumentMalType malType, String fritekst) {
        Long behandlingId = behandling.getId();
        if (!grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            throw new TekniskException("FPT-612901", String.format("Kravgrunnlag finnes ikke for behandling=%s, kan ikke sende innhent-dokumentasjonbrev", behandlingId));
        }

        ProsessTaskData sendInnhentDokumentasjonBrev = ProsessTaskData.forProsessTask(InnhentDokumentasjonbrevTask.class);
        sendInnhentDokumentasjonBrev.setPayload(fritekst);
        sendInnhentDokumentasjonBrev.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());

        taskTjeneste.lagre(sendInnhentDokumentasjonBrev);
    }
}
