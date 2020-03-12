package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;


import java.time.LocalDate;
import java.time.OffsetDateTime;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class BehandlingTilstandTjenesteTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private BehandlingTjeneste behandlingTjeneste;
    @Inject
    private BehandlingresultatRepository behandlingresultatRepository;

    @Inject
    private BehandlingTilstandTjeneste tjeneste;

    @Test
    public void skal_utlede_behandlingtilstand_for_nyopprettet_behandling() {
        Behandling behandling = ScenarioSimple.simple().lagre(behandlingRepositoryProvider);

        BehandlingTilstand tilstand = tjeneste.hentBehandlingensTilstand(behandling.getId());

        Assertions.assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        Assertions.assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        Assertions.assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        Assertions.assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        Assertions.assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        Assertions.assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.IKKE_FASTSATT);
        Assertions.assertThat(tilstand.venterPåBruker()).isFalse();
        Assertions.assertThat(tilstand.venterPåØkonomi()).isFalse();
        Assertions.assertThat(tilstand.erBehandlingManueltOpprettet()).isFalse();
        Assertions.assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        Assertions.assertThat(tilstand.getTekniskTid()).isNull();
        Assertions.assertThat(tilstand.getAnsvarligBeslutter()).isNull();
        Assertions.assertThat(tilstand.getAnsvarligSaksbehandler()).isNull();
        Assertions.assertThat(tilstand.getBehandlendeEnhetKode()).isNull();
    }

    @Test
    public void skal_utlede_behandlingtilstand_for_fattet_behandling() {
        Behandling behandling = ScenarioSimple.simple().lagre(behandlingRepositoryProvider);
        behandlingresultatRepository.lagre(Behandlingsresultat.builder().medBehandling(behandling).medBehandlingResultatType(BehandlingResultatType.FASTSATT).build());

        behandling.setBehandlendeOrganisasjonsEnhet(new OrganisasjonsEnhet("1234", "foo bar"));
        behandling.setAnsvarligSaksbehandler("Z111111");
        behandling.setAnsvarligBeslutter("Z111112");
        behandling.avsluttBehandling();
        repositoryRule.getEntityManager().persist(behandling);
        repositoryRule.getEntityManager().flush();
        repositoryRule.getEntityManager().clear();

        BehandlingTilstand tilstand = tjeneste.hentBehandlingensTilstand(behandling.getId());

        Assertions.assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        Assertions.assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        Assertions.assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        Assertions.assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        Assertions.assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
        Assertions.assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.FASTSATT);
        Assertions.assertThat(tilstand.venterPåBruker()).isFalse();
        Assertions.assertThat(tilstand.venterPåØkonomi()).isFalse();
        Assertions.assertThat(tilstand.erBehandlingManueltOpprettet()).isFalse();
        Assertions.assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        Assertions.assertThat(tilstand.getTekniskTid()).isNull();
        Assertions.assertThat(tilstand.getAnsvarligBeslutter()).isEqualTo("Z111112");
        Assertions.assertThat(tilstand.getAnsvarligSaksbehandler()).isEqualTo("Z111111");
        Assertions.assertThat(tilstand.getBehandlendeEnhetKode()).isEqualTo("1234");
    }

    @Test
    public void skal_utlede_behandlingstilstand_for_behandling_på_vent() {
        System.setProperty("frist.brukerrespons.varsel", "P3W");
        Behandling behandling = ScenarioSimple.simple().lagre(behandlingRepositoryProvider);

        behandlingTjeneste.settBehandlingPaVent(behandling.getId(), LocalDate.now().plusDays(1), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        repositoryRule.getEntityManager().flush();
        repositoryRule.getEntityManager().clear();

        BehandlingTilstand tilstand = tjeneste.hentBehandlingensTilstand(behandling.getId());

        Assertions.assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        Assertions.assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        Assertions.assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        Assertions.assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        Assertions.assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        Assertions.assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.IKKE_FASTSATT);
        Assertions.assertThat(tilstand.venterPåBruker()).isTrue();
        Assertions.assertThat(tilstand.venterPåØkonomi()).isFalse();
        Assertions.assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());


        System.out.println(BehandlingTilstandMapper.tilJsonString(tilstand));

    }
}
