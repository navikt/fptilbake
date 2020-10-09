package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.BaseDokument;

public class FritekstVedtaksbrevDokument extends BaseDokument {

    @JsonProperty("hovedresultat")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private VedtakResultatType hovedresultat;

    public VedtakResultatType getHovedresultat() {
        return hovedresultat;
    }

    public void setHovedresultat(VedtakResultatType hovedresultat) {
        this.hovedresultat = hovedresultat;
    }
}
