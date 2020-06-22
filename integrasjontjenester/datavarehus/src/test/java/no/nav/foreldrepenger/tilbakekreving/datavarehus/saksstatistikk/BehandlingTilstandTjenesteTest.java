package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.Before;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
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

    private Behandling behandling;
    private static final UUID EKSTERN_UUID = UUID.randomUUID();

    @Before
    public void setup(){
        behandling = ScenarioSimple.simple().lagre(behandlingRepositoryProvider);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), EKSTERN_UUID);
        behandlingRepositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);
    }

    @Test
    public void skal_utlede_behandlingtilstand_for_nyopprettet_behandling() {
        BehandlingTilstand tilstand = tjeneste.hentBehandlingensTilstand(behandling.getId());

        assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(tilstand.getReferteFagsakBehandling()).isEqualTo(EKSTERN_UUID);
        assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.IKKE_FASTSATT);
        assertThat(tilstand.venterPåBruker()).isFalse();
        assertThat(tilstand.venterPåØkonomi()).isFalse();
        assertThat(tilstand.erBehandlingManueltOpprettet()).isFalse();
        assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getTekniskTid()).isNull();
        assertThat(tilstand.getAnsvarligBeslutter()).isNull();
        assertThat(tilstand.getAnsvarligSaksbehandler()).isNull();
        assertThat(tilstand.getBehandlendeEnhetKode()).isNull();
    }

    @Test
    public void skal_utlede_behandlingtilstand_for_fattet_behandling() {
        behandlingresultatRepository.lagre(Behandlingsresultat.builder().medBehandling(behandling).medBehandlingResultatType(BehandlingResultatType.FASTSATT).build());

        behandling.setBehandlendeOrganisasjonsEnhet(new OrganisasjonsEnhet("1234", "foo bar"));
        behandling.setAnsvarligSaksbehandler("Z111111");
        behandling.setAnsvarligBeslutter("Z111112");
        behandling.avsluttBehandling();
        repositoryRule.getEntityManager().persist(behandling);
        repositoryRule.getEntityManager().flush();
        repositoryRule.getEntityManager().clear();

        BehandlingTilstand tilstand = tjeneste.hentBehandlingensTilstand(behandling.getId());

        assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(tilstand.getReferteFagsakBehandling()).isEqualTo(EKSTERN_UUID);
        assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
        assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.FASTSATT);
        assertThat(tilstand.venterPåBruker()).isFalse();
        assertThat(tilstand.venterPåØkonomi()).isFalse();
        assertThat(tilstand.erBehandlingManueltOpprettet()).isFalse();
        assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getTekniskTid()).isNull();
        assertThat(tilstand.getAnsvarligBeslutter()).isEqualTo("Z111112");
        assertThat(tilstand.getAnsvarligSaksbehandler()).isEqualTo("Z111111");
        assertThat(tilstand.getBehandlendeEnhetKode()).isEqualTo("1234");
    }

    @Test
    public void skal_utlede_behandlingstilstand_for_behandling_på_vent() {
        System.setProperty("frist.brukerrespons.varsel", "P3W");
        behandlingTjeneste.settBehandlingPaVent(behandling.getId(), LocalDate.now().plusDays(1), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        repositoryRule.getEntityManager().flush();
        repositoryRule.getEntityManager().clear();

        BehandlingTilstand tilstand = tjeneste.hentBehandlingensTilstand(behandling.getId());

        assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(tilstand.getReferteFagsakBehandling()).isEqualTo(EKSTERN_UUID);
        assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.IKKE_FASTSATT);
        assertThat(tilstand.venterPåBruker()).isTrue();
        assertThat(tilstand.venterPåØkonomi()).isFalse();
        assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());


        System.out.println(BehandlingTilstandMapper.tilJsonString(tilstand));

    }
}
