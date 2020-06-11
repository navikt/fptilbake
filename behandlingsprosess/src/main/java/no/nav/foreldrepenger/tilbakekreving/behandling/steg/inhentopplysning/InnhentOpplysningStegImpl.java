package no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@BehandlingStegRef(kode = "INOPPSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class InnhentOpplysningStegImpl implements InnhentOpplysningSteg {

    private static final Logger logger = LoggerFactory.getLogger(InnhentOpplysningStegImpl.class);

    private EksternBehandlingRepository eksternBehandlingRepository;
    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;

    private FagsystemKlient fagsystemKlient;

    public InnhentOpplysningStegImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentOpplysningStegImpl(BehandlingRepositoryProvider repositoryProvider, FagsystemKlient fagsystemKlient) {
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();

        this.fagsystemKlient = fagsystemKlient;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        //FIXME ?? tror ikke dette steget er i bruk



        Long behandlingId = kontekst.getBehandlingId();
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();

        SamletEksternBehandlingInfo samletEksternBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternBehandling.getEksternUuid(), Tillegsinformasjon.VARSELTEKST);
        String varselTekst = samletEksternBehandlingInfo.getVarseltekst();
        if (StringUtils.isNotEmpty(varselTekst)) {
            varselRepository.lagre(behandlingId, varselTekst, null);
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

}
