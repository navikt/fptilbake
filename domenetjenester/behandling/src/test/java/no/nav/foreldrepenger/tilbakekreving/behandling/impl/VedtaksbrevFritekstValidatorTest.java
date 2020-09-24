package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class VedtaksbrevFritekstValidatorTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;

    @Inject
    private VedtaksbrevFritekstValidator validator;

    private LocalDate jan1 = LocalDate.of(2019, 1, 1);
    private LocalDate jan3 = LocalDate.of(2019, 1, 3);
    private LocalDate jan24 = LocalDate.of(2019, 1, 24);

    private NavBruker bruker = NavBruker.opprettNy(new AktørId(1L), Språkkode.nb);
    private Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("123"), bruker);
    private Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
    private Behandling revurderingBehandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING_TILBAKEKREVING).build();
    private Behandling revurderingEtterKlageBehandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING_TILBAKEKREVING)
        .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_KA))
        .build();
    private Long behandlingId;
    private Long revurderingBehandlingId;
    private Long revurderingEtterKlageBehandlingId;

    @Before
    public void setUp() {
        fagsakRepository.lagre(fagsak);
        behandlingId = behandlingRepository.lagre(behandling, new BehandlingLås(null));
        revurderingBehandlingId = behandlingRepository.lagre(revurderingBehandling, new BehandlingLås(null));
        revurderingEtterKlageBehandlingId = behandlingRepository.lagre(revurderingEtterKlageBehandling, new BehandlingLås(null));
    }

    @Test
    public void skal_feile_når_påkrevet_fritekst_mangler() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_ANNET_HENDELSE_TYPE)
            .medHendelseUndertype(HendelseUnderType.ANNET_FRITEKST)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        expectedException.expectMessage("Ugyldig input: Når 'ANNET_FRITEKST' er valgt er fritekst påkrevet. Mangler for periode 01.01.2019-24.01.2019 og avsnitt FAKTA_AVSNITT");
        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, Collections.emptyList(), null);
    }

    @Test
    public void skal_feile_når_påkrevet_fritekst_mangler_for_deler_av_periode() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_BEREGNING_TYPE)
            .medHendelseUndertype(HendelseUnderType.ENDRING_GRUNNLAG)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        List<VedtaksbrevFritekstPeriode> fritekstperioder = Collections.singletonList(
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan1, jan3)).build()
        );

        expectedException.expectMessage("Når 'ENDRING_GRUNNLAG' er valgt er fritekst påkrevet. Mangler for periode 04.01.2019-24.01.2019 og avsnitt FAKTA_AVSNITT");

        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, fritekstperioder, null);
    }

    @Test
    public void skal_feile_når_påkrevet_fritekst_mangler_for_deler_av_periode_2() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.SVP_BEREGNING_TYPE)
            .medHendelseUndertype(HendelseUnderType.SVP_ENDRING_GRUNNLAG)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        List<VedtaksbrevFritekstPeriode> fritekstperioder = Arrays.asList(
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan1, jan1)).build(),
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan3, jan24)).build()
        );

        expectedException.expectMessage("Når 'SVP_ENDRING_GRUNNLAG' er valgt er fritekst påkrevet. Mangler for periode 02.01.2019-02.01.2019 og avsnitt FAKTA_AVSNITT");
        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, fritekstperioder, null);
    }

    @Test
    public void skal_feile_når_påkrevet_oppsummering_fritekst_mangler_for_revurdering() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(HendelseUnderType.GRADERT_UTTAK)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(revurderingBehandlingId, fakta);

        expectedException.expectMessage("Ugyldig input: Når det er revurdering, så er oppsummering fritekst påkrevet");
        validator.validerAtPåkrevdeFriteksterErSatt(revurderingBehandlingId, Collections.emptyList(), null);
    }

    @Test
    public void skal_feile_når_påkrevet_oppsummering_fritekst_mangler_for_revurdering_2() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(HendelseUnderType.GRADERT_UTTAK)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(revurderingBehandlingId, fakta);

        expectedException.expectMessage("Ugyldig input: Når det er revurdering, så er oppsummering fritekst påkrevet");
        validator.validerAtPåkrevdeFriteksterErSatt(revurderingBehandlingId, Collections.emptyList(), new VedtaksbrevFritekstOppsummering());
    }

    @Test
    public void skal_ikke_feile_på_påkrevet_oppsummering_fritekst_mangler_for_revurdering_etter_klage() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(HendelseUnderType.GRADERT_UTTAK)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(revurderingEtterKlageBehandlingId, fakta);

        validator.validerAtPåkrevdeFriteksterErSatt(revurderingEtterKlageBehandlingId, Collections.emptyList(), null);
    }

    @Test
    public void skal_ikke_feile_når_alle_påkrevet_fritekst_er_utfylt() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(HendelseUnderType.GRADERT_UTTAK)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, Collections.emptyList(), null);
    }
}
