package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;

public class DokumentBehandlingTjenesteTest extends DokumentBestillerTestOppsett {

    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste = new DokumentBehandlingTjeneste(repositoryProvider, brevdataRepository);

    @Test
    public void skal_henteBrevMal_for_behandling_som_har_ikke_sendt_varsel() {
        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.INNHENT_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.FRITEKST_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.VARSEL_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.KORRIGERT_VARSEL_DOK)).isEmpty();
    }

    @Test
    public void skal_henteBrevMal_for_behandling_som_har_sendt_varsel() {
        lagreInfoOmVarselbrev(behandling.getId(), "123", "1243");

        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.INNHENT_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.FRITEKST_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.VARSEL_DOK)).isEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler,DokumentMalType.KORRIGERT_VARSEL_DOK)).isPresent();
    }

    @Test
    public void skal_henteBrevMal_for_behandling_som_har_avsluttet() {
        behandling.avsluttBehandling();

        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        Optional<BrevmalDto> innhentBrevMal = hentSpesifiskBrevMal(brevMaler,DokumentMalType.INNHENT_DOK);
        assertThat(innhentBrevMal).isPresent();
        assertThat(innhentBrevMal.get().getTilgjengelig()).isTrue();

        Optional<BrevmalDto> fritekstBrevMal = hentSpesifiskBrevMal(brevMaler,DokumentMalType.FRITEKST_DOK);
        assertThat(fritekstBrevMal).isPresent();
        assertThat(fritekstBrevMal.get().getTilgjengelig()).isTrue();

        Optional<BrevmalDto> varselBrevMal = hentSpesifiskBrevMal(brevMaler,DokumentMalType.VARSEL_DOK);
        assertThat(varselBrevMal).isPresent();
        assertThat(varselBrevMal.get().getTilgjengelig()).isFalse();

        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.KORRIGERT_VARSEL_DOK)).findFirst()).isEmpty();
    }

    private Optional<BrevmalDto> hentSpesifiskBrevMal(List<BrevmalDto> brevMaler, DokumentMalType malType){
        return brevMaler.stream().filter(brevmalDto -> malType.getKode().equals(brevmalDto.getKode())).findFirst();
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
