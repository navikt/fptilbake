package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.EnvEntry;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.msg.client.wmq.compat.jms.internal.JMSC;

import no.nav.foreldrepenger.konfig.Environment;

class JmsKonfig {

    private final static Environment ENV = Environment.current();

    private JmsKonfig() { // Util class
    }

    static void settOppJndiConnectionfactory(String jndiName) throws JMSException, NamingException {
        MQConnectionFactory mqConnectionFactory = createConnectionFactory(
            ENV.getProperty("mqGateway02.hostname"),
            ENV.getProperty("mqGateway02.port", Integer.class),
            ENV.getProperty("mqGateway02.channel"),
            ENV.getProperty("mqGateway02.name"));

        new EnvEntry(jndiName, mqConnectionFactory);
    }

    static void settOppJndiMessageQueue(String jndiName, String queueNameProp) throws NamingException, JMSException {
        MQQueue queue = new MQQueue(ENV.getProperty(queueNameProp));

        new EnvEntry(jndiName, queue);
    }

    private static MQConnectionFactory createConnectionFactory(String hostName, Integer port, String channel, String queueManagerName) throws JMSException {
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        connectionFactory.setHostName(hostName);
        connectionFactory.setPort(port);
        if (channel != null) {
            connectionFactory.setChannel(channel);
        }
        connectionFactory.setQueueManager(queueManagerName);
        connectionFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
        connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);

        if (!ENV.isProd()) { // TODO sjekk om den fortsatt trenges - ble avviklet i fpsak - den brukes nok ikke til noe
            connectionFactory.setSSLCipherSuite("TLS_RSA_WITH_AES_128_CBC_SHA");

            // Denne trengs for at IBM MQ libs skal bruke/gjenkjenne samme ciphersuite navn som Oracle JRE:
            // (Uten denne vil ikke IBM MQ libs gjenkjenne "TLS_RSA_WITH_AES_128_CBC_SHA")
            System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        }

        return connectionFactory;
    }
}
