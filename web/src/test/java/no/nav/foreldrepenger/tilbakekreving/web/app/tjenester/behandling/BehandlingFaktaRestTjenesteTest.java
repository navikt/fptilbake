package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriodeMedFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingFeilutbetalingFaktaDto;

public class BehandlingFaktaRestTjenesteTest {

    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste = mock(FaktaFeilutbetalingTjeneste.class);
    private BehandlingTjeneste behandlingTjeneste = mock(BehandlingTjeneste.class);

    private BehandlingFaktaRestTjeneste restTjeneste = new BehandlingFaktaRestTjeneste(faktaFeilutbetalingTjeneste, behandlingTjeneste);

    @Test
    public void skalHenteFeilutbetalingFakta() {
        when(faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(anyLong())).thenReturn(lagBehandlingFeilutbetalingFakta());
        BehandlingReferanse dto = new BehandlingReferanse(123455L);

        BehandlingFeilutbetalingFaktaDto resultat = restTjeneste.hentFeilutbetalingFakta(dto);

        assertThat(resultat).isNotNull();
    }

    private BehandlingFeilutbetalingFakta lagBehandlingFeilutbetalingFakta() {
        return BehandlingFeilutbetalingFakta.builder()
            .medAktuellFeilUtbetaltBeløp(BigDecimal.valueOf(2500))
            .medDatoForRevurderingsvedtak(LocalDate.now().minusDays(40))
            .medPerioder(Collections.singletonList(LogiskPeriodeMedFaktaDto.lagPeriode(LocalDate.now().minusDays(120), LocalDate.now().minusDays(80), BigDecimal.valueOf(20000))))
            .medTidligereVarsletBeløp(3000l)
            .medTotalPeriodeFom(LocalDate.now().minusDays(150))
            .medTotalPeriodeTom(LocalDate.now().minusDays(70))
            .build();
    }

}
