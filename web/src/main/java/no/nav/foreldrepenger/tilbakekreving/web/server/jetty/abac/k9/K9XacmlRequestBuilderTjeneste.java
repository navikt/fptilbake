package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

@ApplicationScoped
@K9tilbake
public class K9XacmlRequestBuilderTjeneste implements XacmlRequestBuilderTjeneste {

    public K9XacmlRequestBuilderTjeneste() {
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(K9AbacAttributter.XACML_1_0_ACTION_ACTION_ID, pdpRequest.getString(K9AbacAttributter.XACML_1_0_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        List<IdentKey> identer = hentIdenter(pdpRequest, K9AbacAttributter.RESOURCE_FELLES_PERSON_FNR, K9AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE);

        if (identer.isEmpty()) {
            populerResources(xacmlBuilder, pdpRequest, null);
        } else {
            for (var ident : identer) {
                populerResources(xacmlBuilder, pdpRequest, ident);
            }
        }

        return xacmlBuilder;
    }

    private void populerResources(XacmlRequestBuilder xacmlBuilder, PdpRequest pdpRequest, IdentKey ident) {
        List<String> aksjonspunktTyper = pdpRequest.getListOfString(K9AbacAttributter.RESOURCE_K9_SAK_AKSJONSPUNKT_TYPE);
        if (aksjonspunktTyper.isEmpty()) {
            xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident, null));
        } else {
            for (String aksjonspunktType : aksjonspunktTyper) {
                xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident, aksjonspunktType));
            }
        }
    }

    private XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, IdentKey ident, String aksjonsounktType) {
        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(K9AbacAttributter.RESOURCE_FELLES_DOMENE, pdpRequest.getString(K9AbacAttributter.RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(K9AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getString(K9AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE));
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, K9AbacAttributter.RESOURCE_K9_SAK_SAKSSTATUS);
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, K9AbacAttributter.RESOURCE_K9_SAK_BEHANDLINGSSTATUS);
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, K9AbacAttributter.RESOURCE_K9_SAK_ANSVARLIG_SAKSBEHANDLER);

        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.key(), ident.ident());
        }
        if (aksjonsounktType != null) {
            resourceAttributeSet.addAttribute(K9AbacAttributter.RESOURCE_K9_SAK_AKSJONSPUNKT_TYPE, aksjonsounktType);
        }

        return resourceAttributeSet;
    }

    private void setOptionalValueinAttributeSet(XacmlAttributeSet resourceAttributeSet, PdpRequest pdpRequest, String key) {
        pdpRequest.getOptional(key).ifPresent(s -> resourceAttributeSet.addAttribute(key, s));
    }

    private List<IdentKey> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        List<IdentKey> identer = new ArrayList<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream().map(it -> new IdentKey(key, it)).collect(Collectors.toList()));
        }
        return identer;
    }

    private static record IdentKey(String key, String ident) {
    }
}
