package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.mock;

import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TpsAdapterWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;

@Specializes
public class HentKravgrunnlagMapperMock extends HentKravgrunnlagMapper {

    private Logger logger = LoggerFactory.getLogger(HentKravgrunnlagMapperMock.class);

    @Inject
    public HentKravgrunnlagMapperMock(TpsAdapterWrapper tpsAdapterWrapper) {
        super(tpsAdapterWrapper);
    }

    @Override
    protected String hentAktoerId(GjelderType identType, String ident) {
        logger.warn("Hentet kravgrunnlag i utviklermodus. Skal ikke skje i testmiljø eller produksjon, det er for lokalt utviklingsmiljø. Lagrer mocket aktørId i databasen.");
        return "mock" + ident;
    }
}
