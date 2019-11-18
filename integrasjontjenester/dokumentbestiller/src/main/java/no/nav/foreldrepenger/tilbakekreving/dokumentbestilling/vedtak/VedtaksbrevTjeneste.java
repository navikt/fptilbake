package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SøknadType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;


@ApplicationScoped
@Transaction
public class VedtaksbrevTjeneste {

    private static final String TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG = "Vedtaksbrev Tilbakekreving";
    private static final int KLAGEFRIST_UKER = 6;

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VarselRepository varselRepository;
    private FaktaFeilutbetalingRepository faktaRepository;
    private VurdertForeldelseRepository foreldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;
    private VarselbrevSporingRepository varselbrevSporingRepository;
    private VedtaksbrevSporingRepository vedtaksbrevSporingRepository;

    private BehandlingTjeneste behandlingTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;

    @Inject
    public VedtaksbrevTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                               TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                               BehandlingTjeneste behandlingTjeneste,
                               EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                               FritekstbrevTjeneste bestillDokumentTjeneste,
                               HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = behandlingRepositoryProvider.getVarselRepository();
        this.faktaRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.foreldelseRepository = behandlingRepositoryProvider.getVurdertForeldelseRepository();
        this.vilkårsvurderingRepository = behandlingRepositoryProvider.getVilkårsvurderingRepository();
        this.vedtaksbrevFritekstRepository = behandlingRepositoryProvider.getVedtaksbrevFritekstRepository();
        this.varselbrevSporingRepository=behandlingRepositoryProvider.getVarselbrevSporingRepository();
        this.vedtaksbrevSporingRepository = behandlingRepositoryProvider.getVedtaksbrevSporingRepository();

        this.behandlingTjeneste = behandlingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;

    }

    public VedtaksbrevTjeneste() {
    }

    public void sendVedtaksbrev(Long behandlingId) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId);
        VedtakResultatType hovedresultat = vedtaksbrevData.getHovedresultat();
        String fagsakTypeNavn = vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk();
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(VedtaksbrevOverskrift.finnOverskriftVedtaksbrev(fagsakTypeNavn, hovedresultat))
            .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(vedtaksbrevData.getVedtaksbrevData()))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        opprettHistorikkinnslag(behandling, dokumentreferanse);
        lagreInfoOmVedtaksbrev(behandlingId, dokumentreferanse);
    }

    public byte[] hentForhåndsvisningVedtaksbrevSomPdf(HentForhåndvisningVedtaksbrevPdfDto dto) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(dto.getBehandlingId(), dto.getOppsummeringstekst(), dto.getPerioderMedTekst());
        VedtakResultatType hovedresultat = vedtaksbrevData.getHovedresultat();
        String fagsakTypeNavn = vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk();
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(VedtaksbrevOverskrift.finnOverskriftVedtaksbrev(fagsakTypeNavn, hovedresultat))
            .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(vedtaksbrevData.getVedtaksbrevData()))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
    }

    public List<Avsnitt> hentForhåndsvisningVedtaksbrevSomTekst(Long behandlingId) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId);
        VedtakResultatType hovedresultat = vedtaksbrevData.getHovedresultat();
        String fagsakTypeNavn = vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk();
        String hovedoverskrift = VedtaksbrevOverskrift.finnOverskriftVedtaksbrev(fagsakTypeNavn, hovedresultat);
        return TekstformatererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(vedtaksbrevData.getVedtaksbrevData(), hovedoverskrift);
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse) {
        String tittel = TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG;
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(behandling, dokumentreferanse.getJournalpostId(), dokumentreferanse.getDokumentId(), tittel);
    }

    private void lagreInfoOmVedtaksbrev(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse) {
        VedtaksbrevSporing vedtaksbrevSporing = new VedtaksbrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .build();
        vedtaksbrevSporingRepository.lagreVedtaksbrevData(vedtaksbrevSporing);
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId) {
        String fritekstOppsummering = hentOppsummeringFritekst(behandlingId);
        List<PeriodeMedTekstDto> fritekstPerioder = hentFriteksterTilPerioder(behandlingId);
        return hentDataForVedtaksbrev(behandlingId, fritekstOppsummering, fritekstPerioder);
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId, String oppsummeringFritekst, List<PeriodeMedTekstDto> perioderFritekst) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        //TODO hent data fra fpsak i tidligere steg, og hent fra repository her
        SamletEksternBehandlingInfo fpsakBehandling = hentDataFraFpsak(behandlingId);
        Long varsletBeløp = finnVarsletBeløp(behandlingId);
        LocalDate varsletDato = finnVarsletDato(behandlingId);
        BeregningResultat beregnetResultat = tilbakekrevingBeregningTjeneste.beregn(behandlingId);
        List<BeregningResultatPeriode> resulatPerioder = beregnetResultat.getBeregningResultatPerioder();
        VedtakResultatType vedtakResultatType = beregnetResultat.getVedtakResultatType();
        FaktaFeilutbetaling fakta = faktaRepository.finnFaktaOmFeilutbetaling(behandlingId).orElseThrow();
        List<VilkårVurderingPeriodeEntitet> vilkårPerioder = finnVilkårVurderingPerioder(behandlingId);
        VurdertForeldelse foreldelse = foreldelseRepository.finnVurdertForeldelse(behandlingId).orElse(null);

        BigDecimal totaltTilbakekrevesUtenRenter = summer(resulatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløpUtenRenter);
        BigDecimal totaltTilbakekrevesMedRenter = summer(resulatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);
        BigDecimal totaltSkattetrekk = summer(resulatPerioder, BeregningResultatPeriode::getSkattBeløp);
        BigDecimal totaltRentebeløp = summer(resulatPerioder, BeregningResultatPeriode::getRenteBeløp);
        BigDecimal totaltTilbakekrevesBeløpMedRenterUtenSkatt = totaltTilbakekrevesMedRenter.subtract(totaltSkattetrekk);
        String hjemmelstekst = VedtakHjemmel.lagHjemmelstekst(vedtakResultatType, foreldelse, vilkårPerioder);
        HbVedtaksbrevFelles.Builder vedtakDataBuilder = HbVedtaksbrevFelles.builder()
            .medYtelsetype(behandling.getFagsak().getFagsakYtelseType())
            .medVarsletDato(varsletDato)
            .medVarsletBeløp(varsletBeløp)
            .medDatoFagsakvedtak(fpsakBehandling.getGrunninformasjon().getVedtakDato())
            .medAntallBarn(fpsakBehandling.getAntallBarnSøktFor())
            .medErFødsel(SøknadType.FØDSEL == fpsakBehandling.getSøknadType())
            .medErAdopsjon(SøknadType.ADOPSJON == fpsakBehandling.getSøknadType())
            .medFritekstOppsummering(oppsummeringFritekst)
            .medLovhjemmelVedtak(hjemmelstekst)
            .medTotaltTilbakekrevesBeløp(totaltTilbakekrevesUtenRenter)
            .medTotaltRentebeløp(totaltRentebeløp)
            .medTotaltTilbakekrevesBeløpMedRenter(totaltTilbakekrevesMedRenter)
            .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(totaltTilbakekrevesBeløpMedRenterUtenSkatt)
            .medHovedresultat(vedtakResultatType)
            .medKlagefristUker(KLAGEFRIST_UKER);

        List<HbVedtaksbrevPeriode> perioder = resulatPerioder.stream()
            .map(brp -> lagBrevdataPeriode(brp, fakta, vilkårPerioder, foreldelse, perioderFritekst))
            .collect(Collectors.toList());

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtakDataBuilder.build(), perioder);
        BrevMetadata brevMetadata = lagMetadataForVedtaksbrev(behandling, vedtakResultatType, fpsakBehandling);
        return new VedtaksbrevData(data, brevMetadata);
    }

    private SamletEksternBehandlingInfo hentDataFraFpsak(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandlingId).getEksternUuid();
        return eksternDataForBrevTjeneste.hentBehandlingFpsak(fpsakBehandlingUuid, Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.SØKNAD);
    }

    private List<VilkårVurderingPeriodeEntitet> finnVilkårVurderingPerioder(Long behandlingId) {
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .map(VilkårVurderingEntitet::getPerioder)
            .orElse(Collections.emptyList());
    }

    private LocalDate finnVarsletDato(Long behandlingId) {
        List<VarselbrevSporing> varselbrevData = varselbrevSporingRepository.hentVarselbrevData(behandlingId);
        Optional<LocalDateTime> nyesteVarselbrevTidspunkt = VedtaksbrevUtil.finnNyesteVarselbrevTidspunkt(varselbrevData);
        return nyesteVarselbrevTidspunkt.map(LocalDateTime::toLocalDate).orElse(null);
    }

    private Long finnVarsletBeløp(Long behandlingId) {
        Optional<VarselInfo> varselInfo = varselRepository.finnVarsel(behandlingId);
        return varselInfo.map(VarselInfo::getVarselBeløp).orElse(null);
    }

    BrevMetadata lagMetadataForVedtaksbrev(Behandling behandling, VedtakResultatType vedtakResultatType, SamletEksternBehandlingInfo eksternBehandlingsinfo) {
        String aktørId = eksternBehandlingsinfo.getPersonopplysninger().getAktoerId();
        FagsakYtelseType fagsakType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = eksternBehandlingsinfo.getGrunninformasjon().getSpråkkodeEllerDefault();

        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakType, språkkode);

        boolean tilbakekreves = VedtakResultatType.FULL_TILBAKEBETALING.equals(vedtakResultatType) ||
            VedtakResultatType.DELVIS_TILBAKEBETALING.equals(vedtakResultatType);

        return new BrevMetadata.Builder()
            .medAnsvarligSaksbehandler(StringUtils.isNotEmpty(behandling.getAnsvarligSaksbehandler()) ? behandling.getAnsvarligSaksbehandler() : "VL")
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medMottakerAdresse(adresseinfo)
            .medFagsaktype(fagsakType)
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medSakspartNavn(personinfo.getNavn())
            .medSprakkode(personinfo.getForetrukketSpråk())
            .medTittel(VedtaksbrevOverskrift.finnTittelVedtaksbrev(ytelseNavn.getNavnPåBokmål(), tilbakekreves))
            .build();
    }

    private BigDecimal summer(List<BeregningResultatPeriode> beregningResultatPerioder, Function<BeregningResultatPeriode, BigDecimal> hva) {
        return beregningResultatPerioder.stream()
            .map(hva)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HbVedtaksbrevPeriode lagBrevdataPeriode(BeregningResultatPeriode resultatPeriode, FaktaFeilutbetaling fakta, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, VurdertForeldelse foreldelse, List<PeriodeMedTekstDto> perioderFritekst) {
        Periode periode = resultatPeriode.getPeriode();

        HbVedtaksbrevPeriode.Builder builder = HbVedtaksbrevPeriode.builder()
            .medPeriode(periode)
            .medHendelsetype(finnHendelseType(periode, fakta))
            .medHendelseUndertype(finnHendelseUnderType(periode, fakta))
            .medFeilutbetaltBeløp(resultatPeriode.getFeilutbetaltBeløp())
            .medTilbakekrevesBeløp(resultatPeriode.getTilbakekrevingBeløpUtenRenter())
            .medRiktigBeløp(resultatPeriode.getRiktigYtelseBeløp())
            .medUtbetaltBeløp(resultatPeriode.getUtbetaltYtelseBeløp())
            .medRenterBeløp(resultatPeriode.getRenteBeløp());

        PeriodeMedTekstDto fritekst = finnPeriodeFritekster(periode, perioderFritekst);
        if (fritekst != null) {
            builder
                .medFritekstFakta(fritekst.getFaktaAvsnitt())
                .medFritekstVilkår(fritekst.getVilkårAvsnitt())
                .medFritekstSærligeGrunner(fritekst.getSærligeGrunnerAvsnitt())
                .medFritekstSærligGrunnAnnet(fritekst.getSærligeGrunnerAnnetAvsnitt());
        }

        VilkårVurderingPeriodeEntitet vilkårvurdering = finnVilkårvurdering(periode, vilkårPerioder);
        if (vilkårvurdering != null) {
            builder.medVilkårResultat(vilkårvurdering.getVilkårResultat());
            VilkårVurderingAktsomhetEntitet aktsomhet = vilkårvurdering.getAktsomhet();
            if (aktsomhet != null) {
                builder.medUnntasInnkrevingPgaLavtBeløp(Boolean.FALSE.equals(aktsomhet.getTilbakekrevSmåBeløp()));
                builder.medAktsomhetResultat(aktsomhet.getAktsomhet());
                builder.medSærligeGrunner(aktsomhet
                    .getSærligGrunner().stream()
                    .map(VilkårVurderingSærligGrunnEntitet::getGrunn)
                    .collect(Collectors.toSet())
                );
                if (aktsomhet.getTilbakekrevSmåBeløp() != null) {
                    builder.medUnntasInnkrevingPgaLavtBeløp(!aktsomhet.getTilbakekrevSmåBeløp());
                }
            }
            VilkårVurderingGodTroEntitet godTro = vilkårvurdering.getGodTro();
            if (godTro != null) {
                builder.medAktsomhetResultat(AnnenVurdering.GOD_TRO);
                builder.medBeløpIBehold(resultatPeriode.getManueltSattTilbakekrevingsbeløp());
            }
        }

        VurdertForeldelsePeriode foreldelsePeriode = finnForeldelsePeriode(foreldelse, periode);
        if (foreldelsePeriode != null) {
            if (foreldelsePeriode.erForeldet()) {
                builder.medAktsomhetResultat(AnnenVurdering.FORELDET);
                builder.medForeldetBeløp(resultatPeriode.getFeilutbetaltBeløp().subtract(resultatPeriode.getTilbakekrevingBeløp()));
            }
            builder.medForeldelsevurdering(foreldelsePeriode.getForeldelseVurderingType());
        } else {
            builder.medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT);
        }
        return builder.build();
    }

    private VurdertForeldelsePeriode finnForeldelsePeriode(VurdertForeldelse foreldelse, Periode periode) {
        if (foreldelse == null) {
            return null;
        }
        return foreldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(p -> p.getPeriode().omslutter(periode))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke VurdertForeldelse-periode som omslutter periode " + periode));
    }


    private PeriodeMedTekstDto finnPeriodeFritekster(Periode periode, List<PeriodeMedTekstDto> perioder) {
        for (PeriodeMedTekstDto fritekstPeriode : perioder) {
            if (fritekstPeriode.getPeriode().equals(periode)) {
                return fritekstPeriode;
            }
        }
        return null;
    }

    private VilkårVurderingPeriodeEntitet finnVilkårvurdering(Periode periode, List<VilkårVurderingPeriodeEntitet> vilkårPerioder) {
        for (VilkårVurderingPeriodeEntitet vurdering : vilkårPerioder) {
            if (vurdering.getPeriode().omslutter(periode)) {
                return vurdering;
            }
        }
        return null; //skjer ved foreldet periode
    }

    private HendelseType finnHendelseType(Periode periode, FaktaFeilutbetaling fakta) {
        for (FaktaFeilutbetalingPeriode faktaPeriode : fakta.getFeilutbetaltPerioder()) {
            if (faktaPeriode.getPeriode().omslutter(periode)) {
                return faktaPeriode.getHendelseType();
            }
        }
        throw new IllegalArgumentException("Fant ikke fakta-periode som omslutter periode " + periode);
    }

    private HendelseUnderType finnHendelseUnderType(Periode periode, FaktaFeilutbetaling fakta) {
        for (FaktaFeilutbetalingPeriode faktaPeriode : fakta.getFeilutbetaltPerioder()) {
            if (faktaPeriode.getPeriode().omslutter(periode)) {
                return faktaPeriode.getHendelseUndertype();
            }
        }
        throw new IllegalArgumentException("Fant ikke fakta-periode som omslutter periode " + periode);
    }

    private List<PeriodeMedTekstDto> hentFriteksterTilPerioder(Long behandlingId) {
        List<VedtaksbrevFritekstPeriode> eksisterendePerioderForBrev = vedtaksbrevFritekstRepository.hentVedtaksbrevPerioderMedTekst(behandlingId);
        return VedtaksbrevUtil.mapFritekstFraDb(eksisterendePerioderForBrev);
    }

    private String hentOppsummeringFritekst(Long behandlingId) {
        Optional<VedtaksbrevFritekstOppsummering> vedtaksbrevOppsummeringOpt = vedtaksbrevFritekstRepository.hentVedtaksbrevOppsummering(behandlingId);
        return vedtaksbrevOppsummeringOpt.map(VedtaksbrevFritekstOppsummering::getOppsummeringFritekst).orElse(null);
    }

}
