package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.AvklartVergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AvklartVergeDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklartVergeDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklartVergeOppdaterer implements AksjonspunktOppdaterer<AvklartVergeDto> {

    private AvklartVergeTjeneste avklartVergeTjeneste;

    @Inject
    public AvklartVergeOppdaterer(AvklartVergeTjeneste avklartVergeTjeneste) {
        this.avklartVergeTjeneste = avklartVergeTjeneste;
    }

    @Override
    public void oppdater(AvklartVergeDto avklartVergeDto, Behandling behandling) {
        avklartVergeTjeneste.lagreVergeInformasjon(behandling.getId(), avklartVergeDto.getVergeFakta());
    }
}
