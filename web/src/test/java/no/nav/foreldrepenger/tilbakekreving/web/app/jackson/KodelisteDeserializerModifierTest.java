package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KodelisteDeserializerModifierTest {

    static List<ObjectMapper> objectmappers = Arrays.asList(
        ObjectMapperFactory.getDefaultObjectMapperCopy(true),
        ObjectMapperFactory.getDefaultObjectMapperCopy(false)
    );

    // Test enum Kodeverdi endra til å ikkje ha objekt serialisering/deserialisering annotasjoner, kun JsonValue på kode.
    // Denne tester også at ObjectToPropertyPreProcessor er satt opp med korrekt propertyName for Kodeverdi.class.
    // Viss denne test feiler etter refaktorering av Kodeverdi, sjekk at propertyName gitt til ObjectToPropertyPreProcessor er korrekt.
    @ParameterizedTest
    @FieldSource("objectmappers")
    public void testObjektbasertDeserialiseringForJsonValue(ObjectMapper om) throws JsonProcessingException {
        // Serialiser
        final var inp1 = TestKodeverdiEnum.VALUE_TWO;
        final String jsonStr = om.writeValueAsString(inp1);
        // Deserialiser
        final TestKodeverdiEnum out1 = om.readValue(jsonStr, TestKodeverdiEnum.class);
        assertThat(out1).isEqualTo(inp1);
    }

    // Test enum Kodeverdi som framleis har gammal JsonFormat annotasjon og JsonCreator for i utgangspunktet å serialisere til objekt.
    // Kan fjernast når alle Kodeverdi implementasjoner er endra til å bruke kun @JsonValue for serialisering
    @ParameterizedTest
    @FieldSource("objectmappers")
    public void testObjektbasertDeserialiseringForJsonObject(ObjectMapper om) throws JsonProcessingException {
        // Serialiser
        final var inp1 = FagsakYtelseType.OPPLÆRINGSPENGER;
        final String jsonStr = om.writeValueAsString(inp1);
        // Deserialiser
        final FagsakYtelseType out1 = om.readValue(jsonStr, FagsakYtelseType.class);
        assertThat(out1).isEqualTo(inp1);
    }
}
