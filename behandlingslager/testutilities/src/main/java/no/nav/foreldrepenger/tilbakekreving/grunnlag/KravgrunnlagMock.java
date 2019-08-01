package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

// Klasse brukte bare for å teste grunnlag
public class KravgrunnlagMock {

    private Periode periode;
    private KlasseType klasseType;
    private BigDecimal nyBelop;
    private BigDecimal tilbakekrevesBelop;
    private KlasseKode klasseKode;

    public KravgrunnlagMock(LocalDate fom, LocalDate tom, KlasseType klasseType, BigDecimal nyBelop, BigDecimal tilbakekrevesBelop) {
        this.periode = Periode.of(fom, tom);
        this.klasseType = klasseType;
        this.nyBelop = nyBelop;
        this.tilbakekrevesBelop = tilbakekrevesBelop;
    }

    public Periode getPeriode() {
        return periode;
    }

    public KlasseType getKlasseType() {
        return klasseType;
    }

    public BigDecimal getNyBelop() {
        return nyBelop;
    }

    public BigDecimal getTilbakekrevesBelop() {
        return tilbakekrevesBelop;
    }

    public KlasseKode getKlasseKode() {
        return klasseKode;
    }

    public void setKlasseKode(KlasseKode klasseKode) {
        this.klasseKode = klasseKode;
    }
}
