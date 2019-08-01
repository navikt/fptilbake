package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

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
        return new ØkonomiConsumerImpl(port);
    }

    public ØkonomiSelftestConsumer økonomiSelftestConsumer() {
        TilbakekrevingPortType port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new ØkonomiSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    TilbakekrevingPortType wrapWithSts(TilbakekrevingPortType port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
