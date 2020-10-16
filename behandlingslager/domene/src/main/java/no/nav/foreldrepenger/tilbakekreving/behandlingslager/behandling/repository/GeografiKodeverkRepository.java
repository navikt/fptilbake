package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;

@ApplicationScoped
public class GeografiKodeverkRepository {

    private KodeverkRepository kodeverkRepository;

    GeografiKodeverkRepository() {
        // for CDI proxy
    }

    @Inject
    public GeografiKodeverkRepository(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    public SivilstandType finnSivilstandType(String kode) {
        return kodeverkRepository.finn(SivilstandType.class, kode);
    }

    public Landkoder finnLandkode(String kode) {
        return kodeverkRepository.finn(Landkoder.class, kode);
    }

}
