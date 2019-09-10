package no.nav.foreldrepenger.tilbakekreving;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatGodTroDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.Feilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingPeriodeÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;

/**
 * Opprettet for å forenkle unit-tester
 * Setter opp repoer og tjenester.
 * feltnavn angir om repo/tjeneste er mock eller ikke.
 */
public class FellesTestOppsett extends TestOppsett {

    protected static final LocalDate FOM = LocalDate.of(2016, 3, 10);
    protected static final LocalDate TOM = LocalDate.of(2016, 5, 31);
    protected static final Long SUM_FEIL_UTBETALT = 23000L;
    protected static final Long SUM_INNTREKK = 1000L;
    protected static final String ÅRSAK = "UTTAK_UTSETTELSE_TYPE";
    protected static final String UNDER_ÅRSAK = "ARBEID_HELTID";
    protected static final String BEGRUNNELSE = "ABC";
    protected static final String ÅRSAK_KODEVERK = HendelseType.DISCRIMINATOR;
    protected static final String UNDER_ÅRSAK_KODEVERK = HendelseUnderType.DISCRIMINATOR;
    protected static final Period defaultVentetid = Period.ofWeeks(4);
    protected static final String BEHANDLENDE_ENHET_ID = "4833";
    protected static final String BEHANDLENDE_ENHET_NAVN = "NAV Familie- og pensjonsytelser Oslo 1";

    protected AktørId aktørId;
    protected Saksnummer saksnummer;
    protected Long fagsakId;
    protected Long internBehandlingId;
    protected Long eksternBehandlingId;
    protected UUID eksternBehandlingUuid;
    protected Behandling behandling;

    protected FagsakTjeneste fagsakTjeneste = new FagsakTjeneste(
        mockTpsTjeneste,
        repoProvider.getFagsakRepository(),
        brukerRepository);

    protected BehandlingTjeneste behandlingTjeneste = new BehandlingTjenesteImpl(
        repoProvider,
        behandlingskontrollProvider,
        mockSimuleringIntegrasjonTjeneste,
        fagsakTjeneste,
        mockHistorikkTjeneste,
        mockFpsakKlient,
        defaultVentetid);

    private TestUtility testUtility = new TestUtility(behandlingTjeneste);

    private SimuleringResultatDto simResDto = new SimuleringResultatDto(SUM_FEIL_UTBETALT, SUM_INNTREKK);

    @Before
    public void init() {
        aktørId = testUtility.genererAktørId();
        when(mockTpsTjeneste.hentBrukerForAktør(aktørId)).thenReturn(testUtility.lagPersonInfo(aktørId));
        when(mockFpsakKlient.hentBehandling(any(UUID.class))).thenReturn(lagEksternBehandlingInfoDto());

        TestUtility.SakDetaljer sakDetaljer = testUtility.opprettFørstegangsBehandling(aktørId);
        mapSakDetaljer(sakDetaljer);
        when(mockSimuleringIntegrasjonTjeneste.hentResultat(eksternBehandlingId)).thenReturn(Optional.of(simResDto));
    }

    protected String formatDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    protected String getTilVerdi(Optional<HistorikkinnslagFelt> felt) {
        if (felt.isPresent()) {
            return felt.get().getTilVerdi();
        }
        return null;
    }

    protected String getFraVerdi(Optional<HistorikkinnslagFelt> felt) {
        if (felt.isPresent()) {
            return felt.get().getFraVerdi();
        }
        return null;
    }

    protected FeilutbetalingAggregate formFeilutbetalingAggregate() {
        Feilutbetaling feilutbetaling = new Feilutbetaling();
        FeilutbetalingPeriodeÅrsak periodeÅrsak = FeilutbetalingPeriodeÅrsak.builder()
            .medÅrsak(ÅRSAK)
            .medÅrsakKodeverk(ÅRSAK_KODEVERK)
            .medUnderÅrsak(UNDER_ÅRSAK)
            .medUnderÅrsakKodeverk(UNDER_ÅRSAK_KODEVERK)
            .medPeriode(FOM, TOM)
            .medFeilutbetalinger(feilutbetaling).build();
        feilutbetaling.leggTilFeilutbetaltPeriode(periodeÅrsak);
        return FeilutbetalingAggregate.builder()
            .medBehandlingId(internBehandlingId)
            .medFeilutbetaling(feilutbetaling).build();
    }

    protected VilkårsvurderingPerioderDto formVilkårsvurderingPerioderDto(VilkårResultat resultat, LocalDate fom, LocalDate tom, Aktsomhet aktsomhet) {
        VilkårsvurderingPerioderDto perioderDto = new VilkårsvurderingPerioderDto();
        perioderDto.setVilkårResultat(resultat);
        perioderDto.setFom(fom);
        perioderDto.setTom(tom);
        perioderDto.setBegrunnelse("begrunnelse");
        if (resultat == VilkårResultat.GOD_TRO) {
            perioderDto.setVilkarResultatInfo(new VilkårResultatGodTroDto("godTro begrunnelse", true, BigDecimal.valueOf(1000.00)));
        } else {
            VilkårResultatAktsomhetDto aktsomhetDto = new VilkårResultatAktsomhetDto();
            aktsomhetDto.setSærligeGrunner(Lists.newArrayList(SærligGrunn.GRAD_AV_UAKTSOMHET, SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL));
            if (aktsomhet == Aktsomhet.GROVT_UAKTSOM) {
                aktsomhetDto.setHarGrunnerTilReduksjon(false);
                aktsomhetDto.setIleggRenter(true);
                aktsomhetDto.setAndelTilbakekreves(100);
            } else if (aktsomhet == Aktsomhet.SIMPEL_UAKTSOM) {
                aktsomhetDto.setHarGrunnerTilReduksjon(true);
                aktsomhetDto.setTilbakekrevesBelop(BigDecimal.valueOf(2000.00));
            }
            perioderDto.setVilkarResultatInfo(new VilkårResultatAnnetDto("annet begrunnelse", aktsomhet, aktsomhetDto));
        }
        return perioderDto;
    }

    private void mapSakDetaljer(TestUtility.SakDetaljer sakDetaljer) {
        aktørId = sakDetaljer.getAktørId();
        saksnummer = sakDetaljer.getSaksnummer();
        fagsakId = sakDetaljer.getFagsakId();
        internBehandlingId = sakDetaljer.getInternBehandlingId();
        eksternBehandlingId = sakDetaljer.getEksternBehandlingId();
        eksternBehandlingUuid = sakDetaljer.getEksternUuid();
        behandling = sakDetaljer.getBehandling();
    }

    private Optional<EksternBehandlingsinfoDto> lagEksternBehandlingInfoDto() {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setId(10001L);
        eksternBehandlingsinfoDto.setBehandlendeEnhetId(BEHANDLENDE_ENHET_ID);
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn(BEHANDLENDE_ENHET_NAVN);
        return Optional.of(eksternBehandlingsinfoDto);
    }
}
