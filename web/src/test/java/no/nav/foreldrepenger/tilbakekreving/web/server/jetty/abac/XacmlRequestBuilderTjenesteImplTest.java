package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import static no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.PdpConsumer;
import no.nav.vedtak.sikkerhet.pdp.PdpKlientImpl;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

public class XacmlRequestBuilderTjenesteImplTest {

    public static final String JWT_TOKEN = "ew0KICAidHlwIjogIkpXVCIsDQogICJraWQiOiAiZm9vIiwNCiAgImFsZyI6ICJSUzI1NiINCn0.ew0KICAiYXVkIjogIk9JREMiLA0KICAiYXpwIjogIk9JREMiLA0KICAiYXV0aF90aW1lIjogMTQ5ODAzOTkxNCwNCiAgImV4cCI6IDE0OTgwNDM1MTUsDQogICJpYXQiOiAxNDk4MDM5OTE1LA0KICAiaXNzIjogImh0dHBzOi8vZm9vLmJhci5ubzo0NDMvaXNzby9vYXV0aDIiLA0KICAicmVhbG0iOiAiLyIsDQogICJzdWIiOiAic29tZW9uZSIsDQogICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iDQp9.asdf";
    private static final String PEP_ID = "pepId";
    private PdpKlientImpl pdpKlient;
    private PdpConsumer pdpConsumerMock;
    private XacmlRequestBuilderTjenesteImpl xamlRequestBuilderTjeneste;

    @Before
    public void setUp() {
        pdpConsumerMock = mock(PdpConsumer.class);
        xamlRequestBuilderTjeneste = new XacmlRequestBuilderTjenesteImpl();
        pdpKlient = new PdpKlientImpl(pdpConsumerMock, xamlRequestBuilderTjeneste, PEP_ID);
    }

    @Test
    public void kallPdpMedSamlTokenNårIdTokenErSamlToken() throws Exception {
        AbacIdToken idToken = AbacIdToken.withSamlToken("SAML");
        XacmlResponseWrapper responseWrapper = createResponse("xacml/xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_FNR, Collections.singleton("12345678900"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(CommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN)).isTrue();
    }

    @Test
    public void kallPdpUtenFnrResourceHvisPersonlisteErTom() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml/xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_FNR, Collections.emptySet());
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(RESOURCE_FELLES_PERSON_FNR)).isFalse();
    }

    @Test
    public void kallPdpMedJwtTokenBodyNårIdTokenErJwtToken() throws Exception {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml/xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_FNR, Collections.singleton("12345678900"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY)).isTrue();
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn1() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml/xacml3response.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        String xacmlRequestString = captor.getValue().build().toString();

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();
    }

    @Test
    public void sporingsloggListeSkalHaSammeRekkefølgePåidenterSomXacmlRequest() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml/xacml3response.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        JsonObject xacmlRequest = captor.getValue().build();
        JsonArray resourceArray = xacmlRequest.getJsonObject("Request").getJsonArray("Resource");

        List<String> personer = pdpRequest.getListOfString(RESOURCE_FELLES_PERSON_FNR);

        for (int i = 0; i < personer.size(); i++) {
            assertThat(resourceArray.get(i).toString().contains(personer.get(i))).isTrue();
        }
    }

    private PdpRequest lagPdpRequest() {
        PdpRequest request = new PdpRequest();
        request.put(CommonAttributter.RESOURCE_FELLES_DOMENE, "foreldrepenger");
        request.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, BeskyttetRessursActionAttributt.READ.getEksternKode());
        request.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, BeskyttetRessursResourceAttributt.FAGSAK.getEksternKode());
        return request;
    }

    @SuppressWarnings("resource")
    private XacmlResponseWrapper createResponse(String jsonFile) throws FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource(jsonFile).getFile());
        JsonReader reader = Json.createReader(new FileReader(file));
        JsonObject jo = (JsonObject) reader.read();
        return new XacmlResponseWrapper(jo);
    }
}
