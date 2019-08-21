package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;

@ApplicationScoped
public class DokumentBehandlingTjeneste {
    private DokumentRepository dokumentRepository;
    private BehandlingRepository behandlingRepository;
    private BrevdataRepository brevdataRepository;

    DokumentBehandlingTjeneste() {
        // for cdi proxy
    }

    @Inject
    public DokumentBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider, DokumentRepository dokumentRepository,
                                      BrevdataRepository brevdataRepository) {

        Objects.requireNonNull(repositoryProvider, "repositoryProvider");
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.dokumentRepository = dokumentRepository;
        this.brevdataRepository = brevdataRepository;
    }


    public List<BrevmalDto> hentBrevmalerFor(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<DokumentMalType> kandidater = new ArrayList<>(dokumentRepository.hentAlleDokumentMalTyper());
        List<DokumentMalType> fjernes = filtrerUtilgjengeligBrevmaler(behandlingId);
        kandidater.removeAll(fjernes);
        return tilBrevmalDto(behandling, sorterte(kandidater));
    }

    private List<DokumentMalType> sorterte(List<DokumentMalType> kandidater) {
        List<DokumentMalType> sorterte = new ArrayList<>();
        kandidater.stream()
            .filter(dm -> DokumentMalRestriksjon.INGEN.equals(dm.getDokumentMalRestriksjon()))
            .forEach(sorterte::add);
        kandidater.stream()
            .filter(dm -> !(DokumentMalRestriksjon.INGEN.equals(dm.getDokumentMalRestriksjon())))
            .forEach(sorterte::add);
        return sorterte;
    }


    // Fjerner dokumentmaler som aldri er relevante for denne behandlingstypen
    private List<DokumentMalType> filtrerUtilgjengeligBrevmaler(Long behandlingId) {
        List<DokumentMalType> fjernes = new ArrayList<>();
        if (brevdataRepository.harVarselBrevSendtForBehandlingId(behandlingId)) {
            fjernes.add(dokumentRepository.hentDokumentMalType(DokumentMalType.VARSEL_DOK));
        } else {
            fjernes.add(dokumentRepository.hentDokumentMalType(DokumentMalType.KORRIGERT_VARSEL_DOK));
        }
        return fjernes;
    }

    // Markerer som ikke tilgjengelige de brevmaler som ikke er aktuelle i denne behandlingen
    private List<BrevmalDto> tilBrevmalDto(Behandling behandling, List<DokumentMalType> dmtList) {
        List<BrevmalDto> brevmalDtoList = new ArrayList<>(dmtList.size());
        for (DokumentMalType dmt : dmtList) {
            boolean tilgjengelig = sjekkOmTilgjengelig(behandling, dmt);
            brevmalDtoList.add(new BrevmalDto(dmt.getKode(), dmt.getNavn(), dmt.getDokumentMalRestriksjon(), tilgjengelig));
        }
        return brevmalDtoList;
    }

    private boolean sjekkOmTilgjengelig(Behandling behandling, DokumentMalType mal) {
        DokumentMalRestriksjon restriksjon = mal.getDokumentMalRestriksjon();
        if (DokumentMalRestriksjon.ÅPEN_BEHANDLING.equals(restriksjon)) {
            return !behandling.erSaksbehandlingAvsluttet() && !behandling.erAvsluttet();
        }
        return true;
    }

}
