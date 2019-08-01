package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public interface NavBrukerRepository extends BehandlingslagerRepository {

    Optional<NavBruker> hent(AktørId aktorId);
    NavBruker opprett(NavBruker bruker);

}
