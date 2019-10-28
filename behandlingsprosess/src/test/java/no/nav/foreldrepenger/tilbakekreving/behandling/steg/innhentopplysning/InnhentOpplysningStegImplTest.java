package no.nav.foreldrepenger.tilbakekreving.behandling.steg.innhentopplysning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning.InnhentOpplysningSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning.InnhentOpplysningStegImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;

public class InnhentOpplysningStegImplTest extends FellesTestOppsett {

    private InnhentOpplysningSteg innhentOpplysningSteg = new InnhentOpplysningStegImpl(repositoryProvider, fpsakKlientMock);
    private VarselRepository varselRepository = repositoryProvider.getVarselRepository();
    private final String varselTekst = "Dette er varselTekst";

    @Before
    public void setup(){
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @Test
    public void utføreSteg_medVarsel() {
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = SamletEksternBehandlingInfo
            .builder(Tillegsinformasjon.VARSELTEKST)
            .setVarseltekst(varselTekst).build();
        when(fpsakKlientMock.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.VARSELTEKST)).thenReturn(samletEksternBehandlingInfo);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        Optional<VarselEntitet> entitet = varselRepository.finnVarsel(behandling.getId());
        assertThat(entitet).isPresent();
        VarselEntitet varselEntitet = entitet.get();
        assertThat(varselEntitet.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(varselEntitet.getVarselTekst()).isEqualTo(varselTekst);
    }

    @Test
    public void utføreSteg_utenVarsel() {
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = SamletEksternBehandlingInfo
            .builder(Tillegsinformasjon.VARSELTEKST)
            .setVarseltekst("").build();
        when(fpsakKlientMock.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.VARSELTEKST)).thenReturn(samletEksternBehandlingInfo);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        Optional<VarselEntitet> entitet = varselRepository.finnVarsel(behandling.getId());
        assertThat(entitet).isEmpty();
    }

}
