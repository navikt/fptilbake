package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9DataKeys;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PdpRequestBuilder;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PipBehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PipFagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.Advice;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.Decision;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlResponse;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlResponseMapper;
import no.nav.sif.abac.kontrakt.abac.AbacBehandlingStatus;
import no.nav.sif.abac.kontrakt.abac.AbacFagsakStatus;
import no.nav.sif.abac.kontrakt.abac.BeskyttetRessursActionAttributt;
import no.nav.sif.abac.kontrakt.abac.ResourceType;
import no.nav.sif.abac.kontrakt.abac.dto.OperasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonTilgangskontrollInputDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksnummerDto;
import no.nav.sif.abac.kontrakt.abac.resultat.IkkeTilgangÅrsak;
import no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

@ApplicationScoped
public class AppPdpKlientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(AppPdpKlientImpl.class);

    private static final String DOMENE = "k9";

    private AppPdpConsumerImpl pdp;
    private SifAbacPdpRestKlient sifAbacPdpRestKlient = new SifAbacPdpRestKlient();
    private TokenProvider tokenProvider;
    private K9AbacAuditlogger abacAuditlogger;
    private K9PdpRequestBuilder pdpRequestBuilder;
    private String konfigurasjon;

    public AppPdpKlientImpl() {
        // CDI
    }

    @Inject
    public AppPdpKlientImpl(AppPdpConsumerImpl pdp, TokenProvider tokenProvider, K9AbacAuditlogger abacAuditlogger,
                            K9PdpRequestBuilder pdpRequestBuilder,
                            @KonfigVerdi(value = "VALGT_PDP_K9", defaultVerdi = "abac-k9") String konfigurasjon) {
        this.pdp = pdp;
        this.tokenProvider = tokenProvider;
        this.abacAuditlogger = abacAuditlogger;
        this.pdpRequestBuilder = pdpRequestBuilder;
        this.konfigurasjon = konfigurasjon;
    }

    public K9AbacResultat forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var appRessursData = pdpRequestBuilder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());
        K9AbacResultat hovedresultat = switch (konfigurasjon) {
            case "abac-k9":
                yield forespørTilgangAbacK9(beskyttetRessursAttributter, appRessursData);
            case "sif-abac-pdp":
                yield mapResultat(forespørTilgangSifAbacPdp(beskyttetRessursAttributter, appRessursData));
            case "begge": {
                K9AbacResultat resultatGammel = forespørTilgangAbacK9(beskyttetRessursAttributter, appRessursData);
                try {
                    Tilgangsbeslutning resultatNy = forespørTilgangSifAbacPdp(beskyttetRessursAttributter, appRessursData);
                    K9AbacResultat resultatNyMapped = mapResultat(resultatNy);
                    if (resultatNyMapped != resultatGammel) {
                        LOG.warn("Ulikt resultat fra ny/gammel abac. Ny årsaker {} mapped til {} gammel {}",
                            resultatNy.årsakerForIkkeTilgang(),
                            resultatNyMapped,
                            resultatGammel);
                    }
                } catch (Exception e) {
                    LOG.warn("Ny tilgangskontroll feilet, bruker resultat fra gammel", e);
                }
                yield resultatGammel;
            }
            default:
                throw new IllegalArgumentException("Ikke-støttet konfigurasjonsverdi: " + konfigurasjon);
        };

        abacAuditlogger.loggUtfall(hovedresultat, beskyttetRessursAttributter, appRessursData);
        return hovedresultat;
    }

    private K9AbacResultat forespørTilgangAbacK9(BeskyttetRessursAttributter beskyttetRessursAttributter, K9AppRessursData appRessursData) {
        var token = Token.withOidcToken(tokenProvider.openIdToken());
        var request = XacmlRequestMapper.lagXacmlRequest(beskyttetRessursAttributter, DOMENE, appRessursData, token);
        var response = pdp.evaluate(request);
        var hovedresultat = resultatFraResponse(response);
        return hovedresultat;
    }

    private Tilgangsbeslutning forespørTilgangSifAbacPdp(BeskyttetRessursAttributter beskyttetRessursAttributter, K9AppRessursData appRessursData) {
        SaksnummerDto saksnummer = new SaksnummerDto(appRessursData.getResource(K9DataKeys.SAKSNUMMER).verdi());
        ResourceType resource = switch (beskyttetRessursAttributter.getResourceType()) {
            case APPLIKASJON -> ResourceType.APPLIKASJON;
            case DRIFT -> ResourceType.DRIFT;
            case FAGSAK -> ResourceType.FAGSAK;
            case VENTEFRIST -> ResourceType.VENTEFRIST;
            default -> throw new IllegalArgumentException("Ikke-støttet resource type for k9: " + beskyttetRessursAttributter.getResourceType());
        };
        BeskyttetRessursActionAttributt action = switch (beskyttetRessursAttributter.getActionType()) {
            case READ -> BeskyttetRessursActionAttributt.READ;
            case UPDATE -> BeskyttetRessursActionAttributt.UPDATE;
            case CREATE -> BeskyttetRessursActionAttributt.CREATE;
            default -> throw new IllegalArgumentException("Ikke-støttet action type for k9: " + beskyttetRessursAttributter.getActionType());
        };
        K9RessursData fagsakStatusData = appRessursData.getResource(K9DataKeys.FAGSAK_STATUS);
        K9RessursData behandlingStatusData = appRessursData.getResource(K9DataKeys.BEHANDLING_STATUS);
        K9RessursData saksbehandlerData = appRessursData.getResource(K9DataKeys.SAKSBEHANDLER);
        OperasjonDto operasjon = new OperasjonDto(resource, action);
        SaksinformasjonDto saksinformasjonDto = new SaksinformasjonDto(
            saksbehandlerData != null ? saksbehandlerData.verdi() : null,
            behandlingStatusData != null ? mapBehandlingStatus(behandlingStatusData.verdi()) : null,
            fagsakStatusData != null ? mapFagsakStatus(fagsakStatusData.verdi()) : null,
            Set.of());
        SaksinformasjonTilgangskontrollInputDto inputDto = new SaksinformasjonTilgangskontrollInputDto(saksnummer, operasjon, saksinformasjonDto);
        return sifAbacPdpRestKlient.sjekkTilgangForInnloggetBruker(inputDto);
    }

    private K9AbacResultat mapResultat(Tilgangsbeslutning tilgangsbeslutning) {
        if (tilgangsbeslutning.harTilgang()) {
            return K9AbacResultat.GODKJENT;
        } else if (tilgangsbeslutning.årsakerForIkkeTilgang().contains(IkkeTilgangÅrsak.HAR_IKKE_TILGANG_TIL_KODE6_PERSON)) {
            return K9AbacResultat.AVSLÅTT_KODE_6;
        } else if (tilgangsbeslutning.årsakerForIkkeTilgang().contains(IkkeTilgangÅrsak.HAR_IKKE_TILGANG_TIL_KODE7_PERSON)) {
            return K9AbacResultat.AVSLÅTT_KODE_7;
        } else if (tilgangsbeslutning.årsakerForIkkeTilgang().contains(IkkeTilgangÅrsak.HAR_IKKE_TILGANG_TIL_EGEN_ANSATT)) {
            return K9AbacResultat.AVSLÅTT_EGEN_ANSATT;
        } else {
            return K9AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
        }

    }

    private AbacBehandlingStatus mapBehandlingStatus(String verdi) {
        if (verdi == null) {
            return null;
        }
        if (verdi.equals(K9PipBehandlingStatus.UTREDES.getVerdi())) {
            return AbacBehandlingStatus.UTREDES;
        }
        if (verdi.equals(K9PipBehandlingStatus.FATTE_VEDTAK.getVerdi())) {
            return AbacBehandlingStatus.FATTE_VEDTAK;
        }
        if (verdi.equals(K9PipBehandlingStatus.OPPRETTET.getVerdi())) {
            return AbacBehandlingStatus.OPPRETTET;
        }
        throw new IllegalArgumentException("Ikke-støttet behandlingstatus: " + verdi);
    }

    private AbacFagsakStatus mapFagsakStatus(String verdi) {
        if (verdi == null) {
            return null;
        }
        if (verdi.equals(K9PipFagsakStatus.UNDER_BEHANDLING.getVerdi())) {
            return AbacFagsakStatus.UNDER_BEHANDLING;
        }
        if (verdi.equals(K9PipFagsakStatus.OPPRETTET.getVerdi())) {
            return AbacFagsakStatus.OPPRETTET;
        }
        throw new IllegalArgumentException("Ikke-støttet fagsakstatus: " + verdi);
    }


    private static K9AbacResultat resultatFraResponse(XacmlResponse response) {
        var decisions = XacmlResponseMapper.getDecisions(response);

        for (var decision : decisions) {
            if (decision == Decision.Indeterminate) {
                throw new TekniskException("F-080281",
                    String.format("Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", decision, response));
            }
        }

        var biasedDecision = createAggregatedDecision(decisions);
        handlObligation(response);

        if (biasedDecision == Decision.Permit) {
            return K9AbacResultat.GODKJENT;
        }

        var denyAdvice = XacmlResponseMapper.getAdvice(response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: {}", LoggerUtils.toStringWithoutLineBreaks(denyAdvice));
        }
        if (denyAdvice.contains(Advice.DENY_KODE_6)) {
            return K9AbacResultat.AVSLÅTT_KODE_6;
        }
        if (denyAdvice.contains(Advice.DENY_KODE_7)) {
            return K9AbacResultat.AVSLÅTT_KODE_7;
        }
        if (denyAdvice.contains(Advice.DENY_EGEN_ANSATT)) {
            return K9AbacResultat.AVSLÅTT_EGEN_ANSATT;
        }
        return K9AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static Decision createAggregatedDecision(List<Decision> decisions) {
        for (var decision : decisions) {
            if (decision != Decision.Permit) {
                return Decision.Deny;
            }
        }
        return Decision.Permit;
    }

    private static void handlObligation(XacmlResponse response) {
        var obligations = XacmlResponseMapper.getObligations(response);
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
        }
    }
}
