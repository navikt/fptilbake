package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagDokumentLinkDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkDtoFellesMapper.TOM_LINJE;
import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkDtoFellesMapper.leggTilAlleTeksterIHovedliste;
import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkDtoFellesMapper.tilHistorikkInnslagDto;

public class HistorikkV2Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(HistorikkV2Adapter.class);

    public static HistorikkinnslagDtoV2 map(Historikkinnslag h, UUID behandlingUUID, URI dokumentPath) {
        return switch (h.getType()) {
            case BEH_GJEN, BEH_MAN_GJEN, BEH_STARTET, BEH_STARTET_PÅ_NYTT, BEH_STARTET_FORFRA,
                 VEDLEGG_MOTTATT, BREV_SENT, BREV_BESTILT, REVURD_OPPR, REGISTRER_PAPIRSØK, MANGELFULL_SØKNAD, INNSYN_OPPR,
                 NYE_REGOPPLYSNINGER, TBK_OPPR, KLAGEBEH_STARTET, OPPGAVE_VEDTAK -> fraMaltype1(h, behandlingUUID, dokumentPath);
            case FORSLAG_VEDTAK, VEDTAK_FATTET, VEDTAK_FATTET_AUTOMATISK, REGISTRER_OM_VERGE -> fraMaltype2(h, behandlingUUID);
            case SAK_RETUR -> fraMaltype3(h, behandlingUUID);
            case AVBRUTT_BEH, BEH_VENT, FJERNET_VERGE -> fraMalType4(h, behandlingUUID);
            case FAKTA_ENDRET, KLAGE_BEH_NK, KLAGE_BEH_NFP, BYTT_ENHET -> fraMalType5(h, behandlingUUID, dokumentPath);
            case NY_KRAVGRUNNLAG_MOTTAT -> fraMalType6(h, behandlingUUID);
            case FAKTA_OM_FEILUTBETALING -> fraMaltypeFeilutbetaling(h, behandlingUUID);
            case FORELDELSE -> fraMaltypeForeldelse(h, behandlingUUID);
            case TILBAKEKREVING -> fraMaltypeTilbakekreving(h, behandlingUUID);
            case OPPTJENING, OVERSTYRT -> throw new IllegalStateException(String.format("Kode: %s har ingen maltype", h.getType())); // Ingen historikkinnslag for denne typen i DB!
            default -> throw new IllegalStateException(String.format("Ukjent historikkinnslagType: %s har ingen maltype", h.getType()));
        };
    }

    private static HistorikkinnslagDtoV2 fraMaltype1(Historikkinnslag innslag, UUID behandlingUUID, URI dokumentPath) {
        var del = innslag.getHistorikkinnslagDeler().getFirst();
        var begrunnelsetekst = begrunnelseFraDel(del).map(List::of);
        var body = begrunnelsetekst.orElse(List.of());
        return tilHistorikkInnslagDto(innslag, behandlingUUID, tilDokumentlenker(innslag.getDokumentLinker(), dokumentPath), body);
    }

    private static HistorikkinnslagDtoV2 fraMaltype2(Historikkinnslag h, UUID behandlingUUID) {
        var del = h.getHistorikkinnslagDeler().getFirst();
        var hendelse = del.getHendelse().map(HistorikkDtoFellesMapper::fraHendelseFelt).orElseThrow();
        var tekst = del.getResultatFelt()
            .map(s -> String.format("%s: %s", hendelse, fraHistorikkResultat(s)))
            .orElse(hendelse);
        return tilHistorikkInnslagDto(h, behandlingUUID, List.of(tekst));
    }

    private static HistorikkinnslagDtoV2 fraMaltype3(Historikkinnslag h, UUID behandlingUUID) {
        var tekster = new ArrayList<String>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var aksjonspunkt = del.getTotrinnsvurderinger().stream()
                .map(HistorikkV2Adapter::fraAksjonspunktFelt)
                .flatMap(List::stream)
                .toList();
            leggTilAlleTeksterIHovedliste(tekster, aksjonspunkt);
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMalType4(Historikkinnslag h, UUID behandlingUUID) {
        var tekster = new ArrayList<String>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var årsakTekst = del.getAarsakFelt().stream()
                .flatMap(felt -> finnÅrsakKodeListe(felt).stream())
                .map(Kodeverdi::getNavn)
                .toList();
            var begrunnelsetekst = begrunnelseFraDel(del).stream().toList();

            leggTilAlleTeksterIHovedliste(tekster, årsakTekst, begrunnelsetekst);
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMalType5(Historikkinnslag h, UUID behandlingUUID, URI dokumentPath) {
        var tekster = new ArrayList<String>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var endretFelter = del.getEndredeFelt().stream()
                .map(HistorikkV2Adapter::fraEndretFelt)
                .toList();
            leggTilAlleTeksterIHovedliste(endretFelter);
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tilDokumentlenker(h.getDokumentLinker(), dokumentPath), tekster);
    }

    private static HistorikkinnslagDtoV2 fraMalType6(Historikkinnslag h, UUID behandlingUUID) {
        var tekster = new ArrayList<String>();
        for (var del : h.getHistorikkinnslagDeler()) {
            var opplysninger = del.getOpplysninger().stream()
                .map(HistorikkV2Adapter::fraOpplysningMaltype6)
                .toList();

            leggTilAlleTeksterIHovedliste(tekster, opplysninger);
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMaltypeFeilutbetaling(Historikkinnslag h, UUID behandlingUUID) {
        var tekster = new ArrayList<String>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var endredeFelt = del.getEndredeFelt();
            if (!endredeFelt.isEmpty()) {
                var periodeFom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_FOM).orElse("");
                var periodeTom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_TOM).orElse("");
                var opplysningTekst = String.format("For perioden __%s - %s__", periodeFom, periodeTom);
                var endretFelter = fraEndretFeltFeilutbetaling(endredeFelt);

                leggTilAlleTeksterIHovedliste(tekster, List.of(opplysningTekst), List.of(endretFelter));
            }
        }
        // Henter fra første del slik som frontend
        var begrunnelsetekst = begrunnelseFraDel(h.getHistorikkinnslagDeler().getFirst()).stream().toList();
        leggTilAlleTeksterIHovedliste(tekster, begrunnelsetekst);
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMaltypeForeldelse(Historikkinnslag h, UUID behandlingUUID) {
        var tekster = new ArrayList<String>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var periodeFom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_FOM).orElse("");
            var periodeTom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_TOM).orElse("");

            var manuelVurderingTekst = String.format("__Manuell vurdering__ av perioden %s-%s.", periodeFom, periodeTom);
            var endretFelter = del.getEndredeFelt().stream()
                .map(HistorikkV2Adapter::fraEndretFeltForeldelse)
                .toList();
            var begrunnelsetekst = begrunnelseFraDel(h.getHistorikkinnslagDeler().getFirst()).stream().toList();

            leggTilAlleTeksterIHovedliste(tekster, List.of(manuelVurderingTekst), endretFelter, begrunnelsetekst);

        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }


    private static HistorikkinnslagDtoV2 fraMaltypeTilbakekreving(Historikkinnslag h, UUID behandlingUUID) {
        var tekster = new ArrayList<String>();
        for (var del : h.getHistorikkinnslagDeler()) {
            var periodeFom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_FOM).orElse("");
            var periodeTom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_TOM).orElse("");
            var opplysningBegrunnelse = opplysingFraDel(del, HistorikkOpplysningType.TILBAKEKREVING_OPPFYLT_BEGRUNNELSE);
            var sarligGrunnerBegrunnelseFelt = opplysingFraDel(del, HistorikkOpplysningType.SÆRLIG_GRUNNER_BEGRUNNELSE);
            var begrunnelseFritekst = begrunnelseFraDel(del);

            var vurderingAvPerioden = String.format("__Vurdering__ av perioden %s-%s.", periodeFom, periodeTom);
            var teksterEndretFelt = tekstFraEndredeFelter(del, begrunnelseFritekst, sarligGrunnerBegrunnelseFelt, opplysningBegrunnelse);


            var begrunnelse = del.getEndredeFelt().isEmpty()
                ? begrunnelseFritekst
                : Optional.<String>empty();

            leggTilAlleTeksterIHovedliste(tekster, List.of(vurderingAvPerioden, TOM_LINJE), teksterEndretFelt, begrunnelse.stream().toList());
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);

    }

    private static ArrayList<String> tekstFraEndredeFelter(HistorikkinnslagDel del,
                                                Optional<String> begrunnelseFritekst,
                                                Optional<String> sarligGrunnerBegrunnelseFelt,
                                                Optional<String> opplysningBegrunnelse) {
        var teksterEndretFelt = new ArrayList<String>();
        var antallEndredeFelter = del.getEndredeFelt().size();
        for (int i = 0; i < antallEndredeFelter; i++) {
            var historikkinnslagFelt = del.getEndredeFelt().get(i);
            var historikkEndretFeltType = HistorikkEndretFeltType.fraKode(historikkinnslagFelt.getNavn());
            if (Set.of(HistorikkEndretFeltType.BELØP_TILBAKEKREVES, HistorikkEndretFeltType.ANDEL_TILBAKEKREVES, HistorikkEndretFeltType.ILEGG_RENTER).contains(historikkEndretFeltType) && historikkinnslagFelt.getTilVerdi() == null) {
                continue;
            }

            var visBegrunnelse = HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT.equals(historikkEndretFeltType);
            var erSisteEndretFeltElement = i == antallEndredeFelter - 1;
            var visAktsomhetBegrunnelse = begrunnelseFritekst.isPresent() && erSisteEndretFeltElement;
            var visSarligGrunnerBegrunnelse = sarligGrunnerBegrunnelseFelt.isPresent() && erSisteEndretFeltElement;

            if (visBegrunnelse && opplysningBegrunnelse.isPresent()) {
                teksterEndretFelt.add(opplysningBegrunnelse.get());
                teksterEndretFelt.add(TOM_LINJE);
            }
            if (visAktsomhetBegrunnelse) {
                teksterEndretFelt.add(begrunnelseFritekst.get());
                teksterEndretFelt.add(TOM_LINJE);
            }

            teksterEndretFelt.add(fraEndretFeltForeldelse(historikkinnslagFelt)); // Bruker samme
            teksterEndretFelt.add(TOM_LINJE);
            if (visSarligGrunnerBegrunnelse) {
                teksterEndretFelt.add(sarligGrunnerBegrunnelseFelt.get());
                teksterEndretFelt.add(TOM_LINJE);
            }
        }
        return teksterEndretFelt;
    }

    private static String fraEndretFeltForeldelse(HistorikkinnslagFelt felt) {
        var historikkEndretFeltType = HistorikkEndretFeltType.fraKode(felt.getNavn());
        var fraVerdi = felt.getFraVerdi();
        var tilVerdi = felt.getTilVerdi();

        var visProsentverdi = HistorikkEndretFeltType.ANDEL_TILBAKEKREVES.equals(historikkEndretFeltType);
        var formatertTilVerdi = visProsentverdi && tilVerdi != null ? String.format("%s%%", tilVerdi) : tilVerdi;
        if (felt.getFraVerdi() != null) {
            var formatertFraVerdi = visProsentverdi && fraVerdi != null ? String.format("%s%%", fraVerdi) : fraVerdi;
            return String.format("__%s__ endret fra %s til __%s__", historikkEndretFeltType.getNavn(), formatertFraVerdi, formatertTilVerdi);
        } else {
            return String.format("__%s__ er satt til __%s__.", historikkEndretFeltType.getNavn(), formatertTilVerdi);
        }

    }

    private static String fraEndretFeltFeilutbetaling(List<HistorikkinnslagFelt> endretFelt) {
        var årsakFelt = endretFelt.stream()
            .filter(felt -> HistorikkEndretFeltType.HENDELSE_ÅRSAK.getKode().equals(felt.getNavn()))
            .findFirst()
            .orElseThrow();
        var underårsakFelt = endretFelt.stream()
            .filter(felt -> HistorikkEndretFeltType.HENDELSE_UNDER_ÅRSAK.getKode().equals(felt.getNavn()))
            .findFirst();
        var underÅrsakFraVerdi = underårsakFelt.isPresent() && underårsakFelt.get().getKlFraVerdi() != null
            ? HendelseUnderType.fraKode(underårsakFelt.get().getFraVerdi())
            : null;
        var underÅrsakTilVerdi = underårsakFelt.isPresent() && underårsakFelt.get().getKlTilVerdi() != null
            ? HendelseUnderType.fraKode(underårsakFelt.get().getTilVerdi())
            : null;
        var endret = endretFelt.stream().anyMatch(felt -> felt.getFraVerdi() != null);

        var tilVerdiNavn = årsakFelt.getKlTilVerdi() != null
            ? HendelseType.fraKode(årsakFelt.getTilVerdiKode()).getNavn()
            : "";


        if (endret) {
            var årsakNavn = årsakFelt.getKlFraVerdi() != null
                ? HendelseType.fraKode(årsakFelt.getFraVerdiKode()).getNavn()
                : "";
            var fraVerdi = underÅrsakFraVerdi != null ? String.format("%s (%s)", årsakNavn, underÅrsakFraVerdi) : årsakNavn;
            var tilVerdi = underÅrsakTilVerdi != null ? String.format("%s (%s)", tilVerdiNavn, underÅrsakTilVerdi) : tilVerdiNavn;


            return String.format("__Hendelse__ er endret fra %s til __%s__", fraVerdi, tilVerdi);
        } else {
            var feltverdi = underÅrsakTilVerdi != null ? String.format("%s (%s)", tilVerdiNavn, underÅrsakTilVerdi) : tilVerdiNavn;
            return String.format("__Hendelse__ er satt til __%s__", feltverdi);
        }
    }

    private static Optional<String> opplysingFraDel(HistorikkinnslagDel del, HistorikkOpplysningType periodeTom) {
        return del.getOpplysninger().stream()
            .filter(o -> periodeTom.getKode().equals(o.getNavn()))
            .map(HistorikkinnslagFelt::getTilVerdi)
            .findFirst();
    }

    private static String fraOpplysningMaltype6(HistorikkinnslagFelt opplysning) {
        var historikkOpplysningType = HistorikkOpplysningType.fraKode(opplysning.getNavn());
        return String.format("%s: %s", historikkOpplysningType.getNavn(), opplysning.getTilVerdi());
    }

    private static String fraEndretFelt(HistorikkinnslagFelt felt) {
        var feltNavn = HistorikkEndretFeltType.fraKode(felt.getNavn()).getNavn();
        var tilVerdi = konverterBoolean(felt.getTilVerdi());
        if (felt.getTilVerdi() != null && tilVerdi == null) {
            tilVerdi = kodeverdiTilStrengEndretFeltTilverdi(felt.getTilVerdiKode(), felt.getTilVerdi());
        }

        if (felt.getFraVerdi() == null) {
            return String.format("__%s__ er satt til __%s__.", feltNavn, tilVerdi);
        }

        var fraVerdi = konverterBoolean(felt.getFraVerdi());
        if (fraVerdi == null) {
            fraVerdi = kodeverdiTilStrengEndretFeltTilverdi(felt.getFraVerdiKode(), felt.getFraVerdi());
        }

        return String.format("__%s__ endret fra %s til __%s__", feltNavn, fraVerdi, tilVerdi);
    }

    private static String konverterBoolean(String verdi) {
        if ("true".equalsIgnoreCase(verdi)) {
            return "Ja";
        }
        if ("false".equalsIgnoreCase(verdi)) {
            return "Nei";
        }
        return null;
    }

    private static String kodeverdiTilStrengEndretFeltTilverdi(String verdiKode, String verdi) {
        if (verdiKode == null) {
            return verdi;
        }

        return FeltType.getByKey(verdiKode).getText();
    }

    private static String fraHistorikkResultat(HistorikkinnslagFelt resultat) {
        var vedtakResultatType = VedtakResultatType.valueOf(resultat.getTilVerdiKode());
        return switch (vedtakResultatType) {
            case FULL_TILBAKEBETALING -> "Full tilbakebetaling";
            default -> vedtakResultatType.getNavn();
        };
    }

    private static Optional<String> begrunnelseFraDel(HistorikkinnslagDel historikkinnslagDel) {
        return historikkinnslagDel.getBegrunnelseFelt()
            .flatMap(HistorikkV2Adapter::finnÅrsakKodeListe)
            .map(Kodeverdi::getNavn)
            .or(historikkinnslagDel::getBegrunnelse);
    }

    // Fra HistorikkinnslagDelTo
    private static Optional<Kodeverdi> finnÅrsakKodeListe(HistorikkinnslagFelt aarsak) {
        var aarsakVerdi = aarsak.getTilVerdi();
        if (Objects.equals("-", aarsakVerdi)) {
            return Optional.empty();
        }
        if (aarsak.getKlTilVerdi() == null) {
            return Optional.empty();
        }

        var kodeverdiMap = HistorikkInnslagTekstBuilder.KODEVERK_KODEVERDI_MAP.get(aarsak.getKlTilVerdi());
        if (kodeverdiMap == null) {
            throw new IllegalStateException("Har ikke støtte for HistorikkinnslagFelt#klTilVerdi=" + aarsak.getKlTilVerdi());
        }
        return Optional.ofNullable(kodeverdiMap.get(aarsakVerdi));
    }

    private static List<HistorikkInnslagDokumentLinkDto> tilDokumentlenker(List<HistorikkinnslagDokumentLink> dokumentLinker, URI dokumentPath) {
        if (dokumentLinker == null) {
            return List.of();
        }
        return dokumentLinker.stream().map(d -> tilDokumentlenker(d, dokumentPath)) //
            .toList();
    }

    private static HistorikkInnslagDokumentLinkDto tilDokumentlenker(HistorikkinnslagDokumentLink lenke, URI dokumentPath) {
        var dto = new HistorikkInnslagDokumentLinkDto();
        dto.setTag(lenke.getLinkTekst());
        dto.setUtgått(false);
        dto.setDokumentId(lenke.getDokumentId());
        dto.setJournalpostId(lenke.getJournalpostId().getVerdi());
        if (lenke.getJournalpostId().getVerdi() != null && lenke.getDokumentId() != null && dokumentPath != null) {
            var builder = UriBuilder.fromUri(dokumentPath)
                .queryParam("journalpostId", lenke.getJournalpostId().getVerdi())
                .queryParam("dokumentId", lenke.getDokumentId());
            dto.setUrl(builder.build());
        }
        return dto;
    }

    private static List<String> fraAksjonspunktFelt(HistorikkinnslagTotrinnsvurdering aksjonspunktFelt) {
        var aksjonspunktTekst = aksjonspunktFelt.getAksjonspunktDefinisjon().getNavn();
        if (aksjonspunktFelt.erGodkjent()) {
            return List.of(String.format("__%s er godkjent__", aksjonspunktTekst));
        } else {
            return List.of(
                String.format("__%s må vurderes på nytt__", aksjonspunktTekst),
                String.format("Kommentar: %s", aksjonspunktFelt.getBegrunnelse())
            );
        }
    }
}