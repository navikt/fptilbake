package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;

public class DokumentBehandlingTjenesteTest extends DokumentBestillerTestOppsett {

    private DokumentRepository dokumentRepository = new DokumentRepository(entityManager);

    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste = new DokumentBehandlingTjeneste(repositoryProvider, dokumentRepository, brevdataRepository);

    @Test
    public void skal_henteBrevMal_ForBehandlingId_som_har_ikke_sendt_varsel() {
        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.INNHENT_DOK)).findFirst()).isPresent();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.FRITEKST_DOK)).findFirst()).isPresent();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.VARSEL_DOK)).findFirst()).isPresent();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.KORRIGERT_VARSEL_DOK)).findFirst()).isEmpty();
    }

    @Test
    public void skal_henteBrevMal_ForBehandlingId_som_har_sendt_varsel() {
        lagreInfoOmVarselbrev(behandling.getId(), "123", "1243");

        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.INNHENT_DOK)).findFirst()).isPresent();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.FRITEKST_DOK)).findFirst()).isPresent();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.VARSEL_DOK)).findFirst()).isEmpty();
        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.KORRIGERT_VARSEL_DOK)).findFirst()).isPresent();
    }

    @Test
    public void skal_henteBrevMal_ForBehandlingId_som_har_avsluttet() {
        behandling.avsluttBehandling();

        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        Optional<BrevmalDto> innhentBrevMal = brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.INNHENT_DOK)).findFirst();
        assertThat(innhentBrevMal).isPresent();
        assertThat(innhentBrevMal.get().getTilgjengelig()).isTrue();

        Optional<BrevmalDto> fritekstBrevMal = brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.FRITEKST_DOK)).findFirst();
        assertThat(fritekstBrevMal).isPresent();
        assertThat(fritekstBrevMal.get().getTilgjengelig()).isTrue();

        Optional<BrevmalDto> varselBrevMal = brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.VARSEL_DOK)).findFirst();
        assertThat(varselBrevMal).isPresent();
        assertThat(varselBrevMal.get().getTilgjengelig()).isFalse();

        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.KORRIGERT_VARSEL_DOK)).findFirst()).isEmpty();
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, String journalpostId, String dokumentId) {
        VarselbrevSporing varselbrevSporing = new VarselbrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentId)
            .medJournalpostId(journalpostId)
            .build();
        brevdataRepository.lagreVarselbrevData(varselbrevSporing);
    }

}
