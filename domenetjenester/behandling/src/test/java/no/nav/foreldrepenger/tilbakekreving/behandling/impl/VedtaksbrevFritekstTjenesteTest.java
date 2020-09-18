package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
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
public class VedtaksbrevFritekstTjenesteTest {

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
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;

    @Inject
    private VedtaksbrevFritekstTjeneste tjeneste;

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
            .medHendelseUndertype(HendelseUnderType.ANNET_FRITEKST)
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
            .medHendelseUndertype(HendelseUnderType.ANNET_FRITEKST)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering oppsummering = new VedtaksbrevFritekstOppsummering.Builder().medBehandlingId(behandlingId).build();
        List<VedtaksbrevFritekstPeriode> fritekstperioder = Collections.emptyList();
        expectedException.expectMessage("Ugyldig input");
        tjeneste.lagreFriteksterFraSaksbehandler(behandlingId, oppsummering, fritekstperioder);
    }


}
