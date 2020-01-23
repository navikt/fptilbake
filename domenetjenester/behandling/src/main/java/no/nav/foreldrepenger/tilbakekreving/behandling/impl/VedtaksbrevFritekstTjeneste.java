package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;

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

    public void lagreFriteksterFraSaksbehandler(Long behandlingId, VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering, List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, vedtaksbrevFritekstPerioder);

        vedtaksbrevFritekstRepository.slettOppsummering(behandlingId);
        vedtaksbrevFritekstRepository.slettPerioderMedFritekster(behandlingId);

        vedtaksbrevFritekstRepository.lagreVedtakPerioderOgTekster(vedtaksbrevFritekstPerioder);
        if (vedtaksbrevFritekstOppsummering != null) {
            vedtaksbrevFritekstRepository.lagreVedtaksbrevOppsummering(vedtaksbrevFritekstOppsummering);
        }
    }

}
