package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.exception.TekniskException;

public class BehandlingTjenesteImplTest extends FellesTestOppsett {

    @Test
    public void skalReturnereTomFeilutbetalingFaktaNårGrunnlagIkkeFinnes() {
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        assertThat(feilutbetalingFakta).isEmpty();
    }

    @Test
    public void skalFeileNårSimuleringIkkeFinnes() {
        when(mockSimuleringIntegrasjonTjeneste.hentResultat(eksternBehandlingId)).thenReturn(Optional.empty());

        KravgrunnlagMock mock = lagKravgrunnlag(LocalDate.now(), LocalDate.now(), KlasseType.FEIL, BigDecimal.ZERO, BigDecimal.ZERO);
        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mock));
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder()
            .medGrunnlagØkonomi(kravgrunnlag431)
            .medBehandlingId(internBehandlingId).build();
        grunnlagRepository.lagre(kravgrunnlagAggregate);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-7428495");

        behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
    }

    @Test
    public void skalHenteFeilutbetalingFaktaMedEnkelPeriode() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder()
            .medGrunnlagØkonomi(kravgrunnlag431)
            .medBehandlingId(internBehandlingId).build();

        grunnlagRepository.lagre(kravgrunnlagAggregate);
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);

        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(TOM);
    }

    @Test
    public void skalHenteFeilutbetalingFaktaMedFlerePerioder() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, LocalDate.of(2016, 03, 31), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = lagKravgrunnlag(LocalDate.of(2016, 04, 01), LocalDate.of(2016, 04, 15),
            KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = lagKravgrunnlag(LocalDate.of(2016, 04, 22), TOM,
            KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2,
            mockMedFeilPostering3, mockMedYtelPostering));
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder()
            .medGrunnlagØkonomi(kravgrunnlag431)
            .medBehandlingId(internBehandlingId).build();

        grunnlagRepository.lagre(kravgrunnlagAggregate);
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);

        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(37000));
        assertThat(fakta.getPerioder().size()).isEqualTo(2);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 04, 15));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualTo(BigDecimal.valueOf(22000));

        assertThat(fakta.getPerioder().get(1).getTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder().get(1).getFom()).isEqualTo(LocalDate.of(2016, 04, 22));
        assertThat(fakta.getPerioder().get(1).getBelop()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    public void skalHenteFeilutbetalingFaktaMedFlerePerioderOgSisteDagIHelgen() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, LocalDate.of(2016, 03, 26), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = lagKravgrunnlag(LocalDate.of(2016, 03, 28), LocalDate.of(2016, 04, 15),
            KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = lagKravgrunnlag(LocalDate.of(2016, 04, 19), TOM,
            KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2,
            mockMedFeilPostering3, mockMedYtelPostering));
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder()
            .medGrunnlagØkonomi(kravgrunnlag431)
            .medBehandlingId(internBehandlingId).build();

        grunnlagRepository.lagre(kravgrunnlagAggregate);
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);

        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(37000));
        assertThat(fakta.getPerioder().size()).isEqualTo(2);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 04, 15));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualTo(BigDecimal.valueOf(22000));

        assertThat(fakta.getPerioder().get(1).getTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder().get(1).getFom()).isEqualTo(LocalDate.of(2016, 04, 19));
        assertThat(fakta.getPerioder().get(1).getBelop()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    public void skal_opprette_behandling_automatisk() {
        behandling.avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternBehandlingUuid, eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId);
    }


    @Test
    public void skal_opprette_behandling_automatisk_med_allerede_åpen_behandling() {
        expectedException.expectMessage("FPT-663486");
        behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternBehandlingUuid, eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manell() {
        behandling.avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER.getKode(), BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId);
    }

    @Test
    public void skal_opprette_behandling_manell_med_allerede_åpen_behandling() {
        expectedException.expectMessage("FPT-663486");
        behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER.getKode(), BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manell_med_allerede_åpen_revurdeing_behandling() {
        UUID eksternUUID = UUID.randomUUID();
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        revurderingTjeneste.opprettRevurdering(saksnummer, eksternBehandlingUuid, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR.getKode());

        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternUUID, FagsakYtelseType.FORELDREPENGER.getKode(), BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId);
    }


    private KravgrunnlagMock lagKravgrunnlag(LocalDate fom, LocalDate tom, KlasseType klasseType, BigDecimal nyBeløp, BigDecimal tilbakeBeløp) {
        return new KravgrunnlagMock(fom, tom, klasseType, nyBeløp, tilbakeBeløp);
    }




    private void fellesFaktaResponsSjekk(BehandlingFeilutbetalingFakta fakta) {
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(23000));
        assertThat(fakta.getTotalPeriodeFom()).isEqualTo(FOM);
        assertThat(fakta.getTotalPeriodeTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder()).isNotEmpty();
    }

    private void fellesBehandlingAssert(Long behandlingId) {
        assertThat(behandlingId).isNotNull();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        assertThat(behandling).isNotNull();
        assertThat(behandling.getFagsak().getFagsakYtelseType()).isEqualByComparingTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(behandling.getType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);
        assertThat(behandling.getBehandlendeEnhetId()).isNotEmpty();
        assertThat(behandling.getBehandlendeEnhetNavn()).isNotEmpty();
    }
}
