package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.AvklartVergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeDto;
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
        avklartVergeTjeneste.lagreVergeInformasjon(behandling, lagVergeDto(avklartVergeDto));
    }

    private VergeDto lagVergeDto(AvklartVergeDto avklartVergeDto) {
        VergeDto vergeDto = new VergeDto();
        vergeDto.setFom(avklartVergeDto.getFom());
        vergeDto.setTom(avklartVergeDto.getTom());
        vergeDto.setFnr(avklartVergeDto.getFnr());
        vergeDto.setOrganisasjonsnummer(avklartVergeDto.getOrganisasjonsnummer());
        vergeDto.setNavn(avklartVergeDto.getNavn());
        vergeDto.setBegrunnelse(avklartVergeDto.getBegrunnelse());
        vergeDto.setVergeType(avklartVergeDto.getVergeType());
        return vergeDto;
    }
}
