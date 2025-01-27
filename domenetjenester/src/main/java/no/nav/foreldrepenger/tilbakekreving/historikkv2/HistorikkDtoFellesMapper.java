package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

import no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkinnslagDtoV2.Linje;

public class HistorikkDtoFellesMapper {

    private static final Logger LOG = LoggerFactory.getLogger(HistorikkDtoFellesMapper.class);

    public static HistorikkinnslagDtoV2 tilHistorikkInnslagDto(HistorikkinnslagOld h, UUID behandlingUUID, List<Linje> linjer) {
        return tilHistorikkInnslagDto(h, behandlingUUID, null, linjer);
    }

    public static HistorikkinnslagDtoV2 tilHistorikkInnslagDto(HistorikkinnslagOld h, UUID behandlingUUID, List<HistorikkInnslagDokumentLinkDto> lenker, List<Linje> linjer) {
        var skjermlenkeOpt = skjermlenkeFra(h);
        return new HistorikkinnslagDtoV2(
            behandlingUUID,
            HistorikkinnslagDtoV2.HistorikkAktørDto.fra(h.getAktør(), h.getOpprettetAv()),
            skjermlenkeOpt.orElse(null),
            h.getOpprettetTidspunkt(),
            lenker,
            skjermlenkeOpt.isEmpty() ? lagTittel(h) : null,
            fjernTrailingAvsnittFraTekst(linjer)
        );
    }

    private static String lagTittel(HistorikkinnslagOld h) {
        var hendelseFelt = h.getHistorikkinnslagDeler().stream()
            .map(HistorikkinnslagOldDel::getHendelse)
            .flatMap(Optional::stream)
            .toList();
        if (hendelseFelt.size() > 1) {
            LOG.info("HistorikkV2: Flere deler med HENDELSE-felt for historikkinnslag {} på sak {}. Er alle like? Er det noe grunn til å ha undertittler? ", h.getId(), h.getFagsakId());
        }

        if (hendelseFelt.isEmpty()) {
            return h.getType().getNavn();
        }
        return fraHendelseFelt(hendelseFelt.getFirst());
    }

    // BEH_VENT har satt tilverdi som brukes i tittelen (Behandling på vent 05.12.2024)
    public static String fraHendelseFelt(HistorikkinnslagOldFelt felt) {
        var hendelsetekst = HistorikkinnslagType.fraKode(felt.getNavn()).getNavn();
        return felt.getTilVerdi() != null
            ? String.format("%s %s", hendelsetekst, felt.getTilVerdi())
            : hendelsetekst;
    }

    @SafeVarargs
    public static List<Linje> konverterTilLinjerMedLinjeskift(List<String>... alleTekster) {
        var linjer = new ArrayList<Linje>();
        for (var tekster : alleTekster) {
            linjer.addAll(tekster.stream().map(Linje::tekstlinje).toList());
        }
        linjer.add(Linje.linjeskift());
        return linjer;
    }


    private static List<Linje> fjernTrailingAvsnittFraTekst(List<Linje> tekster) {
        if (tekster.isEmpty()) {
            return tekster;
        }
        if (tekster.getLast().erLinjeskift()) {
            tekster.removeLast();
        }
        return tekster.stream().toList();
    }

    private static Optional<SkjermlenkeType> skjermlenkeFra(HistorikkinnslagOld h) {
        var skjermlenker = h.getHistorikkinnslagDeler().stream()
            .flatMap(del -> del.getSkjermlenke().stream())
            .distinct()
            .toList();
        if (skjermlenker.size() > 1) {
            return Optional.empty();
        } else {
            return skjermlenker.stream()
                .map(SkjermlenkeType::fraKode)
                .findFirst();
        }
    }
}
