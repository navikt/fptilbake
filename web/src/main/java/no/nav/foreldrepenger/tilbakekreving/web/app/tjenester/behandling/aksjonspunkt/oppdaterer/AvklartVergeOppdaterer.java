package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.OpprettVergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.dto.OpprettVergeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarVergeDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarVergeDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklartVergeOppdaterer implements AksjonspunktOppdaterer<AvklarVergeDto> {

    private OpprettVergeTjeneste opprettVergeTjeneste;

    @Inject
    public AvklartVergeOppdaterer(OpprettVergeTjeneste opprettVergeTjeneste) {
        this.opprettVergeTjeneste = opprettVergeTjeneste;
    }

    @Override
    public void oppdater(AvklarVergeDto avklarVergeDto, Behandling behandling) {
        opprettVergeTjeneste.opprettVerge(behandling.getId(), behandling.getFagsakId(), map(avklarVergeDto));
    }

    private OpprettVergeDto map(AvklarVergeDto dto) {
        return new OpprettVergeDto(
                dto.getNavn(),
                dto.getFnr(),
                dto.getGyldigFom(),
                dto.getGyldigTom(),
                dto.getVergeType(),
                dto.getOrganisasjonsnummer(),
                dto.getBegrunnelse()
        );
    }
}
