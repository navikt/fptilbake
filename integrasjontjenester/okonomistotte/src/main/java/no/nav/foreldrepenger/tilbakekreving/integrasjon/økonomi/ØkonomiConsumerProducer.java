package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingPortType;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class ØkonomiConsumerProducer {

    private ØkonomiConsumerConfig consumerConfig;

    @Inject
    public void setConfig(ØkonomiConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public ØkonomiConsumer økonomiConsumer() {
        TilbakekrevingPortType port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        disableCnCheck(port);
        return new ØkonomiConsumerImpl(port);
    }

    public ØkonomiSelftestConsumer økonomiSelftestConsumer() {
        TilbakekrevingPortType port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        disableCnCheck(port);
        return new ØkonomiSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    TilbakekrevingPortType wrapWithSts(TilbakekrevingPortType port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

    private void disableCnCheck(TilbakekrevingPortType port) {
        Client client = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setDisableCNCheck(true);

        conduit.setTlsClientParameters(tlsParams);
    }
}
