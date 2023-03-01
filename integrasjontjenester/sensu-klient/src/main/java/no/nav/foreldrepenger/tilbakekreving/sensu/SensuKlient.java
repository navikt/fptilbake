package no.nav.foreldrepenger.tilbakekreving.sensu;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.metrics.Controllable;

@ApplicationScoped
public class SensuKlient implements Controllable {

    private static final Logger LOG = LoggerFactory.getLogger(SensuKlient.class);
    private static ExecutorService executorService;
    private static final int MAX_RETRY_SEND_SENSU = 2;
    private String sensuHost;
    private int sensuPort;
    private boolean lansert;
    private final AtomicBoolean kanKobleTilSensu = new AtomicBoolean(false);

    SensuKlient() {
        // CDI-proxy
    }

    @Inject
    public SensuKlient(@KonfigVerdi(value = "sensu.host", defaultVerdi = "sensu.nais") String sensuHost,
                       @KonfigVerdi(value = "sensu.port", defaultVerdi = "3030") Integer sensuPort,
                       @KonfigVerdi(value = "toggle.enable.sensu", defaultVerdi = "false") boolean lansert) {
        this.sensuHost = sensuHost;
        this.sensuPort = sensuPort;
        this.lansert = lansert;
    }

    private static String formatForException(String json, int antallEvents) {
        if (antallEvents > 1) {
            int maxSubstrLen = 1000;
            String substr = json.substring(0, Math.min(json.length(), maxSubstrLen)) + "....";
            return String.format("events[%s]: %s", antallEvents, substr);
        } else {
            return String.format("events[%s]: %s", antallEvents, json);
        }
    }

    public void logMetrics(SensuEvent metrics) {
        logMetrics(List.of(metrics));
    }

    /**
     * Sender et set med events samlet til Sensu.
     */
    public void logMetrics(List<SensuEvent> metrics) {
        var event = SensuEvent.createBatchSensuRequest(metrics);
        logMetrics(event);
    }

    /**
     * @param sensuRequest - requst til å sende sensu. Kan inneholde mange metrikker
     */
    public void logMetrics(SensuEvent.SensuRequest sensuRequest) {
        var callId = MDCOperations.getCallId();
        if (executorService != null) {
            if (!kanKobleTilSensu.get()) {
                return; // ignorer, har skrudd av pga ingen tilkobling til sensu
            }
            final String json = sensuRequest.toJson();
            final String jsonForEx = formatForException(json, sensuRequest.getAntallEvents());
            executorService.execute(() -> {
                //long startTs = System.nanoTime(); //NOSONAR
                try {
                    int rounds = MAX_RETRY_SEND_SENSU; // prøver par ganger hvis broken pipe, uten å logge første gang
                    while (rounds > 0 && kanKobleTilSensu.get() && !Thread.currentThread().isInterrupted()) {
                        rounds--;
                        // sensu har en ping/pong/heartbeat protokol, men støtter ikke det p.t., så
                        // åpner ny socket/outputstream for hver melding
                        try (var socket = new Socket()) {
                            establishSocketConnectionIfNeeded(socket);
                            try (OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                                writer.write(json, 0, json.length());
                                writer.flush();
                            }
                        } catch (IOException ex) {
                            // ink. SocketException
                            if (rounds <= 0) {
                                LOG.warn("Feil ved tilkobling til metrikkendepunkt. Kan ikke publisere melding fra callId[" + callId + "]: "
                                    + jsonForEx, ex);
                                break;
                            }
                        } catch (Exception ex) {
                            sjekkBroken(callId, jsonForEx, ex);
                            break;
                        }

                        Thread.sleep(50); // kort pause før retry
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // long tidBrukt = System.nanoTime() - startTs; //NOSONAR
                    // Enable ved behov LOG.debug("Ferdig med logging av metrikker for callId {}. Tid brukt: {}ms", callId, TimeUnit.NANOSECONDS.toMillis(tidBrukt)); //NOSONAR
                }
            });
        } else {
            LOG.info("Sensu klienten er ikke startet ennå!");
        }
    }

    private void sjekkBroken(String callId, String json, Exception ex) {
        if (System.getenv("NAIS_CLUSTER_NAME") != null) {
            // broken, skrur av tilkobling så ikke flooder loggen
            kanKobleTilSensu.set(false);
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Feil ved tilkobling til metrikkendepunkt. callId[%s]. Skrur av. Forsøkte melding: %s", callId, json), ex);
            }
        }
    }

    private synchronized void establishSocketConnectionIfNeeded(Socket socket) throws IOException {
        socket.setSoTimeout(60000);
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress(sensuHost, sensuPort), 30000);
    }

    @Override
    public synchronized void start() {
        if (Environment.current().isLocal()) {
            LOG.info("Kjører lokalt, kobler ikke opp mot sensu-server.");
        } else if (!lansert) {
            LOG.info("Starter ikke sensu klient.");
        } else {
            startService();
        }
    }

    synchronized void startService() {
        if (executorService != null) {
            throw new IllegalArgumentException("Service allerede startet, stopp først.");
        }
        executorService = Executors.newFixedThreadPool(3);
        kanKobleTilSensu.set(true);
    }

    @Override
    public synchronized void stop() {
        if (executorService != null) {
            kanKobleTilSensu.set(false);
            executorService.shutdown();
            try {
                executorService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executorService.shutdownNow();
        }
    }
}
