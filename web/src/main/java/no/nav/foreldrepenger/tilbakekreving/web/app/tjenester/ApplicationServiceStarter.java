package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.server.Controllable;

@ApplicationScoped
public class ApplicationServiceStarter {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationServiceStarter.class);

    private Set<Controllable> services;

    ApplicationServiceStarter() {
        // CDI
    }

    @Inject
    public ApplicationServiceStarter(@Any Instance<Controllable> services) {
        this(services.stream().collect(Collectors.toSet()));
    }

    ApplicationServiceStarter(Controllable service) {
        this(Set.of(service));
    }

    ApplicationServiceStarter(Set<Controllable> services) {
        this.services = services;
    }

    public void startServices() {
        // Services
        LOG.info("Starter {} services", services.size());
        CompletableFuture.allOf(services.stream().map(service -> runAsync(service::start)).toArray(CompletableFuture[]::new)).join();
        LOG.info("Startet {} services", services.size());
    }

    public void stopServices() {
        LOG.info("Stopper {} services", services.size());
        CompletableFuture.allOf(services.stream().map(service -> runAsync(service::stop)).toArray(CompletableFuture[]::new))
            .orTimeout(31, TimeUnit.SECONDS)
            .join();
        LOG.info("Stoppet {} services", services.size());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [services=" + services
            .stream()
            .map(Object::getClass)
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", ")) + "]";
    }
}
