package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDelDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.vedtak.util.StringUtils;

@ApplicationScoped
public class AvklartFaktaFeilutbetalingTjeneste {

    private static final String UNDERÅRSAK_KODE = "UNDERÅRSAK_KODE";
    private static final String UNDERÅRSAK = "UNDERÅRSAK";
    private static final String UNDERÅRSAK_KODEVERK = "UNDERÅRSAK_KODEVERK";

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private KodeverkRepository kodeverkRepository;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    AvklartFaktaFeilutbetalingTjeneste() {
        // For CDI
    }

    @Inject
    public AvklartFaktaFeilutbetalingTjeneste(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository, KodeverkRepository kodeverkRepository,
                                              HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.kodeverkRepository = kodeverkRepository;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
    }

    public void lagreÅrsakForFeilutbetalingPeriode(Behandling behandling, List<FaktaFeilutbetalingDto> feilutbetalingFaktas, String begrunnelse) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_OM_FEILUTBETALING);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        // brukte denne objekt for å opprette bare en historikkinnslagDel når saksbehandler endret bare begrunnelse
        HistorikkinnslagDelDto historikkinnslagDelDto = new HistorikkinnslagDelDto();

        Optional<FaktaFeilutbetalingAggregate> forrigeFeilutbetalingAggregate = faktaFeilutbetalingRepository.finnFeilutbetaling(behandling.getId());

        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();
        boolean behovForHistorikkInnslag = false;
        for (FaktaFeilutbetalingDto faktaFeilutbetalingDto : feilutbetalingFaktas) {
            UnderÅrsakDto underÅrsakDto = null;
            List<UnderÅrsakDto> underÅrsaker = faktaFeilutbetalingDto.getÅrsak().getUnderÅrsaker();
            if (!underÅrsaker.isEmpty()) {
                // Fordi vi har bare et underÅrsak som saksbehandler kan velge
                underÅrsakDto = underÅrsaker.get(0);
            }
            FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder()
                .medPeriode(faktaFeilutbetalingDto.getFom(), faktaFeilutbetalingDto.getTom())
                .medÅrsak(faktaFeilutbetalingDto.getÅrsak().getÅrsakKode(), kodeverkRepository)
                .medUnderÅrsak(sjekkOgReturnereUnderårsak(underÅrsakDto, UNDERÅRSAK_KODE), kodeverkRepository)
                .medFeilutbetalinger(faktaFeilutbetaling)
                .build();
            faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);

            // lag historikkinnslagDeler
            boolean harEndret = lagHistorikkInnslagDeler(behandling, historikkinnslag, begrunnelse, forrigeFeilutbetalingAggregate, faktaFeilutbetalingDto, underÅrsakDto, historikkinnslagDelDto);
            behovForHistorikkInnslag = !behovForHistorikkInnslag ? harEndret : behovForHistorikkInnslag;
        }
        FaktaFeilutbetalingAggregate faktaFeilutbetalingAggregate = FaktaFeilutbetalingAggregate.builder()
            .medFeilutbetaling(faktaFeilutbetaling)
            .medBehandlingId(behandling.getId()).build();

        faktaFeilutbetalingRepository.lagre(faktaFeilutbetalingAggregate);

        if (behovForHistorikkInnslag) {
            historikkTjenesteAdapter.lagInnslag(historikkinnslag);
        }
    }

    private boolean lagHistorikkInnslagDeler(Behandling behandling, Historikkinnslag historikkinnslag, String begrunnelse,
                                             Optional<FaktaFeilutbetalingAggregate> forrigeFeilutbetalingAggregate,
                                             FaktaFeilutbetalingDto faktaFeilutbetalingDto,
                                             UnderÅrsakDto underÅrsakDto, HistorikkinnslagDelDto historikkinnslagDelDto) {
        boolean harEndret = false;
        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();
        if (forrigeFeilutbetalingAggregate.isPresent()) {
            List<FaktaFeilutbetalingPeriode> feilutbetalingPerioder = forrigeFeilutbetalingAggregate.get().getFaktaFeilutbetaling().getFeilutbetaltPerioder();
            Optional<FaktaFeilutbetalingPeriode> forrigeFeilutbetalingPeriodeÅrsak = feilutbetalingPerioder.stream()
                .filter(fpå -> fpå.getPeriode().equals(faktaFeilutbetalingDto.tilPeriode()))
                .findFirst();
            if (forrigeFeilutbetalingPeriodeÅrsak.isPresent()) {
                harEndret = håndtereEndretÅrsak(behandling, forrigeFeilutbetalingPeriodeÅrsak.get(), faktaFeilutbetalingDto, underÅrsakDto,
                    begrunnelse, tekstBuilder, historikkinnslagDelDto);
            } else { // det betyr perioder har endret
                opprettNyHistorikkinnslagDel(begrunnelse, faktaFeilutbetalingDto, underÅrsakDto, tekstBuilder);
                harEndret = true;
            }

        } else {
            opprettNyHistorikkinnslagDel(begrunnelse, faktaFeilutbetalingDto, underÅrsakDto, tekstBuilder);
            harEndret = true;
        }
        if (harEndret) {
            tekstBuilder.build(historikkinnslag);
        }
        return harEndret;
    }

    private void opprettNyHistorikkinnslagDel(String begrunnelse, FaktaFeilutbetalingDto faktaFeilutbetalingDto, UnderÅrsakDto underÅrsakDto, HistorikkInnslagTekstBuilder tekstBuilder) {
        mapFellesVerdier(tekstBuilder, begrunnelse, faktaFeilutbetalingDto);
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_ÅRSAK, null, faktaFeilutbetalingDto.getÅrsak().getÅrsak());
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_UNDER_ÅRSAK, null, sjekkOgReturnereUnderårsak(underÅrsakDto, UNDERÅRSAK));
    }

    private void mapFellesVerdier(HistorikkInnslagTekstBuilder tekstBuilder, String begrunnelse, FaktaFeilutbetalingDto faktaFeilutbetalingDto) {
        tekstBuilder.medSkjermlenke(SkjermlenkeType.FAKTA_OM_FEILUTBETALING)
            .medBegrunnelse(begrunnelse)
            .medOpplysning(HistorikkOpplysningType.PERIODE_FOM, faktaFeilutbetalingDto.getFom())
            .medOpplysning(HistorikkOpplysningType.PERIODE_TOM, faktaFeilutbetalingDto.getTom());
    }

    private boolean håndtereEndretÅrsak(Behandling behandling, FaktaFeilutbetalingPeriode forrigePeiodeÅrsak, FaktaFeilutbetalingDto faktaFeilutbetalingDto,
                                        UnderÅrsakDto underÅrsakDto, String begrunnelse,
                                        HistorikkInnslagTekstBuilder tekstBuilder, HistorikkinnslagDelDto historikkinnslagDelDto) {
        if (sjekkHvisÅrsakEllerUnderÅrsakEndret(faktaFeilutbetalingDto, sjekkOgReturnereUnderårsak(underÅrsakDto, UNDERÅRSAK_KODE), forrigePeiodeÅrsak)) {
            mapFellesVerdier(tekstBuilder, begrunnelse, faktaFeilutbetalingDto);
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_ÅRSAK,
                kodeverkRepository.hentKodeliste(forrigePeiodeÅrsak.getÅrsakKodeverk(), forrigePeiodeÅrsak.getÅrsak()).getNavn(),
                faktaFeilutbetalingDto.getÅrsak().getÅrsak());
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_UNDER_ÅRSAK,
                hentForrigeUnderÅrsak(forrigePeiodeÅrsak.getUnderÅrsakKodeverk(), forrigePeiodeÅrsak.getUnderÅrsak()),
                sjekkOgReturnereUnderårsak(underÅrsakDto, UNDERÅRSAK));
            return true;
        } else if (historikkinnslagDelDto.getBegrunnelseFritekst() == null && harBegrunnelseEndret(behandling, begrunnelse)) {
            tekstBuilder.medSkjermlenke(SkjermlenkeType.FAKTA_OM_FEILUTBETALING)
                .medBegrunnelse(begrunnelse);
            historikkinnslagDelDto.setBegrunnelseFritekst(begrunnelse);
            return true;

        }
        return false;
    }

    private String hentForrigeUnderÅrsak(String underÅrsakKodeverk, String underÅrsakKode) {
        if (StringUtils.nullOrEmpty(underÅrsakKode)) {
            return null;
        }
        return kodeverkRepository.hentKodeliste(underÅrsakKodeverk, underÅrsakKode).getNavn();
    }

    private boolean sjekkHvisÅrsakEllerUnderÅrsakEndret(FaktaFeilutbetalingDto faktaFeilutbetalingDto, String underÅrsakKode,
                                                        FaktaFeilutbetalingPeriode forrigePeriodeÅrsak) {
        boolean erEndret = false;
        if (faktaFeilutbetalingDto.tilPeriode().isEqual(forrigePeriodeÅrsak.getPeriode())) {
            erEndret = !forrigePeriodeÅrsak.getÅrsak().equals(faktaFeilutbetalingDto.getÅrsak().getÅrsakKode());
            if (!erEndret && !StringUtils.nullOrEmpty(forrigePeriodeÅrsak.getUnderÅrsak())) {
                erEndret = !forrigePeriodeÅrsak.getUnderÅrsak().equals(underÅrsakKode);
            }
        }
        return erEndret;
    }

    private String sjekkOgReturnereUnderårsak(UnderÅrsakDto underÅrsakDto, String felt) {
        switch (felt) {
            case UNDERÅRSAK_KODE:
                return underÅrsakDto != null ? underÅrsakDto.getUnderÅrsakKode() : null;
            case UNDERÅRSAK:
                return underÅrsakDto != null ? underÅrsakDto.getUnderÅrsak() : null;
            case UNDERÅRSAK_KODEVERK:
                return underÅrsakDto != null ? underÅrsakDto.getKodeverk() : null;
            default:
                return null;
        }
    }

    private boolean harBegrunnelseEndret(Behandling behandling, String begrunnelse) {
        String forrigeBegrunnelse = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING).getBegrunnelse();
        return !forrigeBegrunnelse.equals(begrunnelse);
    }

}
