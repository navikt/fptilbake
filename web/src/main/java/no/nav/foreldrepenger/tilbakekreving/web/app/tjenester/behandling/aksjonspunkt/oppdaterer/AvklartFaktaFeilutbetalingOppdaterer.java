package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.AvklartFaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AvklartFaktaFeilutbetalingDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklartFaktaFeilutbetalingDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklartFaktaFeilutbetalingOppdaterer implements AksjonspunktOppdaterer<AvklartFaktaFeilutbetalingDto> {

    private AvklartFaktaFeilutbetalingTjeneste avklartFaktaFeilUtbetalingTjeneste;

    @Inject
    public AvklartFaktaFeilutbetalingOppdaterer(AvklartFaktaFeilutbetalingTjeneste avklartFaktaFeilUtbetalingTjeneste) {
        this.avklartFaktaFeilUtbetalingTjeneste = avklartFaktaFeilUtbetalingTjeneste;
    }

    @Override
    public void oppdater(AvklartFaktaFeilutbetalingDto dto, Behandling behandling) {
        if (!dto.getFeilutbetalingFakta().isEmpty()) {
            avklartFaktaFeilUtbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(behandling,
                    dto.getFeilutbetalingFakta(), dto.getBegrunnelse());
        }
    }
}
