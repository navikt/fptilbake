package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;

@ApplicationScoped
public class GeografiKodeverkRepository {

    private EntityManager entityManager;

    private KodeverkRepository kodeverkRepository;

    GeografiKodeverkRepository() {
        // for CDI proxy
    }

    @Inject
    public GeografiKodeverkRepository(EntityManager entityManager,
                                      KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.kodeverkRepository = kodeverkRepository;
    }

    public GeografiKodeverkRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        }
    }

    public SivilstandType finnSivilstandType(String kode) {
        return kodeverkRepository.finn(SivilstandType.class, kode);
    }

    public Landkoder finnLandkode(String kode) {
        return kodeverkRepository.finn(Landkoder.class, kode);
    }

}
