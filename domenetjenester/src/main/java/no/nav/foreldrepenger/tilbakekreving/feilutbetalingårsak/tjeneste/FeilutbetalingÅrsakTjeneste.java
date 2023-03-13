package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

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

        var hendelseTypePrYtelseType = HendelseTypePrYtelseType.getHendelsetypeHierarki();
        var hendelseUndertypePrHendelseType = HendelseUndertypePrHendelseType.getHendelsetypeHierarki();

        for (var entry : hendelseTypePrYtelseType.entrySet()) {
            var ytelseType = entry.getKey();
            var hendelseTyper = sortereHendelseTypeBasertPåEkstradata2(entry.getValue());

            List<HendelseTypeMedUndertyperDto> dtoer = new ArrayList<>();
            for (var hendelseType : hendelseTyper) {
                var undertyper = hendelseUndertypePrHendelseType.get(hendelseType);
                var sorterteUndertyper = sortereHendelseUnderTypeBasertPåEkstradata(undertyper);
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
                .toList();
    }

    private static List<HendelseType> sortereHendelseTypeBasertPåEkstradata2(Set<HendelseType> kodelistene) {
        if (kodelistene.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Fins null i HendelseType: " + kodelistene);
        }
        return kodelistene
                .stream()
                .sorted(Comparator.comparing(HendelseType::getSortering))
                .toList();
    }

}
