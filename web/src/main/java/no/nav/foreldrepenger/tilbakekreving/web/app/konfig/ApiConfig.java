package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingFaktaRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BrevRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.ForeldelseRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.TotrinnskontrollRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.VilkårsvurderingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.beregningsresultat.TilbakekrevingResultatRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument.DokumentRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling.FeilutbetalingSisteBehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling.FeilutbetalingÅrsakRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling.FordelRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningBehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningKravgrunnlagRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.init.InitielleLinksRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig.KonfigRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.los.LosRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag.GrunnlagRestTestTjenesteLocalDev;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons.VarselresponsRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.JettyServer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    private static final Environment ENV = Environment.current();

    public static final String API_URI = "/api";

    public ApiConfig() {
        var oas = new OpenAPI();
        var info = new Info()
                .title("Vedtaksløsningen - Tilbakekreving")
                .version("1.0")
                .description("REST grensesnitt for tilbakekreving.");

        oas.info(info).addServersItem(new Server().url(JettyServer.getContextPath()));

        var oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()));
        try {
            new GenericOpenApiContextBuilder<>()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // eksponert grensesnitt
        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(InitielleLinksRestTjeneste.class);
        classes.add(KodeverkRestTjeneste.class);
        classes.add(BehandlingRestTjeneste.class);
        classes.add(AksjonspunktRestTjeneste.class);
        classes.add(KonfigRestTjeneste.class);
        classes.add(HistorikkRestTjeneste.class);
        classes.add(DokumentRestTjeneste.class);
        classes.add(ForeldelseRestTjeneste.class);
        classes.add(VarselresponsRestTjeneste.class);
        classes.add(BehandlingFaktaRestTjeneste.class);
        classes.add(FeilutbetalingSisteBehandlingRestTjeneste.class);
        classes.add(FeilutbetalingÅrsakRestTjeneste.class);
        classes.add(VilkårsvurderingRestTjeneste.class);
        classes.add(TilbakekrevingResultatRestTjeneste.class);
        classes.add(TotrinnskontrollRestTjeneste.class);
        classes.add(BrevRestTjeneste.class);
        classes.add(FordelRestTjeneste.class);
        classes.add(ForvaltningBehandlingRestTjeneste.class);
        classes.add(ForvaltningKravgrunnlagRestTjeneste.class);
        classes.add(VergeRestTjeneste.class);
        classes.add(LosRestTjeneste.class);
        if (ENV.isLocal()) {
            classes.add(GrunnlagRestTestTjenesteLocalDev.class);
        }

        // swagger
        classes.add(OpenApiResource.class);

        // Applikasjonsoppsett
        classes.add(JacksonJsonConfig.class);

        // ExceptionMappers pga de som finnes i Jackson+Jersey-media
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);

        // Generell exceptionmapper m/logging for øvrige tilfelle
        classes.add(GeneralRestExceptionMapper.class);

        return Collections.unmodifiableSet(classes);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }
}
