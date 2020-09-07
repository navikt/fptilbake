package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;

@ApplicationScoped
public class KravgrunnlagMapperProvider {

    private KravgrunnlagMapper kravgrunnlagMapper;
    private HentKravgrunnlagMapper hentKravgrunnlagMapper;

    KravgrunnlagMapperProvider(){
        // for CDI
    }

    @Inject
    public KravgrunnlagMapperProvider(KravgrunnlagMapper kravgrunnlagMapper, HentKravgrunnlagMapper hentKravgrunnlagMapper) {
        this.kravgrunnlagMapper = kravgrunnlagMapper;
        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
    }

    public KravgrunnlagMapper getKravgrunnlagMapper() {
        return kravgrunnlagMapper;
    }

    public HentKravgrunnlagMapper getHentKravgrunnlagMapper() {
        return hentKravgrunnlagMapper;
    }
}
