package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
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
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@CdiDbAwareTest
public class VedtaksbrevFritekstValidatorTest {

    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    @Inject
    private VedtaksbrevFritekstValidator validator;

    private final LocalDate jan1 = LocalDate.of(2019, 1, 1);
    private final LocalDate jan3 = LocalDate.of(2019, 1, 3);
    private final LocalDate jan24 = LocalDate.of(2019, 1, 24);

    private final NavBruker bruker = NavBruker.opprettNy(new AktørId(1L), Språkkode.nb);
    private final Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("123"), bruker);
    private final Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
    private final Behandling revurderingBehandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING_TILBAKEKREVING).build();
    private final Behandling revurderingEtterKlageBehandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING_TILBAKEKREVING)
        .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_KA))
        .build();
    private Long behandlingId;
    private Long revurderingBehandlingId;
    private Long revurderingEtterKlageBehandlingId;

    @BeforeEach
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

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, Collections.emptyList(), null, VedtaksbrevType.ORDINÆR))
            .hasMessageContaining("Noen fakta-valg medfører påkrevet fritekst. Det mangler fritekst for 01.01.2019-24.01.2019 i fakta-avsnittet");
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

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, fritekstperioder, null, VedtaksbrevType.ORDINÆR))
            .hasMessageContaining("Noen fakta-valg medfører påkrevet fritekst. Det mangler fritekst for 04.01.2019-24.01.2019 i fakta-avsnittet");
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

        List<VedtaksbrevFritekstPeriode> fritekstperioder = List.of(
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan1, jan1)).build(),
            new VedtaksbrevFritekstPeriode.Builder().medBehandlingId(behandlingId).medFritekst("foo").medFritekstType(VedtaksbrevFritekstType.FAKTA_AVSNITT).medPeriode(Periode.of(jan3, jan24)).build()
        );

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, fritekstperioder, null, VedtaksbrevType.ORDINÆR))
            .hasMessageContaining("Noen fakta-valg medfører påkrevet fritekst. Det mangler fritekst for 02.01.2019-02.01.2019 i fakta-avsnittet");
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

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(revurderingBehandlingId, Collections.emptyList(), null, VedtaksbrevType.ORDINÆR))
            .hasMessageContaining("Ugyldig input: Når det er revurdering, så er oppsummering fritekst påkrevet");
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

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(revurderingBehandlingId, Collections.emptyList(), new VedtaksbrevFritekstOppsummering(), VedtaksbrevType.ORDINÆR))
            .hasMessageContaining("Ugyldig input: Når det er revurdering, så er oppsummering fritekst påkrevet");
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

        validator.validerAtPåkrevdeFriteksterErSatt(revurderingEtterKlageBehandlingId, Collections.emptyList(), null, VedtaksbrevType.ORDINÆR);
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

        validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, Collections.emptyList(), null, VedtaksbrevType.ORDINÆR);
    }

    @Test
    public void skal_feile_når_fritekst_er_for_lang_for_ordinær() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(HendelseUnderType.GRADERT_UTTAK)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering fritekstOppsummering = new VedtaksbrevFritekstOppsummering.Builder()
            .medBehandlingId(behandlingId)
            .medOppsummeringFritekst("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed rutrum massa eget rutrum lobortis. Donec in metus suscipit, dignissim quam at, suscipit ante. Vivamus tempor laoreet viverra. Proin mollis consectetur ipsum at faucibus. Morbi tortor massa, iaculis at tincidunt nec, scelerisque et quam. Sed hendrerit augue velit, ut dictum turpis dignissim quis. Pellentesque venenatis augue ac sapien rutrum, eget ornare neque tempor. Duis sed lectus scelerisque enim dapibus finibus. Curabitur lacinia purus erat, vitae fringilla tellus ultrices id. Integer sed pharetra nibh. Praesent id massa quis est hendrerit varius ornare in dui. Etiam non malesuada est, ac vestibulum massa. Duis elementum metus a enim convallis, eget rhoncus urna aliquet. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc eu diam arcu.\n\n" +
                "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Praesent felis mauris, sagittis ut tristique ut, tincidunt euismod dolor. Interdum et malesuada fames ac ante ipsum primis in faucibus. Proin libero lectus, porttitor nec interdum blandit, facilisis eget dui. Mauris placerat ex eget erat tincidunt rhoncus. Sed venenatis turpis sem, vitae vehicula odio vehicula at. Vestibulum cursus ligula magna, vitae semper tellus commodo porta. Integer consectetur magna est, id pulvinar tellus vestibulum id. Mauris id dolor dolor. Morbi viverra nunc id aliquam porta. In ut diam non tortor iaculis lacinia. Praesent odio erat, fringilla sit amet odio eu, facilisis ultricies nisl. Donec laoreet, ligula ac semper dignissim, nulla mauris faucibus ante, at egestas elit felis sed lorem. Nulla sit amet augue mauris. Vivamus rhoncus tristique scelerisque. Quisque vel dapibus ante.\n\n" +
                "In vitae leo non orci sagittis sollicitudin ac vel nisi. Duis ornare, nibh eget pulvinar pharetra, nibh orci consequat sapien, ut pulvinar dui erat eget libero. Cras vitae tempus eros, ut venenatis leo. Cras tellus est, egestas vitae nibh ac, consectetur semper turpis. Nullam facilisis, augue malesuada congue sollicitudin, lorem nisl tempus nisi, sit amet fermentum mauris erat ut diam. Nunc massa tortor, pharetra lobortis finibus vitae, tincidunt placerat sapien. Maecenas quis pharetra elit. Integer finibus, dolor ut imperdiet pharetra, elit nisl dignissim sapien, id lobortis nisl enim in ex. Etiam sit amet lorem at urna fringilla elementum non eget est. Nullam cursus ex eu arcu vehicula, at faucibus metus luctus. Duis quis vehicula nulla.\n\n" +
                "Ut at ullamcorper mauris. Nulla purus urna, ultricies vel massa in, convallis posuere nunc. Vestibulum feugiat, diam id egestas dignissim, mi tellus faucibus eros, nec congue nibh augue vel magna. Praesent pellentesque eget orci sit amet facilisis. Vivamus sagittis commodo quam. Suspendisse venenatis tortor id sem pharetra condimentum. Phasellus lacinia, purus in volutpat fringilla, ligula nunc aliquet purus, a semper sem nunc a sem. Nam ex libero, ultrices non nisi nec, malesuada auctor sapien. Vivamus turpis velit, convallis non facilisis sed, gravida a sapien. Nunc tristique rhoncus dui. Integer blandit rhoncus enim in suscipit. Ut ac libero auctor, lobortis ligula sed, tempor lorem. Vivamus tincidunt, ipsum eu tincidunt malesuada, lectus magna finibus elit, egestas maximus libero urna euismod felis.\n\n" +
                "In hac habitasse platea dictumst. Aliquam posuere sapien eget est maximus semper. Nulla ac tristique nulla. Nunc imperdiet tristique nulla, ac vulputate mi. Curabitur luctus nulla sed mauris venenatis, quis rhoncus nisi tincidunt. Ut nec metus et nunc mollis fermentum. Curabitur pulvinar ipsum vel sapien aliquam egestas. Integer non eleifend magna, vitae consectetur nisl. Nulla eleifend ligula sed efficitur varius.\n\n" +
                "Aliquam lacinia, tellus a pellentesque scelerisque, ipsum dolor suscipit turpis, non luctus est ligula et ligula. Duis lectus nunc, laoreet vitae elit ut, porta rhoncus arcu. Sed posuere ornare tellus, vitae pretium augue semper vel. Ut viverra, sem sed fermentum commodo, dui nullam.")
            .build();

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, Collections.emptyList(), fritekstOppsummering, VedtaksbrevType.ORDINÆR))
            .hasMessageContaining("Ugyldig input: Oppsummeringstekst er for lang.");
    }

    @Test
    public void skal_feile_når_fritekst_er_for_lang_for_feilutbetaling_bortfalt() {
        FaktaFeilutbetaling fakta = new FaktaFeilutbetaling();
        fakta.setBegrunnelse("foo");
        fakta.leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode.builder()
            .medFeilutbetalinger(fakta)
            .medHendelseType(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(HendelseUnderType.GRADERT_UTTAK)
            .medPeriode(jan1, jan24)
            .build());
        faktaFeilutbetalingRepository.lagre(behandlingId, fakta);

        VedtaksbrevFritekstOppsummering fritekstOppsummering = new VedtaksbrevFritekstOppsummering.Builder()
            .medBehandlingId(behandlingId)
            .medOppsummeringFritekst("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sollicitudin cursus justo, eu euismod enim. Proin fermentum varius nunc, sit amet sagittis mauris congue id. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean venenatis eros a sagittis porta. Donec lacinia finibus sapien, sit amet aliquet dolor pharetra ac. Sed sed eros eu erat faucibus pellentesque vitae sit amet turpis. Curabitur eget eleifend enim, nec venenatis metus. Ut maximus tristique congue. Cras ut ipsum faucibus tellus laoreet pretium. Praesent blandit est at purus interdum, eu rutrum odio bibendum.\n\n" +
                "Cras aliquet, purus sed facilisis ultrices, massa odio cursus augue, id suscipit ligula nisi vitae ante. Praesent vehicula commodo purus, nec pellentesque odio efficitur non. Vivamus sagittis orci enim, quis condimentum purus elementum in. Etiam mattis dui et ante tempus posuere. Aliquam facilisis eros et nibh dapibus, varius viverra libero commodo. In ultrices lectus non risus lacinia, non egestas dolor condimentum. Fusce ullamcorper consequat malesuada. Quisque interdum felis ipsum. Nulla at pharetra odio. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In et eleifend purus. Curabitur sed varius lorem, varius ultricies urna. Donec dignissim ex at urna commodo vehicula venenatis eu justo. Sed suscipit justo in bibendum faucibus.\n\n" +
                "Vestibulum tempus ut sem non pellentesque. In tempor velit ac risus rutrum, pharetra eleifend sapien efficitur. Ut feugiat ipsum in mauris fermentum, et gravida nibh dictum. Fusce interdum elementum quam sit amet ultricies. Duis augue sem, scelerisque sit amet bibendum quis, efficitur in arcu. Nulla facilisi. Maecenas vel urna in felis pellentesque fringilla vel nec orci. Donec lacinia leo ac tempor tempus. Suspendisse varius efficitur libero.\n\n" +
                "Suspendisse commodo nulla ut ex ultrices consectetur. In sed felis non velit auctor consectetur auctor et felis. Pellentesque sagittis quam in ultrices malesuada. Maecenas lacinia gravida sem, in consequat dolor. Phasellus tincidunt ullamcorper libero volutpat luctus. Ut accumsan dignissim ligula sit amet fermentum. Mauris nulla justo, ultricies sed ultricies a, posuere vel augue. Duis sagittis id lacus et rutrum. Sed posuere, diam sed eleifend varius, lacus lorem ultrices nisi, eget feugiat tellus nibh sed erat. Donec imperdiet iaculis pretium. Sed ac diam nibh. Suspendisse ut lorem euismod, maximus neque sed, sodales est. Proin aliquam mi diam, a ultrices tortor semper in. Nulla at ex augue. Duis ultrices cursus ipsum, at rhoncus mi faucibus ac.\n\n" +
                "Nullam vel dui quam. Proin porttitor ex vel mauris maximus, in pretium quam scelerisque. Nunc mollis vestibulum fringilla. Quisque quis magna commodo, mollis sapien sit amet, sagittis dolor. Quisque et massa dapibus, faucibus mi et, fermentum odio. Curabitur non molestie nibh. Fusce enim urna, convallis et metus vitae, vulputate fermentum lectus. Quisque ac neque non neque elementum venenatis. Quisque auctor faucibus augue vitae cursus. Proin suscipit est eros, a fermentum dui ultrices eget. Phasellus congue volutpat commodo. Ut condimentum turpis turpis, ac laoreet dolor aliquam a.\n\n" +
                "Sed posuere tempor rhoncus. Maecenas condimentum elit at enim iaculis, ultricies sagittis augue tincidunt. Vivamus diam nisi, gravida consequat mollis quis, imperdiet tempus diam. Mauris vitae nunc vel nunc elementum malesuada et a nibh. Quisque quis enim sed odio pellentesque laoreet. Ut viverra accumsan tincidunt. Nullam sed hendrerit libero, id venenatis risus. Nulla scelerisque egestas placerat. Quisque molestie, eros vitae feugiat porta, sem ligula egestas ante, eget finibus lectus metus quis libero. Nunc aliquet augue diam, tempor mollis nibh cursus sit amet. Sed luctus diam leo, ac fermentum urna mollis at. Pellentesque tellus odio, vehicula eu mollis vitae, sagittis eu mauris.\n\n" +
                "Nunc varius mauris vel arcu finibus, vel dapibus dolor posuere. Fusce sollicitudin leo vel erat pellentesque ullamcorper. Phasellus et condimentum lorem. Ut commodo quis est ac ullamcorper. Suspendisse tempor, sem et ultricies fermentum, libero nibh ullamcorper libero, vel semper ante mauris sit amet nisi. Ut ornare molestie neque, sit amet viverra sem hendrerit eget. Vestibulum eu dictum leo.\n\n" +
                "Nunc luctus ultrices interdum. Nullam et dui turpis. Aenean gravida eros at mauris varius faucibus. Nulla vel nunc dictum leo ultricies euismod sit amet a sem. Aliquam pellentesque, urna ut viverra porta, massa neque cursus ex, id tristique risus libero ac dolor. Nunc posuere enim sit amet libero tempor, quis placerat ex pretium. Mauris nec nisl laoreet, blandit ante in, iaculis urna. Proin sit amet tellus consectetur, egestas sapien ut, imperdiet enim. Morbi vel mauris diam. Proin a congue dolor.\n\n" +
                "Quisque sed sapien eget dui placerat bibendum a sit amet nunc. Nam in risus a dui scelerisque rutrum. Fusce dictum lorem at nunc facilisis pharetra. Vestibulum laoreet feugiat commodo. Pellentesque sit amet laoreet ipsum. Fusce aliquam mattis sollicitudin. Proin fermentum congue condimentum.\n\n" +
                "Nulla laoreet quam ut gravida convallis. Donec ut erat ac libero mattis efficitur sed tempus nisi. Donec quis ligula nulla. Praesent vel rhoncus ligula, dignissim lacinia erat. In hac habitasse platea dictumst. Duis congue augue est, sit amet dapibus nulla dapibus quis. Mauris pharetra, elit non porta suscipit, magna purus malesuada turpis, eget laoreet felis lacus ac diam. Cras ut vulputate felis. Vestibulum lobortis massa eget massa convallis accumsan. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut vitae lectus libero.\n\n" +
                "Nulla pretium est velit, vitae eleifend libero suscipit non. Ut nec efficitur neque. Nam lobortis justo eget felis placerat molestie. Proin dictum turpis nec aliquet congue. Pellentesque eu sapien luctus, tincidunt quam non, vulputate lacus. In vel maximus massa, eu auctor ante. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce mattis urna ac velit egestas, id dictum purus porta. Quisque egestas nibh viverra, dictum libero vel, varius arcu. Sed cursus dolor in turpis efficitur, id gravida libero consequat. Interdum et malesuada fames ac ante ipsum primis in faucibus. In accumsan faucibus ante, vel cursus libero tincidunt rhoncus.\n\n" +
                "Mauris commodo augue ut sodales tempus. Quisque diam ligula, condimentum sed vehicula in, ornare in nisl. Etiam nec tincidunt sapien. Etiam ornare lacinia urna in suscipit. Ut pulvinar dapibus porta. Maecenas sit amet scelerisque purus. Morbi vestibulum arcu nec justo fringilla interdum. Aliquam erat volutpat. Praesent ut libero eleifend, fermentum risus ut, tempus arcu. In eget erat in mauris gravida sollicitudin. Nunc faucibus porttitor tortor, quis convallis velit venenatis at.\n\n" +
                "Maecenas non facilisis mauris. Maecenas at velit neque. Donec varius ex quis nulla posuere blandit. Pellentesque sollicitudin tincidunt lacus vitae tempor. Aliquam ac luctus dolor. Vestibulum in arcu nisi. Praesent sit amet vestibulum massa. Duis quis consequat erat. Sed nec lorem nisl. Aliquam ut nunc eu sapien accumsan tempus sed quis nisl. Curabitur ac posuere metus. Aenean consectetur lobortis dui, in commodo lacus dictum lacinia. Integer varius varius mi nec convallis. Donec venenatis viverra lacus, a tristique ligula venenatis vel.\n\n" +
                "Vivamus egestas neque tortor, quis condimentum ante pharetra id. Vestibulum ornare turpis ut ante luctus, ac molestie odio venenatis. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Cras iaculis iaculis eros, lacinia rhoncus sapien pretium et. Duis ac sapien magna. Nulla fringilla auctor risus vitae scelerisque. Ut vehicula ornare ligula vitae ultricies. Aenean at turpis nec mi volutpat semper et a mauris. Nullam elit nulla, ullamcorper eget viverra eu, molestie id quam. Integer ac tortor nec magna tempus condimentum at eget risus. Mauris arcu leo, molestie congue sem bibendum, convallis interdum elit. Nullam vitae volutpat metus.\n\n" +
                "Donec eget tortor a turpis fermentum pharetra vel non sem. Nam at ipsum laoreet magna mattis convallis sit amet nec ex. Sed erat elit, congue vel odio sagittis, vulputate tempor mauris. Vivamus faucibus dictum eros a dignissim. Phasellus pretium dignissim nulla, ut maximus nulla euismod sed. Vivamus lacus ante, egestas id fringilla pellentesque, sagittis nec metus. Cras cursus scelerisque ipsum nec egestas.\n\n" +
                "Sed suscipit mi sem, sed porttitor orci semper sed. Duis in efficitur felis, vel varius urna. Aliquam finibus sem dolor, efficitur bibendum tellus elementum at. Etiam vitae diam ultricies, pulvinar mauris a, interdum dui. Curabitur aliquam eget purus ac blandit. Donec ultricies accumsan felis eu scelerisque. Vivamus placerat est magna, ac aliquam nulla faucibus quis. Donec imperdiet sit amet diam in euismod. Nulla mollis sapien quis risus dapibus scelerisque. In eleifend, magna in vehicula tempus, nunc nisi placerat nisl, vitae maximus purus odio sed libero. Nam placerat ac quam ac volutpat.\n\n" +
                "Sed lacinia, metus sed commodo scelerisque, arcu turpis tincidunt eros, vel pharetra mauris nisi a quam. Nam consectetur mattis velit, imperdiet vestibulum magna pellentesque vel. Curabitur mollis velit ac nisi eleifend dapibus. Mauris porttitor mi ut ipsum elementum, ac sollicitudin ipsum auctor. Etiam egestas risus vitae felis tempus fringilla. Aliquam facilisis risus ut lacus fringilla, non suscipit sapien bibendum. Vivamus consequat metus eget elit eleifend viverra. Sed pharetra neque justo, ut euismod enim lobortis quis. Praesent consectetur luctus enim, non tincidunt risus mollis eget. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Integer fringilla, lacus ut iaculis aliquet, justo tortor blandit dolor, ac fermentum orci lorem at mi. Phasellus dignissim elementum quam, sit amet maximus nisl sagittis ac. Orci varius natoque penatibus et magnis metus.")
            .build();

        assertThatThrownBy(() -> validator.validerAtPåkrevdeFriteksterErSatt(behandlingId, Collections.emptyList(), fritekstOppsummering, VedtaksbrevType.FRITEKST_FEILUTBETALING_BORTFALT))
            .hasMessageContaining("Ugyldig input: Oppsummeringstekst er for lang.");
    }
}
