package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.TilbakekrevingAbacAttributtType;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Implementasjon av PDP request for k9-tilbake.
 */
@ApplicationScoped
@Alternative
@Priority(2)
@K9tilbake
public class K9PdpRequestBuilder implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "k9";

    private PipRepository pipRepository;

    K9PdpRequestBuilder() {
        // For CDI proxy
    }

    @Inject
    public K9PdpRequestBuilder(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        System.out.println("Bruker PdpRequestBuilder for k9");

        PipBehandlingData behandlingData = null;

        Set<String> aktørIder = utledAktørIder(attributter, behandlingData);
        Set<String> aksjonspunkttype = pipRepository.hentAksjonspunkttypeForAksjonspunktkoder(attributter.getVerdier(AppAbacAttributtType.AKSJONSPUNKT_KODE));

        //legger til utledede attributter til AbacAttributtSamling, slik at de kan bli logget til sporingslogg
        AbacDataAttributter utlededeAttributter = AbacDataAttributter.opprett();
        utlededeAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, aktørIder);
//        fpsakBehandlingId.ifPresent(uuid -> utlededeAttributter.leggTil(TilbakekrevingAbacAttributtType.FPSAK_BEHANDLING_UUID, uuid));
//        behandlingUuid.ifPresent(uuid -> utlededeAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID,uuid));
        attributter.leggTil(utlededeAttributter);

        return lagPdpRequest(attributter, aktørIder, aksjonspunkttype);
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> aktørId, Collection<String> aksjonspunktType) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);
        pdpRequest.put(ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, aksjonspunktType);
        return pdpRequest;
    }

    private Set<String> utledAktørIder(AbacAttributtSamling attributter, PipBehandlingData behandlingData) {
        Set<String> resultat = new HashSet<>();
        resultat.addAll(attributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        if (behandlingData != null) {
            resultat.addAll(behandlingData.getAktørIdSomStrenger());
        }
        Set<String> saksnumre = attributter.getVerdier(AppAbacAttributtType.SAKSNUMMER);
//        if (saksnumre.size() == 1) {
//            resultat.addAll(fpsakPipKlient.hentAktørIderSomString(new Saksnummer(saksnumre.iterator().next())));
//        }
        if (saksnumre.size() > 1) {
            throw FPPdpRequestBuilder.PdpRequestBuilderFeil.FACTORY.ugyldigInputFlereSaksnumre(saksnumre).toException();
        }
        return resultat;
    }
}
