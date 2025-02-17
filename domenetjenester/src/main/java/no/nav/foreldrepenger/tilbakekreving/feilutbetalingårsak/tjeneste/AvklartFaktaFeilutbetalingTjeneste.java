package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagEndretEvent;

@ApplicationScoped
public class AvklartFaktaFeilutbetalingTjeneste {

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private AvklartFaktaFeilutbetalingHistorikkTjeneste avklartFaktaFeilutbetalingHistorikkTjeneste;

    AvklartFaktaFeilutbetalingTjeneste() {
        // For CDI
    }

    @Inject
    public AvklartFaktaFeilutbetalingTjeneste(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository,
                                              AvklartFaktaFeilutbetalingHistorikkTjeneste avklartFaktaFeilutbetalingHistorikkTjeneste) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.avklartFaktaFeilutbetalingHistorikkTjeneste = avklartFaktaFeilutbetalingHistorikkTjeneste;
    }

    public void lagreÅrsakForFeilutbetalingPeriode(Behandling behandling, List<FaktaFeilutbetalingDto> feilutbetalingFaktas, String begrunnelse) {
        var forrigeFakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandling.getId());
        var faktaFeilutbetaling = lagNyFaktaFeilutbetaling(feilutbetalingFaktas, begrunnelse);
        faktaFeilutbetalingRepository.lagre(behandling.getId(), faktaFeilutbetaling);
        avklartFaktaFeilutbetalingHistorikkTjeneste.lagHistorikkinnslagForAvklartFaktaFeilutbetaling(behandling, feilutbetalingFaktas, forrigeFakta, begrunnelse);
    }

    private static FaktaFeilutbetaling lagNyFaktaFeilutbetaling(List<FaktaFeilutbetalingDto> feilutbetalingFaktas, String begrunnelse) {
        var faktaFeilutbetaling = new FaktaFeilutbetaling();
        for (var faktaFeilutbetalingDto : feilutbetalingFaktas) {
            var faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder()
                    .medPeriode(faktaFeilutbetalingDto.getFom(), faktaFeilutbetalingDto.getTom())
                    .medHendelseType(faktaFeilutbetalingDto.getHendelseType())
                    .medHendelseUndertype(faktaFeilutbetalingDto.getHendelseUndertype())
                    .medFeilutbetalinger(faktaFeilutbetaling)
                    .build();
            faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);
        }
        faktaFeilutbetaling.setBegrunnelse(begrunnelse);
        return faktaFeilutbetaling;
    }

    public void slettGammelFaktaData(@Observes KravgrunnlagEndretEvent event) {
        faktaFeilutbetalingRepository.slettFaktaFeilutbetaling(event.getBehandlingId());
    }
}
