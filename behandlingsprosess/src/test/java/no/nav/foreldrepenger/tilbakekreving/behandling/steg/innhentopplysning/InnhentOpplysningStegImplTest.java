package no.nav.foreldrepenger.tilbakekreving.behandling.steg.innhentopplysning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning.InnhentOpplysningSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.inhentopplysning.InnhentOpplysningStegImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

public class InnhentOpplysningStegImplTest extends FellesTestOppsett {

    private InnhentOpplysningSteg innhentOpplysningSteg = new InnhentOpplysningStegImpl(repositoryProvider, fpsakKlientMock);
    private VarselRepository varselRepository = repositoryProvider.getVarselRepository();

    private static final String VARSEL_TEKST = "Dette er varselTekst";
    private static final LocalDate PERIODE_FOM = LocalDate.of(2019, 1, 1);
    private static final LocalDate PERIODE_TOM = LocalDate.of(2019, 1, 31);

    private Behandling behandling;
    private BehandlingLås behandlingLås;
    private EksternBehandling eksternBehandling;

    @Before
    public void setup() {
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).medManueltOpprettet(true).build();
        behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        when(fpsakKlientMock.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.VARSELTEKST)).thenReturn(lagSamletEksternBehandlingInfo(VARSEL_TEKST));
    }

    @Test
    public void skal_hente_varseltekst_fra_fpsak_og_lagre() {
        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));

        Optional<VarselInfo> entitet = varselRepository.finnVarsel(behandling.getId());
        assertThat(entitet).isPresent();
        VarselInfo varselInfo = entitet.get();
        assertThat(varselInfo.getVarselTekst()).isEqualTo(VARSEL_TEKST);
    }

    @Test
    public void skal_forsøke_å_hente_varseltekst_fra_fpsak_og_ikke_lagre_varsel_når_varseltekst_ikke_finnes() {
        when(fpsakKlientMock.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.VARSELTEKST)).thenReturn(lagSamletEksternBehandlingInfo(""));
        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));

        Optional<VarselInfo> entitet = varselRepository.finnVarsel(behandling.getId());
        assertThat(entitet).isEmpty();
    }

    @Test
    public void skal_ikke_oppdatere_grunnlag_referanse_hvis_grunnlag_ikke_finnes_for_manuell_opprettet_behandling() {
        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));

        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternId()).isEqualByComparingTo(FPSAK_BEHANDLING_ID);
    }

    @Test
    public void skal_ikke_oppdatere_hvis_grunnlag_finnes_med_samme_referanse_for_manuell_opprettet_behandling() {
        lagKravgrunnlag(String.valueOf(FPSAK_BEHANDLING_ID));
        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));

        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternId()).isEqualByComparingTo(FPSAK_BEHANDLING_ID);
    }

    @Test
    public void skal_oppdatere_hvis_grunnlag_finnes_med_forskjellige_referanse_for_manuell_opprettet_behandling() {
        Long gammelFpsakBehandlingIdSomPåvirkerGrunnlag = 9999951L;
        lagKravgrunnlag(String.valueOf(gammelFpsakBehandlingIdSomPåvirkerGrunnlag));
        EksternBehandlingsinfoDto førsteVedtak = new EksternBehandlingsinfoDto();
        førsteVedtak.setId(gammelFpsakBehandlingIdSomPåvirkerGrunnlag);
        førsteVedtak.setUuid(UUID.randomUUID());

        EksternBehandlingsinfoDto andreVedtak = new EksternBehandlingsinfoDto();
        andreVedtak.setId(FPSAK_BEHANDLING_ID);
        andreVedtak.setUuid(FPSAK_BEHANDLING_UUID);

        when(fpsakKlientMock.hentBehandlingForSaksnummer(fagsak.getSaksnummer().getVerdi())).thenReturn(Lists.newArrayList(førsteVedtak, andreVedtak));
        assertThat(eksternBehandling.getEksternId()).isEqualByComparingTo(FPSAK_BEHANDLING_ID);

        innhentOpplysningSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));

        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternId()).isEqualByComparingTo(gammelFpsakBehandlingIdSomPåvirkerGrunnlag);
    }

    private SamletEksternBehandlingInfo lagSamletEksternBehandlingInfo(String varselTekst) {
        return SamletEksternBehandlingInfo
            .builder(Tillegsinformasjon.VARSELTEKST)
            .setVarseltekst(varselTekst).build();
    }

    private void lagKravgrunnlag(String referanse) {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(PERIODE_FOM, PERIODE_TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(PERIODE_FOM, PERIODE_TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));
        Kravgrunnlag431 kravgrunnlag431 = Kravgrunnlag431.builder()
            .medVedtakId(100000l)
            .medEksternKravgrunnlagId("12123")
            .medKravStatusKode(KravStatusKode.NYTT)
            .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
            .medFagSystemId("100000000000")
            .medGjelderVedtakId("100000000")
            .medGjelderType(GjelderType.ORGANISASJON)
            .medUtbetalesTilId("100000000")
            .medUtbetIdType(GjelderType.ORGANISASJON)
            .medAnsvarligEnhet("8020")
            .medBehandlendeEnhet("8020")
            .medBostedEnhet("8020")
            .medFeltKontroll("00")
            .medReferanse(referanse)
            .medSaksBehId("Z991035").build();

        kravgrunnlag431.leggTilPeriode(KravgrunnlagMockUtil.lagMockPeriode(mockMedFeilPostering, kravgrunnlag431));
        kravgrunnlag431.leggTilPeriode(KravgrunnlagMockUtil.lagMockPeriode(mockMedYtelPostering, kravgrunnlag431));

        grunnlagRepository.lagre(behandling.getId(), kravgrunnlag431);
    }

}
