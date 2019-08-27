package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

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

@ApplicationScoped
public class DokumentBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private BrevdataRepository brevdataRepository;


    DokumentBehandlingTjeneste() {
        // for cdi proxy
    }

    @Inject
    public DokumentBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider, BrevdataRepository brevdataRepository) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.brevdataRepository = brevdataRepository;
    }


    public List<BrevmalDto> hentBrevmalerFor(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<DokumentMalType> gyldigBrevMaler = new ArrayList<>();

        gyldigBrevMaler.add(kodeverkRepository.finn(DokumentMalType.class, DokumentMalType.INNHENT_DOK));
        gyldigBrevMaler.add(kodeverkRepository.finn(DokumentMalType.class, DokumentMalType.FRITEKST_DOK));

        leggTilVarselBrevmaler(behandlingId, gyldigBrevMaler);

        return tilBrevmalDto(behandling, gyldigBrevMaler);
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

}
