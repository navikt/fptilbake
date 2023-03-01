package no.nav.foreldrepenger.tilbakekreving.sensu;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensuKlientTest {

    private static ServerSocket serverSocket;
    private static SensuKlient sensuKlient;

    private BlockingQueue<String> socketOutput = new ArrayBlockingQueue<>(100);

    @BeforeAll
    static void beforeAll() {
        System.setProperty("app.name", "k9-tilbake");
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty("app.name");
    }

    private static final String expectedJsonBeforeTimestamp = "{" +
            "\"name\":\"sensu-event-k9-tilbake\"," +
            "\"type\":\"metric\"," +
            "\"handlers\":[\"events_nano\"]," +
            "\"status\":0," +
            "\"output\":\"k9-tilbake.registrert.task,application=k9-tilbake,cluster=local,namespace=teamforeldrepenger,task_type=task.registerSøknad counter=1i";

    @BeforeEach
    void init() throws IOException {
        serverSocket = new ServerSocket(0);
        serverSocket.setSoTimeout(1000);
        sensuKlient = new SensuKlient("localhost", serverSocket.getLocalPort(), true);
        sensuKlient.startService();

        new Thread(() -> {
            try (Socket socket = serverSocket.accept()) {
                StringBuilder sb = new StringBuilder();
                try (Reader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        sb.append((char) c);
                    }
                }
                socketOutput.put(sb.toString());
            } catch (InterruptedException | IOException e) {
                throw new IllegalStateException("Kunne ikke lese fra socket", e);
            }
        }).start();
    }

    @AfterEach
    void teardown() throws IOException {
        sensuKlient.stop();
        serverSocket.close();
    }

    @Test
    void logMetrics() throws Exception {
        // Perform
        sensuKlient.logMetrics(SensuEvent.createSensuEvent(
                "registrert.task",
                Map.of("task_type", "task.registerSøknad"),
                Map.of("counter", 1)));

        // Assert
        String resultat = readFromSocket();
        assertThat(resultat).isNotNull().startsWith(expectedJsonBeforeTimestamp);
    }

    private String readFromSocket() throws InterruptedException {
        return socketOutput.poll(10, TimeUnit.SECONDS);
    }
}
