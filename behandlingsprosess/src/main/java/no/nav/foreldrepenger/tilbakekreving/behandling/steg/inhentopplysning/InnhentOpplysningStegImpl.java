package no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@BehandlingStegRef(kode = "INOPPSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class InnhentOpplysningStegImpl implements InnhentOpplysningSteg {

    private static final Logger logger = LoggerFactory.getLogger(InnhentOpplysningStegImpl.class);

    private EksternBehandlingRepository eksternBehandlingRepository;
    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;

    private FpsakKlient fpsakKlient;

    public InnhentOpplysningStegImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentOpplysningStegImpl(BehandlingRepositoryProvider repositoryProvider, FpsakKlient fpsakKlient) {
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();

        this.fpsakKlient = fpsakKlient;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Long behandlingId = kontekst.getBehandlingId();
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        Optional<String> grunnlagReferanse = sjekkOgHentRiktigReferanseForManuellOpprettetBehandling(behandling, eksternBehandling);
        if (grunnlagReferanse.isPresent()) {
            oppdatereEksternBehandlingMedRiktigReferanse(behandling, saksnummer, Long.valueOf(grunnlagReferanse.get()));
        }

        SamletEksternBehandlingInfo samletEksternBehandlingInfo = fpsakKlient.hentBehandlingsinfo(eksternBehandling.getEksternUuid(), Tillegsinformasjon.VARSELTEKST);
        String varselTekst = samletEksternBehandlingInfo.getVarseltekst();
        if (StringUtils.isNotEmpty(varselTekst)) {
            varselRepository.lagre(behandlingId, varselTekst, null);
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    public Optional<String> sjekkOgHentRiktigReferanseForManuellOpprettetBehandling(Behandling behandling, EksternBehandling eksternBehandling) {
        Long behandlingId = behandling.getId();
        if (behandling.isManueltOpprettet() && grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandlingId);
            if (!kravgrunnlag431.getReferanse().equalsIgnoreCase(String.valueOf(eksternBehandling.getEksternId()))) {
                logger.info("Tilkoblet grunnlag har en annen referanse enn behandling for behandlingId={}", behandlingId);
                return Optional.of(kravgrunnlag431.getReferanse());
            }
        }
        return Optional.empty();
    }

    public void oppdatereEksternBehandlingMedRiktigReferanse(Behandling behandling, Saksnummer saksnummer, Long grunnlagReferanse) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fpsakKlient.hentBehandlingForSaksnummer(saksnummer.getVerdi());
        if (!eksternBehandlinger.isEmpty()) {
            Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDto = eksternBehandlinger.stream()
                .filter(eksternBehandling -> eksternBehandling.getId().equals(grunnlagReferanse)).findFirst();
            if (eksternBehandlingsinfoDto.isPresent()) {
                logger.info("Oppdaterer ekstern behandling referanse med referanse={} for behandlingId={}", grunnlagReferanse, behandling.getId());
                EksternBehandlingsinfoDto fpsakEksternBehandling = eksternBehandlingsinfoDto.get();
                EksternBehandling eksternBehandling = new EksternBehandling(behandling, fpsakEksternBehandling.getId(), fpsakEksternBehandling.getUuid());
                eksternBehandlingRepository.lagre(eksternBehandling);
            }
        }
    }
}
