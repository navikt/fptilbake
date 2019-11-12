package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class FaktaFeilutbetalingTjeneste {

    private KravgrunnlagRepository grunnlagRepository;
    private VarselRepository varselRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private FpsakKlient fpsakKlient;

    FaktaFeilutbetalingTjeneste() {
        // for CDI proxy
    }

    @Inject
    public FaktaFeilutbetalingTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider, KravgrunnlagTjeneste kravgrunnlagTjeneste, FpsakKlient fpsakKlient) {
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.fpsakKlient = fpsakKlient;

        this.faktaFeilutbetalingRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.grunnlagRepository = behandlingRepositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = behandlingRepositoryProvider.getVarselRepository();

    }

    public void hentFeilutbetalingÅrsak(Long behandlingId, UtbetaltPeriode utbetaltPeriode) {
        Optional<FaktaFeilutbetaling> fakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        if (fakta.isPresent()) {
            Optional<FaktaFeilutbetalingPeriode> feilutbetalingPeriodeÅrsak = fakta.get()
                .getFeilutbetaltPerioder()
                .stream()
                .filter(periodeÅrsak -> utbetaltPeriode.tilPeriode().equals(periodeÅrsak.getPeriode()))
                .findFirst();
            utbetaltPeriode.setFeilutbetalingÅrsakDto(mapFra(feilutbetalingPeriodeÅrsak));
        }
    }

    public BehandlingFeilutbetalingFakta hentBehandlingFeilutbetalingFakta(Long behandlingId) {
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Optional<VarselInfo> resultat = varselRepository.finnVarsel(behandlingId);
        UUID eksternUuid = eksternBehandling.getEksternUuid();
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = hentEksternBehandlingFraFpsak(eksternUuid);
        Optional<TilbakekrevingValgDto> tilbakekrevingValg = fpsakKlient.hentTilbakekrevingValg(eksternUuid);

        List<KravgrunnlagPeriode432> feilutbetaltPerioder = kravgrunnlagTjeneste.finnKravgrunnlagPerioderMedFeilutbetaltPosteringer(behandlingId);
        BigDecimal aktuellFeilUtbetaltBeløp = BigDecimal.ZERO;
        List<UtbetaltPeriode> utbetaltPerioder = kravgrunnlagTjeneste.utledLogiskPeriode(feilutbetaltPerioder);
        LocalDate totalPeriodeFom = null;
        LocalDate totalPeriodeTom = null;
        for (UtbetaltPeriode utbetaltPeriode : utbetaltPerioder) {
            aktuellFeilUtbetaltBeløp = aktuellFeilUtbetaltBeløp.add(utbetaltPeriode.getBelop());
            hentFeilutbetalingÅrsak(behandlingId, utbetaltPeriode);
            totalPeriodeFom = totalPeriodeFom == null || totalPeriodeFom.isAfter(utbetaltPeriode.getFom()) ? utbetaltPeriode.getFom() : totalPeriodeFom;
            totalPeriodeTom = totalPeriodeTom == null || totalPeriodeTom.isBefore(utbetaltPeriode.getTom()) ? utbetaltPeriode.getTom() : totalPeriodeTom;
        }
        String begrunnelse = hentFaktaBegrunnelse(behandlingId);
        Periode totalPeriode = new Periode(totalPeriodeFom, totalPeriodeTom);
        return lagBehandlingFeilUtbetalingFakta(resultat, aktuellFeilUtbetaltBeløp, utbetaltPerioder, totalPeriode, eksternBehandlingsinfoDto, tilbakekrevingValg, begrunnelse);
    }


    private BehandlingFeilutbetalingFakta lagBehandlingFeilUtbetalingFakta(Optional<VarselInfo> varselEntitet, BigDecimal aktuellFeilUtbetaltBeløp,
                                                                           List<UtbetaltPeriode> utbetaltPerioder, Periode totalPeriode,
                                                                           EksternBehandlingsinfoDto eksternBehandlingsinfoDto, Optional<TilbakekrevingValgDto> tilbakekrevingValgDto,
                                                                           String begrunnelse) {
        Long tidligereVarseltBeløp = varselEntitet.isPresent() ? varselEntitet.get().getVarselBeløp() : null;
        return BehandlingFeilutbetalingFakta.builder()
            .medPerioder(utbetaltPerioder)
            .medAktuellFeilUtbetaltBeløp(aktuellFeilUtbetaltBeløp)
            .medTidligereVarsletBeløp(tidligereVarseltBeløp)
            .medTotalPeriodeFom(totalPeriode.getFom())
            .medTotalPeriodeTom(totalPeriode.getTom())
            .medDatoForRevurderingsvedtak(eksternBehandlingsinfoDto.getVedtakDato())
            .medBehandlingsResultat(eksternBehandlingsinfoDto.getBehandlingsresultat())
            .medBehandlingÅrsaker(eksternBehandlingsinfoDto.getBehandlingÅrsaker())
            .medTilbakekrevingValg(tilbakekrevingValgDto.orElse(null))
            .medBegrunnelse(begrunnelse)
            .build();
    }

    private HendelseTypeMedUndertypeDto mapFra(Optional<FaktaFeilutbetalingPeriode> årsak) {
        if (årsak.isPresent()) {
            FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = årsak.get();
            return new HendelseTypeMedUndertypeDto(faktaFeilutbetalingPeriode.getHendelseType(), faktaFeilutbetalingPeriode.getHendelseUndertype());
        }
        return null;
    }


    public String hentFaktaBegrunnelse(Long behandlingId) {
        Optional<FaktaFeilutbetaling> faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        return faktaFeilutbetaling.map(FaktaFeilutbetaling::getBegrunnelse).orElse(null);
    }

    private EksternBehandlingsinfoDto hentEksternBehandlingFraFpsak(UUID eksternUuid) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsInfo = fpsakKlient.hentBehandling(eksternUuid);
        if (eksternBehandlingsInfo.isPresent()) {
            return eksternBehandlingsInfo.get();
        }
        throw BehandlingFeil.FACTORY.fantIkkeEksternBehandlingForUuid(eksternUuid.toString()).toException();
    }
}
