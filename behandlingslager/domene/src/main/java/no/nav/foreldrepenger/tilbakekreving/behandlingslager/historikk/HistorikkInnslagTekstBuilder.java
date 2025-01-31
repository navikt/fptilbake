package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;


import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagTekstBuilderFormater.formatString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;

public class HistorikkInnslagTekstBuilder {

    private boolean begrunnelseEndret = false;
    private boolean gjeldendeFraSatt = false;

    private HistorikkinnslagOldDel.Builder historikkinnslagDelBuilder = HistorikkinnslagOldDel.builder();
    private List<HistorikkinnslagOldDel> historikkinnslagDeler = new ArrayList<>();
    private int antallEndredeFelter = 0;
    private int antallAksjonspunkter = 0;
    private int antallOpplysninger = 0;

    public static final Map<String, Map<String, ? extends Kodeverdi>> KODEVERK_KODEVERDI_MAP = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(Venteårsak.KODEVERK, Venteårsak.kodeMap()),
            new AbstractMap.SimpleEntry<>(HistorikkBegrunnelseType.KODEVERK, HistorikkBegrunnelseType.kodeMap()),
            new AbstractMap.SimpleEntry<>(BehandlingÅrsakType.KODEVERK, BehandlingÅrsakType.kodeMap()),
            new AbstractMap.SimpleEntry<>(BehandlingResultatType.KODEVERK, BehandlingResultatType.kodeMap()));

    public HistorikkInnslagTekstBuilder() {
        //
    }

    public List<HistorikkinnslagOldDel> getHistorikkinnslagDeler() {
        return historikkinnslagDeler;
    }

    public HistorikkInnslagTekstBuilder medHendelse(HistorikkinnslagType historikkInnslagsType) {
        return medHendelse(historikkInnslagsType, null);
    }

    public HistorikkInnslagTekstBuilder medHendelse(HistorikkinnslagType historikkinnslagType, Object verdi) {
        if (!HistorikkinnslagType.FAKTA_ENDRET.equals(historikkinnslagType)
                && !HistorikkinnslagType.OVERSTYRT.equals(historikkinnslagType)
                && !HistorikkinnslagType.OPPTJENING.equals(historikkinnslagType)) { // PKMANTIS-753 FPFEIL-805
            String verdiStr = formatString(verdi);
            HistorikkinnslagOldFelt.builder()
                    .medFeltType(HistorikkinnslagFeltType.HENDELSE)
                    .medNavn(historikkinnslagType)
                    .medTilVerdi(verdiStr)
                    .build(historikkinnslagDelBuilder);
        }
        return this;
    }

    public HistorikkInnslagTekstBuilder medSkjermlenke(SkjermlenkeType skjermlenkeType) {
        if (SkjermlenkeType.UDEFINERT.equals(skjermlenkeType)) {
            return this;
        }
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.SKJERMLENKE)
                .medTilVerdi(skjermlenkeType)
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medNavnOgGjeldendeFra(HistorikkEndretFeltType endretFelt, String navnVerdi, LocalDate gjeldendeFraDato) {
        if (gjeldendeFraDato != null) {
            gjeldendeFraSatt = true;
        }
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.GJELDENDE_FRA)
                .medNavn(endretFelt)
                .medNavnVerdi(navnVerdi)
                .medTilVerdi(formatString(gjeldendeFraDato))
                .build(historikkinnslagDelBuilder);
        return this;
    }


    public HistorikkInnslagTekstBuilder medGjeldendeFra(LocalDate localDate) {
        if (localDate != null) {
            gjeldendeFraSatt = true;
        }
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.GJELDENDE_FRA)
                .medTilVerdi(formatString(localDate))
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public <K extends Kodeverdi> HistorikkInnslagTekstBuilder medÅrsak(K årsak) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.AARSAK)
                .medTilVerdi(årsak)
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medTema(HistorikkEndretFeltType endretFeltType, String verdi) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.ANGÅR_TEMA)
                .medNavn(endretFeltType)
                .medNavnVerdi(verdi)
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medResultat(Kodeverdi resultat) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.RESULTAT)
                .medTilVerdi(resultat)
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(Periode begrunnelse) {
        return medBegrunnelse(formatString(begrunnelse), true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(LocalDate begrunnelse) {
        return medBegrunnelse(formatString(begrunnelse), true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(Kodeverdi begrunnelse) {
        return medBegrunnelse(begrunnelse, true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(String begrunnelse) {
        String begrunnelseStr = formatString(begrunnelse);
        return medBegrunnelse(begrunnelseStr, true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(String begrunnelse, boolean erBegrunnelseEndret) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.BEGRUNNELSE)
                .medTilVerdi(begrunnelse)
                .build(historikkinnslagDelBuilder);
        this.begrunnelseEndret = erBegrunnelseEndret;
        return this;
    }

    public <K extends Kodeverdi> HistorikkInnslagTekstBuilder medBegrunnelse(K begrunnelse, boolean erBegrunnelseEndret) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.BEGRUNNELSE)
                .medTilVerdi(begrunnelse)
                .build(historikkinnslagDelBuilder);
        this.begrunnelseEndret = erBegrunnelseEndret;
        return this;
    }

    public <T> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, String navnVerdi, T fraVerdi, T tilVerdi) {
        String fraVerdiStr = formatString(fraVerdi);
        String tilVerdiStr = formatString(tilVerdi);

        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
                .medNavn(historikkEndretFeltType)
                .medNavnVerdi(navnVerdi)
                .medFraVerdi(fraVerdiStr)
                .medTilVerdi(tilVerdiStr)
                .medSekvensNr(getNesteEndredeFeltSekvensNr())
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public <K extends Kodeverdi> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, K fraVerdi, K tilVerdi) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
                .medNavn(historikkEndretFeltType)
                .medFraVerdi(fraVerdi)
                .medTilVerdi(tilVerdi)
                .medSekvensNr(getNesteEndredeFeltSekvensNr())
                .build(historikkinnslagDelBuilder);
        return this;
    }

    public <T> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, T fraVerdi, T tilVerdi) {
        if (fraVerdi instanceof Kodeverdi || tilVerdi instanceof Kodeverdi) {
            Kodeverdi fraVerdiKl = (Kodeverdi) fraVerdi;
            Kodeverdi tilVerdiKl = (Kodeverdi) tilVerdi;
            return medEndretFelt(historikkEndretFeltType, fraVerdiKl, tilVerdiKl);
        }
        String fraVerdiStr = formatString(fraVerdi);
        String tilVerdiStr = formatString(tilVerdi);

        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
                .medNavn(historikkEndretFeltType)
                .medFraVerdi(fraVerdiStr)
                .medTilVerdi(tilVerdiStr)
                .medSekvensNr(getNesteEndredeFeltSekvensNr())
                .build(historikkinnslagDelBuilder);
        return this;
    }

    private int getNesteEndredeFeltSekvensNr() {
        int neste = antallEndredeFelter;
        antallEndredeFelter++;
        return neste;
    }

    public <T> HistorikkInnslagTekstBuilder medOpplysning(HistorikkOpplysningType opplysningType, T verdi) {
        String tilVerdi = formatString(verdi);
        int sekvensNr = hentNesteOpplysningSekvensNr();
        HistorikkinnslagOldFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.OPPLYSNINGER)
                .medNavn(opplysningType)
                .medTilVerdi(tilVerdi)
                .medSekvensNr(sekvensNr)
                .build(historikkinnslagDelBuilder);
        return this;
    }

    private int hentNesteOpplysningSekvensNr() {
        int sekvensNr = antallOpplysninger;
        antallOpplysninger++;
        return sekvensNr;
    }

    public HistorikkInnslagTekstBuilder medTotrinnsvurdering(Map<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> vurdering,
                                                             List<HistorikkinnslagTotrinnsvurdering> vurderingUtenVilkar) {
        boolean første = true;
        for (HistorikkinnslagTotrinnsvurdering totrinnsVurdering : vurderingUtenVilkar) {
            if (første) {
                første = false;
            } else {
                ferdigstillHistorikkinnslagDel();
            }
            leggTilTotrinnsvurdering(totrinnsVurdering);
        }

        List<Map.Entry<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>>> sortedList = vurdering.entrySet().stream()
                .sorted(getHistorikkDelComparator()).collect(Collectors.toList());

        for (Map.Entry<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> lenkeVurdering : sortedList) {
            if (første) {
                første = false;
            } else {
                ferdigstillHistorikkinnslagDel();
            }
            SkjermlenkeType skjermlenkeType = lenkeVurdering.getKey();
            List<HistorikkinnslagTotrinnsvurdering> totrinnsVurderinger = lenkeVurdering.getValue();
            totrinnsVurderinger.sort(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getAksjonspunktSistEndret));
            medSkjermlenke(skjermlenkeType);
            totrinnsVurderinger.forEach(this::leggTilTotrinnsvurdering);
        }
        return this;
    }

    private Comparator<Map.Entry<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>>> getHistorikkDelComparator() {
        return (o1, o2) -> {
            List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger1 = o1.getValue();
            List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger2 = o2.getValue();
            totrinnsvurderinger1.sort(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getAksjonspunktSistEndret));
            totrinnsvurderinger2.sort(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getAksjonspunktSistEndret));
            LocalDateTime date1 = totrinnsvurderinger1.get(0).getAksjonspunktSistEndret();
            LocalDateTime date2 = totrinnsvurderinger2.get(0).getAksjonspunktSistEndret();
            if (date1 == null || date2 == null) {
                return -1;
            }
            return date1.isAfter(date2) ? 1 : -1;
        };
    }


    private HistorikkInnslagTekstBuilder leggTilTotrinnsvurdering(HistorikkinnslagTotrinnsvurdering totrinnsvurdering) {
        int sekvensNr = getNesteAksjonspunktSekvensNr();
        leggTilFelt(HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE, totrinnsvurdering.getBegrunnelse(), sekvensNr);
        leggTilFelt(HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT, totrinnsvurdering.erGodkjent(), sekvensNr);
        leggTilFelt(HistorikkinnslagFeltType.AKSJONSPUNKT_KODE, totrinnsvurdering.getAksjonspunktDefinisjon().getKode(), sekvensNr);
        return this;
    }

    private <T> void leggTilFelt(HistorikkinnslagFeltType feltType, T verdi, int sekvensNr) {
        HistorikkinnslagOldFelt.builder()
                .medFeltType(feltType)
                .medTilVerdi(verdi != null ? verdi.toString() : null)
                .medSekvensNr(sekvensNr)
                .build(historikkinnslagDelBuilder);
    }

    private int getNesteAksjonspunktSekvensNr() {
        int sekvensNr = antallAksjonspunkter;
        antallAksjonspunkter++;
        return sekvensNr;
    }

    public int antallEndredeFelter() {
        return antallEndredeFelter;
    }

    /**
     * Returnerer om begrunnelse er endret.
     */
    public boolean getErBegrunnelseEndret() {
        return begrunnelseEndret;
    }

    /**
     * Returnerer om gjeldendeFra er satt.
     */
    public boolean getErGjeldendeFraSatt() {
        return gjeldendeFraSatt;
    }

    public HistorikkInnslagTekstBuilder ferdigstillHistorikkinnslagDel() {
        if (!historikkinnslagDelBuilder.harFelt()) {
            return this;
        }
        historikkinnslagDeler.add(historikkinnslagDelBuilder.build());
        historikkinnslagDelBuilder = HistorikkinnslagOldDel.builder();
        antallEndredeFelter = 0;
        antallAksjonspunkter = 0;
        antallOpplysninger = 0;
        begrunnelseEndret = false;
        return this;
    }

    public List<HistorikkinnslagOldDel> build(HistorikkinnslagOld historikkinnslag) {
        ferdigstillHistorikkinnslagDel();
        verify(historikkinnslag.getType());
        historikkinnslag.setHistorikkinnslagDeler(historikkinnslagDeler);
        return historikkinnslagDeler;
    }

    /**
     * Sjekker at alle påkrevde felter for gitt historikkinnslagstype er angitt
     *
     * @param historikkinnslagType
     */
    private void verify(HistorikkinnslagType historikkinnslagType) {
        List<VLException> verificationResults = new ArrayList<>();
        historikkinnslagDeler.forEach(del -> {
            Optional<VLException> exception = verify(historikkinnslagType, del);
            exception.ifPresent(verificationResults::add);
        });
        // kast exception dersom alle deler feiler valideringen
        if (verificationResults.size() == historikkinnslagDeler.size()) {
            throw verificationResults.get(0);
        }
    }

    private Optional<VLException> verify(HistorikkinnslagType historikkinnslagType, HistorikkinnslagOldDel historikkinnslagDel) {
        String type = historikkinnslagType.getMal();

        if (HistorikkInnslagMal.MAL_TYPE_1.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE);
        }
        if (HistorikkInnslagMal.MAL_TYPE_2.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE, HistorikkinnslagFeltType.SKJERMLENKE);
        }
        if (HistorikkInnslagMal.MAL_TYPE_3.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE, HistorikkinnslagFeltType.AKSJONSPUNKT_KODE);
        }
        if (HistorikkInnslagMal.MAL_TYPE_4.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE);
        }
        if (HistorikkInnslagMal.MAL_TYPE_5.equals(type) || HistorikkInnslagMal.MAL_TYPE_7.equals(type) || HistorikkInnslagMal.MAL_TYPE_8.equals(type)
                || HistorikkInnslagMal.MAL_TYPE_10.equals(type)) {
            return checkAtLeastOnePresent(type, historikkinnslagDel, HistorikkinnslagFeltType.SKJERMLENKE,
                    HistorikkinnslagFeltType.HENDELSE,
                    HistorikkinnslagFeltType.ENDRET_FELT,
                    HistorikkinnslagFeltType.BEGRUNNELSE);
        }
        if (HistorikkInnslagMal.MAL_TYPE_6.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.OPPLYSNINGER);
        }
        if (HistorikkInnslagMal.MAL_TYPE_9.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE, HistorikkinnslagFeltType.ENDRET_FELT);
        }
        throw new TekniskException("FPT-876692", String.format("Ukjent historikkinnslagstype: %s", type));
    }

    private Optional<VLException> checkFieldsPresent(String type, HistorikkinnslagOldDel del, HistorikkinnslagFeltType... fields) {
        List<HistorikkinnslagFeltType> fieldList = Arrays.asList(fields);
        Set<HistorikkinnslagFeltType> harFelt = findFields(del, fieldList).collect(Collectors.toCollection(LinkedHashSet::new));

        // harFelt skal inneholde alle de samme feltene som fieldList
        if (harFelt.size() == fields.length) {
            return Optional.empty();
        } else {
            List<String> feltKoder = fieldList.stream().map(HistorikkinnslagFeltType::getKode).collect(Collectors.toList());
            return Optional.of(new TekniskException("FPT-876694", String.format("For type %s, mangler felter %s for historikkinnslag.", type, feltKoder)));
        }
    }

    private Optional<VLException> checkAtLeastOnePresent(String type, HistorikkinnslagOldDel del, HistorikkinnslagFeltType... fields) {
        List<HistorikkinnslagFeltType> fieldList = Arrays.asList(fields);
        Optional<HistorikkinnslagFeltType> opt = findFields(del, fieldList).findAny();

        if (opt.isPresent()) {
            return Optional.empty();
        } else {
            List<String> feltKoder = fieldList.stream().map(HistorikkinnslagFeltType::getKode).collect(Collectors.toList());
            return Optional.of(new TekniskException("FPT-876693", String.format("For type %s, forventer minst et felt av type %s", type, feltKoder)));
        }
    }

    private Stream<HistorikkinnslagFeltType> findFields(HistorikkinnslagOldDel del, List<HistorikkinnslagFeltType> fieldList) {
        return del.getHistorikkinnslagFelt().stream().map(HistorikkinnslagOldFelt::getFeltType).filter(fieldList::contains);
    }

    /*
     * https://confluence.adeo.no/display/MODNAV/OMR-13+SF4+Sakshistorikk+-+UX+og+grafisk+design
     *
     * Fem design patterns:
     *
     * +----------------------------+
     * | Type 1 |
     * | BEH_VENT |
     * | BEH_GJEN |
     * | BEH_STARTET |
     * | VEDLEGG_MOTTATT |
     * | BREV_SENT |
     * | REGISTRER_PAPIRSØK |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>
     * <OPTIONAL begrunnelsestekst>
     *
     *
     * +----------------------------+
     * | Type 2 |
     * | FORSLAG_VEDTAK |
     * | VEDTAK_FATTET |
     * | OVERSTYRT (hvis beslutter) |
     * | UENDRET UTFALL |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>: <resultat>
     * <skjermlinke>
     * <OPTIONAL totrinnskontroll>
     *
     *
     * +----------------------------+
     * | Type 3 |
     * | SAK_RETUR |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>
     * <totrinnsvurdering> med <skjermlinke> til vilkåret og liste med <aksjonspunkter>
     *
     *
     * +----------------------------+
     * | Type 4 |
     * | AVBRUTT_BEH |
     * | OVERSTYRT (hvis saksbeh.) |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>
     * <årsak>
     * <begrunnelsestekst>
     *
     *
     * +----------------------------+
     * | Type 5 |
     * | FAKTA_ENDRET |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <skjermlinke>
     * <feltnavn> er endret <fra-verdi> til <til-verdi>
     * <radiogruppe> er satt til <verdi>
     * <begrunnelsestekst>
     *
     */

}
