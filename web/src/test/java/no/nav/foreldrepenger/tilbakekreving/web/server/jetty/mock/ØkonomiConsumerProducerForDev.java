package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.mock;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerConfig;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerImpl;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerProducer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiSelftestConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiSelftestConsumerImpl;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingPortType;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Alternative
@Priority(2)
@Dependent
//TODO fjern denne klassen
public class ØkonomiConsumerProducerForDev extends ØkonomiConsumerProducer {

    private ØkonomiConsumerConfig consumerConfig;

    @Inject
    public void setConfig(ØkonomiConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public ØkonomiConsumer økonomiConsumer() {
        TilbakekrevingPortType port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        String økonomiMock = System.getProperty("økonomi.mock");
        return "J".equals(økonomiMock) ? new ØkonomiConsumerMockImpl() : new ØkonomiConsumerImpl(port);
    }

    public ØkonomiSelftestConsumer økonomiSelftestConsumer() {
        TilbakekrevingPortType port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new ØkonomiSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    TilbakekrevingPortType wrapWithSts(TilbakekrevingPortType port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
