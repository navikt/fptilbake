package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VedtakAksjonspunktData;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.FatteVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.FatteVedtakDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FatteVedtakDto.class, adapter = AksjonspunktOppdaterer.class)
public class FatteVedtakOppdaterer implements AksjonspunktOppdaterer<FatteVedtakDto> {

    private FatteVedtakTjeneste fatteVedtakTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;

    public FatteVedtakOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FatteVedtakOppdaterer(BehandlingRepositoryProvider repositoryProvider, FatteVedtakTjeneste fatteVedtakTjeneste) {
        this.fatteVedtakTjeneste = fatteVedtakTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public void oppdater(FatteVedtakDto dto, Behandling behandling) {
        Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtoList = dto.getAksjonspunktGodkjenningDtos();

        Set<VedtakAksjonspunktData> aksjonspunkter = aksjonspunktGodkjenningDtoList.stream()
                .map(a -> {
                    // map til VedtakAksjonsonspunktData fra DTO
                    AksjonspunktDefinisjon aksDef = aksjonspunktRepository.finnAksjonspunktDefinisjon(a.getAksjonspunktKode());
                    return new VedtakAksjonspunktData(aksDef, a.isGodkjent(), a.getBegrunnelse(), fraDto(a.getArsaker()));
                })
                .collect(Collectors.toSet());

        fatteVedtakTjeneste.opprettTotrinnsVurdering(behandling, aksjonspunkter);
    }

    private Collection<String> fraDto(Collection<VurderÅrsak> arsaker) {
        if (arsaker == null) {
            return Collections.emptySet();
        }
        return arsaker.stream().map(Kodeliste::getKode).collect(Collectors.toSet());
    }
}
