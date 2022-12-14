package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.request.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;

@ApplicationScoped
public class KravgrunnlagHenter {

    private static final Logger LOG = LoggerFactory.getLogger(KravgrunnlagHenter.class);
    private static final Logger SECURE_LOG = LoggerFactory.getLogger("secureLogger");

    private HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy;
    private ØkonomiProxyKlient økonomiProxyKlient;

    private ØkonomiConsumer økonomiConsumer;
    private HentKravgrunnlagMapper hentKravgrunnlagMapper;

    public KravgrunnlagHenter() {
        // for CDI
    }

    @Inject
    public KravgrunnlagHenter(ØkonomiProxyKlient økonomiProxyKlient,
                              HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy,
                              ØkonomiConsumer økonomiConsumer,
                              HentKravgrunnlagMapper hentKravgrunnlagMapper) {
        this.økonomiProxyKlient = økonomiProxyKlient;
        this.hentKravgrunnlagMapperProxy = hentKravgrunnlagMapperProxy;
        this.økonomiConsumer = økonomiConsumer;
        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
    }



    public Kravgrunnlag431 hentKravgrunnlagMedFailsafeSammenligningMotProxy(Long behandlingId, HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto) {
        try {
            var kravgrunnlag = hentKravgrunnlagDirekteIntegrasjon(behandlingId, hentKravgrunnlagDetaljDto);
            sammenlignKravgrunnlagFailSafe(behandlingId, hentKravgrunnlagDetaljDto, kravgrunnlag);
            return kravgrunnlag;

        } catch (Exception e) {
            sammenlignException(behandlingId, hentKravgrunnlagDetaljDto, e);
            throw e;
        }
    }

    private Kravgrunnlag431 hentKravgrunnlagDirekteIntegrasjon(Long behandlingId, HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto) {
        var respons = økonomiConsumer.hentKravgrunnlag(behandlingId, hentKravgrunnlagDetaljDto);
        return hentKravgrunnlagMapper.mapTilDomene(respons);
    }

    private void sammenlignKravgrunnlagFailSafe(Long behandlingId, HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto, Kravgrunnlag431 kravgrunnlagFraDirekteIntegrasjon) {
        try {
            var kravgrunnlagFraFpWsProxy = hentKravgrunnlagFraFpwsproxy(behandlingId, hentKravgrunnlagDetaljDto);

            if (!kravgrunnlagFraDirekteIntegrasjon.equals(kravgrunnlagFraFpWsProxy)) {
                LOG.info("Avvik funnet i integrasjon med OS direkte og via proxy! Sjekk secure logg for mer info.");
                SECURE_LOG.info(
                    """
                    Avviket
                    Direkte: {}
                    Fpwsproxy: {}
                    """, kravgrunnlagFraDirekteIntegrasjon, kravgrunnlagFraFpWsProxy);
            } else {
                LOG.info("Ingen avvik funnet i kravgrunnlag fra direkte integrasjon med OS og via proxy");
            }
        } catch (Exception e) {
            LOG.info("Avvik! Noe gikk galt med sammenlignign av direkte integrasjon med OS og via proxy", e);
        }
    }

    private Kravgrunnlag431 hentKravgrunnlagFraFpwsproxy(Long behandlingId, HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto) {
        var request = tilHentKravgrunnlagDetaljDto(hentKravgrunnlagDetaljDto, behandlingId);
        var respons = økonomiProxyKlient.hentKravgrunnlag(request);
        return hentKravgrunnlagMapperProxy.mapTilDomene(respons);
    }

    private void sammenlignException(Long behandlingId, HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto, Exception opprinneligException) {
        try {
            var kravgrunnlag431 = hentKravgrunnlagFraFpwsproxy(behandlingId, hentKravgrunnlagDetaljDto);
            if (kravgrunnlag431 != null) {
                LOG.info("Avvik! Skulle returnert exception men returnerte et gyldig kravgrunnlag!");
            }
        } catch (Exception exceptionFraFpwsproxy) {
            if (!opprinneligException.getClass().equals(exceptionFraFpwsproxy.getClass())) {
                LOG.info("Avvik! Exception fra fpwsproxy er forskjellig enn det mottatt med direkteintegrasjon {} vs {}", opprinneligException.getClass(), exceptionFraFpwsproxy.getClass());
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
