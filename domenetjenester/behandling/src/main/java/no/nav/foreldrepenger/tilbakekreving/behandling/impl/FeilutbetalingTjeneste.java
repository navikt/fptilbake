package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingUtil.sjekkAvvikHvisSisteDagIHelgen;
import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil.beregnBelop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Days;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

@ApplicationScoped
public class FeilutbetalingTjeneste {

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;

    FeilutbetalingTjeneste() {
        // for CDI proxy
    }

    @Inject
    public FeilutbetalingTjeneste(BehandlingRepositoryProvider repositoryProvider) {
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
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

    public BehandlingFeilutbetalingFakta lagBehandlingFeilUtbetalingFakta(Optional<VarselInfo> varselEntitet, BigDecimal aktuellFeilUtbetaltBeløp,
                                                                          List<UtbetaltPeriode> utbetaltPerioder, Periode totalPeriode,
                                                                          EksternBehandlingsinfoDto eksternBehandlingsinfoDto, Optional<TilbakekrevingValgDto> tilbakekrevingValgDto,
                                                                          String begrunnelse) {
        BigDecimal tidligereVarseltBeløp = varselEntitet.isPresent() ? BigDecimal.valueOf(varselEntitet.get().getVarselBeløp()).abs() : BigDecimal.ZERO;
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

    public List<KravgrunnlagPeriode432> finnPerioderMedFeilutbetaltPosteringer(List<KravgrunnlagPeriode432> allePerioder) {
        List<KravgrunnlagPeriode432> feilutbetaltPerioder = new ArrayList<>();
        for (KravgrunnlagPeriode432 kravgrunnlagPeriode432 : allePerioder) {
            List<KravgrunnlagBelop433> posteringer = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
                .filter(belop433 -> belop433.getKlasseType().equals(KlasseType.FEIL)).collect(Collectors.toList());
            if (!posteringer.isEmpty()) {
                kravgrunnlagPeriode432.setKravgrunnlagBeloper433(posteringer);
                feilutbetaltPerioder.add(kravgrunnlagPeriode432);
            }
        }
        return feilutbetaltPerioder;
    }

    public List<UtbetaltPeriode> finnesLogiskPeriode(List<KravgrunnlagPeriode432> feilutbetaltPerioder) {
        LocalDate førsteDag = null;
        LocalDate sisteDag = null;
        BigDecimal belopPerPeriode = BigDecimal.ZERO;
        feilutbetaltPerioder.sort(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        List<UtbetaltPeriode> beregnetPerioider = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : feilutbetaltPerioder) {
            // for første gang
            Periode periode = kgPeriode.getPeriode();
            if (førsteDag == null && sisteDag == null) {
                førsteDag = periode.getFom();
                sisteDag = periode.getTom();
            } else {
                // beregn forskjellen mellom to perioder
                int antallDager = Days.between(sisteDag, periode.getFom()).getAmount();
                // hvis forskjellen er mer enn 1 dager eller siste dag er i helgen
                if (antallDager > 1 && sjekkAvvikHvisSisteDagIHelgen(sisteDag, antallDager)) {
                    // lag ny perioder hvis forskjellen er mer enn 1 dager
                    beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
                    førsteDag = periode.getFom();
                    belopPerPeriode = BigDecimal.ZERO;
                }
                sisteDag = periode.getTom();
            }
            belopPerPeriode = belopPerPeriode.add(beregnBelop(kgPeriode.getKravgrunnlagBeloper433()));
        }
        if (belopPerPeriode != BigDecimal.ZERO) {
            beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
        }
        return beregnetPerioider;
    }

    public String hentFaktaBegrunnelse(Long behandlingId){
        Optional<FaktaFeilutbetaling> faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        return faktaFeilutbetaling.map(FaktaFeilutbetaling::getBegrunnelse).orElse(null);
    }

}
