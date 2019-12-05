package no.nav.foreldrepenger.tilbakekreving.web.app.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.util.MdcExtendedLogContext;

@BeskyttetRessurs(action = BeskyttetRessursActionAttributt.DUMMY, ressurs = BeskyttetRessursResourceAttributt.DUMMY)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 12)
@Dependent
/**
 * Denne inteceptoren sørger for at det logges behandlingId og fagsakId når noe logges i kontekst av et REST-kall
 */
public class MDCLoggingInterceptor {

    private static final MdcExtendedLogContext MDC_EXTENDED_LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    @AroundInvoke
    public Object wrapTransaction(final InvocationContext invocationContext) throws Exception {
        AbacAttributtSamling abacAttributtSamling = hentAbacAttributter(invocationContext);

        leggTilMDC("behandling", abacAttributtSamling.getVerdier(AppAbacAttributtType.BEHANDLING_ID));
        leggTilMDC("fagsak", abacAttributtSamling.getVerdier(AppAbacAttributtType.FAGSAK_ID));

        return invocationContext.proceed();

        //NOTE: kan ikke fjerne fra MDC kontekst her, siden dette brukes i GeneralRestExceptionMapper som invokeres senere
    }

    private void leggTilMDC(String navn, Set<Long> verdi) {
        if (verdi.size() > 1) {
            throw new IllegalArgumentException("Ikke støttet å ha flere " + navn + "Id-er i samme metode. Trengs det, må støtte legges til.");
        }
        MDC_EXTENDED_LOG_CONTEXT.remove(navn);
        if (verdi.size() == 1) {
            MDC_EXTENDED_LOG_CONTEXT.add(navn, Long.toString(verdi.iterator().next()));
        }
    }

    private AbacAttributtSamling hentAbacAttributter(InvocationContext invocationContext) {
        Method method = invocationContext.getMethod();
        AbacAttributtSamling attributter = AbacAttributtSamling.medSamlToken("Dummy token. Token trengs ikke for " + MDCLoggingInterceptor.class.getName());
        Parameter[] parameterDecl = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Object parameterValue = invocationContext.getParameters()[i];
            TilpassetAbacAttributt tilpassetAnnotering = parameterDecl[i].getAnnotation(TilpassetAbacAttributt.class);
            leggTilAttributterFraParameter(attributter, parameterValue, tilpassetAnnotering);
        }
        return attributter;
    }

    @SuppressWarnings("rawtypes")
    static void leggTilAttributterFraParameter(AbacAttributtSamling attributter, Object parameterValue, TilpassetAbacAttributt tilpassetAnnotering) {
        if (tilpassetAnnotering != null) {
            leggTil(attributter, tilpassetAnnotering, parameterValue);
        } else {
            if (parameterValue instanceof AbacDto) { //NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                attributter.leggTil(((AbacDto) parameterValue).abacAttributter());
            } else if (parameterValue instanceof Collection) { //NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                leggTilAbacDtoSamling(attributter, (Collection) parameterValue);
            }
        }
    }

    private static void leggTilAbacDtoSamling(AbacAttributtSamling attributter, Collection<?> parameterValue) {
        for (Object value : parameterValue) {
            if (value instanceof AbacDto) {
                attributter.leggTil(((AbacDto) value).abacAttributter());
            }
        }
    }

    private static void leggTil(AbacAttributtSamling attributter, TilpassetAbacAttributt tilpassetAnnotering, Object verdi) {
        try {
            AbacDataAttributter dataAttributter = tilpassetAnnotering.supplierClass().getDeclaredConstructor().newInstance().apply(verdi);
            attributter.leggTil(dataAttributter);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }


}
