package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class VedtaksbrevFritekstTjenesteTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BehandlingRepository behandlingRepository = new BehandlingRepositoryImpl(repoRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository = new FaktaFeilutbetalingRepository(repoRule.getEntityManager());
    private VilkårsvurderingRepository vilkårsvurderingRepository = new VilkårsvurderingRepository(repoRule.getEntityManager());
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository = new VedtaksbrevFritekstRepository(repoRule.getEntityManager());

    private VedtaksbrevFritekstTjeneste tjeneste = new VedtaksbrevFritekstTjeneste(faktaFeilutbetalingRepository, vilkårsvurderingRepository, vedtaksbrevFritekstRepository);

    private LocalDate jan1 = LocalDate.of(2019, 1, 1);
    private LocalDate jan2 = LocalDate.of(2019, 1, 2);
    private LocalDate jan3 = LocalDate.of(2019, 1, 3);
    private LocalDate jan24 = LocalDate.of(2019, 1, 24);

    private NavBruker bruker = NavBruker.opprettNy(new AktørId(1L), Språkkode.nb);
    private Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("123"), bruker);
    private Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
    private Long behandlingId;

    @Before
    public void setUp() {
        fagsakRepository.lagre(fagsak);
        behandlingId = behandlingRepository.lagre(behandling, new BehandlingLås(null));
    }

    @Test
    public void skal_lagre_påkrevet_fritekst() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_ANNET_HENDELSE_TYPE)
            .medHendelseUndertype(FellesUndertyper.ANNET_FRITEKST)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering oppsummering = new VedtaksbrevFritekstOppsummering.Builder().medBehandlingId(behandlingId).build();
        List<VedtaksbrevFritekstPeriode> fritekstperioder = Arrays.asList(
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan1, jan2)).build(),
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan3, jan24)).build()
        );

        tjeneste.lagreFriteksterFraSaksbehandler(behandlingId, oppsummering, fritekstperioder);
        repoRule.getEntityManager().flush();

        List<VedtaksbrevFritekstPeriode> fritekster = vedtaksbrevFritekstRepository.hentVedtaksbrevPerioderMedTekst(behandlingId);
        assertThat(fritekster).hasSize(2);
        assertThat(fritekster.get(0).getFritekst()).isEqualTo("foo");
        assertThat(fritekster.get(1).getFritekst()).isEqualTo("foo");
    }

    @Test
    public void skal_feile_når_påkrevet_fritekst_mangler() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_ANNET_HENDELSE_TYPE)
            .medHendelseUndertype(FellesUndertyper.ANNET_FRITEKST)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering oppsummering = new VedtaksbrevFritekstOppsummering.Builder().medBehandlingId(behandlingId).build();
        List<VedtaksbrevFritekstPeriode> fritekstperioder = Collections.emptyList();
        expectedException.expectMessage("Ugyldig input: Når ANNET er valgt er fritekst påkrevet. Mangler for periode Periode[2019-01-01,2019-01-24] og avsnitt FAKTA_AVSNITT");
        tjeneste.lagreFriteksterFraSaksbehandler(behandlingId, oppsummering, fritekstperioder);
    }

    @Test
    public void skal_feile_når_påkrevet_fritekst_mangler_for_deler_av_periode() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_ANNET_HENDELSE_TYPE)
            .medHendelseUndertype(FellesUndertyper.ANNET_FRITEKST)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering oppsummering = new VedtaksbrevFritekstOppsummering.Builder().medBehandlingId(behandlingId).build();
        List<VedtaksbrevFritekstPeriode> fritekstperioder = Collections.singletonList(
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan1, jan3)).build()
        );

        expectedException.expectMessage("Ugyldig input: Når ANNET er valgt er fritekst påkrevet. Mangler for periode Periode[2019-01-04,2019-01-24] og avsnitt FAKTA_AVSNITT");

        tjeneste.lagreFriteksterFraSaksbehandler(behandlingId, oppsummering, fritekstperioder);
    }

    @Test
    public void skal_feile_når_påkrevet_fritekst_mangler_for_deler_av_periode_2() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_ANNET_HENDELSE_TYPE)
            .medHendelseUndertype(FellesUndertyper.ANNET_FRITEKST)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering oppsummering = new VedtaksbrevFritekstOppsummering.Builder().medBehandlingId(behandlingId).build();
        List<VedtaksbrevFritekstPeriode> fritekstperioder = Arrays.asList(
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan1, jan1)).build(),
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan3, jan24)).build()
        );

        expectedException.expectMessage("Ugyldig input: Når ANNET er valgt er fritekst påkrevet. Mangler for periode Periode[2019-01-02,2019-01-02] og avsnitt FAKTA_AVSNITT");

        tjeneste.lagreFriteksterFraSaksbehandler(behandlingId, oppsummering, fritekstperioder);
    }
}
