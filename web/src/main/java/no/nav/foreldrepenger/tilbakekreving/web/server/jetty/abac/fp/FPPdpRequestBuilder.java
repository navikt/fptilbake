package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

/**
 * Implementasjon av PDP request for fptilbake.
 */
@ApplicationScoped
public class FPPdpRequestBuilder implements PdpRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FPPdpRequestBuilder.class);

    private PipRepository pipRepository;

    FPPdpRequestBuilder() {
        // For CDI proxy
    }

    @Inject
    public FPPdpRequestBuilder(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public  AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        var behandlingData = utledBehandlingData(dataAttributter);
        var saksnummer = utledSaksnummer(dataAttributter, behandlingData);


        var behandlingStatus = Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::statusForBehandling).orElse(PipBehandlingStatus.UTREDES);
        var ressursData = AppRessursData.builder()
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.FNR))
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(behandlingStatus);

        if (saksnummer.isPresent()) {
            ressursData.medSaksnummer(saksnummer.get());
            ressursData.medLoggSaksnummer(saksnummer.get());
        } else if (behandlingData != null && behandlingData.fpsakUuid() != null) {
            ressursData.medBehandling(behandlingData.fpsakUuid());
            ressursData.medLoggBehandling(behandlingData.fpsakUuid());
        }

        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::statusForBehandling).ifPresent(ressursData::medBehandlingStatus);
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::ansvarligSaksbehandler).ifPresent(ressursData::medAnsvarligSaksbehandler);

        return ressursData
            .medLoggSaksnummer(saksnummer.orElse(null))
            .medLoggBehandling(Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::behandlingUuid).orElse(null))
            .medLoggBehandlingId(Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::behandlingId).orElse(null))
            .build();
    }

    @Override
    public  AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        var behandlingData = utledBehandlingData(dataAttributter);
        var saksnummer = utledSaksnummer(dataAttributter, behandlingData);

        var behandlingStatus = Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::statusForBehandling).orElse(PipBehandlingStatus.UTREDES);
        var ressursData = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(behandlingStatus);
        return ressursData
            .medLoggSaksnummer(saksnummer.orElse(null))
            .medLoggBehandling(Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::behandlingUuid).orElse(null))
            .medLoggBehandlingId(Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::behandlingId).orElse(null))
            .build();
    }

    private FpPipBehandlingInfo utledBehandlingData(AbacDataAttributter dataAttributter) {
        Set<Long> behandlingId = new HashSet<>(dataAttributter.getVerdier(TilbakekrevingAbacAttributtType.BEHANDLING_ID));
        Set<UUID> behandlingUuid = new HashSet<>(dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID));
        Set<UUID> fpsakBehandlingId = new HashSet<>(dataAttributter.getVerdier(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID));
        if (behandlingId.size() + behandlingUuid.size() + fpsakBehandlingId.size() > 1) {
            Set<String> behandlinger = new HashSet<>(behandlingUuid.stream().map(UUID::toString).toList());
            behandlinger.addAll(behandlingId.stream().map(String::valueOf).toList());
            behandlinger.addAll(fpsakBehandlingId.stream().map(UUID::toString).toList());
            var tekst = String.format("Ugyldig request. Støtter bare 0 eller 1 behandling, men har %s", behandlinger);
            LOG.warn(tekst);
            throw new TekniskException("FPT-426124", tekst);
        }

        if (!behandlingId.isEmpty()) {
            return lagBehandlingData(behandlingId.stream().findFirst().orElseThrow());
        } else if (!behandlingUuid.isEmpty()) {
            return lagBehandlingData(behandlingUuid.stream().findFirst().orElseThrow());
        } else if (!fpsakBehandlingId.isEmpty()) {
            var behandling = fpsakBehandlingId.stream().findFirst().orElseThrow();
            return new FpPipBehandlingInfo(null, null, null, behandling, PipBehandlingStatus.UTREDES, null, behandling);
        } else {
            return null;
        }
    }

    private Optional<String> utledSaksnummer(AbacDataAttributter attributter, FpPipBehandlingInfo behandlingData) {
        Set<String> saksnumre = new HashSet<>(attributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER));
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::saksnummer).ifPresent(saksnumre::add);
        if (saksnumre.size() > 1) {
            var tekst = String.format("Ugyldig request. Støtter bare 0 eller 1 saker, men har %s", saksnumre);
            LOG.warn(tekst);
            throw new TekniskException("FPT-426124", tekst);
        }
        return saksnumre.stream().findFirst();
    }

    private FpPipBehandlingInfo lagBehandlingData(Long behandlingId) {
        return pipRepository.hentBehandlingData(behandlingId)
            .map(FpPipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(String.valueOf(behandlingId)));
    }

    private FpPipBehandlingInfo lagBehandlingData(UUID behandlingUuid) {
        return pipRepository.hentBehandlingData(behandlingUuid)
            .map(FpPipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(behandlingUuid.toString()));
    }


    private static TekniskException fantIkkeBehandling(String behandlingRef) {
        return new TekniskException("FPT-426125", String.format("Ugyldig input. Fant ikke behandlingId %s", behandlingRef));
    }

}
