package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeMedBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class ForeldelseRestTjenesteTest {

    private static final long behandlingId = 123456;
    private static final BehandlingReferanse idDto = new BehandlingReferanse(behandlingId);

    private VurdertForeldelseTjeneste vurdertForeldelseTjenesteMock = mock(VurdertForeldelseTjeneste.class);
    private KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjenesteMock =mock(KravgrunnlagBeregningTjeneste.class);
    private BehandlingTjeneste behandlingTjeneste = mock(BehandlingTjeneste.class);

    private ForeldelseRestTjeneste restTjeneste = new ForeldelseRestTjeneste(vurdertForeldelseTjenesteMock, kravgrunnlagBeregningTjenesteMock, behandlingTjeneste);

    @Test
    public void skal_kalle_hentLogiskePerioder() {
        restTjeneste.hentLogiskePerioder(idDto);

        verify(vurdertForeldelseTjenesteMock).hentFaktaPerioder(behandlingId);
    }

    @Test
    public void skal_kalle_henteVurdertPerioder() {
        restTjeneste.hentVurdertPerioder(idDto);

        verify(vurdertForeldelseTjenesteMock).henteVurdertForeldelse(behandlingId);
    }

    @Test
    public void skal_få_med_feilutbetalt_beløp() {
        ForeldelsePeriodeMedBeløpDto foreldelsePeriodeMedBeløpDto = new ForeldelsePeriodeMedBeløpDto();
        foreldelsePeriodeMedBeløpDto.setFom(LocalDate.now().minusDays(10));
        foreldelsePeriodeMedBeløpDto.setTom(LocalDate.now().minusDays(2));
        foreldelsePeriodeMedBeløpDto.setBelop(BigDecimal.valueOf(5000));
        foreldelsePeriodeMedBeløpDto.setForeldelseVurderingType(ForeldelseVurderingType.IKKE_FORELDET);
        foreldelsePeriodeMedBeløpDto.setBegrunnelse("Begrunnelse");

        FeilutbetalingPerioderDto dto = new FeilutbetalingPerioderDto();
        dto.setPerioder(Collections.singletonList(foreldelsePeriodeMedBeløpDto));
        dto.setBehandlingId(1000L);

        Map<Periode, BigDecimal> feilutbetaltePerioder = Map.of(Periode.of(foreldelsePeriodeMedBeløpDto.getFom(), foreldelsePeriodeMedBeløpDto.getTom()), BigDecimal.valueOf(3999));
        Mockito.when(kravgrunnlagBeregningTjenesteMock.beregnFeilutbetaltBeløp(Mockito.anyLong(), Mockito.any())).thenReturn(feilutbetaltePerioder);

        FeilutbetalingPerioderDto resultat = restTjeneste.beregnBeløp(dto);

        assertThat(resultat.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(3999));
    }

}
