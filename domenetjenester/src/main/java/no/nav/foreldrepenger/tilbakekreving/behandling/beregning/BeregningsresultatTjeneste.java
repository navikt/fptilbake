package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.Beregningsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;

@Dependent
public class BeregningsresultatTjeneste {

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
            Optional<Beregningsresultat> lagretResultat = beregningsresultatRepository.hentHvisEksisterer(behandlingId);
            if (lagretResultat.isPresent()) {
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
