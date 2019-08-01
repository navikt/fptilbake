package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;

public interface NavBrukerKodeverkRepository extends BehandlingslagerRepository {

    NavBrukerKjønn finnBrukerKjønn(String kode);

    PersonstatusType finnPersonstatus(String kode);
}
