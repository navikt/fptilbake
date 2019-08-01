package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;

public interface BehandlingModellRepository extends BehandlingslagerRepository {

    BehandlingModell getModell(BehandlingType behandlingType);

    BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon();

    KodeverkRepository getKodeverkRepository();


}
