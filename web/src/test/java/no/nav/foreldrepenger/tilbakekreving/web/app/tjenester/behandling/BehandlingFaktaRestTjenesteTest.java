package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingFeilutbetalingFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;

public class BehandlingFaktaRestTjenesteTest {

    private BehandlingTjeneste behandlingTjeneste = mock(BehandlingTjeneste.class);

    private BehandlingFaktaRestTjeneste restTjeneste = new BehandlingFaktaRestTjeneste(behandlingTjeneste);

    @Test
    public void skalHenteFeilutbetalingFakta() {
        when(behandlingTjeneste.hentBehandlingFeilutbetalingFakta(anyLong())).thenReturn(Optional.of(lagBehandlingFeilutbetalingFakta()));
        BehandlingIdDto dto = new BehandlingIdDto(123455L);

        BehandlingFeilutbetalingFaktaDto resultat = restTjeneste.hentFeilutbetalingFakta(dto);

        assertThat(resultat).isNotNull();
    }

    private BehandlingFeilutbetalingFakta lagBehandlingFeilutbetalingFakta() {
        return BehandlingFeilutbetalingFakta.builder()
                .medAktuellFeilUtbetaltBeløp(BigDecimal.valueOf(2500))
                .medDatoForRevurderingsvedtak(LocalDate.now().minusDays(40))
                .medDatoForVarselSendt(LocalDate.now().minusDays(40))
                .medPerioder(Collections.singletonList(UtbetaltPeriode.lagPeriode(LocalDate.now().minusDays(120), LocalDate.now().minusDays(80), BigDecimal.valueOf(20000))))
                .medResultatFeilutbetaling("resFeilUtb")
                .medTidligereVarsletBeløp(BigDecimal.valueOf(3000))
                .medTotalPeriodeFom(LocalDate.now().minusDays(150))
                .medTotalPeriodeTom(LocalDate.now().minusDays(70))
                .medÅrsakRevurdering("årsak revurdering")
                .medResultatRevurdering("resultat revurdering")
                .build();
    }

}
