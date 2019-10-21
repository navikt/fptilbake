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

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.PeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;

public class ForeldelseRestTjenesteTest {

    private static final long behandlingId = 123456;
    private static final BehandlingIdDto idDto = new BehandlingIdDto(behandlingId);

    private VurdertForeldelseTjeneste vurdertForeldelseTjenesteMock = mock(VurdertForeldelseTjeneste.class);

    private ForeldelseRestTjeneste restTjeneste = new ForeldelseRestTjeneste(vurdertForeldelseTjenesteMock);

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
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setFom(LocalDate.now().minusDays(10));
        periodeDto.setTom(LocalDate.now().minusDays(2));
        periodeDto.setBelop(BigDecimal.valueOf(5000));
        periodeDto.setForeldelseVurderingType(ForeldelseVurderingType.IKKE_FORELDET);
        periodeDto.setBegrunnelse("Begrunnelse");

        FeilutbetalingPerioderDto dto = new FeilutbetalingPerioderDto();
        dto.setPerioder(Collections.singletonList(periodeDto));
        dto.setBehandlingId(1000L);

        Map<Periode, BigDecimal> feilutbetaltePerioder = Map.of(Periode.of(periodeDto.getFom(), periodeDto.getTom()), BigDecimal.valueOf(3999));
        Mockito.when(vurdertForeldelseTjenesteMock.beregnFeilutbetaltBeløpForPerioder(Mockito.anyLong(), Mockito.any())).thenReturn(feilutbetaltePerioder);

        FeilutbetalingPerioderDto resultat = restTjeneste.beregnBeløp(dto);

        assertThat(resultat.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(3999));
    }

}
