package no.nav.foreldrepenger.tilbakekreving.web.app;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.FellesKlasserForRest;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.avstemming.AvstemmingRestTestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.batch.BatchRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingFaktaRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BrevRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.ForeldelseRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.TilbakekrevingResulattRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.TotrinnskontrollRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.VilkårsvurderingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument.DokumentRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling.FeilutbetalingÅrsakRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling.FordelRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningBehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningTekniskRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig.KonfigRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon.MigrasjonRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.saksbehandler.NavAnsattRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag.GrunnlagRestTestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons.VarselresponsRestTjeneste;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    public static final String API_URI = "/api";

    public ApplicationConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
            .title("Vedtaksløsningen - Tilbakekreving")
            .version("1.0")
            .description("REST grensesnitt for Vedtaksløsningen.");

        oas.info(info)
            .addServersItem(new Server()
                .url("/fptilbake"));
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
            .openAPI(oas)
            .prettyPrint(true)
            .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
            .resourcePackages(Stream.of("no.nav.vedtak", "no.nav.foreldrepenger")
                .collect(Collectors.toSet()));

        try {
            new JaxrsOpenApiContextBuilder<>()
                .openApiConfiguration(oasConfig)
                .buildContext(true)
                .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(OpenApiResource.class);

        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.addAll(FellesKlasserForRest.getClasses());

        classes.add(ProsessTaskRestTjeneste.class);

        classes.add(KodeverkRestTjeneste.class);
        classes.add(BehandlingRestTjeneste.class);
        classes.add(AksjonspunktRestTjeneste.class);
        classes.add(BatchRestTjeneste.class);
        classes.add(KonfigRestTjeneste.class);
        classes.add(NavAnsattRestTjeneste.class);
        classes.add(HistorikkRestTjeneste.class);
        classes.add(DokumentRestTjeneste.class);
        classes.add(ForeldelseRestTjeneste.class);

        classes.add(VarselresponsRestTjeneste.class);
        classes.add(BehandlingFaktaRestTjeneste.class);
        classes.add(FeilutbetalingÅrsakRestTjeneste.class);
        classes.add(VilkårsvurderingRestTjeneste.class);
        classes.add(TilbakekrevingResulattRestTjeneste.class);
        classes.add(TotrinnskontrollRestTjeneste.class);
        classes.add(BrevRestTjeneste.class);
        classes.add(FordelRestTjeneste.class);
        classes.add(ForvaltningTekniskRestTjeneste.class);
        classes.add(ForvaltningBehandlingRestTjeneste.class);
        classes.add(MigrasjonRestTjeneste.class);

        //HAXX legger til en test-tjeneste i alle miljø utenom prod
        Optional<String> envName = EnvironmentProperty.getEnvironmentName();
        if (envName.isPresent() && !EnvironmentProperty.PROD.equalsIgnoreCase(envName.get())) {
            logger.warn("Legger til testklasser (skal ikke skje i prod): {}", AvstemmingRestTestTjeneste.class.getSimpleName());
            classes.add(AvstemmingRestTestTjeneste.class);
        }

        //HAXX GrunnlagRestTjenesteTest skal bare være tilgjengelig for lokal utvikling, brukes for å sette opp test
        //hvis denne legges til i en egen Application isdf i denne, kan man ikke bruke swagger for å nå tjenesten
        //bruker derfor CDI for å slå opp klassen
        Instance<GrunnlagRestTestTjeneste> grunnlagTestTjeneste = CDI.current().select(GrunnlagRestTestTjeneste.class);
        if (!grunnlagTestTjeneste.isUnsatisfied()) {
            TargetInstanceProxy proxy = (TargetInstanceProxy) grunnlagTestTjeneste.get();
            classes.add(proxy.weld_getTargetClass());
        }

        return Collections.unmodifiableSet(classes);
    }
}
