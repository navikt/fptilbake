package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.sensu.SensuKlient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class BeregningsresultatTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(BeregningsresultatTjeneste.class);

    private TilbakekrevingBeregningTjeneste beregningTjeneste;
    private BeregningsresultatRepository beregningsresultatRepository;
    private boolean lansertLagring;

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
            Optional<BeregningsresultatEntitet> lagretResultat = beregningsresultatRepository.hentHvisEksisterer(behandlingId);
            if (lagretResultat.isPresent()) {
                LOG.info("BEREGNING-REST: Finner lagret beregningsgrunnlag.");
                return BeregningsresultatMapper.map(lagretResultat.get());
            }
        }
        return beregningTjeneste.beregn(behandlingId);
    }

    public void beregnOgLagre(Long behandlingId) {
        if (lansertLagring) {
            BeregningResultat beregnet = beregningTjeneste.beregn(behandlingId);
            beregningsresultatRepository.lagre(behandlingId, BeregningsresultatMapper.map(beregnet));
        }
    }

}
