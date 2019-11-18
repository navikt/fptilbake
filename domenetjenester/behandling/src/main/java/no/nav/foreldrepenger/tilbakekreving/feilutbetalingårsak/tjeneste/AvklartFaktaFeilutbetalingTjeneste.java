package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEvent;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDelDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

@ApplicationScoped
public class AvklartFaktaFeilutbetalingTjeneste {

    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    AvklartFaktaFeilutbetalingTjeneste() {
        // For CDI
    }

    @Inject
    public AvklartFaktaFeilutbetalingTjeneste(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository,
                                              HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
    }

    public void lagreÅrsakForFeilutbetalingPeriode(Behandling behandling, List<FaktaFeilutbetalingDto> feilutbetalingFaktas, String begrunnelse) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_OM_FEILUTBETALING);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        // brukte denne objekt for å opprette bare en historikkinnslagDel når saksbehandler endret bare begrunnelse
        HistorikkinnslagDelDto historikkinnslagDelDto = new HistorikkinnslagDelDto();

        Optional<FaktaFeilutbetaling> forrigeFakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandling.getId());

        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();
        boolean behovForHistorikkInnslag = false;
        for (FaktaFeilutbetalingDto faktaFeilutbetalingDto : feilutbetalingFaktas) {
            FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder()
                .medPeriode(faktaFeilutbetalingDto.getFom(), faktaFeilutbetalingDto.getTom())
                .medHendelseType(faktaFeilutbetalingDto.getHendelseType())
                .medHendelseUndertype(faktaFeilutbetalingDto.getHendelseUndertype())
                .medFeilutbetalinger(faktaFeilutbetaling)
                .build();
            faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);

            // lag historikkinnslagDeler
            boolean harEndret = lagHistorikkInnslagDeler(historikkinnslag, begrunnelse, forrigeFakta, faktaFeilutbetalingDto, historikkinnslagDelDto);
            behovForHistorikkInnslag = !behovForHistorikkInnslag ? harEndret : behovForHistorikkInnslag;
        }
        faktaFeilutbetaling.setBegrunnelse(begrunnelse);
        faktaFeilutbetalingRepository.lagre(behandling.getId(), faktaFeilutbetaling);

        if (behovForHistorikkInnslag) {
            historikkTjenesteAdapter.lagInnslag(historikkinnslag);
        }
    }

    public void slettGammelFaktaData(@Observes SlettGrunnlagEvent slettGrunnlagEvent){
        faktaFeilutbetalingRepository.slettFaktaFeilutbetaling(slettGrunnlagEvent.getBehandlingId());
    }

    private boolean lagHistorikkInnslagDeler(Historikkinnslag historikkinnslag, String begrunnelse,
                                             Optional<FaktaFeilutbetaling> forrigeFakta,
                                             FaktaFeilutbetalingDto faktaFeilutbetalingDto,
                                             HistorikkinnslagDelDto historikkinnslagDelDto) {
        boolean harEndret = false;
        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();
        if (forrigeFakta.isPresent()) {
            List<FaktaFeilutbetalingPeriode> feilutbetalingPerioder = forrigeFakta.get().getFeilutbetaltPerioder();
            String forrigeBegrunnelse = forrigeFakta.get().getBegrunnelse();
            Optional<FaktaFeilutbetalingPeriode> forrigeFeilutbetalingPeriodeÅrsak = feilutbetalingPerioder.stream()
                .filter(fpå -> fpå.getPeriode().equals(faktaFeilutbetalingDto.tilPeriode()))
                .findFirst();
            if (forrigeFeilutbetalingPeriodeÅrsak.isPresent()) {
                harEndret = håndtereEndretÅrsak(forrigeFeilutbetalingPeriodeÅrsak.get(), faktaFeilutbetalingDto, begrunnelse, forrigeBegrunnelse,
                    tekstBuilder, historikkinnslagDelDto);
            } else { // det betyr perioder har endret
                opprettNyHistorikkinnslagDel(begrunnelse, faktaFeilutbetalingDto, tekstBuilder);
                harEndret = true;
            }

        } else {
            opprettNyHistorikkinnslagDel(begrunnelse, faktaFeilutbetalingDto, tekstBuilder);
            harEndret = true;
        }
        if (harEndret) {
            tekstBuilder.build(historikkinnslag);
        }
        return harEndret;
    }

    private void opprettNyHistorikkinnslagDel(String begrunnelse, FaktaFeilutbetalingDto faktaFeilutbetalingDto, HistorikkInnslagTekstBuilder tekstBuilder) {
        mapFellesVerdier(tekstBuilder, begrunnelse, faktaFeilutbetalingDto);
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_ÅRSAK, null, faktaFeilutbetalingDto.getHendelseType());
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_UNDER_ÅRSAK, null, faktaFeilutbetalingDto.getHendelseUndertype());
    }

    private void mapFellesVerdier(HistorikkInnslagTekstBuilder tekstBuilder, String begrunnelse, FaktaFeilutbetalingDto faktaFeilutbetalingDto) {
        tekstBuilder.medSkjermlenke(SkjermlenkeType.FAKTA_OM_FEILUTBETALING)
            .medBegrunnelse(begrunnelse)
            .medOpplysning(HistorikkOpplysningType.PERIODE_FOM, faktaFeilutbetalingDto.getFom())
            .medOpplysning(HistorikkOpplysningType.PERIODE_TOM, faktaFeilutbetalingDto.getTom());
    }

    private boolean håndtereEndretÅrsak(FaktaFeilutbetalingPeriode forrigePeiodeÅrsak, FaktaFeilutbetalingDto faktaFeilutbetalingDto,
                                        String begrunnelse, String forrigeBegrunnelse,
                                        HistorikkInnslagTekstBuilder tekstBuilder, HistorikkinnslagDelDto historikkinnslagDelDto) {

        if (erÅrsakEllerUnderÅrsakEndret(faktaFeilutbetalingDto, forrigePeiodeÅrsak)) {
            mapFellesVerdier(tekstBuilder, begrunnelse, faktaFeilutbetalingDto);
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_ÅRSAK, forrigePeiodeÅrsak.getHendelseType(), faktaFeilutbetalingDto.getHendelseType());
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.HENDELSE_UNDER_ÅRSAK, forrigePeiodeÅrsak.getHendelseUndertype(), faktaFeilutbetalingDto.getHendelseUndertype());
            return true;
        } else if (historikkinnslagDelDto.getBegrunnelseFritekst() == null && harBegrunnelseEndret(forrigeBegrunnelse, begrunnelse)) {
            tekstBuilder.medSkjermlenke(SkjermlenkeType.FAKTA_OM_FEILUTBETALING)
                .medBegrunnelse(begrunnelse);
            historikkinnslagDelDto.setBegrunnelseFritekst(begrunnelse);
            return true;

        }
        return false;
    }

    private boolean erÅrsakEllerUnderÅrsakEndret(FaktaFeilutbetalingDto faktaFeilutbetalingDto,
                                                 FaktaFeilutbetalingPeriode forrigePeriodeÅrsak) {
        boolean erEndret = false;
        HendelseType nyHendelseType = faktaFeilutbetalingDto.getHendelseType();
        HendelseUnderType nyHendelseUndertype = faktaFeilutbetalingDto.getHendelseUndertype();

        if (faktaFeilutbetalingDto.tilPeriode().isEqual(forrigePeriodeÅrsak.getPeriode())) {
            erEndret = !Objects.equals(forrigePeriodeÅrsak.getHendelseType(), nyHendelseType) ||
                !Objects.equals(forrigePeriodeÅrsak.getHendelseUndertype(), nyHendelseUndertype);
        }
        return erEndret;
    }

    private boolean harBegrunnelseEndret(String forrigeBegrunnelse, String begrunnelse) {
        return StringUtils.isNotEmpty(forrigeBegrunnelse) && !forrigeBegrunnelse.equalsIgnoreCase(begrunnelse);
    }

}
