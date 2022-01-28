package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;

@ApplicationScoped
public class AutomatiskVurdertForeldelseTjeneste {

    private VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder;
    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;

    AutomatiskVurdertForeldelseTjeneste() {
        // for CDI
    }

    @Inject
    public AutomatiskVurdertForeldelseTjeneste(VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder,
                                               VurdertForeldelseTjeneste vurdertForeldelseTjeneste) {
        this.vurderForeldelseAksjonspunktUtleder = vurderForeldelseAksjonspunktUtleder;
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
    }

    public void automatiskVurdetForeldelse(Behandling behandling, String begrunnelse) {
        long behandlingId = behandling.getId();
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = vurdertForeldelseTjeneste.hentFaktaPerioder(behandlingId);
        List<ForeldelsePeriodeDto> foreldelsePerioder = feilutbetalingPerioderDto.getPerioder().stream()
                .filter(periode -> vurderForeldelseAksjonspunktUtleder.erForeldet(LocalDate.now(), periode.getFom()))
                .map(periode -> new ForeldelsePeriodeDto(periode.getFom(), periode.getTom(), ForeldelseVurderingType.IKKE_FORELDET, begrunnelse))
                .collect(Collectors.toList());
        if (!foreldelsePerioder.isEmpty()) {
            vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(behandlingId, foreldelsePerioder);
        }
    }
}
