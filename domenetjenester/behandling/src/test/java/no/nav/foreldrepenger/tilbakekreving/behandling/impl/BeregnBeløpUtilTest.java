package no.nav.foreldrepenger.tilbakekreving.behandling.impl;


import java.math.BigDecimal;
import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class BeregnBeløpUtilTest {

    @Test
    public void skal_normalt_ikke_telle_helgedager_som_overlapper_mellom_kravgrunnlag_og_periode() {
        BeregnBeløpUtil beløpUtil = BeregnBeløpUtil.forFagområde(FagOmrådeKode.FORELDREPENGER);

        LocalDate mandag = LocalDate.of(2020, 9, 7);
        LocalDate lørdag = mandag.plusDays(5);
        LocalDate søndag = lørdag.plusDays(1);
        LocalDate nesteSøndag = lørdag.plusDays(1);

        Periode kravgrunnlagPeriode = Periode.of(mandag, søndag);
        Periode vilkårPeriode = Periode.of(lørdag, nesteSøndag);

        BigDecimal feilPerDag = BigDecimal.valueOf(100);
        BigDecimal resultat = beløpUtil.beregnBeløp(kravgrunnlagPeriode, vilkårPeriode, feilPerDag);

        Assertions.assertThat(resultat).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_telle_engangstønad_selv_om_den_er_i_helg() {
        BeregnBeløpUtil beløpUtil = BeregnBeløpUtil.forFagområde(FagOmrådeKode.ENGANGSSTØNAD);

        LocalDate mandag = LocalDate.of(2020, 9, 7);
        LocalDate lørdag = mandag.plusDays(5);

        Periode kravgrunnlagPeriode = Periode.of(lørdag, lørdag);
        Periode vilkårPeriode = Periode.of(lørdag, lørdag);

        BigDecimal feilPerDag = BigDecimal.valueOf(100000);
        BigDecimal resultat = beløpUtil.beregnBeløp(kravgrunnlagPeriode, vilkårPeriode, feilPerDag);

        Assertions.assertThat(resultat).isEqualByComparingTo(BigDecimal.valueOf(100000));
    }
}
