package no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;

@BehandlingStegRef(kode = "INOPPSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class InnhentOpplysningStegImpl implements InnhentOpplysningSteg {

    private EksternBehandlingRepository eksternBehandlingRepository;
    private VarselRepository varselRepository;

    private FpsakKlient restKlient;

    public InnhentOpplysningStegImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentOpplysningStegImpl(BehandlingRepositoryProvider repositoryProvider, FpsakKlient fpsakKlient) {
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = repositoryProvider.getVarselRepository();

        this.restKlient = fpsakKlient;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Long behandlingId = kontekst.getBehandlingId();
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);

        SamletEksternBehandlingInfo samletEksternBehandlingInfo = restKlient.hentBehandlingsinfo(eksternBehandling.getEksternUuid(), Tillegsinformasjon.VARSELTEKST);
        String varselTekst = samletEksternBehandlingInfo.getVarseltekst();
        if (StringUtils.isNotEmpty(varselTekst)) {
            varselRepository.lagre(behandlingId, varselTekst, null);
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }
}
