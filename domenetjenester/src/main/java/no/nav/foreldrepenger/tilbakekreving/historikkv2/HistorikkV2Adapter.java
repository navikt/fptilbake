package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkDtoFellesMapper.TOM_LINJE;
import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkDtoFellesMapper.konverterTilLinjerMedLinjeskift;
import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkDtoFellesMapper.tilHistorikkInnslagDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagDokumentLinkDto;

public class HistorikkV2Adapter {

    public static HistorikkinnslagDtoV2 map(HistorikkinnslagOld h, UUID behandlingUUID, URI dokumentPath) {
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

    private static HistorikkinnslagDtoV2 fraMaltype1(HistorikkinnslagOld innslag, UUID behandlingUUID, URI dokumentPath) {
        var del = innslag.getHistorikkinnslagDeler().getFirst();
        var begrunnelsetekst = begrunnelseFraDel(del).map(List::of);
        var body = begrunnelsetekst.orElse(List.of());
        return tilHistorikkInnslagDto(innslag, behandlingUUID, tilDokumentlenker(innslag.getDokumentLinker(), dokumentPath), konverterTilLinjerMedLinjeskift(body));
    }

    private static HistorikkinnslagDtoV2 fraMaltype2(HistorikkinnslagOld h, UUID behandlingUUID) {
        var del = h.getHistorikkinnslagDeler().getFirst();
        var hendelse = del.getHendelse().map(HistorikkDtoFellesMapper::fraHendelseFelt).orElseThrow();
        var tekst = del.getResultatFelt()
            .map(s -> String.format("%s: %s", hendelse, fraHistorikkResultat(s)))
            .orElse(hendelse);
        return tilHistorikkInnslagDto(h, behandlingUUID, konverterTilLinjerMedLinjeskift(List.of(tekst)));
    }

    private static HistorikkinnslagDtoV2 fraMaltype3(HistorikkinnslagOld h, UUID behandlingUUID) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var aksjonspunkt = del.getTotrinnsvurderinger().stream()
                .map(HistorikkV2Adapter::fraAksjonspunktFelt)
                .flatMap(List::stream)
                .toList();
            tekster.addAll(konverterTilLinjerMedLinjeskift(aksjonspunkt));
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMalType4(HistorikkinnslagOld h, UUID behandlingUUID) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var årsakTekst = del.getAarsakFelt().stream()
                .flatMap(felt -> finnÅrsakKodeListe(felt).stream())
                .map(Kodeverdi::getNavn)
                .toList();
            var begrunnelsetekst = begrunnelseFraDel(del).stream().toList();

            tekster.addAll(konverterTilLinjerMedLinjeskift(årsakTekst, begrunnelsetekst));
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMalType5(HistorikkinnslagOld h, UUID behandlingUUID, URI dokumentPath) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var endretFelter = del.getEndredeFelt().stream()
                .map(HistorikkV2Adapter::fraEndretFeltUtenKodeverk)
                .toList();
            tekster.addAll(konverterTilLinjerMedLinjeskift(endretFelter));
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tilDokumentlenker(h.getDokumentLinker(), dokumentPath), tekster);
    }

    private static HistorikkinnslagDtoV2 fraMalType6(HistorikkinnslagOld h, UUID behandlingUUID) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
        for (var del : h.getHistorikkinnslagDeler()) {
            var opplysninger = del.getOpplysninger().stream()
                .map(HistorikkV2Adapter::fraOpplysningMaltype6)
                .toList();

            tekster.addAll(konverterTilLinjerMedLinjeskift(opplysninger));
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMaltypeFeilutbetaling(HistorikkinnslagOld h, UUID behandlingUUID) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
        for(var del : h.getHistorikkinnslagDeler()) {
            // Endret felt ehr bruker kodeverdier (unikt for feilutbetaling)
            var endredeFelt = del.getEndredeFelt();
            if (!endredeFelt.isEmpty()) {
                var periodeFom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_FOM).orElse("");
                var periodeTom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_TOM).orElse("");
                var opplysningTekst = String.format("Vurdering av perioden __%s-%s__", periodeFom, periodeTom);
                var endretFelter = fraEndretFeltFeilutbetaling(endredeFelt);
                tekster.addAll(konverterTilLinjerMedLinjeskift(List.of(opplysningTekst), List.of(endretFelter)));
            }

        }
        // Henter fra første del slik som frontend
        var begrunnelsetekst = begrunnelseFraDel(h.getHistorikkinnslagDeler().getFirst()).stream().toList();
        tekster.addAll(konverterTilLinjerMedLinjeskift(begrunnelsetekst));
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }

    private static HistorikkinnslagDtoV2 fraMaltypeForeldelse(HistorikkinnslagOld h, UUID behandlingUUID) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
        for(var del : h.getHistorikkinnslagDeler()) {
            var periodeFom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_FOM).orElse("");
            var periodeTom = opplysingFraDel(del, HistorikkOpplysningType.PERIODE_TOM).orElse("");

            var manuelVurderingTekst = String.format("__Manuell vurdering__ av perioden %s-%s.", periodeFom, periodeTom);
            var endretFelter = del.getEndredeFelt().stream()
                .map(HistorikkV2Adapter::fraEndretFeltUtenKodeverk)
                .toList();
            var begrunnelsetekst = begrunnelseFraDel(h.getHistorikkinnslagDeler().getFirst()).stream().toList();

            tekster.addAll(konverterTilLinjerMedLinjeskift(List.of(manuelVurderingTekst), endretFelter, begrunnelsetekst));

        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);
    }


    private static HistorikkinnslagDtoV2 fraMaltypeTilbakekreving(HistorikkinnslagOld h, UUID behandlingUUID) {
        var tekster = new ArrayList<HistorikkinnslagDtoV2.Linje>();
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

            tekster.addAll(konverterTilLinjerMedLinjeskift(List.of(vurderingAvPerioden, TOM_LINJE), teksterEndretFelt, begrunnelse.stream().toList()));
        }
        return tilHistorikkInnslagDto(h, behandlingUUID, tekster);

    }

    private static ArrayList<String> tekstFraEndredeFelter(HistorikkinnslagOldDel del,
                                                           Optional<String> begrunnelseFritekst,
                                                           Optional<String> sarligGrunnerBegrunnelseFelt,
                                                           Optional<String> opplysningBegrunnelse) {
        var teksterEndretFelt = new ArrayList<String>();
        var antallEndredeFelter = del.getEndredeFelt().size();
        for (int i = 0; i < antallEndredeFelter; i++) {
            var endretfelt = del.getEndredeFelt().get(i);
            var historikkEndretFeltType = HistorikkEndretFeltType.fraKode(endretfelt.getNavn());
            if (Set.of(HistorikkEndretFeltType.BELØP_TILBAKEKREVES, HistorikkEndretFeltType.ANDEL_TILBAKEKREVES, HistorikkEndretFeltType.ILEGG_RENTER).contains(historikkEndretFeltType) && endretfelt.getTilVerdi() == null) {
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

            teksterEndretFelt.add(fraEndretFeltUtenKodeverk(endretfelt)); // Bruker samme
            teksterEndretFelt.add(TOM_LINJE);
            if (visSarligGrunnerBegrunnelse) {
                teksterEndretFelt.add(sarligGrunnerBegrunnelseFelt.get());
                teksterEndretFelt.add(TOM_LINJE);
            }
        }
        return teksterEndretFelt;
    }

    private static String fraEndretFeltUtenKodeverk(HistorikkinnslagOldFelt felt) {
        if (felt.getFraVerdiKode() != null || felt.getTilVerdiKode() != null) {
            throw new IllegalStateException("Finner kodeverdi i endret felt for " + felt.getNavn());
        }
        var historikkEndretFeltType = HistorikkEndretFeltType.fraKode(felt.getNavn());
        var fraVerdi = felt.getFraVerdi();
        var tilVerdi = felt.getTilVerdi();

        if (HistorikkEndretFeltType.ANDEL_TILBAKEKREVES.equals(historikkEndretFeltType)) {
            fraVerdi = fraVerdi != null ? String.format("%s%%", fraVerdi) : fraVerdi;
            tilVerdi = tilVerdi != null ? String.format("%s%%", tilVerdi) : tilVerdi;
        } else {
            tilVerdi = hvisBooleanKonveterTilJaNei(felt.getTilVerdi());
            fraVerdi = hvisBooleanKonveterTilJaNei(felt.getFraVerdi());
        }

        return felt.getFraVerdi() == null
            ? String.format("__%s__ er satt til __%s__.", historikkEndretFeltType.getNavn(), tilVerdi)
            : String.format("__%s__ endret fra %s til __%s__", historikkEndretFeltType.getNavn(), fraVerdi, tilVerdi);
    }

    private static String fraEndretFeltFeilutbetaling(List<HistorikkinnslagOldFelt> endretFelt) {
        var årsakFelt = endretFelt.stream()
            .filter(felt -> HistorikkEndretFeltType.HENDELSE_ÅRSAK.getKode().equals(felt.getNavn()))
            .findFirst()
            .orElseThrow();
        var underårsakFelt = endretFelt.stream()
            .filter(felt -> HistorikkEndretFeltType.HENDELSE_UNDER_ÅRSAK.getKode().equals(felt.getNavn()))
            .findFirst();
        var underÅrsakFraVerdi = underårsakFelt.isPresent() && underårsakFelt.get().getKlFraVerdi() != null
            ? HendelseUnderType.fraKode(underårsakFelt.get().getFraVerdi()).getNavn()
            : null;
        var underÅrsakTilVerdi = underårsakFelt.isPresent() && underårsakFelt.get().getKlTilVerdi() != null
            ? HendelseUnderType.fraKode(underårsakFelt.get().getTilVerdi()).getNavn()
            : null;
        var endret = endretFelt.stream().anyMatch(felt -> felt.getFraVerdi() != null);

        var tilVerdiNavn = årsakFelt.getKlTilVerdi() != null
            ? HendelseType.fraKode(årsakFelt.getTilVerdiKode()).getNavn()
            : "";


        if (endret) {
            var årsakNavn = årsakFelt.getKlFraVerdi() != null
                ? HendelseType.fraKode(årsakFelt.getFraVerdiKode()).getNavn()
                : "";
            var fraVerdi = underÅrsakFraVerdi != null ? String.format("%s, %s", årsakNavn, underÅrsakFraVerdi) : årsakNavn;
            var tilVerdi = underÅrsakTilVerdi != null ? String.format("%s, %s", tilVerdiNavn, underÅrsakTilVerdi) : tilVerdiNavn;

            return String.format("__Årsak til feilutbetaling__ er endret fra %s til __%s__", fraVerdi, tilVerdi);
        } else {
            var feltverdi = underÅrsakTilVerdi != null ? String.format("%s, %s", tilVerdiNavn, underÅrsakTilVerdi) : tilVerdiNavn;
            return String.format("__Årsak til feilutbetaling__ er satt til __%s__", feltverdi);
        }
    }

    private static Optional<String> opplysingFraDel(HistorikkinnslagOldDel del, HistorikkOpplysningType opplysningType) {
        return del.getOpplysninger().stream()
            .filter(o -> opplysningType.getKode().equals(o.getNavn()))
            .map(HistorikkinnslagOldFelt::getTilVerdi)
            .filter(Objects::nonNull)
            .findFirst();
    }

    private static String fraOpplysningMaltype6(HistorikkinnslagOldFelt opplysning) {
        var historikkOpplysningType = HistorikkOpplysningType.fraKode(opplysning.getNavn());
        return String.format("%s: %s", historikkOpplysningType.getNavn(), opplysning.getTilVerdi());
    }

    private static String hvisBooleanKonveterTilJaNei(String verdi) {
        if ("true".equalsIgnoreCase(verdi)) {
            return "Ja";
        }
        if ("false".equalsIgnoreCase(verdi)) {
            return "Nei";
        }
        return verdi;
    }

    private static String fraHistorikkResultat(HistorikkinnslagOldFelt resultat) {
        var vedtakResultatType = VedtakResultatType.valueOf(resultat.getTilVerdiKode());
        return switch (vedtakResultatType) {
            case FULL_TILBAKEBETALING -> "Full tilbakebetaling";
            default -> vedtakResultatType.getNavn();
        };
    }

    private static Optional<String> begrunnelseFraDel(HistorikkinnslagOldDel historikkinnslagDel) {
        return historikkinnslagDel.getBegrunnelseFelt()
            .flatMap(HistorikkV2Adapter::finnBegrunnelseKodeListe)
            .map(Kodeverdi::getNavn)
            .or(historikkinnslagDel::getBegrunnelse);
    }

    private static Optional<Kodeverdi> finnBegrunnelseKodeListe(HistorikkinnslagOldFelt begrunnelseFelt) {
        var begrunnelseVerdi = begrunnelseFelt.getTilVerdi();
        if (Objects.equals("-", begrunnelseVerdi)) {
            return Optional.empty();
        }
        if (begrunnelseFelt.getKlTilVerdi() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(switch (begrunnelseFelt.getKlTilVerdi()) {
            case "BEHANDLING_AARSAK" -> BehandlingÅrsakType.kodeMap().get(begrunnelseVerdi);
            default -> throw new IllegalStateException("Ikke støttet kodeverk for historikkinnslagfelt type BEGRUNNELSE med kodeverk=" + begrunnelseFelt.getKlTilVerdi());
        });
    }

    private static Optional<Kodeverdi> finnÅrsakKodeListe(HistorikkinnslagOldFelt aarsak) {
        var aarsakVerdi = aarsak.getTilVerdi();
        if (Objects.equals("-", aarsakVerdi)) {
            return Optional.empty();
        }
        if (aarsak.getKlTilVerdi() == null) {
            return Optional.empty();
        }

        return Optional.of(switch (aarsak.getKlTilVerdi()) {
                case "VENT_AARSAK" -> Venteårsak.kodeMap().get(aarsakVerdi);
                case "BEHANDLING_RESULTAT_TYPE" -> BehandlingResultatType.kodeMap().get(aarsakVerdi);
                default -> throw new IllegalStateException("Ikke støttet kodeverk for historikkinnslagfelt type AARSAK med kodeverk=" + aarsak.getKlTilVerdi());
            }
        );
    }

    private static List<HistorikkInnslagDokumentLinkDto> tilDokumentlenker(List<HistorikkinnslagOldDokumentLink> dokumentLinker, URI dokumentPath) {
        if (dokumentLinker == null) {
            return List.of();
        }
        return dokumentLinker.stream().map(d -> tilDokumentlenker(d, dokumentPath)) //
            .toList();
    }

    private static HistorikkInnslagDokumentLinkDto tilDokumentlenker(HistorikkinnslagOldDokumentLink lenke, URI dokumentPath) {
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
