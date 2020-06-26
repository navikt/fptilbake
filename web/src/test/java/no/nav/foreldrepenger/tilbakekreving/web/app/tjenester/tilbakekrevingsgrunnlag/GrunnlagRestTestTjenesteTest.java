package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.testtjenester.GrunnlagRestTestTjenesteLocalDev;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.testtjenester.KravgrunnlagDto;

public class GrunnlagRestTestTjenesteTest {

    private TpsTjeneste tpsTjenesteMock = mock(TpsTjeneste.class);
    private KravgrunnlagTjeneste kravgrunnlagTjenesteMock = mock(KravgrunnlagTjeneste.class);

    private GrunnlagRestTestTjenesteLocalDev grunnlagTjeneste = new GrunnlagRestTestTjenesteLocalDev(tpsTjenesteMock, kravgrunnlagTjenesteMock);

    @Test
    public void lagreUtbetalinger() throws Exception {
        Mockito.when(tpsTjenesteMock.hentAktørForFnr(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Response response = grunnlagTjeneste.lagreUtbetalinger(new BehandlingReferanse("1"), lagMockKravgrunnlagDto());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        verify(kravgrunnlagTjenesteMock, atLeastOnce()).lagreTilbakekrevingsgrunnlagFraØkonomi(anyLong(), any(Kravgrunnlag431.class), any(boolean.class));
    }

    private KravgrunnlagDto lagMockKravgrunnlagDto() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(getClass().getClassLoader().getResource("grunnlag/sampelgrunnlag.json").getFile());
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(file, KravgrunnlagDto.class);
    }
}
