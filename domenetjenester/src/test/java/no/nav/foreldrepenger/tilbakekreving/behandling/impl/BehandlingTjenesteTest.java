package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VergeDto;
import no.nav.vedtak.exception.TekniskException;

class BehandlingTjenesteTest extends FellesTestOppsett {

    private static final LocalDate NOW = LocalDate.now();
    private VergeRepository vergeRepository;

    @BeforeEach
    void setup() {
        entityManager.setFlushMode(FlushModeType.AUTO);
        when(mockFagsystemKlient.hentTilbakekrevingValg(eksternBehandlingUuid)).thenReturn(Optional.of(new TilbakekrevingValgDto(VidereBehandling.TILBAKEKR_OPPRETT)));
        when(mockFagsystemKlient.hentBehandlingOptional(eksternBehandlingUuid)).thenReturn(Optional.of(lagEksternBehandlingsInfo()));
        vergeRepository = repoProvider.getVergeRepository();
    }

    @Test
    void skal_opprette_behandling_automatisk() {
        avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, UUID.randomUUID(), henvisning, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, false);
    }


    @Test
    void skal_opprette_behandling_automatisk_med_allerede_åpen_behandling() {
        assertThatThrownBy(() -> behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer,
                eksternBehandlingUuid, henvisning, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING))
                .hasMessageContaining("FPT-663486");

    }

    @Test
    void skal_opprette_behandling_manuell() {
        avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, UUID.randomUUID(), FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING).getId();
        fellesBehandlingAssert(behandlingId, true);
    }

    @Test
    void skal_opprette_behandling_manuell_med_allerede_åpen_behandling() {
        assertThatThrownBy(() -> behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, eksternBehandlingUuid,
                FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING))
                .hasMessageContaining("FPT-663486");
    }

    @Test
    void skal_opprette_behandling_manuell_med_allerede_åpen_revurdeing_behandling() {
        UUID eksternUUID = UUID.randomUUID();
        avsluttBehandling();
        revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);

        Long behandlingId = behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, eksternUUID, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING).getId();
        fellesBehandlingAssert(behandlingId, true);
    }

    @Test
    void skal_opprette_behandling_manuell_med_allerede_avsluttet_behandling_med_samme_fpsak_revurdering() {
        avsluttBehandling();
        assertThatThrownBy(() -> behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, eksternBehandlingUuid,
                FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING))
                .hasMessageContaining("FPT-663488");
    }

    @Test
    void skal_opprette_behandling_manuell_med_allerede_henlagt_avsluttet_behandling_med_samme_fpsak_revurdering() {
        lagBehandlingsResulatat();
        avsluttBehandling();

        Long behandlingId = behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING).getId();
        fellesBehandlingAssert(behandlingId, true);
    }

    @Test
    void skal_opprette_behandling_manuell_med_verge_informasjon_når_verge_er_en_organsisasjon() {
        avsluttBehandling();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = lagSamletEksternBehandlingInfo(VergeType.ADVOKAT);
        VergeDto vergeDto = samletEksternBehandlingInfo.getVerge();
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(samletEksternBehandlingInfo);
        Long behandlingId = behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, UUID.randomUUID(), FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING).getId();
        fellesBehandlingAssert(behandlingId, true);
        verify(mockTpsTjeneste, never()).hentAktørForFnr(any(PersonIdent.class));
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        assertThat(vergeEntitet).isNotEmpty();
        VergeEntitet vergeOrg = vergeEntitet.get();
        fellesVergeAssert(vergeDto, vergeOrg);
        assertThat(vergeOrg.getVergeAktørId()).isNull();
        assertThat(vergeOrg.getOrganisasjonsnummer()).isEqualTo(vergeDto.getOrganisasjonsnummer());
    }

    @Test
    void skal_opprette_behandling_automatisk_med_verge_informasjon_når_verge_er_en_person() {
        avsluttBehandling();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = lagSamletEksternBehandlingInfo(VergeType.BARN);
        VergeDto vergeDto = samletEksternBehandlingInfo.getVerge();
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(samletEksternBehandlingInfo);
        when(mockTpsTjeneste.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getAktørId()));
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, UUID.randomUUID(), henvisning, aktørId,
                FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, false);
        //TODO (sladek) FINN_KRAVGRUNNLAG_TASK opprettes ikke for opprettBehandlingAutomatisk - skal den??? assertThat(taskTjeneste.finnAlle(ProsessTaskStatus.KLAR).stream().filter(t -> BehandlingTjeneste.FINN_KRAVGRUNNLAG_TASK.equals(t.taskType())).collect(Collectors.toList())).isNotEmpty();
        verify(mockTpsTjeneste, atLeastOnce()).hentAktørForFnr(any(PersonIdent.class));
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        assertThat(vergeEntitet).isNotEmpty();
        VergeEntitet vergePerson = vergeEntitet.get();
        fellesVergeAssert(vergeDto, vergePerson);
        assertThat(vergePerson.getOrganisasjonsnummer()).isNull();
        assertThat(vergePerson.getVergeAktørId()).isEqualTo(behandling.getAktørId());
    }

    @Test
    void skal_ikke_opprette_behandling_automatisk_med_verge_informasjon_når_verge_er_en_person_og_aktørId_ikke_finnes_i_tps() {
        avsluttBehandling();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = lagSamletEksternBehandlingInfo(VergeType.BARN);
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(samletEksternBehandlingInfo);
        when(mockTpsTjeneste.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.empty());
        UUID uuid = UUID.randomUUID();
        var e = assertThrows(TekniskException.class, () -> behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, uuid,
                henvisning, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING));
        assertThat(e.getMessage()).contains("FPT-7428494");
    }

    @Test
    void skal_opprette_behandling_automatisk_uten_verge_informasjon_når_verge_er_utløpt() {
        avsluttBehandling();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = lagSamletEksternBehandlingInfo(VergeType.BARN);
        samletEksternBehandlingInfo.getVerge().setGyldigTom(LocalDate.now().minusDays(2));
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(samletEksternBehandlingInfo);
        when(mockTpsTjeneste.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getAktørId()));
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, UUID.randomUUID(), henvisning, aktørId,
                FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId, false);
        //TODO (sladek) FINN_KRAVGRUNNLAG_TASK opprettes ikke for opprettBehandlingAutomatisk - skal den??? assertThat(taskTjeneste.finnAlle(ProsessTaskStatus.KLAR).stream().filter(t -> BehandlingTjeneste.FINN_KRAVGRUNNLAG_TASK.equals(t.taskType())).collect(Collectors.toList())).isNotEmpty();
        verify(mockTpsTjeneste, never()).hentAktørForFnr(any(PersonIdent.class));
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        assertThat(vergeEntitet).isEmpty();
    }


    @Test
    void kan_opprette_behandling_med_åpen_behandling_finnes() {
        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isFalse();
    }

    @Test
    void kan_opprette_behandling_med_allerede_avsluttet_behandling_med_samme_fpsak_revurdering() {
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isFalse();
    }

    @Test
    void kan_opprette_behandling_med_allerede_avsluttet_henlagt_behandling_med_samme_fpsak_revurdering() {
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
    void kan_opprette_behandling() {
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, UUID.randomUUID());
        assertThat(result).isTrue();
    }

    @Test
    void skal_oppdatere_behandling_medEksternReferanse() {
        UUID eksternUuid = testUtility.genererEksternUuid();
        long eksternBehandlingId = 5L;
        Henvisning nyHenvisning = Henvisning.fraEksternBehandlingId(eksternBehandlingId);
        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(saksnummer, nyHenvisning, eksternUuid);

        EksternBehandling eksternBehandling = repoProvider.getEksternBehandlingRepository().hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternUuid()).isEqualTo(eksternUuid);
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(nyHenvisning);
    }

    @Test
    void skal_oppdatere_behandling_medEksternReferanse_med_ugyldig_saksnummer() {
        UUID eksternUuid = testUtility.genererEksternUuid();
        assertThatThrownBy(() -> behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(new Saksnummer("1233434"),
                Henvisning.fraEksternBehandlingId(5L), eksternUuid))
                .hasMessageContaining("FPT-663490");
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
        eksternBehandlingsinfo.setHenvisning(henvisning);
        eksternBehandlingsinfo.setVedtakDato(NOW);
        eksternBehandlingsinfo.setBehandlendeEnhetId(BEHANDLENDE_ENHET_ID);
        eksternBehandlingsinfo.setBehandlendeEnhetNavn(BEHANDLENDE_ENHET_NAVN);

        return eksternBehandlingsinfo;
    }

    private VergeDto lagVergeInformasjon(VergeType vergeType) {
        VergeDto vergeDto = new VergeDto();
        vergeDto.setGyldigFom(LocalDate.of(2019, 1, 1));
        vergeDto.setGyldigTom(LocalDate.now().plusDays(5));
        vergeDto.setVergeType(vergeType);
        vergeDto.setNavn("test");
        if (VergeType.ADVOKAT.equals(vergeType)) {
            vergeDto.setOrganisasjonsnummer("12345");
        } else {
            vergeDto.setFnr("1234");
        }
        return vergeDto;
    }

    private SamletEksternBehandlingInfo lagSamletEksternBehandlingInfo(VergeType vergeType) {
        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setAktoerId(behandling.getAktørId().getId());
        return SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.VERGE)
                .setGrunninformasjon(lagEksternBehandlingsInfo())
                .setPersonopplysninger(personopplysningDto)
                .setVerge(lagVergeInformasjon(vergeType)).build();
    }

    private void fellesVergeAssert(VergeDto vergeDto, VergeEntitet vergeEntitet) {
        assertThat(vergeEntitet.getVergeType()).isEqualByComparingTo(vergeDto.getVergeType());
        assertThat(vergeEntitet.getGyldigFom()).isEqualTo(vergeDto.getGyldigFom());
        assertThat(vergeEntitet.getGyldigTom()).isEqualTo(vergeDto.getGyldigTom());
        assertThat(vergeEntitet.getNavn()).isEqualTo(vergeDto.getNavn());
        assertThat(vergeEntitet.getKilde()).isEqualTo(KildeType.FPSAK.name());
    }

}
