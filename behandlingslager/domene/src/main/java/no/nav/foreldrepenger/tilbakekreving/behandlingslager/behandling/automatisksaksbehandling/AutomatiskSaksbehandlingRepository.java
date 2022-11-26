package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.felles.Satser;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

@ApplicationScoped
public class AutomatiskSaksbehandlingRepository {

    private EntityManager entityManager;

    AutomatiskSaksbehandlingRepository() {
        // for CDI
    }

    @Inject
    public AutomatiskSaksbehandlingRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Behandling> hentAlleBehandlingerSomErKlarForAutomatiskSaksbehandling(LocalDate bestemtDato) {
        TypedQuery<Behandling> query = entityManager.createQuery("""
                from Behandling beh where beh.id in
                (select b.id as behandlingId from Behandling b
                inner join Fagsak f on b.fagsak.id = f.id
                inner join Aksjonspunkt ap on b.id = ap.behandling.id
                inner join KravgrunnlagAggregateEntity grunn on b.id=grunn.behandlingId
                inner join Kravgrunnlag431 kravgrunnlag on grunn.grunnlagØkonomi.id = kravgrunnlag.id
                inner join KravgrunnlagPeriode432 periode on kravgrunnlag.id = periode.kravgrunnlag431.id
                inner join KravgrunnlagBelop433 beløp on periode.id = beløp.kravgrunnlagPeriode432.id
                where
                ap.aksjonspunktDefinisjon=:aksjonspunktDefinisjon
                and ap.status=:aksjonspunktStatus
                and b.ansvarligSaksbehandler is null
                and b.status=:behandlingStatus
                and b.behandlingType in (:behandlingTyper)
                and grunn.aktiv='J'
                and NOT EXISTS (select id from BrevSporing brev where brev.behandlingId = b.id)
                and to_timestamp(kravgrunnlag.kontrollFelt,'YYYY-MM-DD-HH24.mi.ss.ff') < to_timestamp(:bestemtDato)
                and beløp.klasseType=:klasseType
                group by b.id,f.fagsakYtelseType
                having
                sum(beløp.nyBelop) <= case f.fagsakYtelseType
                when 'FP' then :halvtRettsgebyr
                when 'SVP' then :halvtRettsgebyr
                when 'ES' then :halvtRettsgebyr
                when 'PSB' then :halvtRettsgebyr
                when 'PPN' then :halvtRettsgebyr
                when 'OLP' then :halvtRettsgebyr
                when 'OMP' then :halvtRettsgebyr
                when 'FRISINN' then :heltRettsgebyr
                else -1
                end )
                """, Behandling.class);

        query.setParameter("aksjonspunktDefinisjon", AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING);
        query.setParameter("aksjonspunktStatus", AksjonspunktStatus.OPPRETTET);
        query.setParameter("behandlingStatus", BehandlingStatus.UTREDES);
        query.setParameter("behandlingTyper", List.of(BehandlingType.TILBAKEKREVING, BehandlingType.REVURDERING_TILBAKEKREVING));
        query.setParameter("bestemtDato", bestemtDato.atStartOfDay());
        query.setParameter("klasseType", KlasseType.FEIL);
        query.setParameter("heltRettsgebyr", Satser.rettsgebyr());
        query.setParameter("halvtRettsgebyr", Satser.halvtRettsgebyr());
        return query.getResultList();
    }
}
