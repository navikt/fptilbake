package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.PeriodeMedBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingResultatTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingÅrsakMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.YtelseTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.Periode;

@ApplicationScoped
public class BehandlingTilstandTjeneste {

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;


    public BehandlingTilstandTjeneste() {
        //for CDI proxy
    }

    @Inject
    public BehandlingTilstandTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                      KravgrunnlagTjeneste kravgrunnlagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.behandlingresultatRepository = repositoryProvider.getBehandlingresultatRepository();
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
    }

    public BehandlingTilstand hentBehandlingensTilstand(long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        EksternBehandling eksternBehandling = getEksternBehandling(behandlingId);
        BehandlingResultatType behandlingResultatType = behandlingresultatRepository.hent(behandling)
            .map(Behandlingsresultat::getBehandlingResultatType)
            .orElse(BehandlingResultatType.IKKE_FASTSATT);

        boolean venterPåBruker = behandling.getÅpneAksjonspunkter().stream().anyMatch(aksjonspunkt -> Venteårsak.venterPåBruker(aksjonspunkt.getVenteårsak()));
        boolean venterPåØkonomi = behandling.getÅpneAksjonspunkter().stream().anyMatch(aksjonspunkt -> Venteårsak.venterPåØkonomi(aksjonspunkt.getVenteårsak()));

        Optional<BehandlingÅrsak> behandlingsårsak = behandling.getBehandlingÅrsaker().stream().findFirst();
        Optional<Behandling> forrigeBehandling = behandlingsårsak
            .map(BehandlingÅrsak::getOriginalBehandling)
            .filter(Optional::isPresent)
            .map(Optional::get);

        BehandlingTilstand tilstand = new BehandlingTilstand();
        tilstand.setYtelseType(YtelseTypeMapper.getYtelseType(behandling.getFagsak().getFagsakYtelseType()));
        tilstand.setSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi());
        tilstand.setBehandlingUuid(behandling.getUuid());
        tilstand.setReferertFagsakBehandlingUuid(eksternBehandling.getEksternUuid());
        tilstand.setBehandlingType(BehandlingTypeMapper.getBehandlingType(behandling.getType()));
        tilstand.setBehandlingStatus(BehandlingStatusMapper.getBehandlingStatus(behandling.getStatus()));
        tilstand.setBehandlingResultat(BehandlingResultatTypeMapper.getBehandlingResultatType(behandlingResultatType));
        tilstand.setBehandlendeEnhetKode(behandling.getBehandlendeEnhetId());
        tilstand.setAnsvarligBeslutter(behandling.getAnsvarligBeslutter());
        tilstand.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        tilstand.setErBehandlingManueltOpprettet(erSaksbehandler(behandling.getOpprettetAv()));
        tilstand.setFunksjonellTid(OffsetDateTime.now(ZoneOffset.UTC));
        tilstand.setVenterPåBruker(venterPåBruker);
        tilstand.setVenterPåØkonomi(venterPåØkonomi);
        forrigeBehandling.ifPresent(forrige -> tilstand.setForrigeBehandling(forrige.getUuid()));
        behandlingsårsak.ifPresent(årsak -> tilstand.setRevurderingOpprettetÅrsak(BehandlingÅrsakMapper.getRevurderingÅrsak(årsak)));

        Optional<PeriodeMedBeløp> totaltFraKravgrunnlag = kravgrunnlagTjeneste.finnTotaltForKravgrunnlag(behandlingId);
        totaltFraKravgrunnlag.ifPresent(totalt -> {
            var periode = totalt.getPeriode();
            tilstand.setTotalFeilutbetaltPeriode(periode != null ? new Periode(periode.getFom(), periode.getTom()) : null);
            tilstand.setTotalFeilutbetaltBeløp(totalt.getBeløp());
        });


        return tilstand;
    }

    private EksternBehandling getEksternBehandling(long behandlingId) {
        EksternBehandling eksternBehandling;
        if (eksternBehandlingRepository.finnesAktivtEksternBehandling(behandlingId)) {
            eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        } else {
            eksternBehandling = eksternBehandlingRepository.hentForSisteAktivertInternId(behandlingId);
        }
        return eksternBehandling;
    }

    private static boolean erSaksbehandler(String s) {
        return s != null && !s.startsWith("srv") && !s.startsWith("SRV") && !"VL".equals(s);
    }
}
