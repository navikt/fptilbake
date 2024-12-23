package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType.FAKTA_OM_FEILUTBETALING;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.DATE_FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class AvklartFaktaFeilutbetalingTjenesteTest extends FellesTestOppsett {

    private AvklartFaktaFeilutbetalingTjeneste avklartFaktaFeilutbetalingTjeneste;

    private Behandling nyBehandling;

    @BeforeEach
    void setup() {
        var scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        nyBehandling = scenario.lagre(repoProvider);
        var avklartFaktaFeilutbetalingHistorikkTjeneste = new AvklartFaktaFeilutbetalingHistorikkTjeneste(
            historikkinnslagRepository);
        avklartFaktaFeilutbetalingTjeneste = new AvklartFaktaFeilutbetalingTjeneste(faktaFeilutbetalingRepository, avklartFaktaFeilutbetalingHistorikkTjeneste);
    }

    @Test
    void lagreÅrsakForFeilutbetalingPeriode_medUnderÅrsak() {
        FaktaFeilutbetalingDto faktaFeilutbetalingDto = formFaktaFeilutbetaling();

        avklartFaktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(nyBehandling, List.of(faktaFeilutbetalingDto), BEGRUNNELSE);

        Optional<FaktaFeilutbetaling> feilutbetalingAggregate = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(nyBehandling.getId());
        assertThat(feilutbetalingAggregate).isNotEmpty();

        List<FaktaFeilutbetalingPeriode> feilutbetalingPerioder = feilutbetalingAggregate.get().getFeilutbetaltPerioder();
        assertThat(feilutbetalingPerioder.size()).isEqualTo(1);

        FaktaFeilutbetalingPeriode faktaPeriode = feilutbetalingPerioder.get(0);
        assertThat(faktaPeriode.getPeriode()).isEqualTo(Periode.of(faktaFeilutbetalingDto.getFom(), faktaFeilutbetalingDto.getTom()));
        assertThat(faktaPeriode.getHendelseType()).isEqualTo(faktaFeilutbetalingDto.getHendelseType());
        assertThat(faktaPeriode.getHendelseUndertype()).isEqualTo(faktaFeilutbetalingDto.getHendelseUndertype());

        var historikkinnslagene = historikkinnslagRepository.hent(nyBehandling.getFagsak().getSaksnummer());
        assertThat(historikkinnslagene).hasSize(1);
        var historikkinnslag = historikkinnslagene.getFirst();
        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(FAKTA_OM_FEILUTBETALING);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getLinjer()).hasSize(4);
        assertThat(historikkinnslag.getLinjer().get(0).getTekst()).contains("Vurdering av perioden", DATE_FORMATTER.format(faktaFeilutbetalingDto.getFom()), DATE_FORMATTER.format(faktaFeilutbetalingDto.getTom()));
        assertThat(historikkinnslag.getLinjer().get(1).getTekst()).contains("Årsak til feilutbetaling", "er satt til", faktaFeilutbetalingDto.getHendelseType().getNavn(), faktaFeilutbetalingDto.getHendelseUndertype().getNavn());
        assertThat(historikkinnslag.getLinjer().get(2).getType()).isEqualTo(HistorikkinnslagLinjeType.LINJESKIFT);
        assertThat(historikkinnslag.getLinjer().get(3).getTekst()).contains(BEGRUNNELSE);

    }

    @Test
    void lagreÅrsakForFeilutbetalingPeriode_medUnderÅrsakOgFlerePeriode() {
        FaktaFeilutbetalingDto faktaFeilutbetalingDto = formFaktaFeilutbetaling();

        LocalDate sisteDagIPeriode = LocalDate.now();

        HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto = new HendelseTypeMedUndertypeDto(HENDELSE_TYPE, HENDELSE_UNDERTYPE);

        avklartFaktaFeilutbetalingTjeneste.lagreÅrsakForFeilutbetalingPeriode(nyBehandling,
                List.of(faktaFeilutbetalingDto,
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

        var historikkinnslager = testHistorikkInnslag();
        var historikkinnslag = historikkinnslager.getFirst();
        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(FAKTA_OM_FEILUTBETALING);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getLinjer().get(0).getTekst()).contains("Vurdering av perioden", DATE_FORMATTER.format(førstePeriode.getPeriode().getFom()), DATE_FORMATTER.format(førstePeriode.getPeriode().getTom()));
        assertThat(historikkinnslag.getLinjer().get(1).getTekst()).contains("Årsak til feilutbetaling", "er satt til", førstePeriode.getHendelseType().getNavn(), førstePeriode.getHendelseUndertype().getNavn());
        assertThat(historikkinnslag.getLinjer().get(2).getType()).isEqualTo(HistorikkinnslagLinjeType.LINJESKIFT);

        assertThat(historikkinnslag.getLinjer().get(3).getTekst()).contains("Vurdering av perioden", DATE_FORMATTER.format(andrePeriode.getPeriode().getFom()), DATE_FORMATTER.format(andrePeriode.getPeriode().getFom()));
        assertThat(historikkinnslag.getLinjer().get(4).getTekst()).contains("Årsak til feilutbetaling", "er satt til", andrePeriode.getHendelseType().getNavn(), andrePeriode.getHendelseUndertype().getNavn());
        assertThat(historikkinnslag.getLinjer().get(5).getType()).isEqualTo(HistorikkinnslagLinjeType.LINJESKIFT);
        assertThat(historikkinnslag.getLinjer().get(6).getTekst()).contains(BEGRUNNELSE);
    }

    @Test
    void lagreÅrsakForFeilutbetalingPeriode_nårForrigeÅrsakAlleredeFinnes() {

        var gammelFaktaFeilutbetaling = lagFaktaFeilutbetaling();
        faktaFeilutbetalingRepository.lagre(nyBehandling.getId(), gammelFaktaFeilutbetaling);

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

        var historikkinnslager = testHistorikkInnslag();
        var historikkinnslag = historikkinnslager.getFirst();
        assertThat(historikkinnslag.getSkjermlenke()).isEqualTo(FAKTA_OM_FEILUTBETALING);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getLinjer().getFirst().getTekst()).contains(BEGRUNNELSE);
    }

    private FaktaFeilutbetalingDto formFaktaFeilutbetaling() {
        HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto = new HendelseTypeMedUndertypeDto(HENDELSE_TYPE, HENDELSE_UNDERTYPE);
        return new FaktaFeilutbetalingDto(FOM, TOM, feilutbetalingÅrsakDto);
    }

    private List<Historikkinnslag> testHistorikkInnslag() {
        var historikkinnslager = historikkinnslagRepository.hent(nyBehandling.getFagsak().getSaksnummer());
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.getFirst().getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslager.getFirst().getSkjermlenke()).isEqualTo(SkjermlenkeType.FAKTA_OM_FEILUTBETALING);
        return historikkinnslager;
    }

}
