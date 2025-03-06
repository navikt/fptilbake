package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
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
 * Implementasjon av PDP request for fptilbake.
 */
@ApplicationScoped
public class FPPdpRequestBuilder implements PdpRequestBuilder {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private PipRepository pipRepository;
    private FpsakPipKlient fpsakPipKlient;

    FPPdpRequestBuilder() {
        // For CDI proxy
    }

    @Inject
    public FPPdpRequestBuilder(PipRepository pipRepository, FpsakPipKlient fpsakPipKlient) {
        this.pipRepository = pipRepository;
        this.fpsakPipKlient = fpsakPipKlient;
    }

    @Override
    public  AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        LOG_CONTEXT.remove("behandling");
        LOG_CONTEXT.remove("fpsakBehandlingUuid");
        LOG_CONTEXT.remove("behandlingUuid");

        Optional<Long> behandlingId = utledBehandlingId(dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID));
        Optional<UUID> behandlingUuid = utledBehandlingUuId(dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_UUID));
        Optional<UUID> fpsakBehandlingId = utledBehandlingUuId(dataAttributter.getVerdier(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID));
        if (behandlingId.isPresent() && fpsakBehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), fpsakBehandlingId.get());
        }
        if (behandlingUuid.isPresent() && fpsakBehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingUuid.get(), fpsakBehandlingId.get());
        }
        if (behandlingId.isPresent() && behandlingUuid.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), behandlingUuid.get());
        }

        FpPipBehandlingInfo behandlingData = null;
        String auditAktørId = null;
        if (behandlingId.isPresent()) {
            behandlingData = lagBehandlingData(behandlingId.get());
            auditAktørId = behandlingData.getAktørIdNonNull().stream().findFirst().map(PipAktørId::toString).orElse(null);
        } else if (behandlingUuid.isPresent()) {
            behandlingData = lagBehandlingData(behandlingUuid.get());
            auditAktørId = behandlingData.getAktørIdNonNull().stream().findFirst().map(PipAktørId::toString).orElse(null);
        } else if (fpsakBehandlingId.isPresent()) {
            behandlingData = hentFpsakBehandlingData(fpsakBehandlingId.get());
        }

        var ressursData = AppRessursData.builder()
            .medAuditAktørId(auditAktørId)
            .leggTilAktørIdSet(dataAttributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        Optional.ofNullable(behandlingData).ifPresent(bi -> ressursData.leggTilAbacAktørIdSet(bi.getAktørIdNonNull()));
        var saksnumre = utledSaksnummer(dataAttributter, behandlingData);
        saksnumre.forEach(s -> ressursData.leggTilAktørIdSet(fpsakPipKlient.hentAktørIderSomString(new Saksnummer(s))));
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::fagsakstatus).ifPresent(ressursData::medFagsakStatus);
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::statusForBehandling).ifPresent(ressursData::medBehandlingStatus);
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::ansvarligSaksbehandler).ifPresent(ressursData::medAnsvarligSaksbehandler);

        return ressursData.build();
    }

    private Set<String> utledSaksnummer(AbacDataAttributter attributter, FpPipBehandlingInfo behandlingData) {
        Set<String> saksnumre = new HashSet<>(attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER));
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::saksnummer).ifPresent(saksnumre::add);
        saksnumre.stream().findFirst().ifPresent(s -> LOG_CONTEXT.add("fagsak", s));
        return saksnumre;
    }

    private FpPipBehandlingInfo hentFpsakBehandlingData(UUID fpsakBehandlingUuid) {
        LOG_CONTEXT.add("fpsakBehandlingUuid", fpsakBehandlingUuid);
        var identer = fpsakPipKlient.hentAktørIdForBehandling(fpsakBehandlingUuid);
        if (identer.isEmpty()) {
            return null;
        }
        return new FpPipBehandlingInfo(identer, null, PipFagsakStatus.UNDER_BEHANDLING, PipBehandlingStatus.UTREDES, null);
    }

    private FpPipBehandlingInfo lagBehandlingData(Long behandlingId) {
        LOG_CONTEXT.add("behandling", behandlingId);
        return pipRepository.hentBehandlingData(behandlingId)
            .map(FpPipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(behandlingId));
    }

    private FpPipBehandlingInfo lagBehandlingData(UUID behandlingUuid) {
        LOG_CONTEXT.add("behandlingUuid", behandlingUuid);
        return pipRepository.hentBehandlingData(behandlingUuid)
            .map(FpPipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(behandlingUuid));
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
