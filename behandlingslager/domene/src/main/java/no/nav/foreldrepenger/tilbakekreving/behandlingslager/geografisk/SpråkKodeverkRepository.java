package no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class SpråkKodeverkRepository {

    private KodeverkRepository kodeverkRepository;

    SpråkKodeverkRepository() {
        // for CDI proxy
    }

    @Inject
    public SpråkKodeverkRepository(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository"); //$NON-NLS-1$
    }

    public Optional<Språkkode> finnSpråkMedKodeverkEiersKode(String kodeverkEiersKode) {
        try {
            return Optional.of(kodeverkRepository.finnForKodeverkEiersKode(Språkkode.class, kodeverkEiersKode));
        } catch (TekniskException e) { //NOSONAR
            return Optional.empty();
        }
    }
}
