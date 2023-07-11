package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;

@Dependent
public class BeregningsresultatTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(BeregningsresultatTjeneste.class);

    private final TilbakekrevingBeregningTjeneste beregningTjeneste;
    private final BeregningsresultatRepository beregningsresultatRepository;

    @Inject
    public BeregningsresultatTjeneste(TilbakekrevingBeregningTjeneste beregningTjeneste,
                                      BeregningsresultatRepository beregningsresultatRepository) {
        this.beregningTjeneste = beregningTjeneste;
        this.beregningsresultatRepository = beregningsresultatRepository;
    }

    public BeregningResultat finnEllerBeregn(Long behandlingId) {
        var lagretResultat = beregningsresultatRepository.hentHvisEksisterer(behandlingId);
        if (lagretResultat.isPresent()) {
            LOG.info("TBK-BEREGNING: Fant lagret beregningsgrunnlag.");
            return BeregningsresultatMapper.map(lagretResultat.get());
        }

        return beregningTjeneste.beregn(behandlingId);
    }

    public void beregnOgLagre(Long behandlingId) {
        var beregnet = beregningTjeneste.beregn(behandlingId);
        beregningsresultatRepository.lagre(behandlingId, BeregningsresultatMapper.map(beregnet));
    }

}
