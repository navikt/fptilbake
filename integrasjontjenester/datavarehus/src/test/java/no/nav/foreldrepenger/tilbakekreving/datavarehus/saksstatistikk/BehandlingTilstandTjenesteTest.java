package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKontrollRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingMetode;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;

@CdiDbAwareTest
class BehandlingTilstandTjenesteTest {

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private BehandlingTjeneste behandlingTjeneste;
    @Inject
    private BehandlingresultatRepository behandlingresultatRepository;
    @Inject
    private BehandlingTilstandTjeneste tjeneste;
    @Inject
    private EntityManager entityManager;

    private Behandling behandling;
    private static final UUID EKSTERN_UUID = UUID.randomUUID();

    @BeforeAll
    static void setupAlle() {
        System.setProperty("app.name", "fptilbake");
    }

    @AfterAll
    static void teardown() {
        System.clearProperty("app.name");
    }

    @BeforeEach
    void setup() {
        behandling = ScenarioSimple.simple().lagre(behandlingRepositoryProvider);
        var eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), EKSTERN_UUID);
        behandlingRepositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);
    }

    @Test
    void skal_utlede_behandlingtilstand_for_nyopprettet_behandling() {
        var tilstand = tjeneste.hentBehandlingensTilstand(behandling, Fagsystem.FPTILBAKE);

        assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(tilstand.getReferertFagsakBehandlingUuid()).isEqualTo(EKSTERN_UUID);
        assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.IKKE_FASTSATT);
        assertThat(tilstand.venterPåBruker()).isFalse();
        assertThat(tilstand.venterPåØkonomi()).isFalse();
        assertThat(tilstand.erBehandlingManueltOpprettet()).isFalse();
        assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getTekniskTid()).isNull();
        assertThat(tilstand.getRegistrertTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getFerdigBehandletTid()).isNull();
        assertThat(tilstand.getOpprettetAv()).isEqualTo("VL");
        assertThat(tilstand.getAnsvarligBeslutter()).isNull();
        assertThat(tilstand.getAnsvarligSaksbehandler()).isNull();
        assertThat(tilstand.getBehandlendeEnhetKode()).isNull();
    }

    @Test
    void skal_utlede_behandlingtilstand_for_fattet_behandling() {
        var scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FATTE_VEDTAK, BehandlingStegType.FATTE_VEDTAK);
        behandling = scenario.lagre(behandlingRepositoryProvider);
        behandling.getÅpneAksjonspunkter().forEach(ap -> new AksjonspunktKontrollRepository().setTilUtført(ap));
        behandlingRepositoryProvider.getBehandlingRepository().lagre(behandling, behandlingRepositoryProvider.getBehandlingRepository().taSkriveLås(behandling));
        var eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), EKSTERN_UUID);
        behandlingRepositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);
        behandlingresultatRepository.lagre(Behandlingsresultat.builder()
                .medBehandling(behandling)
                .medBehandlingResultatType(BehandlingResultatType.FULL_TILBAKEBETALING).build());

        behandling.setBehandlendeOrganisasjonsEnhet(new OrganisasjonsEnhet("1234", "foo bar"));
        behandling.setAnsvarligSaksbehandler("Z111111");
        behandling.setAnsvarligBeslutter("Z111112");
        behandling.avsluttBehandling();
        entityManager.persist(behandling);
        entityManager.flush();
        entityManager.clear();

        var tilstand = tjeneste.hentBehandlingensTilstand(behandling, Fagsystem.FPTILBAKE);

        assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(tilstand.getReferertFagsakBehandlingUuid()).isEqualTo(EKSTERN_UUID);
        assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
        assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.FULL_TILBAKEBETALING);
        assertThat(tilstand.getBehandlingMetode()).isEqualTo(BehandlingMetode.TOTRINN);
        assertThat(tilstand.venterPåBruker()).isFalse();
        assertThat(tilstand.venterPåØkonomi()).isFalse();
        assertThat(tilstand.erBehandlingManueltOpprettet()).isFalse();
        assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getTekniskTid()).isNull();
        assertThat(tilstand.getRegistrertTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getFerdigBehandletTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getOpprettetAv()).isEqualTo("VL");
        assertThat(tilstand.getAnsvarligBeslutter()).isEqualTo("Z111112");
        assertThat(tilstand.getAnsvarligSaksbehandler()).isEqualTo("Z111111");
        assertThat(tilstand.getBehandlendeEnhetKode()).isEqualTo("1234");
    }

    @Test
    void skal_utlede_behandlingstilstand_for_behandling_på_vent() {
        behandlingTjeneste.settBehandlingPaVent(behandling.getId(), LocalDate.now().plusDays(1), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        entityManager.flush();
        entityManager.clear();

        var tilstand = tjeneste.hentBehandlingensTilstand(behandling, Fagsystem.K9TILBAKE);

        assertThat(tilstand.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(tilstand.getSaksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(tilstand.getBehandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(tilstand.getReferertFagsakBehandlingUuid()).isEqualTo(EKSTERN_UUID);
        assertThat(tilstand.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING);
        assertThat(tilstand.getBehandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        assertThat(tilstand.getBehandlingResultat()).isEqualTo(BehandlingResultat.IKKE_FASTSATT);
        assertThat(tilstand.venterPåBruker()).isTrue();
        assertThat(tilstand.venterPåØkonomi()).isFalse();
        assertThat(tilstand.getFunksjonellTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getRegistrertTid()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now());
        assertThat(tilstand.getFerdigBehandletTid()).isNull();
        assertThat(tilstand.getOpprettetAv()).isEqualTo("VL");
    }
}
