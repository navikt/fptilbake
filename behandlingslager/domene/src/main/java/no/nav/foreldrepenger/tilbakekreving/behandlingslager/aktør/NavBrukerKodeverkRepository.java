package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;

/**
 * TODO (FC): fjern denne, unødvendig adapter.
 */
@ApplicationScoped
public class NavBrukerKodeverkRepository {

    private KodeverkRepository kodeverkRepository;

    NavBrukerKodeverkRepository() {
        // for CDI proxy
    }

    @Inject
    public NavBrukerKodeverkRepository(KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository");
        this.kodeverkRepository = kodeverkRepository;
    }

    public NavBrukerKjønn finnBrukerKjønn(String kode) {
        return kodeverkRepository.finn(NavBrukerKjønn.class, kode);
    }

    public PersonstatusType finnPersonstatus(String kode) {
        return kodeverkRepository.finn(PersonstatusType.class, kode);
    }
}
