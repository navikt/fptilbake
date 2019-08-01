package no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

public class KravgrunnlagTestBuilder {

    private KravgrunnlagRepository kravgrunnlagRepository;

    public KravgrunnlagTestBuilder(KravgrunnlagRepository kravgrunnlagRepository) {
        this.kravgrunnlagRepository = kravgrunnlagRepository;
    }

    public static KravgrunnlagTestBuilder medRepo(KravgrunnlagRepository repo) {
        return new KravgrunnlagTestBuilder(repo);
    }

    public static class KgBeløp {
        private KlasseType klasseType;
        private String klassekode;
        private int beløpNytt;
        private int utbetaltBeløp;
        private int tilbakekrevBeløp;

        public KgBeløp(KlasseType klasseType) {
            this.klasseType = klasseType;
        }

        public static KgBeløp feil(int beløp) {
            return feil(beløp, "foobar");
        }

        public static KgBeløp feil(int beløp, String klassekode) {
            KgBeløp b = new KgBeløp(KlasseType.FEIL);
            b.beløpNytt = beløp;
            b.klassekode = klassekode;
            return b;
        }

        public static KgBeløp ytelse(KlasseKode klasseKode) {
            KgBeløp b = new KgBeløp(KlasseType.YTEL);
            b.klassekode = klasseKode.getKode();
            return b;
        }

        public static KgBeløp trekk(int beløp) {
            return trekk(beløp, "foobaz");
        }

        public static KgBeløp trekk(int beløp, String klassekode) {
            KgBeløp b = new KgBeløp(KlasseType.TREK);
            b.beløpNytt = beløp;
            b.klassekode = klassekode;
            return b;
        }

        public KgBeløp medKlasse(String klassekode) {
            this.klassekode = klassekode;
            return this;
        }

        public KgBeløp medUtbetBeløp(int utbetaltBeløp) {
            this.utbetaltBeløp = utbetaltBeløp;
            return this;
        }

        public KgBeløp medTilbakekrevBeløp(int tilbakekrevBeløp) {
            this.tilbakekrevBeløp = tilbakekrevBeløp;
            return this;
        }

        public KgBeløp medNyttBeløp(int nyttBeløp) {
            this.beløpNytt = nyttBeløp;
            return this;
        }

        public KravgrunnlagBelop433 mapTilØkonomi(KravgrunnlagPeriode432 periode) {
            KravgrunnlagBelop433.Builder builder = KravgrunnlagBelop433.builder();
            builder.medKlasseType(klasseType);
            builder.medKlasseKode(klassekode);
            builder.medNyBelop(BigDecimal.valueOf(beløpNytt));
            builder.medKravgrunnlagPeriode432(periode);
            builder.medOpprUtbetBelop(BigDecimal.valueOf(utbetaltBeløp));
            builder.medTilbakekrevesBelop(BigDecimal.valueOf(tilbakekrevBeløp));
            return builder.build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KgBeløp kgBeløp = (KgBeløp) o;
            return beløpNytt == kgBeløp.beløpNytt &&
                utbetaltBeløp == kgBeløp.utbetaltBeløp &&
                tilbakekrevBeløp == kgBeløp.tilbakekrevBeløp &&
                Objects.equals(klasseType, kgBeløp.klasseType) &&
                Objects.equals(klassekode, kgBeløp.klassekode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(klasseType, klassekode, beløpNytt, utbetaltBeløp, tilbakekrevBeløp);
        }
    }

    public Kravgrunnlag431 lagreKravgrunnlag(Long behandlingId, Map<Periode, List<KgBeløp>> beløp) {
        Kravgrunnlag431 kg = lagKravgrunnlag(beløp);
        kravgrunnlagRepository.lagre(behandlingId, kg);
        return kg;
    }

    public static Kravgrunnlag431 lagKravgrunnlag(Map<Periode, List<KgBeløp>> beløp) {
        Kravgrunnlag431 kg = lagKravgrunnlag();
        for (Map.Entry<Periode, List<KgBeløp>> entry : beløp.entrySet()) {
            KravgrunnlagPeriode432 kgPeriode = new KravgrunnlagPeriode432.Builder().medPeriode(entry.getKey()).medKravgrunnlag431(kg).build();
            for (KgBeløp kgBeløp : entry.getValue()) {
                kgPeriode.leggTilBeløp(kgBeløp.mapTilØkonomi(kgPeriode));
            }
            kg.leggTilPeriode(kgPeriode);
        }
        return kg;
    }

    private static Kravgrunnlag431 lagKravgrunnlag() {
        Long eksternBehandlingId = 1000000L;
        return new Kravgrunnlag431.Builder()
            .medEksternKravgrunnlagId("12341")
            .medFagSystemId("GSAKNR-12312")
            .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
            .medKravStatusKode(KravStatusKode.NYTT)
            .medVedtakId(1412L)
            .medAnsvarligEnhet("8020")
            .medBehandlendeEnhet("8020")
            .medBostedEnhet("8020")
            .medFeltKontroll("kontrollfelt-123")
            .medGjelderType(GjelderType.PERSON)
            .medGjelderVedtakId("???")
            .medSaksBehId("Z111111")
            .medReferanse(Long.toString(eksternBehandlingId))
            .medUtbetalesTilId("99999999999")
            .medUtbetIdType(GjelderType.PERSON)
            .build();
    }

}
