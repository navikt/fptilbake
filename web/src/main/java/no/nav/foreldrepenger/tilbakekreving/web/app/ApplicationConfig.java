package no.nav.foreldrepenger.tilbakekreving.web.app;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import io.swagger.jaxrs.config.BeanConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.FellesKlasserForRest;
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
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig.KonfigRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.saksbehandler.NavAnsattRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag.GrunnlagRestTestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons.VarselresponsRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.isso.config.ServerInfo;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        if (ServerInfo.instance().isUsingTLS()) {
            beanConfig.setSchemes(new String[]{"https"});
        } else {
            beanConfig.setSchemes(new String[]{"http"});

        }
        beanConfig.setBasePath("/fptilbake/api");
        beanConfig.setResourcePackage("no.nav");
        beanConfig.setTitle("Foreldrepenger tilbakekreving - App Skeleton");
        beanConfig.setDescription("Jetty Java App m/sikkerhet, swagger, dokumentasjon, db, metrics, osv. for deployment til NAIS");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

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
