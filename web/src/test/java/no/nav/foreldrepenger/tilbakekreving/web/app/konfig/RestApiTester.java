package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

public class RestApiTester {

    static final List<Class<?>> UNNTATT = Collections.singletonList(OpenApiResource.class);

    protected static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (var klasse : finnAlleRestTjenester()) {
            for (var method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    liste.add(method);
                }
            }
        }
        return liste;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        var alle = new HashSet<>(finnAlleRestTjenester(new ApiConfig()));
        alle.addAll(finnAlleRestTjenester(new ForvaltningApiConfig()));
        return alle;
    }

    static Collection<Class<?>> finnAlleJsonSubTypeClasses(Class<?> klasse) {
        var resultat = new ArrayList<Class<?>>();
        if (klasse.isAnnotationPresent(JsonSubTypes.class)) {
            var jsonSubTypes = klasse.getAnnotation(JsonSubTypes.class);
            for (var subtype : jsonSubTypes.value()) {
                resultat.add(subtype.value());
            }
        }
        return resultat;
    }

    static Collection<Class<?>> finnAlleRestTjenester(Application config) {
        return config.getClasses().stream()
                .filter(c -> c.getAnnotation(Path.class) != null)
                .filter(c -> !UNNTATT.contains(c))
                .collect(Collectors.toList());
    }
}
