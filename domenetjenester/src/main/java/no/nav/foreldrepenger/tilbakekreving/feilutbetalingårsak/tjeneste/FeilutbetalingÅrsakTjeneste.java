package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseTypePrYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUndertypePrHendelseType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertyperDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;

@ApplicationScoped
public class FeilutbetalingÅrsakTjeneste {

    public List<HendelseTyperPrYtelseTypeDto> hentFeilutbetalingårsaker() {
        List<HendelseTyperPrYtelseTypeDto> resultat = new ArrayList<>();

        Map<FagsakYtelseType, Set<HendelseType>> hendelseTypePrYtelseType = HendelseTypePrYtelseType.getHendelsetypeHierarki();
        Map<HendelseType, Set<HendelseUnderType>> hendelseUndertypePrHendelseType = HendelseUndertypePrHendelseType.getHendelsetypeHierarki();

        for (Map.Entry<FagsakYtelseType, Set<HendelseType>> entry : hendelseTypePrYtelseType.entrySet()) {
            FagsakYtelseType ytelseType = entry.getKey();
            List<HendelseType> hendelseTyper = sortereHendelseTypeBasertPåEkstradata2(entry.getValue());

            List<HendelseTypeMedUndertyperDto> dtoer = new ArrayList<>();
            for (HendelseType hendelseType : hendelseTyper) {
                Set<HendelseUnderType> undertyper = hendelseUndertypePrHendelseType.get(hendelseType);
                List<HendelseUnderType> sorterteUndertyper = sortereHendelseUnderTypeBasertPåEkstradata(undertyper);
                dtoer.add(new HendelseTypeMedUndertyperDto(hendelseType, sorterteUndertyper));
            }
            resultat.add(new HendelseTyperPrYtelseTypeDto(ytelseType, dtoer));
        }

        return resultat;
    }

    private static List<HendelseUnderType> sortereHendelseUnderTypeBasertPåEkstradata(Set<HendelseUnderType> kodelistene) {
        if (kodelistene.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Fins null i HendelseUnderType: " + kodelistene);
        }

        return kodelistene
                .stream()
                .sorted(Comparator.comparing(HendelseUnderType::getSortering))
                .collect(Collectors.toList());
    }

    private static List<HendelseType> sortereHendelseTypeBasertPåEkstradata2(Set<HendelseType> kodelistene) {
        if (kodelistene.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Fins null i HendelseType: " + kodelistene);
        }
        return kodelistene
                .stream()
                .sorted(Comparator.comparing(HendelseType::getSortering))
                .collect(Collectors.toList());
    }

}
