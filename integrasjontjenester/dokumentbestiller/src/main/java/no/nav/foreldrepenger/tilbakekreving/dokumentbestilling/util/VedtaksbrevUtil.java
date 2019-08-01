package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatGodTroDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatInfoDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.PeriodeMedBrevtekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;

public class VedtaksbrevUtil {

    private VedtaksbrevUtil() {
        //for static access
    }


    public static List<PeriodeMedBrevtekst> lagSortertePerioderMedTekst(List<VilkårsvurderingPerioderDto> vilkårsvurderingPerioder, List<PeriodeMedTekstDto> saksbehandlersFritekst) {
        //TODO (Trine): slå sammen perioder som kommer rett etter hverandre OG har samme vilkår valgt. PFP-7975
        return vilkårsvurderingPerioder.stream()
            .sorted(Comparator.comparing(VilkårsvurderingPerioderDto::getFom))
            .map(vilkårsperiode -> {
                PeriodeMedBrevtekst.Builder builder = new PeriodeMedBrevtekst.Builder()
                    .medFom(vilkårsperiode.getFom())
                    .medTom(vilkårsperiode.getTom())
                    .medGenerertFaktaAvsnitt(lagGenerertTekstOmFakta(vilkårsperiode))
                    .medGenerertVilkårAvsnitt(lagGenerertTekstOmVilkår(vilkårsperiode))
                    .medGenerertSærligeGrunnerAvsnitt(lagGenerertTekstOmSærligeGrunner(vilkårsperiode));

                Optional<PeriodeMedTekstDto> tilsvarendePeriodeFraSaksbehandler = finnTilsvarendePeriode(saksbehandlersFritekst, vilkårsperiode.getFom());
                tilsvarendePeriodeFraSaksbehandler.ifPresent(periodeMedTekstDto -> builder
                    .medFritekstFakta(periodeMedTekstDto.getFaktaAvsnitt())
                    .medFritekstVilkår(periodeMedTekstDto.getVilkårAvsnitt())
                    .medFritekstSærligeGrunner(periodeMedTekstDto.getSærligeGrunnerAvsnitt()));
                return builder.build();
            }).collect(Collectors.toList());
    }

    private static Optional<PeriodeMedTekstDto> finnTilsvarendePeriode(List<PeriodeMedTekstDto> saksbehandlersFritekst, LocalDate fom) {
        if (saksbehandlersFritekst != null && !saksbehandlersFritekst.isEmpty()) {
            return saksbehandlersFritekst.stream()
                .filter(periodeMedFritekst -> periodeMedFritekst.getFom().equals(fom))
                .findFirst();
        }
        return Optional.empty();
    }

    //TODO (Trine): ordentlig tekst vil legges inn her når Nina er klar med de. Hensikt nå er bare å ramse opp. PFP-7975
    private static String lagGenerertTekstOmFakta(VilkårsvurderingPerioderDto vilkårsperiode) {
        VilkårResultatInfoDto vilkårResultatInfoDto = vilkårsperiode.getVilkarResultatInfo();
        if (vilkårResultatInfoDto instanceof VilkårResultatGodTroDto) {
            return "Bruker har handlet i god tro, med og beløpet som skal tilbakekreves er " +
                ((VilkårResultatGodTroDto) vilkårResultatInfoDto).getTilbakekrevesBelop();
        } else {
            return "Fant ingen vilkår. ";
        }
    }

    //TODO (Trine): ordentlig tekst vil legges inn her når Nina er klar med de. Hensikt nå er bare å ramse opp. PFP-7975
    private static String lagGenerertTekstOmVilkår(VilkårsvurderingPerioderDto vilkårsperiode) {
        return "Vurderingen av denne perioden er: " + vilkårsperiode.getVilkårResultat().getKode();
    }

    //TODO (Trine): ordentlig tekst vil legges inn her når Nina er klar med de. Hensikt nå er bare å ramse opp. PFP-7975
    private static String lagGenerertTekstOmSærligeGrunner(VilkårsvurderingPerioderDto vilkårsperiode) {
        VilkårResultatInfoDto vilkårResultatInfoDto = vilkårsperiode.getVilkarResultatInfo();
        if (vilkårResultatInfoDto instanceof VilkårResultatAnnetDto) {
            Aktsomhet aktsomhet = ((VilkårResultatAnnetDto) vilkårResultatInfoDto).getAktsomhet();
            VilkårResultatAktsomhetDto aktsomhetInfo = ((VilkårResultatAnnetDto) vilkårResultatInfoDto).getAktsomhetInfo();
            if (aktsomhetInfo != null) {
                StringBuilder særligeGrunnerStringBuilder = new StringBuilder();
                List<SærligGrunn> særligeGrunner = aktsomhetInfo.getSærligeGrunner();
                if (særligeGrunner != null && !særligeGrunner.isEmpty()) {
                    særligeGrunnerStringBuilder.append("Bruker har utvist ").append(aktsomhet.getKode()).append(" med følgende særlige grunner: ");
                    for (SærligGrunn særligGrunn : særligeGrunner) {
                        særligeGrunnerStringBuilder.append(særligGrunn.getKode()).append(", ");
                    }
                    return særligeGrunnerStringBuilder.toString();
                }
            }
        }
        return null;
    }

    public static LocalDateTime finnNyesteVarselbrevTidspunkt(List<VarselbrevSporing> utsendteVarselbrev) {
        LocalDateTime nyesteVarselSendt = utsendteVarselbrev.get(0).getOpprettetTidspunkt();
        for (VarselbrevSporing varselbrevData : utsendteVarselbrev) {
            if (varselbrevData.getOpprettetTidspunkt().isAfter(nyesteVarselSendt)) {
                nyesteVarselSendt = varselbrevData.getOpprettetTidspunkt();
            }
        }
        return nyesteVarselSendt;
    }

    public static Long finnTotaltTilbakekrevingsbeløp(List<BeregningResultatPeriode> beregningResultatPerioder) {
        BigDecimal totalTilbakekrevingBeløp = beregningResultatPerioder.stream().map(BeregningResultatPeriode::getTilbakekrevingBeløp)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalTilbakekrevingBeløp.longValue();
    }

    public static List<PeriodeMedTekstDto> mapFritekstFraDb(List<VedtaksbrevPeriode> eksisterendePerioderForBrev) {
        List<PeriodeMedTekstDto> perioderMedTekster = new ArrayList<>();
        for (VedtaksbrevPeriode eksisterendePeriode : eksisterendePerioderForBrev) {
            Optional<PeriodeMedTekstDto> periodeMedTekstOptional = finnOpprettetPeriode(perioderMedTekster, eksisterendePeriode);
            PeriodeMedTekstDto periodeMedTekst;
            if (periodeMedTekstOptional.isPresent()) {
                periodeMedTekst = periodeMedTekstOptional.get();
            } else {
                periodeMedTekst = new PeriodeMedTekstDto();
                periodeMedTekst.setFom(eksisterendePeriode.getPeriode().getFom());
                periodeMedTekst.setTom(eksisterendePeriode.getPeriode().getTom());
                perioderMedTekster.add(periodeMedTekst);
            }

            if (eksisterendePeriode.getFritekstType() == FritekstType.FAKTA_AVSNITT) {
                periodeMedTekst.setFaktaAvsnitt(eksisterendePeriode.getFritekst());
            } else if (eksisterendePeriode.getFritekstType() == FritekstType.VILKAAR_AVSNITT) {
                periodeMedTekst.setVilkårAvsnitt(eksisterendePeriode.getFritekst());
            } else if (eksisterendePeriode.getFritekstType() == FritekstType.SAERLIGE_GRUNNER_AVSNITT) {
                periodeMedTekst.setSærligeGrunnerAvsnitt(eksisterendePeriode.getFritekst());
            }
        }
        return perioderMedTekster;
    }

    private static Optional<PeriodeMedTekstDto> finnOpprettetPeriode(List<PeriodeMedTekstDto> perioderMedTekster, VedtaksbrevPeriode lagretPeriodeMedFritekst) {
        for (PeriodeMedTekstDto periode : perioderMedTekster) {
            if (periode != null && periode.getFom().equals(lagretPeriodeMedFritekst.getPeriode().getFom())) {
                return Optional.of(periode);
            }
        }
        return Optional.empty();
    }
}
