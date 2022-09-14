package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.PipBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

/**
 * Implementasjon av PDP request for k9-tilbake.
 */
@ApplicationScoped
@K9tilbake
public class K9PdpRequestBuilder implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "k9";
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private PipRepository pipRepository;
    private K9sakPipKlient k9sakPipKlient;

    K9PdpRequestBuilder() {
        // For CDI proxy
    }

    @Inject
    public K9PdpRequestBuilder(PipRepository pipRepository, K9sakPipKlient k9sakPipKlient) {
        this.pipRepository = pipRepository;
        this.k9sakPipKlient = k9sakPipKlient;
    }

    @Override
    public String abacDomene() {
        return ABAC_DOMAIN;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        LOG_CONTEXT.remove("saksnummer");
        LOG_CONTEXT.remove("behandling");
        LOG_CONTEXT.remove("k9sakBehandlingUuid");
        LOG_CONTEXT.remove("behandlingUuid");

        Optional<Long> behandlingId = utledBehandlingId(dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID));
        Optional<UUID> behandlingUuid = utledBehandlingUuId(dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_UUID));
        Optional<UUID> ytelesbehandlingId = utledBehandlingUuId(dataAttributter.getVerdier(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID));
        if (behandlingId.isPresent() && ytelesbehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), ytelesbehandlingId.get());
        }
        if (behandlingUuid.isPresent() && ytelesbehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingUuid.get(), ytelesbehandlingId.get());
        }
        if (behandlingId.isPresent() && behandlingUuid.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), behandlingUuid.get());
        }

        PipBehandlingInfo behandlingData = null;
        if (behandlingId.isPresent()) {
            behandlingData = lagBehandlingData(behandlingId.get());
        } else if (behandlingUuid.isPresent()) {
            behandlingData = lagBehandlingData(behandlingUuid.get());
        } else if (ytelesbehandlingId.isPresent()) {
            behandlingData = hentK9sakBehandlingData(ytelesbehandlingId.get());
        }

        Set<AksjonspunktType> aksjonspunkttype = pipRepository.hentAksjonspunktTypeForAksjonspunktKoder(dataAttributter.getVerdier(AppAbacAttributtType.AKSJONSPUNKT_KODE));

        var ressursData = AppRessursData.builder()
            .leggTilAktørIdSet(dataAttributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        Optional.ofNullable(behandlingData).ifPresent(bi -> ressursData.leggTilAbacAktørIdSet(bi.getAktørIdNonNull()));
        utledSaksnummer(dataAttributter, behandlingData).ifPresent(s -> ressursData.leggTilRessurs(K9DataKeys.SAKSNUMMER, s));
        
        Optional.ofNullable(behandlingData).map(PipBehandlingInfo::fagsakstatus)
            .ifPresent(fss -> ressursData.leggTilRessurs(K9DataKeys.FAGSAK_STATUS, fss));
        Optional.ofNullable(behandlingData).map(PipBehandlingInfo::statusForBehandling)
            .ifPresent(bs -> ressursData.leggTilRessurs(K9DataKeys.BEHANDLING_STATUS, bs));
        Optional.ofNullable(behandlingData).map(PipBehandlingInfo::ansvarligSaksbehandler)
            .ifPresent(sbh -> ressursData.leggTilRessurs(K9DataKeys.SAKSBEHANDLER, sbh));
        aksjonspunkttype.stream().map(a->oversettAksjonspunktType(a.getKode()))
            .forEach(at -> ressursData.leggTilRessurs(K9DataKeys.AKSJONSPUNKT_TYPE, at));

        return ressursData.build();
    }


    private Optional<String> utledSaksnummer(AbacDataAttributter attributter, PipBehandlingInfo behandlingData) {
        Set<String> saksnumre = new HashSet<>(attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER));
        Optional.ofNullable(behandlingData).map(PipBehandlingInfo::saksnummer).ifPresent(saksnumre::add);
        if (saksnumre.isEmpty()) {
            return Optional.empty();
        }
        if (saksnumre.size() == 1) {
            var saksnummer = saksnumre.iterator().next();
            LOG_CONTEXT.add("saksnummer", saksnummer);
            return Optional.of(saksnummer);
        }
        throw new IllegalArgumentException("Ikke støttet å ha to saksnumre samtidig");
    }

    private PipBehandlingInfo hentK9sakBehandlingData(UUID k9sakBehandlingUuid) {
        LOG_CONTEXT.add("k9sakBehandlingUuid", k9sakBehandlingUuid);
        K9PipDto pipDto = k9sakPipKlient.hentPipdataForK9sakBehandling(k9sakBehandlingUuid);
        var aktører = pipDto.getAktørIder().stream().map(K9AktørId::getId).map(PipAktørId::new).collect(Collectors.toSet());
        var fagsakStatus = Optional.ofNullable(pipDto.getFagsakStatus()).map(K9PdpRequestBuilder::oversettFagstatus).orElse(null);
        var behandlingStatus = Optional.ofNullable(pipDto.getBehandlingStatus()).map(K9PdpRequestBuilder::oversettBehandlingStatus).orElse(null);
        return new PipBehandlingInfo(aktører, null, fagsakStatus, behandlingStatus, null);
    }

    private PipBehandlingInfo lagBehandlingData(Long behandlingId) {
        LOG_CONTEXT.add("behandling", behandlingId);
        return pipRepository.hentBehandlingData(behandlingId)
            .map(PipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(behandlingId));
    }

    private PipBehandlingInfo lagBehandlingData(UUID behandlingUuid) {
        LOG_CONTEXT.add("behandlingUuid", behandlingUuid);
        return pipRepository.hentBehandlingData(behandlingUuid)
            .map(PipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(behandlingUuid));
    }

    private static PipFagsakStatus oversettFagstatus(String kode) {
        return Arrays.stream(PipFagsakStatus.values()).filter(fss -> kode.equals(fss.getVerdi())).findFirst().orElse(null);
    }

    private static PipAksjonspunktType oversettAksjonspunktType(String kode) {
        return Arrays.stream(PipAksjonspunktType.values()).filter(fss -> kode.equals(fss.getVerdi())).findFirst().orElse(null);
    }

    private static PipBehandlingStatus oversettBehandlingStatus(String kode) {
        return Arrays.stream(PipBehandlingStatus.values()).filter(bhs -> kode.equals(bhs.getVerdi())).findFirst().orElse(null);
    }

    private Optional<Long> utledBehandlingId(Set<Long> behandlingIder) {
        if (behandlingIder.isEmpty()) {
            return Optional.empty();
        } else if (behandlingIder.size() == 1) {
            return Optional.of(behandlingIder.iterator().next());
        }
        throw ugyldigInputFlereBehandlingIder(behandlingIder);
    }

    private Optional<UUID> utledBehandlingUuId(Set<UUID> behandlingUuider) {
        if (behandlingUuider.isEmpty()) {
            return Optional.empty();
        } else if (behandlingUuider.size() == 1) {
            return Optional.of(behandlingUuider.iterator().next());
        }
        throw ugyldigInputFlereBehandlingUuider(behandlingUuider);
    }

    private static TekniskException ugyldigInputFlereBehandlingIder(Collection<Long> behandlingId) {
        return new TekniskException("FPT-426124", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", behandlingId));
    }

    private static TekniskException ugyldigInputFlereBehandlingUuider(Collection<UUID> behandlingUuid) {
        return new TekniskException("FPT-426125", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", behandlingUuid));
    }

    private static TekniskException ugyldigInputFlereBehandlinger(Long behandlingId, UUID fpsakUuid) {
        return new TekniskException("FPT-317633", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s og %s", behandlingId, fpsakUuid));
    }

    private static TekniskException ugyldigInputFlereBehandlinger(UUID behandlingUuid, UUID fpsakUuid) {
        return new TekniskException("FPT-317634", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s og %s", behandlingUuid, fpsakUuid));
    }

    private static TekniskException fantIkkeBehandling(Long behandlingId) {
        return new TekniskException("FPT-426125", String.format("Ugyldig input. Fant ikke behandlingId %s", behandlingId));
    }

    private static TekniskException fantIkkeBehandling(UUID behandlingUuid) {
        return new TekniskException("FPT-426126", String.format("Ugyldig input. Fant ikke behandlingId %s", behandlingUuid));
    }
}
