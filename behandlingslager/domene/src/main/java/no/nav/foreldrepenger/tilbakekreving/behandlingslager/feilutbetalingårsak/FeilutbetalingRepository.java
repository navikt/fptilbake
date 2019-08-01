package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteRelasjon;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FeilutbetalingRepository {

    private EntityManager entityManager;

    FeilutbetalingRepository() {
        // For CDI
    }

    @Inject
    public FeilutbetalingRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<FeilutbetalingÅrsakDefinisjon> henteAlleÅrsaker() {
        TypedQuery<FeilutbetalingÅrsakDefinisjon> query = entityManager.createQuery(
                "from FeilutbetalingÅrsakDefinisjon", FeilutbetalingÅrsakDefinisjon.class);
        return query.getResultList();
    }

    //TODO PFP-8580 Kodeliste og lignende skal hentes fra KodeverkRepository
    public List<KodelisteRelasjon> henteKodelisteRelasjon(String kodeverk, String kode) {
        TypedQuery<KodelisteRelasjon> query = entityManager.createQuery(
                "from KodelisteRelasjon where kodeverk1=:kodeverk and kode1=:kode", KodelisteRelasjon.class);
        query.setParameter("kodeverk", kodeverk);
        query.setParameter("kode", kode);
        return query.getResultList();
    }

    //TODO PFP-8580 Kodeliste og lignende skal hentes fra KodeverkRepository
    public List<Kodeliste> henteKodeliste(List<String> kodeverker) {
        TypedQuery<Kodeliste> query = entityManager.createQuery(
                "from Kodeliste where kodeverk in(:kodeverker)", Kodeliste.class);
        query.setParameter("kodeverker", kodeverker);
        return query.getResultList();
    }

    //TODO PFP-8580  Kodeliste og lignende skal hentes fra KodeverkRepository
    public Kodeliste henteKodeliste(String kodeverk, String kodeliste) {
        TypedQuery<Kodeliste> query = entityManager.createQuery(
                "from Kodeliste where kodeverk=:kodeverk and kode=:kodeliste", Kodeliste.class);
        query.setParameter("kodeverk", kodeverk);
        query.setParameter("kodeliste", kodeliste);
        return query.getSingleResult();
    }

    public Optional<FeilutbetalingAggregate> finnFeilutbetaling(Long behandlingId) {
        TypedQuery<FeilutbetalingAggregate> query = entityManager.createQuery(
                "from FeilutbetalingAggregate where behandlingId=:behandlingId and aktiv=:aktiv", FeilutbetalingAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    public void lagre(FeilutbetalingAggregate feilutbetalingAggregate) {
        Optional<FeilutbetalingAggregate> forrigeAggregate = finnFeilutbetaling(feilutbetalingAggregate.getBehandlingId());
        if (forrigeAggregate.isPresent()) {
            forrigeAggregate.get().disable();
            entityManager.persist(forrigeAggregate.get());
        }
        entityManager.persist(feilutbetalingAggregate.getFeilutbetaling());
        for (FeilutbetalingPeriodeÅrsak periodeÅrsak : feilutbetalingAggregate.getFeilutbetaling().getFeilutbetaltPerioder()) {
            entityManager.persist(periodeÅrsak);
        }
        entityManager.persist(feilutbetalingAggregate);
        entityManager.flush();
    }

}
