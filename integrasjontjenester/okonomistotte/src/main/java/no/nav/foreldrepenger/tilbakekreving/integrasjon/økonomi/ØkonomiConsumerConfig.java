package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingPortType;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class ØkonomiConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tilbakekreving/tilbakekreving-v1-tjenestespesifikasjon.wsdl";
    private static final String NAMESPACE = "http://okonomi.nav.no/tilbakekrevingService/";
    private static final QName SERVICE = new QName(NAMESPACE, "TilbakekrevingService");
    private static final QName PORT = new QName(NAMESPACE, "TilbakekrevingServicePort");

    private String endpointUrl; // NOSONAR

    @Inject
    public ØkonomiConsumerConfig(@KonfigVerdi("tilbakekreving_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    //TODO denne bør ha default-scope
    public TilbakekrevingPortType getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(TilbakekrevingPortType.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(TilbakekrevingPortType.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
