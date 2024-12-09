package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
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

        var fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);

        var behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandlingRepository.lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        // Opprett vurderinger som skal være inaktive
        var inaktivTotrinnsvurdering1 = lagTotrinnsvurdering(behandling, true);
        var inaktivTotrinnsvurdering2 = lagTotrinnsvurdering(behandling, true);
        var inaktivTotrinnsvurdering3 = lagTotrinnsvurdering(behandling, true);

        List<Totrinnsvurdering> inaktivTotrinnsvurderingList = new ArrayList<>();
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering1);
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering2);
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering3);
        totrinnRepository.lagreOgFlush(behandling, inaktivTotrinnsvurderingList);

        // Opprett vurderinger som skal være aktive
        var aktivTotrinnsvurdering1 = lagTotrinnsvurdering(behandling, false);
        var aktivTotrinnsvurdering2 = lagTotrinnsvurdering(behandling, false);
        var aktivTotrinnsvurdering3 = lagTotrinnsvurdering(behandling, false);

        List<Totrinnsvurdering> aktivTotrinnsvurderingList = new ArrayList<>();
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering1);
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering2);
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering3);
        totrinnRepository.lagreOgFlush(behandling, aktivTotrinnsvurderingList);

        // Hent aktive vurderinger etter flush
        var repoAktiveTotrinnsvurderinger = totrinnRepository.hentTotrinnsvurderinger(behandling);

        // Hent inaktive vurderinger etter flush
        var query = entityManager.createQuery(
                "FROM Totrinnsvurdering tav WHERE tav.behandling.id = :behandling_id AND tav.aktiv = false",
                Totrinnsvurdering.class);
        query.setParameter("behandling_id", behandling.getId());
        var repoInaktiveTotrinnsvurderinger = query.getResultList();

        // Sjekk lagrede aktive vurderinger
        assertThat(repoAktiveTotrinnsvurderinger).hasSize(3);
        repoAktiveTotrinnsvurderinger.forEach(totrinnsvurdering -> assertThat(totrinnsvurdering.isAktiv()).isTrue());

        // Sjekk lagrede inaktive vurderinger
        assertThat(repoInaktiveTotrinnsvurderinger).hasSize(3);
        repoInaktiveTotrinnsvurderinger.forEach(totrinnsvurdering -> assertThat(totrinnsvurdering.isAktiv()).isFalse());

    }

    private Totrinnsvurdering lagTotrinnsvurdering(Behandling behandling, boolean godkjent) {
        var totrinnsvurdering = Totrinnsvurdering.builder()
                .medBehandling(behandling)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING)
                .medGodkjent(godkjent)
                .medBegrunnelse("")
                .build();
        totrinnsvurdering.leggTilVurderÅrsakTotrinnsvurdering(VurderÅrsak.FEIL_FAKTA);
        return totrinnsvurdering;
    }


}
