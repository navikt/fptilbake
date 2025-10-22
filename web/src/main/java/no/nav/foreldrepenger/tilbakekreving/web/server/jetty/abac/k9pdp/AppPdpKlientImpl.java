package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9DataKeys;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PdpRequestBuilder;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PipBehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PipFagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.SifAbacPdpRestKlient;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.AbacBehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.AbacFagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.BehandlingUuidOperasjonDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.BeskyttetRessursActionAttributt;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.OperasjonDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.ResourceType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.SaksinformasjonDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.SaksinformasjonTilgangskontrollInputDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.resultat.IkkeTilgangÅrsak;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.resultat.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

@ApplicationScoped
public class AppPdpKlientImpl {

    private SifAbacPdpRestKlient sifAbacPdpRestKlient;
    private K9AbacAuditlogger abacAuditlogger;
    private K9PdpRequestBuilder pdpRequestBuilder;

    public AppPdpKlientImpl() {
        // CDI
    }

    AppPdpKlientImpl(K9AbacAuditlogger abacAuditlogger, K9PdpRequestBuilder pdpRequestBuilder, SifAbacPdpRestKlient sifAbacPdpRestKlient) {
        this.abacAuditlogger = abacAuditlogger;
        this.pdpRequestBuilder = pdpRequestBuilder;
        this.sifAbacPdpRestKlient = sifAbacPdpRestKlient;
    }

    @Inject
    public AppPdpKlientImpl(K9AbacAuditlogger abacAuditlogger, K9PdpRequestBuilder pdpRequestBuilder) {
        this.abacAuditlogger = abacAuditlogger;
        this.pdpRequestBuilder = pdpRequestBuilder;
        sifAbacPdpRestKlient = new SifAbacPdpRestKlient();
    }

    public K9AbacResultat forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var appRessursData = pdpRequestBuilder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());
        K9AbacResultat hovedresultat = mapResultat(forespørTilgangSifAbacPdp(beskyttetRessursAttributter, appRessursData));
        abacAuditlogger.loggUtfall(hovedresultat, beskyttetRessursAttributter, appRessursData);
        return hovedresultat;
    }

    private Tilgangsbeslutning forespørTilgangSifAbacPdp(BeskyttetRessursAttributter beskyttetRessursAttributter, K9AppRessursData appRessursData) {
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
        OperasjonDto operasjon = new OperasjonDto(resource, action);

        if (appRessursData.getResource(K9DataKeys.YTELSESBEHANDLING_UUID) != null) {
            String k9sakBehandlingUuid = appRessursData.getResource(K9DataKeys.YTELSESBEHANDLING_UUID).verdi();
            BehandlingUuidOperasjonDto dto = new BehandlingUuidOperasjonDto(UUID.fromString(k9sakBehandlingUuid), operasjon);
            return sifAbacPdpRestKlient.sjekkTilgangForInnloggetBruker(dto);
        }

        K9RessursData saksnummerResource = appRessursData.getResource(K9DataKeys.SAKSNUMMER);
        SaksnummerDto saksnummer = saksnummerResource != null ? new SaksnummerDto(saksnummerResource.verdi()) : null;

        K9RessursData fagsakStatusData = appRessursData.getResource(K9DataKeys.FAGSAK_STATUS);
        K9RessursData behandlingStatusData = appRessursData.getResource(K9DataKeys.BEHANDLING_STATUS);
        K9RessursData saksbehandlerData = appRessursData.getResource(K9DataKeys.SAKSBEHANDLER);
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
}
