package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import static org.assertj.core.api.Fail.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.felles.testutilities.cdi.WeldContext;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

public class RestApiAbacTest {

    static {
        WeldContext.getInstance(); // init cdi container
    }

    @BeforeAll
    public static void setup() {
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8090");
        System.setProperty("app.name", "fptilbake");
    }

    @AfterAll
    public static void teardown() {
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
        System.clearProperty("app.name");
    }

    /**
     * IKKE ignorer denne testen, sikrer at REST_MED_INNTEKTSMELDING-endepunkter får tilgangskontroll
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her     *
     */
    @Test
    public void test_at_alle_restmetoder_er_annotert_med_BeskyttetRessurs() {
        for (Method restMethod : RestApiTester.finnAlleRestMetoder()) {
            if (restMethod.getAnnotation(BeskyttetRessurs.class) == null) {
                throw new AssertionError("Mangler @" + BeskyttetRessurs.class.getSimpleName() + "-annotering på " + restMethod);
            }
        }
    }

    @Test
    public void sjekk_at_ingen_metoder_er_annotert_med_dummy_verdier() {
        for (Method metode : RestApiTester.finnAlleRestMetoder()) {
            assertPropertyIAnnotering(metode);
        }
    }

    /**
     * IKKE ignorer denne testen, helper til med at input til tilgangskontroll blir riktig
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her     *
     */
    @Test
    public void test_at_alle_input_parametre_til_restmetoder_implementer_AbacDto() {
        String feilmelding = "Parameter på %s.%s av type %s må implementere " + AbacDto.class.getSimpleName() + ".\n";
        StringBuilder feilmeldinger = new StringBuilder();

        for (Method restMethode : RestApiTester.finnAlleRestMetoder()) {
            for (Parameter parameter : restMethode.getParameters()) {
                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    ParameterizedType type = (ParameterizedType) parameter.getParameterizedType();
                    @SuppressWarnings("rawtypes")
                    Class<?> aClass = (Class) (type.getActualTypeArguments()[0]);
                    if (!AbacDto.class.isAssignableFrom(aClass) && !IgnorerteInputTyper.ignore(aClass)) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(), aClass.getSimpleName()));
                    }
                } else {
                    if (!AbacDto.class.isAssignableFrom(parameter.getType()) && !IgnorerteInputTyper.ignore(parameter.getType()) && parameter.getAnnotation(TilpassetAbacAttributt.class) == null) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(), parameter.getType().getSimpleName()));
                    }
                }
            }
        }
        if (feilmeldinger.length() > 0) {
            throw new AssertionError("Følgende inputparametre til REST_MED_INNTEKTSMELDING-tjenester mangler AbacDto-impl\n" + feilmeldinger);
        }
    }

    private void assertPropertyIAnnotering(Method metode) {
        Class<?> klasse = metode.getDeclaringClass();
        BeskyttetRessurs annotation = metode.getAnnotation(BeskyttetRessurs.class);
        if (annotation == null) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Mangler @" + annotation.getClass().getSimpleName());
        }
        if (annotation.property().isEmpty()) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Tom property @" + annotation.getClass().getSimpleName());
        }
        List<String> godkjenteProperties = List.of(
                AbacProperty.APPLIKASJON,
                AbacProperty.BATCH,
                AbacProperty.DRIFT,
                AbacProperty.FAGSAK,
                AbacProperty.VENTEFRIST
        );
        if (!annotation.resource().isEmpty() || annotation.resourceType() != ResourceType.DUMMY) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Skal ikke bruke ressurs, bruk property i @" + annotation.getClass().getSimpleName());
        }
        if (!annotation.resource().isEmpty()) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Skal ikke bruke resource " + annotation.resource() + " , bruk property i @" + annotation.getClass().getSimpleName());
        }
        if (!godkjenteProperties.contains(annotation.property())) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Skal ikke bruke ukjent property " + annotation.property() + " , bruk en av " + godkjenteProperties + "i @" + annotation.getClass().getSimpleName());
        }
        if (annotation.actionType() == ActionType.DUMMY) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Ikke bruk DUMMY-verdi for "
                    + ActionType.class.getSimpleName());
        }
    }

    /**
     * Disse typene slipper naturligvis krav om impl av {@link AbacDto}
     */
    enum IgnorerteInputTyper {
        BOOLEAN(Boolean.class.getName()),
        SERVLET(HttpServletRequest.class.getName());

        private String className;

        IgnorerteInputTyper(String className) {
            this.className = className;
        }

        static boolean ignore(Class<?> klasse) {
            return Arrays.stream(IgnorerteInputTyper.values()).anyMatch(e -> e.className.equals(klasse.getName()));
        }

    }
}
