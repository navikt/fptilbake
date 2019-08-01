package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

public interface ØkonomiConsumer {
    void iverksettTilbakekrevingsvedtak(Long behandlingId, TilbakekrevingsvedtakDto vedtak);

    DetaljertKravgrunnlagDto hentKravgrunnlag(Long behandlingId, HentKravgrunnlagDetaljDto kravgrunnlagDetalj);
}
