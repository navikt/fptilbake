package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;

@ApplicationScoped
public class KravgrunnlagHenter {

    private static final Logger LOG = LoggerFactory.getLogger(KravgrunnlagHenter.class);

    private HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy;
    private ØkonomiProxyKlient økonomiProxyKlient;

    public KravgrunnlagHenter() {
        // for CDI
    }

    @Inject
    public KravgrunnlagHenter(ØkonomiProxyKlient økonomiProxyKlient, HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy) {
        this.økonomiProxyKlient = økonomiProxyKlient;
        this.hentKravgrunnlagMapperProxy = hentKravgrunnlagMapperProxy;
    }

    public Kravgrunnlag431 hentKravgrunnlagFraOS(Long behandlingId, HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto) {
        LOG.info("Henter kravgrunnlag for behandling{} med kravgrunnlagId {}",
            behandlingId != null ? " " + behandlingId : "",
            hentKravgrunnlagDetaljDto.kravgrunnlagId().intValue());
        var respons = økonomiProxyKlient.hentKravgrunnlag(hentKravgrunnlagDetaljDto);
        var kravgrunnlag431 = hentKravgrunnlagMapperProxy.mapTilDomene(respons);
        LOG.info("Kravgrunnlag hentet OK");
        return kravgrunnlag431;
    }
}
