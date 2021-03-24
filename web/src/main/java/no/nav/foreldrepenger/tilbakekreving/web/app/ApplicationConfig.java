package no.nav.foreldrepenger.tilbakekreving.web.app;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

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
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningFritekstbrevRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.init.InitielleLinksRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig.KonfigRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon.MigrasjonRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.saksbehandler.NavAnsattRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag.GrunnlagRestTestTjenesteLocalDev;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons.VarselresponsRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.util.env.Environment;


@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    private static final Environment ENV = Environment.current();

    public static final String API_URI = "/api";
    private static final String APPLIKASJON_NAVN_K9_TILBAKE = "k9-tilbake";
    private static final String APPLIKASJON_NAVN_FPTILBAKE = "fptilbake";

    public ApplicationConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
            .title("Vedtaksløsningen - Tilbakekreving")
            .version("1.0")
            .description("REST grensesnitt for Vedtaksløsningen.");

        oas.info(info).addServersItem(new Server().url(getContextPath()));

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

    private String getContextPath() {
        String applikasjon= ENV.getProperty("app.name");
        switch (applikasjon) {
            case APPLIKASJON_NAVN_FPTILBAKE:
                return "/fptilbake";
            case APPLIKASJON_NAVN_K9_TILBAKE:
                return "/k9/tilbake";
            default:
                throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
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

        classes.add(InitielleLinksRestTjeneste.class);
        classes.add(KodeverkRestTjeneste.class);
        classes.add(BehandlingRestTjeneste.class);
        classes.add(AksjonspunktRestTjeneste.class);
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
        classes.add(ForvaltningBehandlingRestTjeneste.class);
        classes.add(ForvaltningFritekstbrevRestTjeneste.class);
        classes.add(MigrasjonRestTjeneste.class);
        classes.add(VergeRestTjeneste.class);

        if (ENV.isLocal()) {
            classes.add(GrunnlagRestTestTjenesteLocalDev.class);
        }

        return Collections.unmodifiableSet(classes);
    }
}
