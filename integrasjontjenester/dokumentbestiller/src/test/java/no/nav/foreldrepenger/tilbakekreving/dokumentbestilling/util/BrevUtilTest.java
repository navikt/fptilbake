package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevUtil.konverterFraLocaldateTilTekst;

public class BrevUtilTest {

    @Test
    public void skal_finne_riktig_navn_på_fagsaktype() {
        KodelisteNavnI18N engangsstønadPåBokmål = new KodelisteNavnI18N("NB", "engangsstønad");
        KodelisteNavnI18N engangsstønadPåNynorsk = new KodelisteNavnI18N("NN", "eingongsstønad");
        String fagsaktypeNavn = BrevUtil.finnFagsaktypenavnPåAngittSpråk(
            List.of(engangsstønadPåBokmål, engangsstønadPåNynorsk), Språkkode.nn);
        Assertions.assertThat(fagsaktypeNavn).isEqualTo("eingongsstønad");
    }

    @Test
    public void skal_defaulte_til_bokmål_dersom_brukeren_ikke_har_registrert_foretrukket_språk() {
        KodelisteNavnI18N svangerskapspengerPåBokmål = new KodelisteNavnI18N("NB", "svangerskapspenger");
        KodelisteNavnI18N svangerskapspengerPåNynorsk = new KodelisteNavnI18N("NN", "svangerskapspengar");
        String fagsaktypeNavn = BrevUtil.finnFagsaktypenavnPåAngittSpråk(
            List.of(svangerskapspengerPåNynorsk, svangerskapspengerPåBokmål), Språkkode.UDEFINERT);
        Assertions.assertThat(fagsaktypeNavn).isEqualTo("svangerskapspenger");
    }

    @Test
    public void skal_konvertere_dato_til_fin_tekst() {
        Assertions.assertThat(konverterFraLocaldateTilTekst(LocalDate.of(2020, 3, 4))).isEqualTo("4. mars 2020");
    }
}
