package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.AnnullerKravGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.sokos.HentKravgrunnlagMapperSokos;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerRequest;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerResponse;
import no.nav.foreldrepenger.tilbakekreving.sokos.KravgrunnlagAnnullerRequest;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosTilbakekrevingKlient;
import no.nav.vedtak.exception.IntegrasjonException;

@ApplicationScoped
public class KravgrunnlagHenter {

    public static final String ANSVARLIG_ENHET_NØS = "8020";
    public static final String OKO_SAKSBEH_ID = "K231B433";

    private static final Environment ENV = Environment.current();
    private static final Fagsystem HVILKEN_TILBAKE = ApplicationName.hvilkenTilbake();
    private static final Logger LOG = LoggerFactory.getLogger(KravgrunnlagHenter.class);

    private HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy;
    private ØkonomiProxyKlient økonomiProxyKlient;
    private HentKravgrunnlagMapperSokos hentKravgrunnlagMapperSokos;
    private SokosTilbakekrevingKlient sokosTilbakekrevingKlient;
    private BooleanSupplier erDev;

    public KravgrunnlagHenter() {
        // for CDI
    }

    @Inject
    public KravgrunnlagHenter(ØkonomiProxyKlient økonomiProxyKlient, HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy,
                             SokosTilbakekrevingKlient sokosTilbakekrevingKlient, HentKravgrunnlagMapperSokos hentKravgrunnlagMapperSokos) {
        this(økonomiProxyKlient, hentKravgrunnlagMapperProxy, sokosTilbakekrevingKlient, hentKravgrunnlagMapperSokos, () -> (ENV.isDev() && Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) || ENV.isLocal());
    }

    public KravgrunnlagHenter(ØkonomiProxyKlient økonomiProxyKlient, HentKravgrunnlagMapperProxy hentKravgrunnlagMapperProxy,
                       SokosTilbakekrevingKlient sokosTilbakekrevingKlient, HentKravgrunnlagMapperSokos hentKravgrunnlagMapperSokos,
                       BooleanSupplier erDev) {
        this.økonomiProxyKlient = økonomiProxyKlient;
        this.hentKravgrunnlagMapperProxy = hentKravgrunnlagMapperProxy;
        this.sokosTilbakekrevingKlient = sokosTilbakekrevingKlient;
        this.hentKravgrunnlagMapperSokos = hentKravgrunnlagMapperSokos;
        this.erDev = erDev;
    }

    public void annullerKravgrunnlag(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        Objects.requireNonNull(kravgrunnlag,
            "Finnes ikke aktivt kravgrunnlag for behandling " + behandlingId);
        if (erDev.getAsBoolean()) {
            var vedtakId = Math.toIntExact(Objects.requireNonNull(kravgrunnlag.getVedtakId(), "vedtakId"));
            if (vedtakId > 999_999_999) {
                throw new IllegalArgumentException("vedtakId er større enn tillatt i Sokos-kontrakten: " + vedtakId);
            }
            sokosTilbakekrevingKlient.annullerKravgrunnlag(new KravgrunnlagAnnullerRequest(
                KodeAksjon.ANNULERE_GRUNNLAG, vedtakId, ANSVARLIG_ENHET_NØS, OKO_SAKSBEH_ID));
        } else {
            økonomiProxyKlient.anullerKravgrunnlag(new AnnullerKravGrunnlagDto(BigInteger.valueOf(kravgrunnlag.getVedtakId())));
        }
    }

    public Kravgrunnlag431 hentKravgrunnlagFraOS(Long behandlingId, HentKravgrunnlagRequest hentKravgrunnlagRequest) {
        LOG.info("Henter kravgrunnlag for behandling{} med kravgrunnlagId {}",
            behandlingId != null ? " " + behandlingId : "",
            hentKravgrunnlagRequest.kravgrunnlagId());
        var kravgrunnlag431 = erDev.getAsBoolean() ? hentFraSokos(hentKravgrunnlagRequest) : hentFraProxy(hentKravgrunnlagRequest);
        if (!erDev.getAsBoolean() && ENV.isProd() && Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            sammenlignMedSokos(hentKravgrunnlagRequest, kravgrunnlag431);
        }
        LOG.info("Kravgrunnlag hentet OK");
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 hentFraProxy(HentKravgrunnlagRequest request) {
        var dto = new HentKravgrunnlagDetaljDto.Builder()
            .kodeAksjon(mapAksjon(request.kodeAksjon()))
            .kravgrunnlagId(request.kravgrunnlagId())
            .saksbehId(request.saksbehId())
            .enhetAnsvarlig(request.enhetAnsvarlig())
            .build();
        return hentKravgrunnlagMapperProxy.mapTilDomene(økonomiProxyKlient.hentKravgrunnlag(dto));
    }

    private no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon mapAksjon(KodeAksjon kodeAksjon) {
        return switch (kodeAksjon) {
            case FINN_GRUNNLAG_OMGJØRING -> no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon.FINN_GRUNNLAG_OMGJØRING;
            case HENT_KORRIGERT_KRAVGRUNNLAG -> no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG;
            case HENT_GRUNNLAG_OMGJØRING -> no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon.HENT_GRUNNLAG_OMGJØRING;
            case FATTE_VEDTAK -> no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon.FATTE_VEDTAK;
            case ANNULERE_GRUNNLAG -> no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon.ANNULERE_GRUNNLAG;

        };
    }

    private Kravgrunnlag431 hentFraSokos(HentKravgrunnlagRequest request) {
        var kravgrunnlagId = request.kravgrunnlagId().intValueExact();
        if (kravgrunnlagId > 999_999_999) {
            throw new IllegalArgumentException("kravgrunnlagId er større enn tillatt i Sokos-kontrakten: " + kravgrunnlagId);
        }
        var sokosRequest = new HentKravgrunnlagDetaljerRequest(request.kodeAksjon(), kravgrunnlagId,
            request.enhetAnsvarlig(), request.saksbehId());
        try {
            var respons = sokosTilbakekrevingKlient.hentKravgrunnlag(sokosRequest);
            var validerteDetaljer = validerDetaljer(respons);
            return hentKravgrunnlagMapperSokos.mapTilDomene(validerteDetaljer);
        } catch (SokosKravgrunnlagException e) {
            validerFeilkoder(e.melding());
            throw e;
        }
    }

    private void sammenlignMedSokos(HentKravgrunnlagRequest request, Kravgrunnlag431 original) {
        try {
            var sokos = hentFraSokos(request);
            if (Objects.equals(original, sokos)) {
                LOG.info("Kravgrunnlag fra proxy og Sokos er identiske for kravgrunnlagId {}", request.kravgrunnlagId());
            } else {
                LOG.info("Kravgrunnlag fra proxy og Sokos er IKKE identiske for kravgrunnlagId proxy {} sokos {}.", original, sokos);
            }
        } catch (Exception e) {
            LOG.info("Kravgrunnlag fra proxy og Sokos kunne ikke hente fra Sokos for kravgrunnlagId {}.", request.kravgrunnlagId(), e);
        }
    }

    static HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer validerDetaljer(HentKravgrunnlagDetaljerResponse response) {
        if (response == null) {
            throw new IntegrasjonException("F-468817", "Tom respons fra Sokos ved henting av kravgrunnlag.");
        }
        validerFeilkoder(response.melding());
        if (response.status() == null || response.status() != 0 && response.status() != 4 || response.kravgrunnlag() == null) {
            throw new IntegrasjonException("F-468817", "Ugyldig respons fra Sokos ved henting av kravgrunnlag, status " + response.status() + ".");
        }
        return response.kravgrunnlag();
    }

    static void validerFeilkoder(String melding) {
        if (melding != null && melding.contains("B420010I")) {
            throw new ManglendeKravgrunnlagException("FPT-539080", "Fikk feil fra OS ved henting av kravgrunnlag. Kravgrunnlaget finnes ikke.");
        }
        if (melding != null && melding.contains("B420012I")) {
            throw new SperringKravgrunnlagException("FPT-539081", "Fikk feil fra OS ved henting av kravgrunnlag. Kravgrunnlaget er sperret.");
        }
    }
}
