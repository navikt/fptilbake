package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.abac.xacml.NavAttributter;
import no.nav.abac.xacml.StandardAttributter;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.pip.fpinfo.intern.FpsakPipKlient;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.AbacFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.util.MdcExtendedLogContext;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "foreldrepenger";

    private static final MdcExtendedLogContext MDC_EXTENDED_LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private PipRepository pipRepository;
    private FpsakPipKlient fpsakPipKlient;

    PdpRequestBuilderImpl() {
        // For CDI proxy
    }

    @Inject
    public PdpRequestBuilderImpl(PipRepository pipRepository, FpsakPipKlient fpsakPipKlient) {
        this.pipRepository = pipRepository;
        this.fpsakPipKlient = fpsakPipKlient;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        MDC_EXTENDED_LOG_CONTEXT.remove("behandling");

        Optional<Long> behandlingId = utledBehandlingId(attributter);
        Optional<PipBehandlingData> behandlingData = behandlingId.map(this::lagBehandlingData);

        Set<String> aktørIder = utledAktørIder(attributter, behandlingData);
        Set<String> aksjonspunkttype = pipRepository.hentAksjonspunkttypeForAksjonspunktkoder(attributter.getAksjonspunktKode());

        return behandlingData.isPresent()
            ? lagPdpRequest(attributter, aktørIder, aksjonspunkttype, behandlingData.get())
            : lagPdpRequest(attributter, aktørIder, aksjonspunkttype);
    }

    private PipBehandlingData lagBehandlingData(Long behandlingId) {
        MDC_EXTENDED_LOG_CONTEXT.add("behandling", behandlingId);
        Optional<PipBehandlingData> behandlingDataOpt = pipRepository.hentBehandlingData(behandlingId);
        if (behandlingDataOpt.isPresent()) {
            PipBehandlingData behandlingData = behandlingDataOpt.get();
            Set<AktørId> aktørIder = fpsakPipKlient.hentAktørIdForSak(behandlingData.getSaksnummer());
            behandlingData.leggTilAktørId(aktørIder);
            return behandlingData;
        } else {
            throw PdpRequestBuilderFeil.FACTORY.fantIkkeBehandling(behandlingId).toException();
        }
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId, Collection<String> aksjonspunktType) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(NavAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(StandardAttributter.ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
        pdpRequest.put(NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);
        pdpRequest.put(NavAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, aksjonspunktType);
        return pdpRequest;
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId, Collection<String> aksjonspunktType, PipBehandlingData behandlingData) {
        PdpRequest pdpRequest = lagPdpRequest(attributter, aktørId, aksjonspunktType);

        oversettFagstatus(behandlingData.getFagsakstatus())
            .ifPresent(it -> pdpRequest.put(NavAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, it.getEksternKode()));
        oversettBehandlingStatus(behandlingData.getStatusForBehandling())
            .ifPresent(it -> pdpRequest.put(NavAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, it.getEksternKode()));
        behandlingData.getAnsvarligSaksbehandler()
            .ifPresent(it -> pdpRequest.put(NavAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, it));

        return pdpRequest;
    }

    private Optional<AbacFagsakStatus> oversettFagstatus(String kode) {
        if (FagsakStatus.OPPRETTET.getKode().equals(kode)) {
            return Optional.of(AbacFagsakStatus.OPPRETTET);
        } else if (FagsakStatus.UNDER_BEHANDLING.getKode().equals(kode)) {
            return Optional.of(AbacFagsakStatus.UNDER_BEHANDLING);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<AbacBehandlingStatus> oversettBehandlingStatus(String kode) {
        if (BehandlingStatus.OPPRETTET.getKode().equals(kode)) {
            return Optional.of(AbacBehandlingStatus.OPPRETTET);
        } else if (BehandlingStatus.UTREDES.getKode().equals(kode)) {
            return Optional.of(AbacBehandlingStatus.UTREDES);
        } else if (BehandlingStatus.FATTER_VEDTAK.getKode().equals(kode)) {
            return Optional.of(AbacBehandlingStatus.FATTE_VEDTAK);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Long> utledBehandlingId(AbacAttributtSamling attributter) {
        Set<Long> behandlingIder = attributter.getBehandlingsIder();
        if (behandlingIder.isEmpty()) {
            return Optional.empty();
        } else if (behandlingIder.size() == 1) {
            return Optional.of(behandlingIder.iterator().next());
        }
        throw PdpRequestBuilderFeil.FACTORY.ugyldigInputFlereBehandlingIder(behandlingIder).toException();
    }

    private Set<String> utledAktørIder(AbacAttributtSamling attributter) {
        return attributter.getAktørIder();
    }

    private Set<String> utledAktørIder(AbacAttributtSamling attributter, Optional<PipBehandlingData> behandlingData) {
        Set<String> resultat = new HashSet<>(utledAktørIder(attributter));
        behandlingData.ifPresent(bd -> resultat.addAll(bd.getAktørIdSomStrenger()));
        return resultat;
    }

    public interface PdpRequestBuilderFeil extends DeklarerteFeil {
        PdpRequestBuilderFeil FACTORY = FeilFactory.create(PdpRequestBuilderFeil.class);

        @TekniskFeil(feilkode = "FPT-426124", feilmelding = "Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", logLevel = LogLevel.WARN)
        Feil ugyldigInputFlereBehandlingIder(Collection<Long> behandlingId);

        @TekniskFeil(feilkode = "FPT-426125", feilmelding = "Ugyldig input. Fant ikke behandlingId %s", logLevel = LogLevel.WARN)
        Feil fantIkkeBehandling(Long behandlingId);

    }

}
