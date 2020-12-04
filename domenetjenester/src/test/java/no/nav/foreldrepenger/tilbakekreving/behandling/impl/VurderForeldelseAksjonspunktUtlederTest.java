package no.nav.foreldrepenger.tilbakekreving.behandling.impl;


import java.time.LocalDate;
import java.time.Period;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VurderForeldelseAksjonspunktUtlederTest {

    VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder = new VurderForeldelseAksjonspunktUtleder(Period.ofMonths(30), null, null);

    @Test
    public void skalVæreForeldet() {
        LocalDate dagensDato = LocalDate.of(2019, 1, 1);
        LocalDate fradato = LocalDate.of(2016, 1, 1);

        boolean foreldet = vurderForeldelseAksjonspunktUtleder.erForeldet(dagensDato, fradato);
        Assertions.assertThat(foreldet).isTrue();
    }


    @Test
    public void skalVæreForeldetDelvis() {
        LocalDate dagensDato = LocalDate.of(2019, 1, 1);
        LocalDate fradato = LocalDate.of(2016, 1, 1);

        boolean foreldet = vurderForeldelseAksjonspunktUtleder.erForeldet(dagensDato, fradato);
        Assertions.assertThat(foreldet).isTrue();
    }


    @Test
    public void skalIkkeVæreForeldet() {
        LocalDate dagensDato = LocalDate.of(2019, 1, 1);
        LocalDate fradato = LocalDate.of(2018, 1, 1);

        boolean foreldet = vurderForeldelseAksjonspunktUtleder.erForeldet(dagensDato, fradato);
        Assertions.assertThat(foreldet).isFalse();
    }

}
