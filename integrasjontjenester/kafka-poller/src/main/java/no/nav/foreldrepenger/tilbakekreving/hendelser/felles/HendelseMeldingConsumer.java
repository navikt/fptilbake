package no.nav.foreldrepenger.tilbakekreving.hendelser.felles;

import java.util.Properties;

import org.apache.kafka.common.serialization.StringDeserializer;

import no.nav.vedtak.konfig.KonfigVerdi;

public class HendelseMeldingConsumer {

    protected static final int TIMEOUT = 1000;
    protected static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    protected static final String FEED_URL = "fagsystem.fattede.vedtak_topic.url";

    protected Properties lagFellesProperty(String bootstrapServers, String applikasjonNavn){
        Properties properties = new Properties();
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty(BOOTSTRAP_SERVERS, bootstrapServers);
        properties.setProperty("group.id", applikasjonNavn);
        properties.setProperty("client.id", applikasjonNavn);
        properties.setProperty("enable.auto.commit", "false");
        properties.setProperty("max.poll.records", "20");
        properties.setProperty("auto.offset.reset", "earliest"); // TODO sett til 'none' når det har blitt lest fra køen i produksjon
        return properties;
    }

    protected void setSecurity(String username, Properties properties) {
        if (username != null && !username.isEmpty()) {
            properties.setProperty("security.protocol", "SASL_SSL");
            properties.setProperty("sasl.mechanism", "PLAIN");
        }
    }

    protected void addUserToProperties(@KonfigVerdi("kafka.username") String username, @KonfigVerdi("kafka.password") String password, Properties properties) {
        if (notNullNotEmpty(username) && notNullNotEmpty(password)) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            properties.setProperty("sasl.jaas.config", jaasCfg);
        }
    }

    private boolean notNullNotEmpty(String str) {
        return (str != null && !str.isEmpty());
    }
}
