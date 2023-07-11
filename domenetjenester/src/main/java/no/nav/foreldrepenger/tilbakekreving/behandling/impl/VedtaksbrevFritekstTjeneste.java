package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;

@ApplicationScoped
public class VedtaksbrevFritekstTjeneste {

    private VedtaksbrevFritekstValidator validator;
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;

    VedtaksbrevFritekstTjeneste() {
        //for CDI proxy
    }

    @Inject
    public VedtaksbrevFritekstTjeneste(VedtaksbrevFritekstValidator validator, VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository) {
        this.validator = validator;
        this.vedtaksbrevFritekstRepository = vedtaksbrevFritekstRepository;
    }

    public void lagreFriteksterFraSaksbehandler(Long behandlingId,
                                                VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering,
                                                List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder,
                                                VedtaksbrevType brevType) {
        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, vedtaksbrevFritekstPerioder, vedtaksbrevFritekstOppsummering, brevType);

        vedtaksbrevFritekstRepository.slettOppsummering(behandlingId);
        vedtaksbrevFritekstRepository.slettPerioderMedFritekster(behandlingId);

        vedtaksbrevFritekstRepository.lagreVedtakPerioderOgTekster(vedtaksbrevFritekstPerioder);
        if (vedtaksbrevFritekstOppsummering != null) {
            vedtaksbrevFritekstRepository.lagreVedtaksbrevOppsummering(vedtaksbrevFritekstOppsummering);
        }
    }

}
