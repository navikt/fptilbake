package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;

@Dependent
public class BeregningsresultatTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(BeregningsresultatTjeneste.class);

    private final TilbakekrevingBeregningTjeneste beregningTjeneste;
    private final BeregningsresultatRepository beregningsresultatRepository;
    private final boolean lansertLagring;

    @Inject
    public BeregningsresultatTjeneste(TilbakekrevingBeregningTjeneste beregningTjeneste,
                                      BeregningsresultatRepository beregningsresultatRepository,
                                      @KonfigVerdi(value = "toggle.enable.lagre.beregningsresultat", defaultVerdi = "false") boolean lansertLagring) {
        this.beregningTjeneste = beregningTjeneste;
        this.beregningsresultatRepository = beregningsresultatRepository;
        this.lansertLagring = lansertLagring;
    }

    public BeregningResultat finnEllerBeregn(Long behandlingId) {
        if (lansertLagring) {
            var lagretResultat = beregningsresultatRepository.hentHvisEksisterer(behandlingId);
            if (lagretResultat.isPresent()) {
                LOG.info("TBK-BEREGNING: Fant lagret beregningsgrunnlag.");
                return BeregningsresultatMapper.map(lagretResultat.get());
            }
        }
        return beregningTjeneste.beregn(behandlingId);
    }

    public void beregnOgLagre(Long behandlingId) {
        if (lansertLagring) {
            var beregnet = beregningTjeneste.beregn(behandlingId);
            beregningsresultatRepository.lagre(behandlingId, BeregningsresultatMapper.map(beregnet));
        }
    }

}
