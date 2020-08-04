package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.CommonAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

@ApplicationScoped
@Fptilbake
public class FpXacmlRequestBuilderTjeneste implements XacmlRequestBuilderTjeneste {

    public FpXacmlRequestBuilderTjeneste() {
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, pdpRequest.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        int antall = antallResources(pdpRequest);
        for (int i = 0; i < antall; i++) {
            XacmlAttributeSet resourceAttributeSet = byggXacmlResourceAttrSet(pdpRequest, i);
            xacmlBuilder.addResourceAttributeSet(resourceAttributeSet);
        }

        return xacmlBuilder;
    }

    private int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1, antallAksjonspunktTyper(pdpRequest));
    }

    private int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_FNR);
    }

    private int antallAksjonspunktTyper(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
    }

    private XacmlAttributeSet byggXacmlResourceAttrSet(PdpRequest pdpRequest, int index) {

        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(CommonAttributter.RESOURCE_FELLES_DOMENE, pdpRequest.getString(CommonAttributter.RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getString(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));

        int antallFnrPåRequest = pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_FNR);
        if (index < antallFnrPåRequest) {
            setOptionalListValueinAttributeSet(resourceAttributeSet, pdpRequest, CommonAttributter.RESOURCE_FELLES_PERSON_FNR, index % antallFnrPåRequest);
        } else {
            int kalkulertIndex = (index - antallFnrPåRequest) % Math.max(pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE), 1);
            setOptionalListValueinAttributeSet(resourceAttributeSet, pdpRequest, CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, kalkulertIndex);
        }
        setOptionalListValueinAttributeSet(resourceAttributeSet, pdpRequest, FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, (index / Math.max(antallIdenter(pdpRequest), 1)));
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS);
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS);
        setOptionalValueinAttributeSet(resourceAttributeSet, pdpRequest, FpAbacAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER);

        return resourceAttributeSet;
    }

    private void setOptionalValueinAttributeSet(XacmlAttributeSet resourceAttributeSet, PdpRequest pdpRequest, String key) {
        pdpRequest.getOptional(key).ifPresent(s -> resourceAttributeSet.addAttribute(key, s));
    }

    private void setOptionalListValueinAttributeSet(XacmlAttributeSet resourceAttributeSet, PdpRequest pdpRequest, String key, int index) {
        List<String> list = pdpRequest.getListOfString(key);
        if (list.size() >= index + 1) {
            Optional.ofNullable(list.get(index)).ifPresent(s -> resourceAttributeSet.addAttribute(key, s));
        }
    }
}
