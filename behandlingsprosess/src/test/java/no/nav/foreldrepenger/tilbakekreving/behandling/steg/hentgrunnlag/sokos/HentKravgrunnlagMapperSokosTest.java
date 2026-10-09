package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.sokos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerResponse;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class HentKravgrunnlagMapperSokosTest {

    private static final String IDENT = "12345678901";

    @BeforeEach
    void setup() {
        System.setProperty("app.name", "fptilbake");
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("app.name");
    }

    @Test
    void mapper_detaljer_til_domene_uten_positiv_ytelse() {
        var personinfo = mock(PersoninfoAdapter.class);
        when(personinfo.hentAktørForFnr(PersonIdent.fra(IDENT))).thenReturn(Optional.of(new AktørId(999999L)));
        var mapper = new HentKravgrunnlagMapperSokos(new PersonOrganisasjonWrapper(personinfo));
        var respons = DefaultJsonMapper.fromJson("""
            {
              "status": 0, "melding": "OK",
              "kravgrunnlag": {
                "kravgrunnlagId": 570598, "vedtakId": 851138, "kodeStatusKrav": "NY",
                "kodeFagomraade": "FP", "fagsystemId": "152465243100",
                "datoVedtakFagsystem": "2025-10-28", "vedtakIdOmgjort": 0,
                "gjelderId": "12345678901", "typeGjelder": "PERSON",
                "utbetalesTilId": "12345678901", "typeUtbetalesTilId": "PERSON",
                "kodeHjemmel": "", "renterBeregnes": false,
                "enhetAnsvarlig": "8020", "enhetBosted": "0219", "enhetBehandl": "8020",
                "kontrollfelt": "2025-10-28-22.57.21.466798", "saksbehandlerId": "K231B433",
                "referanse": "0123456789", "perioder": [{
                  "periodeFom": "2025-01-01", "periodeTom": "2025-01-31",
                  "belopSkattMnd": 125.50, "posteringer": [
                    {
                      "kodeKlasse": "FPATORD", "typeKlasse": "YTEL",
                      "belopOpprinneligUtbetalt": 1000, "belopNy": 500,
                      "belopTilbakekreves": 500, "belopUinnkrevd": 0, "skattProsent": 25,
                      "kodeResultat": "FULL_TILBAKEKREV", "kodeAarsak": "ANNET", "kodeSkyld": "NAV"
                    },
                    {
                      "kodeKlasse": "FPATORD", "typeKlasse": "YTEL",
                      "belopOpprinneligUtbetalt": 500, "belopNy": 1000,
                      "belopTilbakekreves": 0, "belopUinnkrevd": 0, "skattProsent": 0,
                      "kodeResultat": "", "kodeAarsak": "", "kodeSkyld": ""
                    },
                    {
                      "kodeKlasse": "X-SKAT", "typeKlasse": "SKAT",
                      "belopOpprinneligUtbetalt": 0, "belopNy": 0,
                      "belopTilbakekreves": 125.50, "belopUinnkrevd": 0, "skattProsent": 25,
                      "kodeResultat": "", "kodeAarsak": "", "kodeSkyld": ""
                    }
                  ]
                }]
              }
            }
            """, HentKravgrunnlagDetaljerResponse.class);

        var kravgrunnlag = mapper.mapTilDomene(respons.kravgrunnlag());

        assertThat(kravgrunnlag.getEksternKravgrunnlagId()).isEqualTo("570598");
        assertThat(kravgrunnlag.getVedtakId()).isEqualTo(851138L);
        assertThat(kravgrunnlag.getFagOmrådeKode().getKode()).isEqualTo("FP");
        assertThat(kravgrunnlag.getOmgjortVedtakId()).isNull();
        assertThat(kravgrunnlag.getHjemmelKode()).isNull();
        assertThat(kravgrunnlag.getBeregnesRenter()).isEqualTo("N");
        assertThat(kravgrunnlag.getGjelderVedtakId()).isEqualTo("999999");
        assertThat(kravgrunnlag.getUtbetalesTilId()).isEqualTo("999999");
        assertThat(kravgrunnlag.getAnsvarligEnhet()).isEqualTo("8020");
        assertThat(kravgrunnlag.getReferanse().getVerdi()).isEqualTo("0123456789");
        var periode = kravgrunnlag.getPerioder().getFirst();
        assertThat(periode.getPeriode().getFom()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(periode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(125.50));
        assertThat(periode.getKravgrunnlagBeloper433()).hasSize(2);
        assertThat(periode.getKravgrunnlagBeloper433()).anySatisfy(beløp -> {
            assertThat(beløp.getKlasseType()).isEqualTo(KlasseType.YTEL);
            assertThat(beløp.getKlasseKode()).isEqualTo("FPATORD");
            assertThat(beløp.getOpprUtbetBelop()).isEqualByComparingTo("1000");
            assertThat(beløp.getResultatKode()).isEqualTo("FULL_TILBAKEKREV");
        });
        assertThat(periode.getKravgrunnlagBeloper433()).anySatisfy(beløp -> {
            assertThat(beløp.getKlasseType()).isEqualTo(KlasseType.SKAT);
            assertThat(beløp.getKlasseKode()).isEqualTo("X-SKAT");
            assertThat(beløp.getTilbakekrevesBelop()).isEqualByComparingTo("125.50");
        });
    }
}
