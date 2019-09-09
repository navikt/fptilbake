package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevSpråkUtil;

public class BrevSpråkUtilTest {

    @Test
    public void skal_finne_riktig_navn_på_fagsaktype() {
        KodelisteNavnI18N engangsstønadPåBokmål = new KodelisteNavnI18N("NB", "engangsstønad");
        KodelisteNavnI18N engangsstønadPåNynorsk = new KodelisteNavnI18N("NN", "eingongsstønad");
        String fagsaktypeNavn = BrevSpråkUtil.finnFagsaktypenavnPåAngittSpråk(
            List.of(engangsstønadPåBokmål, engangsstønadPåNynorsk), Språkkode.nn);
        Assertions.assertThat(fagsaktypeNavn).isEqualTo("eingongsstønad");
    }

    @Test
    public void skal_defaulte_til_bokmål_dersom_brukeren_ikke_har_registrert_foretrukket_språk() {
        KodelisteNavnI18N svangerskapspengerPåBokmål = new KodelisteNavnI18N("NB", "svangerskapspenger");
        KodelisteNavnI18N svangerskapspengerPåNynorsk = new KodelisteNavnI18N("NN", "svangerskapspengar");
        String fagsaktypeNavn = BrevSpråkUtil.finnFagsaktypenavnPåAngittSpråk(
            List.of(svangerskapspengerPåNynorsk, svangerskapspengerPåBokmål), Språkkode.UDEFINERT);
        Assertions.assertThat(fagsaktypeNavn).isEqualTo("svangerskapspenger");
    }

}
