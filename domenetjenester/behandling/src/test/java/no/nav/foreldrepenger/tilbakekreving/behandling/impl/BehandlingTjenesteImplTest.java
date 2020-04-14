package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeOrganisasjonEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingsresultatDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakBehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.VergeDto;

public class BehandlingTjenesteImplTest extends FellesTestOppsett {

    private static final LocalDate NOW = LocalDate.now();

    @Before
    public void setup() {
        when(mockFpsakKlient.hentTilbakekrevingValg(eksternBehandlingUuid)).thenReturn(Optional.of(new TilbakekrevingValgDto(VidereBehandling.TILBAKEKREV_I_INFOTRYGD)));
        when(mockFpsakKlient.hentBehandling(eksternBehandlingUuid)).thenReturn(Optional.of(lagEksternBehandlingsInfo()));
    }

    @Test
    public void skal_opprette_behandling_automatisk() {
        avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, UUID.randomUUID(), eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, false);
    }


    @Test
    public void skal_opprette_behandling_automatisk_med_allerede_åpen_behandling() {
        expectedException.expectMessage("FPT-663486");
        behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternBehandlingUuid, eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manuell() {
        avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, UUID.randomUUID(), FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, true);
        assertThat(prosessTaskRepository.finnProsessTaskType(BehandlingTjenesteImpl.FINN_KRAVGRUNNLAG_TASK)).isNotEmpty();
    }

    @Test
    public void skal_opprette_behandling_manuell_med_allerede_åpen_behandling() {
        expectedException.expectMessage("FPT-663486");
        behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manuell_med_allerede_åpen_revurdeing_behandling() {
        UUID eksternUUID = UUID.randomUUID();
        avsluttBehandling();
        revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);

        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternUUID, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, true);
        assertThat(prosessTaskRepository.finnProsessTaskType(BehandlingTjenesteImpl.FINN_KRAVGRUNNLAG_TASK)).isNotEmpty();
    }

    @Test
    public void skal_opprette_behandling_manuell_med_allerede_avsluttet_behandling_med_samme_fpsak_revurdering() {
        avsluttBehandling();
        expectedException.expectMessage("FPT-663488");

        behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manuell_med_allerede_henlagt_avsluttet_behandling_med_samme_fpsak_revurdering() {
        lagBehandlingsResulatat();
        avsluttBehandling();

        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, true);
        assertThat(prosessTaskRepository.finnProsessTaskType(BehandlingTjenesteImpl.FINN_KRAVGRUNNLAG_TASK)).isNotEmpty();
    }

    @Test
    public void skal_opprette_behandling_manuell_med_verge_informasjon() {
        avsluttBehandling();
        when(mockFpsakKlient.hentVergeInformasjon(any(UUID.class))).thenReturn(Optional.of(lagVergeInformasjon()));
        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, UUID.randomUUID(), FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, true);
        assertThat(prosessTaskRepository.finnProsessTaskType(BehandlingTjenesteImpl.FINN_KRAVGRUNNLAG_TASK)).isNotEmpty();
        fellesVergeAssert(behandlingId);
    }

    @Test
    public void skal_opprette_behandling_automatisk_med_verge_informasjon() {
        avsluttBehandling();
        when(mockFpsakKlient.hentVergeInformasjon(any(UUID.class))).thenReturn(Optional.of(lagVergeInformasjon()));
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, UUID.randomUUID(), eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, false);
        assertThat(prosessTaskRepository.finnProsessTaskType(BehandlingTjenesteImpl.FINN_KRAVGRUNNLAG_TASK)).isNotEmpty();
        fellesVergeAssert(behandlingId);
    }


    @Test
    public void kan_opprette_behandling_med_åpen_behandling_finnes() {
        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isFalse();
    }

    @Test
    public void kan_opprette_behandling_med_allerede_avsluttet_behandling_med_samme_fpsak_revurdering() {
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isFalse();
    }

    @Test
    public void kan_opprette_behandling_med_allerede_avsluttet_henlagt_behandling_med_samme_fpsak_revurdering() {
        lagBehandlingsResulatat();
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isTrue();
    }

    private void lagBehandlingsResulatat() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandling(behandling)
            .medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET).build();
        repoProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
    }

    @Test
    public void kan_opprette_behandling() {
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, UUID.randomUUID());
        assertThat(result).isTrue();
    }

    @Test
    public void skal_oppdatere_behandling_medEksternReferanse() {
        UUID eksternUuid = testUtility.genererEksternUuid();
        long eksternBehandlingId = 5l;
        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(saksnummer, eksternBehandlingId, eksternUuid);

        EksternBehandling eksternBehandling = repoProvider.getEksternBehandlingRepository().hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternUuid()).isEqualByComparingTo(eksternUuid);
        assertThat(eksternBehandling.getEksternId()).isEqualByComparingTo(eksternBehandlingId);
    }

    @Test
    public void skal_oppdatere_behandling_medEksternReferanse_med_ugyldig_saksnummer() {
        UUID eksternUuid = testUtility.genererEksternUuid();
        expectedException.expectMessage("FPT-663490");

        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(new Saksnummer("1233434"), 5l, eksternUuid);
    }

    private void avsluttBehandling() {
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = repoProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    private void fellesBehandlingAssert(Long behandlingId, boolean manueltOpprettet) {
        assertThat(behandlingId).isNotNull();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        assertThat(behandling).isNotNull();
        assertThat(behandling.getFagsak().getFagsakYtelseType()).isEqualByComparingTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(behandling.getType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);
        assertThat(behandling.getBehandlendeEnhetId()).isNotEmpty();
        assertThat(behandling.getBehandlendeEnhetNavn()).isNotEmpty();
        assertThat(behandling.isManueltOpprettet()).isEqualTo(manueltOpprettet);
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingsInfo() {
        EksternBehandlingsinfoDto eksternBehandlingsinfo = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfo.setSprakkode(Språkkode.nb);
        eksternBehandlingsinfo.setUuid(eksternBehandlingUuid);
        eksternBehandlingsinfo.setVedtakDato(NOW);

        BehandlingsresultatDto behandlingsresultatDto = new BehandlingsresultatDto();
        behandlingsresultatDto.setType(FpsakBehandlingResultatType.OPPHØR);
        behandlingsresultatDto.setKonsekvenserForYtelsen(Lists.newArrayList(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER));
        eksternBehandlingsinfo.setBehandlingsresultat(behandlingsresultatDto);

        BehandlingÅrsakDto behandlingÅrsakDto = new BehandlingÅrsakDto();
        behandlingÅrsakDto.setBehandlingÅrsakType(BehandlingÅrsakType.RE_KLAGE_KA);
        eksternBehandlingsinfo.setBehandlingÅrsaker(Lists.newArrayList(behandlingÅrsakDto));
        return eksternBehandlingsinfo;
    }

    private VergeDto lagVergeInformasjon() {
        VergeDto vergeDto = new VergeDto();
        vergeDto.setGyldigFom(LocalDate.of(2019, 1, 1));
        vergeDto.setGyldigTom(LocalDate.of(2020, 12, 31));
        vergeDto.setVergeType(VergeType.BARN);
        vergeDto.setNavn("testorg");
        vergeDto.setOrganisasjonsnummer("12345");
        return vergeDto;
    }

    private void fellesVergeAssert(Long behandlingId) {
        EksternBehandling eksternBehandling = repoProvider.getEksternBehandlingRepository().hentFraInternId(behandlingId);
        assertThat(eksternBehandling.getVergeId()).isNotNull();
        long vergeId = eksternBehandling.getVergeId();
        VergeEntitet vergeEntitet = repoProvider.getVergeRepository().hentVergeInformasjon(vergeId);
        assertThat(vergeEntitet).isNotNull();
        assertThat(vergeEntitet.getBruker().getAktørId()).isEqualTo(behandling.getAktørId());
        assertThat(vergeEntitet.getVergeType()).isEqualByComparingTo(VergeType.BARN);
        assertThat(vergeEntitet.getGyldigFom()).isNotNull();
        assertThat(vergeEntitet.getGyldigTom()).isNotNull();
        assertThat(vergeEntitet.getVergeOrganisasjon()).isNotEmpty();
        VergeOrganisasjonEntitet vergeOrganisasjonEntitet = vergeEntitet.getVergeOrganisasjon().get();
        assertThat(vergeOrganisasjonEntitet.getNavn()).isNotEmpty();
        assertThat(vergeOrganisasjonEntitet.getOrganisasjonsnummer()).isNotEmpty();
    }

}
