package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagEndretFeltDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

public class AvklartFaktaFeilutbetalingTjenesteTest extends FellesTestOppsett {

    private DokumentArkivTjeneste mockDokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);

    private HistorikkInnslagKonverter historikkInnslagKonverter = new HistorikkInnslagKonverter(kodeverkRepository, repoProvider.getAksjonspunktRepository());
    private HistorikkTjenesteAdapter historikkTjenesteAdapter = new HistorikkTjenesteAdapter(historikkRepository, historikkInnslagKonverter, mockDokumentArkivTjeneste);

    private AvklartFaktaFeilutbetalingTjeneste avklartFaktaFeilutbetalingTjeneste = new AvklartFaktaFeilutbetalingTjeneste(faktaFeilutbetalingRepository, historikkTjenesteAdapter);

    private Behandling nyBehandling;

    @Before
    public void setup() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        nyBehandling = scenario.lagre(repoProvider);
    }

    @Test
    public void lagreÅrsakForFeilutbetalingPeriode_medUnderÅrsak() {
        FaktaFeilutbetalingDto faktaFeilutbetalingDto = formFaktaFeilutbetaling();

        avklartFaktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(nyBehandling, Arrays.asList(faktaFeilutbetalingDto), BEGRUNNELSE);

        Optional<FaktaFeilutbetaling> feilutbetalingAggregate = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(nyBehandling.getId());
        assertThat(feilutbetalingAggregate).isNotEmpty();

        List<FaktaFeilutbetalingPeriode> feilutbetalingPerioder = feilutbetalingAggregate.get().getFeilutbetaltPerioder();
        assertThat(feilutbetalingPerioder.size()).isEqualTo(1);

        FaktaFeilutbetalingPeriode faktaPeriode = feilutbetalingPerioder.get(0);
        assertThat(faktaPeriode.getPeriode()).isEqualTo(Periode.of(FOM, TOM));
        assertThat(faktaPeriode.getHendelseType()).isEqualTo(HENDELSE_TYPE);
        assertThat(faktaPeriode.getHendelseUndertype()).isEqualTo(HENDELSE_UNDERTYPE);

        testHistorikkInnslag();
    }

    @Test
    public void lagreÅrsakForFeilutbetalingPeriode_medUnderÅrsakOgFlerePeriode() {
        FaktaFeilutbetalingDto faktaFeilutbetalingDto = formFaktaFeilutbetaling();

        LocalDate sisteDagIPeriode = LocalDate.now();

        HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto = new HendelseTypeMedUndertypeDto(HENDELSE_TYPE, HENDELSE_UNDERTYPE);

        avklartFaktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(nyBehandling,
            Arrays.asList(faktaFeilutbetalingDto,
                new FaktaFeilutbetalingDto(TOM, sisteDagIPeriode, feilutbetalingÅrsakDto)), BEGRUNNELSE);

        Optional<FaktaFeilutbetaling> feilutbetalingAggregate = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(nyBehandling.getId());
        assertThat(feilutbetalingAggregate).isNotEmpty();

        List<FaktaFeilutbetalingPeriode> feilutbetalingPerioder = new ArrayList<>(feilutbetalingAggregate.get().getFeilutbetaltPerioder());
        assertThat(feilutbetalingPerioder.size()).isEqualTo(2);
        feilutbetalingPerioder.sort(Comparator.comparing(fpå -> fpå.getPeriode().getFom()));

        FaktaFeilutbetalingPeriode førstePeriode = feilutbetalingPerioder.get(0);
        assertThat(førstePeriode.getPeriode().getFom()).isEqualTo(FOM);
        assertThat(førstePeriode.getHendelseType()).isEqualTo(HENDELSE_TYPE);
        assertThat(førstePeriode.getHendelseUndertype()).isEqualTo(HENDELSE_UNDERTYPE);

        FaktaFeilutbetalingPeriode andrePeriode = feilutbetalingPerioder.get(1);
        assertThat(andrePeriode.getPeriode().getFom()).isEqualTo(TOM);
        assertThat(andrePeriode.getHendelseType()).isEqualTo(HENDELSE_TYPE);
        assertThat(andrePeriode.getHendelseUndertype()).isEqualTo(HENDELSE_UNDERTYPE);

        List<HistorikkinnslagDto> historikkinnslager = testHistorikkInnslag();
        assertThat(historikkinnslager.get(0).getHistorikkinnslagDeler()).isNotEmpty();
        assertThat(historikkinnslager.get(0).getHistorikkinnslagDeler().size()).isEqualTo(2);
    }

    @Test
    public void lagreÅrsakForFeilutbetalingPeriode_nårForrigeÅrsakAlleredeFinnes() {

        faktaFeilutbetalingRepository.lagre(internBehandlingId, lagFaktaFeilutbetaling());

        HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto = new HendelseTypeMedUndertypeDto(HENDELSE_TYPE, HENDELSE_UNDERTYPE);

        avklartFaktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(nyBehandling,
            Arrays.asList(new FaktaFeilutbetalingDto(FOM, TOM, feilutbetalingÅrsakDto)), BEGRUNNELSE);

        Optional<FaktaFeilutbetaling> aggregate = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(nyBehandling.getId());
        assertThat(aggregate).isNotEmpty();

        List<FaktaFeilutbetalingPeriode> feilutbetalingPerioder = aggregate.get().getFeilutbetaltPerioder();
        assertThat(feilutbetalingPerioder.size()).isEqualTo(1);

        FaktaFeilutbetalingPeriode faktaPeriode = feilutbetalingPerioder.get(0);
        assertThat(faktaPeriode.getPeriode()).isEqualTo(Periode.of(FOM, TOM));
        assertThat(faktaPeriode.getHendelseType()).isEqualTo(HENDELSE_TYPE);
        assertThat(faktaPeriode.getHendelseUndertype()).isEqualTo(HENDELSE_UNDERTYPE);

        List<HistorikkinnslagDto> historikkinnslager = testHistorikkInnslag();
        List<HistorikkinnslagEndretFeltDto> endredeFelter = historikkinnslager.get(0).getHistorikkinnslagDeler().get(0).getEndredeFelter();
        assertThat(endredeFelter).isNotEmpty();
        assertThat(endredeFelter.size()).isEqualTo(2);

    }

    private FaktaFeilutbetalingDto formFaktaFeilutbetaling() {
        HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto = new HendelseTypeMedUndertypeDto(HENDELSE_TYPE, HENDELSE_UNDERTYPE);
        return new FaktaFeilutbetalingDto(FOM, TOM, feilutbetalingÅrsakDto);
    }

    private List<HistorikkinnslagDto> testHistorikkInnslag() {
        List<HistorikkinnslagDto> historikkinnslager = historikkTjenesteAdapter.hentAlleHistorikkInnslagForSak(nyBehandling.getFagsak().getSaksnummer());
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.get(0).getAktoer()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslager.get(0).getType()).isEqualTo(HistorikkinnslagType.FAKTA_OM_FEILUTBETALING);
        assertThat(historikkinnslager.get(0).getHistorikkinnslagDeler().get(0).getSkjermlenke()).isEqualTo(SkjermlenkeType.FAKTA_OM_FEILUTBETALING);
        assertThat(historikkinnslager.get(0).getHistorikkinnslagDeler().get(0).getBegrunnelseFritekst()).isEqualTo(BEGRUNNELSE);

        return historikkinnslager;
    }

}
