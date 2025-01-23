package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingResultatTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingÅrsakMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.YtelseTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingMetode;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.Periode;

@ApplicationScoped
public class BehandlingTilstandTjeneste {

    private static final Fagsystem FAGSYSTEM = ApplicationName.hvilkenTilbake();
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
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        return hentBehandlingensTilstand(behandling);
    }

    public BehandlingTilstand hentBehandlingensTilstand(Behandling behandling) {
        return hentBehandlingensTilstand(behandling, FAGSYSTEM);
    }

    public BehandlingTilstand hentBehandlingensTilstand(Behandling behandling, Fagsystem fagsystem) {
        var eksternBehandling = getEksternBehandling(behandling.getId());
        if (eksternBehandling == null) {
            return null;
        }
        BehandlingResultatType behandlingResultatType = behandlingresultatRepository.hent(behandling)
            .map(Behandlingsresultat::getBehandlingResultatType)
            .orElse(BehandlingResultatType.IKKE_FASTSATT);

        boolean venterPåBruker = behandling.getÅpneAksjonspunkter().stream().anyMatch(aksjonspunkt -> Venteårsak.venterPåBruker(aksjonspunkt.getVenteårsak()));
        boolean venterPåØkonomi = behandling.getÅpneAksjonspunkter().stream().anyMatch(aksjonspunkt -> Venteårsak.venterPåØkonomi(aksjonspunkt.getVenteårsak()));

        Optional<BehandlingÅrsak> revurderingBehandlingsårsak = behandling.getBehandlingÅrsaker().stream()
            .filter(it->it.getBehandlingÅrsakType().erRevurderingÅrsak())
            .findFirst();
        Optional<Behandling> forrigeBehandling = revurderingBehandlingsårsak
            .map(BehandlingÅrsak::getOriginalBehandling)
            .filter(Optional::isPresent)
            .map(Optional::get);

        BehandlingTilstand tilstand = new BehandlingTilstand();
        tilstand.setYtelseType(YtelseTypeMapper.getYtelseType(behandling.getFagsak().getFagsakYtelseType()));
        tilstand.setSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi());
        tilstand.setBehandlingUuid(behandling.getUuid());
        tilstand.setReferertFagsakBehandlingUuid(eksternBehandling.getEksternUuid());
        tilstand.setBehandlingType(BehandlingTypeMapper.getBehandlingType(behandling.getType()));
        tilstand.setBehandlingStatus(BehandlingStatusMapper.getBehandlingStatus(behandling.getStatus(),
                Fagsystem.FPTILBAKE.equals(fagsystem) && venterPåBruker, Fagsystem.FPTILBAKE.equals(fagsystem) && venterPåØkonomi));
        tilstand.setBehandlingResultat(BehandlingResultatTypeMapper.getBehandlingResultatType(behandlingResultatType));
        tilstand.setBehandlingMetode(utledBehandlingMetode(behandling));
        tilstand.setBehandlendeEnhetKode(behandling.getBehandlendeEnhetId());
        tilstand.setAnsvarligBeslutter(behandling.getAnsvarligBeslutter());
        tilstand.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        tilstand.setErBehandlingManueltOpprettet(erSaksbehandler(behandling.getOpprettetAv()));
        tilstand.setOpprettetAv(behandling.getOpprettetAv());
        tilstand.setFunksjonellTid(behandling.erAvsluttet() ? tilOffsetDateTime(behandling.getEndretTidspunkt()) : OffsetDateTime.now(ZoneOffset.UTC));
        tilstand.setRegistrertTid(tilOffsetDateTime(behandling.getOpprettetTidspunkt()));
        tilstand.setFerdigBehandletTid(behandling.erAvsluttet() ? tilOffsetDateTime(behandling.getEndretTidspunkt()) : null);
        tilstand.setVenterPåBruker(venterPåBruker);
        tilstand.setVenterPåØkonomi(venterPåØkonomi);
        forrigeBehandling.ifPresent(forrige -> tilstand.setForrigeBehandling(forrige.getUuid()));
        revurderingBehandlingsårsak.ifPresent(årsak -> tilstand.setRevurderingOpprettetÅrsak(BehandlingÅrsakMapper.getRevurderingÅrsak(årsak)));
        Optional<PeriodeMedBeløp> totaltFraKravgrunnlag = kravgrunnlagTjeneste.finnTotaltForKravgrunnlag(behandling.getId());
        totaltFraKravgrunnlag.ifPresent(totalt -> {
            var periode = totalt.getPeriode();
            tilstand.setTotalFeilutbetaltPeriode(periode != null ? new Periode(periode.getFom(), periode.getTom()) : null);
            tilstand.setTotalFeilutbetaltBeløp(totalt.getBeløp());
        });


        return tilstand;
    }

    private EksternBehandling getEksternBehandling(long behandlingId) {
        if (eksternBehandlingRepository.finnesAktivtEksternBehandling(behandlingId)) {
            return eksternBehandlingRepository.hentFraInternId(behandlingId);
        } else {
            return eksternBehandlingRepository.hentForSisteAktivertInternId(behandlingId);
        }
    }

    private static OffsetDateTime tilOffsetDateTime(LocalDateTime tidspunkt) {
        return OffsetDateTime.ofInstant(tidspunkt.atZone(ZoneId.systemDefault()).toInstant(), ZoneOffset.UTC);
    }

    private static BehandlingMetode utledBehandlingMetode(Behandling behandling) {
        if (!behandling.erSaksbehandlingAvsluttet()) {
            return null;
        }
        if (behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.FATTE_VEDTAK).filter(Aksjonspunkt::erUtført).isPresent()) {
            return BehandlingMetode.TOTRINN;
        }
        if (behandling.getAksjonspunkter().stream().anyMatch(BehandlingTilstandTjeneste::harSaksbehandlerVurdertAksjonspunkt)) {
            return BehandlingMetode.MANUELL;
        }
        if (erSaksbehandler(behandling.getOpprettetAv())) {
            return BehandlingMetode.MANUELL;
        }
        return BehandlingMetode.AUTOMATISK;
    }

    private static boolean harSaksbehandlerVurdertAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        return erSaksbehandler(aksjonspunkt.getEndretAv()) || erSaksbehandler(aksjonspunkt.getOpprettetAv());
    }

    private static boolean erSaksbehandler(String s) {
        return s != null && !s.startsWith("srv") && !s.startsWith("SRV") && !"VL".equals(s);
    }
}
