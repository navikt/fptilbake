package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
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
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        LOG_CONTEXT.remove("saksnummer");
        LOG_CONTEXT.remove("behandling");
        LOG_CONTEXT.remove("k9sakBehandlingUuid");
        LOG_CONTEXT.remove("behandlingUuid");

        Optional<Long> behandlingId = utledBehandlingId(attributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID));
        Optional<UUID> behandlingUuid = utledBehandlingUuId(attributter.getVerdier(AppAbacAttributtType.BEHANDLING_UUID));
        Optional<UUID> ytelesbehandlingId = utledBehandlingUuId(attributter.getVerdier(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID));
        if (behandlingId.isPresent() && ytelesbehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), ytelesbehandlingId.get());
        }
        if (behandlingUuid.isPresent() && ytelesbehandlingId.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingUuid.get(), ytelesbehandlingId.get());
        }
        if (behandlingId.isPresent() && behandlingUuid.isPresent()) {
            throw ugyldigInputFlereBehandlinger(behandlingId.get(), behandlingUuid.get());
        }

        PipBehandlingData behandlingData = null;
        if (behandlingId.isPresent()) {
            behandlingData = lagBehandlingData(behandlingId.get());
        } else if (behandlingUuid.isPresent()) {
            behandlingData = lagBehandlingData(behandlingUuid.get());
        } else if (ytelesbehandlingId.isPresent()) {
            behandlingData = hentK9sakBehandlingData(ytelesbehandlingId.get());
        }

        Set<String> aktørIder = utledAktørIder(attributter, behandlingData);

        //legger til utledede attributter til AbacAttributtSamling, slik at de kan bli logget til sporingslogg
        AbacDataAttributter utlededeAttributter = AbacDataAttributter.opprett();
        utlededeAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, aktørIder);
        ytelesbehandlingId.ifPresent(uuid -> utlededeAttributter.leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, uuid));
        behandlingUuid.ifPresent(uuid -> utlededeAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, uuid));
        attributter.leggTil(utlededeAttributter);

        String saksnummer = utledSaksnummer(attributter, behandlingData);

        return behandlingData != null
                ? lagPdpRequest(attributter, aktørIder, saksnummer, behandlingData)
                : lagPdpRequest(attributter, aktørIder, saksnummer);
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

        PipBehandlingData behandlingData = null;
        if (behandlingId.isPresent()) {
            behandlingData = lagBehandlingData(behandlingId.get());
        } else if (behandlingUuid.isPresent()) {
            behandlingData = lagBehandlingData(behandlingUuid.get());
        } else if (ytelesbehandlingId.isPresent()) {
            behandlingData = hentK9sakBehandlingData(ytelesbehandlingId.get());
        }

        Set<String> aktørIder = utledAktørIder(dataAttributter, behandlingData);

        var ressursData = AppRessursData.builder()
            .leggTilAktørIdSet(aktørIder);

        Optional.ofNullable(behandlingData).flatMap(b -> utledSaksnummer(dataAttributter, b))
            .ifPresent(s -> ressursData.leggTilRessurs(K9DataKeys.SAKSNUMMER, s));
        Optional.ofNullable(behandlingData).map(PipBehandlingData::getFagsakstatus)
            .flatMap(this::oversettFagstatus).map(no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus::getVerdi)
            .ifPresent(fss -> ressursData.leggTilRessurs(K9DataKeys.FAGSAK_STATUS, fss));
        Optional.ofNullable(behandlingData).map(PipBehandlingData::getStatusForBehandling)
            .flatMap(K9PdpRequestBuilder::oversettBehandlingStatus).map(no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus::getVerdi)
            .ifPresent(bs -> ressursData.leggTilRessurs(K9DataKeys.BEHANDLING_STATUS, bs));
        Optional.ofNullable(behandlingData).flatMap(PipBehandlingData::getAnsvarligSaksbehandler)
            .ifPresent(sbh -> ressursData.leggTilRessurs(K9DataKeys.SAKSBEHANDLER, sbh));


        return ressursData.build();
    }

    private String utledSaksnummer(AbacAttributtSamling attributter, PipBehandlingData behandlingData) {
        Set<String> saksnumre = new HashSet<>();
        saksnumre.addAll(attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER));
        if (behandlingData != null && behandlingData.getSaksnummer() != null) {
            saksnumre.add(behandlingData.getSaksnummer());
        }
        if (saksnumre.isEmpty()) {
            return null;
        }
        if (saksnumre.size() == 1) {
            var saksnummer = saksnumre.iterator().next();
            LOG_CONTEXT.add("saksnummer", saksnummer);
            return saksnummer;
        }
        throw new IllegalArgumentException("Ikke støttet å ha to saksnumre samtidig");
    }

    private Optional<String> utledSaksnummer(AbacDataAttributter attributter, PipBehandlingData behandlingData) {
        Set<String> saksnumre = new HashSet<>();
        saksnumre.addAll(attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER));
        if (behandlingData != null && behandlingData.getSaksnummer() != null) {
            saksnumre.add(behandlingData.getSaksnummer());
        }
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

    private PipBehandlingData hentK9sakBehandlingData(UUID k9sakBehandlingUuid) {
        LOG_CONTEXT.add("k9sakBehandlingUuid", k9sakBehandlingUuid);
        K9PipDto pipDto = k9sakPipKlient.hentPipdataForK9sakBehandling(k9sakBehandlingUuid);
        PipBehandlingData data = new PipBehandlingData();
        data.setFagsakstatus(pipDto.getFagsakStatus());
        data.setStatusForBehandling(pipDto.getBehandlingStatus());
        data.leggTilAktørId(pipDto.getAktørIder().stream().map(k9aktør -> new AktørId(k9aktør.getId())).collect(Collectors.toSet()));
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

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId, String saksnummer) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);
        if (saksnummer != null) {
            pdpRequest.put(K9AbacAttributter.RESOURCE_K9_SAK_SAKSNUMMER, saksnummer);
        }
        return pdpRequest;
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId, String saksnummer, PipBehandlingData behandlingData) {
        PdpRequest pdpRequest = lagPdpRequest(attributter, aktørId, saksnummer);

        oversettFagstatus(behandlingData.getFagsakstatus())
                .ifPresent(it -> pdpRequest.put(K9AbacAttributter.RESOURCE_K9_SAK_SAKSSTATUS, it.getVerdi()));
        oversettBehandlingStatus(behandlingData.getStatusForBehandling())
                .ifPresent(it -> pdpRequest.put(K9AbacAttributter.RESOURCE_K9_SAK_BEHANDLINGSSTATUS, it.getVerdi()));
        behandlingData.getAnsvarligSaksbehandler()
                .ifPresent(it -> pdpRequest.put(K9AbacAttributter.RESOURCE_K9_SAK_ANSVARLIG_SAKSBEHANDLER, it));


        return pdpRequest;
    }

    private Optional<no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus> oversettFagstatus(String kode) {
        if (FagsakStatus.OPPRETTET.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus.OPPRETTET);
        } else if (FagsakStatus.UNDER_BEHANDLING.getKode().equals(kode)) {
            return Optional.of(no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus.UNDER_BEHANDLING);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus> oversettBehandlingStatus(String kode) {
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
