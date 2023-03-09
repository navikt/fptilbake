package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class TotrinnRepositoryTest {

    private BehandlingRepositoryProvider repositoryProvider;
    private TotrinnRepository totrinnRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;

    @BeforeEach
    void setup(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        totrinnRepository = new TotrinnRepository(entityManager);
    }

    @Test
    void skal_finne_flere_inaktive_totrinnsvurderinger_og_flere_aktive_totrinnsvurdering(EntityManager entityManager) {

        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);

        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandlingRepository.lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        // Opprett vurderinger som skal være inaktive
        Totrinnsvurdering inaktivTotrinnsvurdering1 = lagTotrinnsvurdering(behandling,
                AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, true, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering inaktivTotrinnsvurdering2 = lagTotrinnsvurdering(behandling,
                AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, true, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering inaktivTotrinnsvurdering3 = lagTotrinnsvurdering(behandling,
                AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, true, "", VurderÅrsak.FEIL_FAKTA);

        List<Totrinnsvurdering> inaktivTotrinnsvurderingList = new ArrayList<>();
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering1);
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering2);
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering3);
        totrinnRepository.lagreOgFlush(behandling, inaktivTotrinnsvurderingList);

        // Opprett vurderinger som skal være aktive
        Totrinnsvurdering aktivTotrinnsvurdering1 = lagTotrinnsvurdering(behandling,
                AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, false, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering aktivTotrinnsvurdering2 = lagTotrinnsvurdering(behandling,
                AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, false, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering aktivTotrinnsvurdering3 = lagTotrinnsvurdering(behandling,
                AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, false, "", VurderÅrsak.FEIL_FAKTA);

        List<Totrinnsvurdering> aktivTotrinnsvurderingList = new ArrayList<>();
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering1);
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering2);
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering3);
        totrinnRepository.lagreOgFlush(behandling, aktivTotrinnsvurderingList);

        // Hent aktive vurderinger etter flush
        Collection<Totrinnsvurdering> repoAktiveTotrinnsvurderinger = totrinnRepository.hentTotrinnsvurderinger(behandling);

        // Hent inaktive vurderinger etter flush
        TypedQuery<Totrinnsvurdering> query = entityManager.createQuery(
                "SELECT tav FROM Totrinnsvurdering tav WHERE tav.behandling.id = :behandling_id AND tav.aktiv = 'N'", //$NON-NLS-1$
                Totrinnsvurdering.class);
        query.setParameter("behandling_id", behandling.getId()); //$NON-NLS-1$
        List<Totrinnsvurdering> repoInaktiveTotrinnsvurderinger = query.getResultList();

        // Sjekk lagrede aktive vurderinger
        assertThat(repoAktiveTotrinnsvurderinger.size()).isEqualTo(3);
        repoAktiveTotrinnsvurderinger.forEach(totrinnsvurdering -> assertThat(totrinnsvurdering.isAktiv()).isTrue());

        // Sjekk lagrede inaktive vurderinger
        assertThat(repoInaktiveTotrinnsvurderinger.size()).isEqualTo(3);
        repoInaktiveTotrinnsvurderinger.forEach(totrinnsvurdering -> assertThat(totrinnsvurdering.isAktiv()).isFalse());

    }

    private Totrinnsvurdering lagTotrinnsvurdering(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                   boolean godkjent, String begrunnelse, VurderÅrsak vurderÅrsak) {
        Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder()
                .medBehandling(behandling)
                .medAksjonspunktDefinisjon(aksjonspunktDefinisjon)
                .medGodkjent(godkjent)
                .medBegrunnelse(begrunnelse)
                .build();
        totrinnsvurdering.leggTilVurderÅrsakTotrinnsvurdering(vurderÅrsak);
        return totrinnsvurdering;
    }


}
