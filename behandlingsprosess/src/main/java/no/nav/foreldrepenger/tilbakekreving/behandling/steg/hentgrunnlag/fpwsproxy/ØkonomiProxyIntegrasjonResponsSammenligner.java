package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.request.KodeAksjon;
import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;

@ApplicationScoped
public class ØkonomiProxyIntegrasjonResponsSammenligner {

    private static final Logger LOG = LoggerFactory.getLogger(ØkonomiProxyIntegrasjonResponsSammenligner.class);
    private static final Logger SECURE_LOG = LoggerFactory.getLogger("secureLogger");

    private ØkonomiProxyKlient økonomiProxyKlient;
    private HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy;
    private static final Environment ENV = Environment.current();

    public ØkonomiProxyIntegrasjonResponsSammenligner() {
        // for CDI
    }

    @Inject
    public ØkonomiProxyIntegrasjonResponsSammenligner(ØkonomiProxyKlient økonomiProxyKlient,
                                                      HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy) {
        this.økonomiProxyKlient = økonomiProxyKlient;
        this.hentKravgrunnlagMapperProxy = hentKravgrunnlagMapperProxy;
    }

    public void hentKravgrunnlagFraFpwsproxyOgSammenlignFailsafe(Long behandlingId, HentKravgrunnlagDetaljDto detaljertKravgrunnlag, Kravgrunnlag431 kravgrunnlagFraGammelIntegrasjon) {
        if (!ENV.isProd()) {
            try {
                Kravgrunnlag431Dto kravgrunnlag431Dto = økonomiProxyKlient.hentKravgrunnlag(tilHentKravgrunnlagDetaljDto(detaljertKravgrunnlag, behandlingId));
                var kravgrunnlagNY = hentKravgrunnlagMapperProxy.mapTilDomene(kravgrunnlag431Dto);
                if (!kravgrunnlagFraGammelIntegrasjon.equals(kravgrunnlagNY)) {
                    LOG.info("Avvik funnet i integrasjon med OS direkte og via proxy! Sjekk secure logg for mer info.");
                    SECURE_LOG.info(
                        """
                        Avviket
                        Direkte: {}
                        Fpwsproxy: {}
                        """, kravgrunnlagFraGammelIntegrasjon, kravgrunnlagNY);
                } else {
                    LOG.info("Ingen avvik funnet i kravgrunnlag fra direkte integrasjon med OS og via proxy");
                }
            } catch (Exception e) {
                LOG.info("Noe gikk galt med sammenlignign av direkte integrasjon med OS og via proxy", e);
            }
        }
    }

    private no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto tilHentKravgrunnlagDetaljDto(HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto, Long behandlingId) {
        return new no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto.Builder()
            .kravgrunnlagId(hentKravgrunnlagDetaljDto.getKravgrunnlagId())
            .kodeAksjon(KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG)
            .enhetAnsvarlig(hentKravgrunnlagDetaljDto.getEnhetAnsvarlig())
            .saksbehId(hentKravgrunnlagDetaljDto.getSaksbehId())
            .behandlingsId(behandlingId)
            .build();
    }
}
