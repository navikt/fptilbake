package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;

@ApplicationScoped
public class KravVedtakStatusMapper {

    private PersonOrganisasjonWrapper tpsAdapterWrapper;

    KravVedtakStatusMapper() {
        // for CDI proxy
    }

    @Inject
    public KravVedtakStatusMapper(PersonOrganisasjonWrapper tpsAdapterWrapper) {
        this.tpsAdapterWrapper = tpsAdapterWrapper;
    }

    public KravVedtakStatus437 mapTilDomene(KravOgVedtakstatus kravOgVedtakstatus) {
        GjelderType gjelderType = GjelderType.fraKode(kravOgVedtakstatus.getTypeGjelderId().value());
        return KravVedtakStatus437.builder()
                .medKravStatusKode(KravStatusKode.fraKode(kravOgVedtakstatus.getKodeStatusKrav()))
                .medFagomraadeKode(FagOmrådeKode.fraKode(kravOgVedtakstatus.getKodeFagomraade()))
                .medVedtakId(kravOgVedtakstatus.getVedtakId().longValue())
                .medFagSystemId(kravOgVedtakstatus.getFagsystemId())
                .medReferanse(new Henvisning(kravOgVedtakstatus.getReferanse()))
                .medGjelderType(gjelderType)
                .medGjelderVedtakId(tpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(kravOgVedtakstatus.getVedtakGjelderId(), gjelderType))
                .build();
    }
}
