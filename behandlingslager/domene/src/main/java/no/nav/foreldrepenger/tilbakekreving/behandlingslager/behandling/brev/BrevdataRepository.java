package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.List;
import java.util.Optional;

public interface BrevdataRepository {

    void lagreVarselbrevData(VarselbrevSporing varselbrevSporing);

    void lagreVedtaksbrevData(VedtaksbrevSporing vedtaksbrevSporing);

    void lagreVedtakPerioderOgTekster(List<VedtaksbrevPeriode> vedtaksbrevPerioder);

    void lagreVedtaksbrevOppsummering(VedtaksbrevOppsummering vedtaksbrevOppsummering);

    List<VarselbrevSporing> hentVarselbrevData(Long behandlingId);

    List<VedtaksbrevSporing> hentVedtaksbrevData(Long behandlingId);

    Optional<VedtaksbrevOppsummering> hentVedtaksbrevOppsummering(Long behandlingId);

    List<VedtaksbrevPeriode> hentVedtaksbrevPerioderMedTekst(Long behandlingId);

    void slettOppsummering(Long behandlingId);

    void slettPerioderMedFritekster(Long behandlingId);
}
