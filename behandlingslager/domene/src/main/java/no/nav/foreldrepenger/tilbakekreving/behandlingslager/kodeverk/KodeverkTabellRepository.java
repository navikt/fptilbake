package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import java.util.Collection;
import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;

public interface KodeverkTabellRepository {

    BehandlingStegType finnBehandlingStegType(String kode);

    Venteårsak finnVenteårsak(String kode);

    Set<VurderÅrsak> finnVurderÅrsaker(Collection<String> koder);

    AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode);

}
