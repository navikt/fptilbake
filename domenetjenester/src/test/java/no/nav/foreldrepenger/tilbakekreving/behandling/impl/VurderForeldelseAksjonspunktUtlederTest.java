package no.nav.foreldrepenger.tilbakekreving.behandling.impl;


import java.time.LocalDate;
import java.time.Period;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class VurderForeldelseAksjonspunktUtlederTest {

    VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder = new VurderForeldelseAksjonspunktUtleder(Period.ofMonths(30), null, null);

    @Test
    void skalVæreForeldet() {
        LocalDate dagensDato = LocalDate.of(2019, 1, 1);
        LocalDate fradato = LocalDate.of(2016, 1, 1);

        boolean foreldet = vurderForeldelseAksjonspunktUtleder.erForeldet(dagensDato, fradato);
        Assertions.assertThat(foreldet).isTrue();
    }


    @Test
    void skalVæreForeldetDelvis() {
        LocalDate dagensDato = LocalDate.of(2019, 1, 1);
        LocalDate fradato = LocalDate.of(2016, 1, 1);

        boolean foreldet = vurderForeldelseAksjonspunktUtleder.erForeldet(dagensDato, fradato);
        Assertions.assertThat(foreldet).isTrue();
    }


    @Test
    void skalIkkeVæreForeldet() {
        LocalDate dagensDato = LocalDate.of(2019, 1, 1);
        LocalDate fradato = LocalDate.of(2018, 1, 1);

        boolean foreldet = vurderForeldelseAksjonspunktUtleder.erForeldet(dagensDato, fradato);
        Assertions.assertThat(foreldet).isFalse();
    }

}
