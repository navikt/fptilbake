package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;

public interface ØkonomiConsumer {

    TilbakekrevingsvedtakResponse iverksettTilbakekrevingsvedtak(Long behandlingId, TilbakekrevingsvedtakRequest vedtak);

    DetaljertKravgrunnlagDto hentKravgrunnlag(Long behandlingId, HentKravgrunnlagDetaljDto kravgrunnlagDetalj);

    MmelDto anullereKravgrunnlag(Long behandlingId, AnnullerKravgrunnlagDto annullerKravgrunnlag);
}
