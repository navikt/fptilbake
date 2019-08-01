package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.TittelOverskriftUtil.finnOverskriftVarselbrev;

public class TittelOverskriftUtilTest {

    @Test
    public void skal_finne_overskrift_på_riktig_språk() {
        Assertions.assertThat(finnOverskriftVarselbrev("foreldrepenger")).isEqualTo("NAV vurderer om du må betale tilbake foreldrepenger");
        Assertions.assertThat(finnOverskriftVarselbrev("foreldrepengar")).isEqualTo("NAV vurderer om du må betale tilbake foreldrepengar");
        Assertions.assertThat(finnOverskriftVarselbrev("engangsstønad")).isEqualTo("NAV vurderer om du må betale tilbake engangsstønad");
        Assertions.assertThat(finnOverskriftVarselbrev("eingongsstønad")).isEqualTo("NAV vurderer om du må betale tilbake eingongsstønad");
        Assertions.assertThat(finnOverskriftVarselbrev("svangerskapspenger")).isEqualTo("NAV vurderer om du må betale tilbake svangerskapspenger");
        Assertions.assertThat(finnOverskriftVarselbrev("svangerskapspengar")).isEqualTo("NAV vurderer om du må betale tilbake svangerskapspengar");
    }

}
