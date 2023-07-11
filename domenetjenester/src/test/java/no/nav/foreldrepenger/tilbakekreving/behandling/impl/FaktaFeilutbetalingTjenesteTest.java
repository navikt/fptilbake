package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

class FaktaFeilutbetalingTjenesteTest extends FellesTestOppsett {

    private static final LocalDate NOW = LocalDate.now();

    @BeforeEach
    void setup() {
        entityManager.setFlushMode(FlushModeType.AUTO);
        EksternBehandlingsinfoDto behandlingsinfoDto = lagEksternBehandlingsInfo();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = new SamletEksternBehandlingInfo.Builder(Set.of(Tillegsinformasjon.TILBAKEKREVINGSVALG))
                .setGrunninformasjon(behandlingsinfoDto)
                .setTilbakekrevingvalg(new TilbakekrevingValgDto(VidereBehandling.TILBAKEKREV_I_INFOTRYGD))
                .build();
        when(mockFagsystemKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.TILBAKEKREVINGSVALG)).thenReturn(samletEksternBehandlingInfo);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
    }

    @Test
    void skal_hente_feilutbetalingfakta_når_varselBeløp_ikke_finnes() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);

        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getTidligereVarseltBeløp()).isNull();
    }

    @Test
    void skal_hente_feilutbetalingFakta_med_enkel_periode() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
        varselRepository.lagre(internBehandlingId, "hello", 23000l);
        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(23000l);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(TOM);
    }

    @Test
    void skal_hente_feilutbetalingFakta_med_enkel_periode_når_behandling_er_henlagt() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));
        //TODO
        Kravgrunnlag431 kravgrunnlag431 = Kravgrunnlag431.builder()
                .medVedtakId(100000l)
                .medEksternKravgrunnlagId("12123")
                .medKravStatusKode(KravStatusKode.NYTT)
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medFagSystemId(saksnummer + "100")
                .medGjelderVedtakId("100000000")
                .medGjelderType(GjelderType.ORGANISASJON)
                .medUtbetalesTilId("100000000")
                .medUtbetIdType(GjelderType.ORGANISASJON)
                .medAnsvarligEnhet("8020")
                .medBehandlendeEnhet("8020")
                .medBostedEnhet("8020")
                .medFeltKontroll("00")
                .medSaksBehId("Z991035")
                .medReferanse(henvisning)
                .build();
        KravgrunnlagPeriode432 feilPeriode = KravgrunnlagMockUtil.lagMockPeriode(mockMedFeilPostering, kravgrunnlag431);
        KravgrunnlagPeriode432 ytelPeriode = KravgrunnlagMockUtil.lagMockPeriode(mockMedYtelPostering, kravgrunnlag431);
        kravgrunnlag431.leggTilPeriode(feilPeriode);
        kravgrunnlag431.leggTilPeriode(ytelPeriode);

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
        varselRepository.lagre(internBehandlingId, "hello", 23000l);

        doHenleggBehandling(internBehandlingId, BehandlingResultatType.HENLAGT_FEILOPPRETTET);
        Optional<EksternBehandling> eksternBehandling = repoProvider.getEksternBehandlingRepository().hentEksisterendeDeaktivert(behandling.getId(), henvisning);
        assertThat(eksternBehandling).isPresent();
        assertThat(eksternBehandling.get().getAktiv()).isFalse();

        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(23000l);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(TOM);
    }

    private void doHenleggBehandling(long behandlingId, BehandlingResultatType årsakKode) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtførtForHenleggelse(behandling, kontekst);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        repoProvider.getEksternBehandlingRepository().deaktivateTilkobling(behandlingId);
    }

    @Test
    void skal_hente_feilutbetalingFakta_med_flere_perioder() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, LocalDate.of(2016, 03, 31), KlasseType.FEIL,
                BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = lagKravgrunnlag(LocalDate.of(2016, 04, 01), LocalDate.of(2016, 04, 15),
                KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = lagKravgrunnlag(LocalDate.of(2016, 04, 22), TOM,
                KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedFeilPostering2,
                mockMedFeilPostering3, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
        varselRepository.lagre(internBehandlingId, "hello", 23000l);

        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(37000));
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(23000l);
        assertThat(fakta.getPerioder().size()).isEqualTo(2);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 04, 15));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualTo(BigDecimal.valueOf(22000));

        assertThat(fakta.getPerioder().get(1).getTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder().get(1).getFom()).isEqualTo(LocalDate.of(2016, 04, 22));
        assertThat(fakta.getPerioder().get(1).getBelop()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    void skal_hente_feilutbetalingFakta_med_flere_perioder_og_sistedag_i_helgen() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, LocalDate.of(2016, 03, 26), KlasseType.FEIL,
                BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = lagKravgrunnlag(LocalDate.of(2016, 03, 28), LocalDate.of(2016, 04, 15),
                KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = lagKravgrunnlag(LocalDate.of(2016, 04, 19), TOM,
                KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedFeilPostering2,
                mockMedFeilPostering3, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);
        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
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

    private KravgrunnlagMock lagKravgrunnlag(LocalDate fom, LocalDate tom, KlasseType klasseType, BigDecimal nyBeløp, BigDecimal tilbakeBeløp) {
        return new KravgrunnlagMock(fom, tom, klasseType, nyBeløp, tilbakeBeløp);
    }

    private void fellesFaktaResponsSjekk(BehandlingFeilutbetalingFakta fakta) {
        assertThat(fakta.getTotalPeriodeFom()).isEqualTo(FOM);
        assertThat(fakta.getTotalPeriodeTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder()).isNotEmpty();
        assertThat(fakta.getDatoForRevurderingsvedtak()).isEqualTo(NOW);
        assertThat(fakta.getTilbakekrevingValg().getVidereBehandling()).isEqualToComparingFieldByField(VidereBehandling.TILBAKEKREV_I_INFOTRYGD);
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingsInfo() {
        EksternBehandlingsinfoDto eksternBehandlingsinfo = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfo.setSprakkode(Språkkode.nb);
        eksternBehandlingsinfo.setUuid(eksternBehandlingUuid);
        eksternBehandlingsinfo.setVedtakDato(NOW);

        return eksternBehandlingsinfo;
    }

}
