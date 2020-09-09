package no.nav.foreldrepenger.tilbakekreving.behandling;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public interface VurderForeldelseAksjonspunktUtleder {

    Optional<AksjonspunktDefinisjon> utledAksjonspunkt(Long behandlingId);

    boolean erForeldet(LocalDate dagensDato, LocalDate fradato);

}
