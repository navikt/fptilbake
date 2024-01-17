package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VedtaksbrevFritekstTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(VedtaksbrevFritekstTjeneste.class);

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
        LOG.info("Behandling: {}, lagrer fritekster for {}, med oppsummering {} lang og {} perioder.", behandlingId, brevType,
            (vedtaksbrevFritekstOppsummering != null ? vedtaksbrevFritekstOppsummering.getOppsummeringFritekst().length() : "null"),
            vedtaksbrevFritekstPerioder.size());

        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, vedtaksbrevFritekstPerioder, vedtaksbrevFritekstOppsummering, brevType);

        vedtaksbrevFritekstRepository.slettOppsummering(behandlingId);
        vedtaksbrevFritekstRepository.slettPerioderMedFritekster(behandlingId);

        vedtaksbrevFritekstRepository.lagreVedtakPerioderOgTekster(vedtaksbrevFritekstPerioder);
        if (vedtaksbrevFritekstOppsummering != null) {
            vedtaksbrevFritekstRepository.lagreVedtaksbrevOppsummering(vedtaksbrevFritekstOppsummering);
        }
    }

}
