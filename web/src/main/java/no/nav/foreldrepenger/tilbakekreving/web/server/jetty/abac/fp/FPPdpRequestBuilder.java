package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.CommonAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

/**
 * Implementasjon av PDP request for fptilbake.
 */
@ApplicationScoped
@Fptilbake
public class FPPdpRequestBuilder implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "foreldrepenger";

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

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
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        LOG_CONTEXT.remove("behandling");
        LOG_CONTEXT.remove("fpsakBehandlingUuid");
        LOG_CONTEXT.remove("behandlingUuid");

        Optional<Long> behandlingId = utledBehandlingId(attributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID));
        Optional<UUID> behandlingUuid = utledBehandlingUuId(attributter.getVerdier(AppAbacAttributtType.BEHANDLING_UUID));
        Optional<UUID> fpsakBehandlingId = utledBehandlingUuId(attributter.getVerdier(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID));
        if (behandlingId.isPresent() && fpsakBehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), fpsakBehandlingId.get());
        }
        if (behandlingUuid.isPresent() && fpsakBehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingUuid.get(), fpsakBehandlingId.get());
        }
        if (behandlingId.isPresent() && behandlingUuid.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), behandlingUuid.get());
        }

        PipBehandlingData behandlingData = null;
        if (behandlingId.isPresent()) {
            behandlingData = lagBehandlingData(behandlingId.get());
        } else if (behandlingUuid.isPresent()) {
            behandlingData = lagBehandlingData(behandlingUuid.get());
        } else if (fpsakBehandlingId.isPresent()) {
            behandlingData = hentFpsakBehandlingData(fpsakBehandlingId.get());
        }

        Set<String> aktørIder = utledAktørIder(attributter, behandlingData);

        //legger til utledede attributter til AbacAttributtSamling, slik at de kan bli logget til sporingslogg
        AbacDataAttributter utlededeAttributter = AbacDataAttributter.opprett();
        utlededeAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, aktørIder);
        fpsakBehandlingId.ifPresent(uuid -> utlededeAttributter.leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, uuid));
        behandlingUuid.ifPresent(uuid -> utlededeAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, uuid));
        attributter.leggTil(utlededeAttributter);

        return behandlingData != null
                ? lagPdpRequest(attributter, aktørIder, behandlingData)
                : lagPdpRequest(attributter, aktørIder);
    }

    @Override
    public String abacDomene() {
        return ABAC_DOMAIN;
    }

    @Override
    public boolean nyttAbacGrensesnitt() {
        return true;
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

        PipBehandlingData behandlingData = null;
        if (behandlingId.isPresent()) {
            behandlingData = lagBehandlingData(behandlingId.get());
        } else if (behandlingUuid.isPresent()) {
            behandlingData = lagBehandlingData(behandlingUuid.get());
        } else if (fpsakBehandlingId.isPresent()) {
            behandlingData = hentFpsakBehandlingData(fpsakBehandlingId.get());
        }

        Set<String> aktørIder = utledAktørIder(dataAttributter, behandlingData);

        var ressursData = AppRessursData.builder()
            .leggTilAktørIdSet(aktørIder);
        Optional.ofNullable(behandlingData).map(PipBehandlingData::getFagsakstatus)
                .flatMap(this::oversettAbacFagstatus).ifPresent(ressursData::medFagsakStatus);
        Optional.ofNullable(behandlingData).map(PipBehandlingData::getStatusForBehandling)
            .flatMap(FPPdpRequestBuilder::oversettAbacBehandlingStatus).ifPresent(ressursData::medBehandlingStatus);
        Optional.ofNullable(behandlingData).flatMap(PipBehandlingData::getAnsvarligSaksbehandler).ifPresent(ressursData::medAnsvarligSaksbehandler);

        return ressursData.build();
    }

    private PipBehandlingData hentFpsakBehandlingData(UUID fpsakBehandlingUuid) {
        LOG_CONTEXT.add("fpsakBehandlingUuid", fpsakBehandlingUuid);
        PipDto pipDto = fpsakPipKlient.hentPipdataForFpsakBehandling(fpsakBehandlingUuid);
        PipBehandlingData data = new PipBehandlingData();
        data.setFagsakstatus(pipDto.fagsakStatus());
        data.setStatusForBehandling(pipDto.behandlingStatus());
        data.leggTilAktørId(pipDto.aktørIder());
        return data;
    }

    private PipBehandlingData lagBehandlingData(Long behandlingId) {
        LOG_CONTEXT.add("behandling", behandlingId);
        return pipRepository.hentBehandlingData(behandlingId)
            .orElseThrow(() -> fantIkkeBehandling(behandlingId));
    }

    private PipBehandlingData lagBehandlingData(UUID behandlingUuid) {
        LOG_CONTEXT.add("behandlingUuid", behandlingUuid);
        return pipRepository.hentBehandlingData(behandlingUuid)
            .orElseThrow(() -> fantIkkeBehandling(behandlingUuid));
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);
        return pdpRequest;
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId, PipBehandlingData behandlingData) {
        PdpRequest pdpRequest = lagPdpRequest(attributter, aktørId);

        oversettAbacFagstatus(behandlingData.getFagsakstatus())
                .ifPresent(it -> pdpRequest.put(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, it.getVerdi()));
        oversettAbacBehandlingStatus(behandlingData.getStatusForBehandling())
                .ifPresent(it -> pdpRequest.put(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, it.getVerdi()));
        behandlingData.getAnsvarligSaksbehandler()
                .ifPresent(it -> pdpRequest.put(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, it));

        return pdpRequest;
    }

    private Optional<no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus> oversettAbacFagstatus(String kode) {
        if (FagsakStatus.OPPRETTET.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus.OPPRETTET);
        } else if (FagsakStatus.UNDER_BEHANDLING.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus.UNDER_BEHANDLING);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus> oversettAbacBehandlingStatus(String kode) {
        if (BehandlingStatus.OPPRETTET.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus.OPPRETTET);
        } else if (BehandlingStatus.UTREDES.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus.UTREDES);
        } else if (BehandlingStatus.FATTER_VEDTAK.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus.FATTE_VEDTAK);
        } else {
            return Optional.empty();
        }
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

    private Set<String> utledAktørIder(AbacAttributtSamling attributter, PipBehandlingData behandlingData) {
        Set<String> resultat = new HashSet<>();
        resultat.addAll(attributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        if (behandlingData != null) {
            resultat.addAll(behandlingData.getAktørIdSomStrenger());
        }
        Set<String> saksnumre = attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER);
        if (saksnumre.size() == 1) {
            resultat.addAll(fpsakPipKlient.hentAktørIderSomString(new Saksnummer(saksnumre.iterator().next())));
        }
        if (saksnumre.size() > 1) {
            throw ugyldigInputFlereSaksnumre(saksnumre);
        }
        return resultat;
    }

    private Set<String> utledAktørIder(AbacDataAttributter attributter, PipBehandlingData behandlingData) {
        Set<String> resultat = new HashSet<>();
        resultat.addAll(attributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        if (behandlingData != null) {
            resultat.addAll(behandlingData.getAktørIdSomStrenger());
        }
        Set<String> saksnumre = attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER);
        if (saksnumre.size() == 1) {
            resultat.addAll(fpsakPipKlient.hentAktørIderSomString(new Saksnummer(saksnumre.iterator().next())));
        }
        if (saksnumre.size() > 1) {
            throw ugyldigInputFlereSaksnumre(saksnumre);
        }
        return resultat;
    }


    private static TekniskException ugyldigInputFlereSaksnumre(Collection<String> saksnumre) {
        return new TekniskException("FPT-898315", String.format("Ugyldig input. Støtter bare 0 eller 1 sak, men har %s", saksnumre));
    }

    private static TekniskException ugyldigInputFlereBehandlingIder(Collection<Long> behandlingId) {
        return new TekniskException("FPT-426124", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", behandlingId));
    }

    private static TekniskException ugyldigInputFlereBehandlingUuider(Collection<UUID> behandlingUuid) {
        return new TekniskException("FPT-426125", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", behandlingUuid));
    }

    private static TekniskException ugyldigInputFlereBehandlingUuid(Collection<UUID> behandlingId) {
        return new TekniskException("FPT-651672", String.format("Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", behandlingId));
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
