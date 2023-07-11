package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.jms.JmsConstants;
import com.ibm.msg.client.jakarta.wmq.compat.jms.internal.JMSC;

import jakarta.jms.JMSException;
import no.nav.foreldrepenger.felles.jms.JmsKonfig;
import no.nav.foreldrepenger.konfig.KonfigVerdi;

@ApplicationScoped
public class KravgrunnlagJmsConsumerKonfig {

    private JmsKonfig jmsKonfig;
    private MQConnectionFactory mqConnectionFactory;
    private MQQueue mqQueue;

    KravgrunnlagJmsConsumerKonfig() {
        // CDI
    }

    @Inject
    public KravgrunnlagJmsConsumerKonfig(@KonfigVerdi("systembruker.username") String bruker,
                                         @KonfigVerdi("systembruker.password") String passord,
                                         @KonfigVerdi("mqGateway02.hostname") String host,
                                         @KonfigVerdi("mqGateway02.port") int port,
                                         @KonfigVerdi("mqGateway02.name") String managerName,
                                         @KonfigVerdi(value = "mqGateway02.channel", required = false) String channel,
                                         @KonfigVerdi("fptilbake.kravgrunnlag.queuename") String queueName) throws JMSException {
        this.jmsKonfig = new JmsKonfig(host, port, managerName, channel, bruker, passord, queueName, null);
        this.mqConnectionFactory = settOppConnectionFactory(host, port, channel, managerName);
        this.mqQueue = settOppMessageQueue(queueName);
    }

    private static MQQueue settOppMessageQueue(String queueName) throws JMSException {
        return new MQQueue(queueName);
    }

    private static MQConnectionFactory settOppConnectionFactory(String host, int port, String channel, String manager) throws JMSException {
        return createConnectionFactory(host, port, channel, manager);
    }

    private static MQConnectionFactory createConnectionFactory(String hostName,
                                                               Integer port,
                                                               String channel,
                                                               String queueManagerName) throws JMSException {
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        connectionFactory.setHostName(hostName);
        connectionFactory.setPort(port);
        if (channel != null) {
            connectionFactory.setChannel(channel);
        }
        connectionFactory.setQueueManager(queueManagerName);

        connectionFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
        connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);

        return connectionFactory;
    }

    public MQConnectionFactory getMqConnectionFactory() {
        return mqConnectionFactory;
    }

    public MQQueue getMqQueue() {
        return mqQueue;
    }

    public JmsKonfig getJmsKonfig() {
        return jmsKonfig;
    }
}
