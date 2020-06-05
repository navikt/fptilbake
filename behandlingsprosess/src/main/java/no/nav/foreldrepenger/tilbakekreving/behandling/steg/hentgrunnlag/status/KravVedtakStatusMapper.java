package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TpsAdapterWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;

@ApplicationScoped
public class KravVedtakStatusMapper {

    private TpsAdapterWrapper tpsAdapterWrapper;

    KravVedtakStatusMapper() {
        // for CDI proxy
    }

    @Inject
    public KravVedtakStatusMapper(TpsAdapterWrapper tpsAdapterWrapper) {
        this.tpsAdapterWrapper = tpsAdapterWrapper;
    }

    public Henvisning finnHenvisning(KravOgVedtakstatus kravOgVedtakstatus) {
        return new Henvisning(kravOgVedtakstatus.getReferanse());
    }

    public Long finnVedtakId(KravOgVedtakstatus kravOgVedtakstatus) {
        return kravOgVedtakstatus.getVedtakId().longValue();
    }

    public KravVedtakStatus437 mapTilDomene(KravOgVedtakstatus kravOgVedtakstatus) {
        GjelderType gjelderType = GjelderType.fraKode(kravOgVedtakstatus.getTypeGjelderId().value());
        return KravVedtakStatus437.builder()
            .medKravStatusKode(KravStatusKode.fraKode(kravOgVedtakstatus.getKodeStatusKrav()))
            .medFagomraadeKode(FagOmrådeKode.fraKode(kravOgVedtakstatus.getKodeFagomraade()))
            .medVedtakId(kravOgVedtakstatus.getVedtakId().longValue())
            .medFagSystemId(kravOgVedtakstatus.getFagsystemId())
            .medReferanse(kravOgVedtakstatus.getReferanse())
            .medGjelderType(gjelderType)
            .medGjelderVedtakId(tpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(kravOgVedtakstatus.getVedtakGjelderId(), gjelderType)).build();
    }
}
