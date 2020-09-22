package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbBehandling;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevDatoer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbFakta;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SøknadType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;


@ApplicationScoped
@Transactional
public class VedtaksbrevTjeneste {

    private static final String TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG = "Vedtaksbrev Tilbakekreving";
    private static final String TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG_TIL_VERGE = "Vedtaksbrev Tilbakekreving til verge";
    private static final String TITTEL_VEDTAK_TILBAKEBETALING = "Vedtak tilbakebetaling ";
    private static final String TITTEL_VEDTAK_INGEN_TILBAKEBETALING = "Vedtak ingen tilbakebetaling ";
    private static final int KLAGEFRIST_UKER = 6;

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VarselRepository varselRepository;
    private FaktaFeilutbetalingRepository faktaRepository;
    private VurdertForeldelseRepository foreldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;
    private BrevSporingRepository brevSporingRepository;
    private VergeRepository vergeRepository;

    private BehandlingTjeneste behandlingTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;

    private JournalføringTjeneste journalføringTjeneste;

    @Inject
    public VedtaksbrevTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                               TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                               BehandlingTjeneste behandlingTjeneste,
                               EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                               FritekstbrevTjeneste bestillDokumentTjeneste,
                               HistorikkinnslagTjeneste historikkinnslagTjeneste,
                               JournalføringTjeneste journalføringTjeneste) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = behandlingRepositoryProvider.getVarselRepository();
        this.faktaRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.foreldelseRepository = behandlingRepositoryProvider.getVurdertForeldelseRepository();
        this.vilkårsvurderingRepository = behandlingRepositoryProvider.getVilkårsvurderingRepository();
        this.vedtaksbrevFritekstRepository = behandlingRepositoryProvider.getVedtaksbrevFritekstRepository();
        this.brevSporingRepository = behandlingRepositoryProvider.getBrevSporingRepository();
        this.vergeRepository = behandlingRepositoryProvider.getVergeRepository();

        this.behandlingTjeneste = behandlingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.journalføringTjeneste = journalføringTjeneste;
    }

    public VedtaksbrevTjeneste() {
    }

    public void sendVedtaksbrev(Long behandlingId, BrevMottaker brevMottaker) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId, brevMottaker);
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode()))
            .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(hbVedtaksbrevData))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        byte[] vedlegg = lagVedtaksbrevVedleggTabellPdf(vedtaksbrevData);
        JournalpostIdOgDokumentId vedleggReferanse = journalføringTjeneste.journalførVedlegg(behandlingId, vedlegg);
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data, vedleggReferanse);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        opprettHistorikkinnslag(behandling, dokumentreferanse, brevMottaker);
        lagreInfoOmVedtaksbrev(behandlingId, dokumentreferanse);
    }

    private byte[] lagVedtaksbrevVedleggTabellPdf(VedtaksbrevData vedtaksbrevData) {
        VedtaksbrevVedleggTjeneste vedleggTjeneste = new VedtaksbrevVedleggTjeneste();
        return vedleggTjeneste.lagVedlegg(vedtaksbrevData);
    }

    public byte[] hentForhåndsvisningVedtaksbrevMedVedleggSomPdf(HentForhåndvisningVedtaksbrevPdfDto dto) {
        Long behandlingId = hentBehandlingId(dto);
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId, dto.getOppsummeringstekst(),
            dto.getPerioderMedTekst(),getBrevMottaker(behandlingId));
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode()))
            .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(hbVedtaksbrevData))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        byte[] vedtaksbrevPdf = bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
        byte[] vedlegg = lagVedtaksbrevVedleggTabellPdf(vedtaksbrevData);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDFMergerUtility mergerUtil = new PDFMergerUtility();
        mergerUtil.setDestinationStream(baos);
        mergerUtil.addSource(new ByteArrayInputStream(vedtaksbrevPdf));
        mergerUtil.addSource(new ByteArrayInputStream(vedlegg));
        try {
            mergerUtil.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        } catch (IOException e) {
            throw new RuntimeException("Fikk IO exception ved forhåndsvisning inkl vedlegg", e);
        }
        return baos.toByteArray();
    }

    private Long hentBehandlingId(HentForhåndvisningVedtaksbrevPdfDto dto) {
        Long behandlingId;
        if (dto.getBehandlingReferanse().erInternBehandlingId()) {
            behandlingId = dto.getBehandlingReferanse().getBehandlingId();
        } else {
            Behandling behandling = behandlingRepository.hentBehandling(dto.getBehandlingReferanse().getBehandlingUuid());
            behandlingId = behandling.getId();
        }
        return behandlingId;
    }

    public List<Avsnitt> hentForhåndsvisningVedtaksbrevSomTekst(Long behandlingId) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId,getBrevMottaker(behandlingId));
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        String hovedoverskrift = TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode());
        return TekstformatererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(hbVedtaksbrevData, hovedoverskrift);
    }

    private BrevMottaker getBrevMottaker(Long behandlingId) {
        return vergeRepository.finnesVerge(behandlingId) ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, BrevMottaker brevMottaker) {
        String tittel = BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG_TIL_VERGE
            : TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG;
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(behandling, dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(), tittel);
    }

    private void lagreInfoOmVedtaksbrev(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse) {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .medBrevType(BrevType.VEDTAK_BREV)
            .build();
        brevSporingRepository.lagre(brevSporing);
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId, BrevMottaker brevMottaker) {
        String fritekstOppsummering = hentOppsummeringFritekst(behandlingId);
        List<PeriodeMedTekstDto> fritekstPerioder = hentFriteksterTilPerioder(behandlingId);
        return hentDataForVedtaksbrev(behandlingId, fritekstOppsummering, fritekstPerioder, brevMottaker);
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId,
                                                  String oppsummeringFritekst,
                                                  List<PeriodeMedTekstDto> perioderFritekst,
                                                  BrevMottaker brevMottaker) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        //TODO hent data fra fpsak i tidligere steg, og hent fra repository her
        SamletEksternBehandlingInfo fpsakBehandling = hentDataFraFpsak(behandling);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
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

        boolean erRevurdering = BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType());
        boolean erKlage = behandling.getBehandlingÅrsaker().stream()
            .anyMatch(ba -> ba.getBehandlingÅrsakType() == BehandlingÅrsakType.RE_KLAGE_KA || ba.getBehandlingÅrsakType() == BehandlingÅrsakType.RE_KLAGE_NFP);
        boolean erFrisinn = erFrisinn(behandling);
        VedtakHjemmel.EffektForBruker effektForBruker = erRevurdering
            ? hentEffektForBruker(behandling, totaltTilbakekrevesMedRenter)
            : VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK;
        LocalDate originalBehandlingVedtaksdato = erRevurdering ? finnOriginalBehandlingVedtaksdato(behandling) : null;

        Språkkode språkkode = fpsakBehandling.getGrunninformasjon().getSpråkkodeEllerDefault();
        String hjemmelstekst = VedtakHjemmel.lagHjemmelstekst(vedtakResultatType, foreldelse, vilkårPerioder, effektForBruker, språkkode, !erFrisinn);

        List<HbVedtaksbrevPeriode> perioder = resulatPerioder.stream()
            .map(brp -> lagBrevdataPeriode(brp, fakta, vilkårPerioder, foreldelse, perioderFritekst))
            .collect(Collectors.toList());

        BrevMetadata brevMetadata = lagMetadataForVedtaksbrev(behandling, vedtakResultatType, fpsakBehandling, personinfo , brevMottaker);

        HbSak.Builder hbSakBuilder = HbSak.build()
            .medYtelsetype(behandling.getFagsak().getFagsakYtelseType())
            .medDatoFagsakvedtak(fpsakBehandling.getGrunninformasjon().getVedtakDato())
            .medAntallBarn(fpsakBehandling.getAntallBarnSøktFor());
        if (!erFrisinn) {
            hbSakBuilder
                .medErFødsel(SøknadType.FØDSEL == fpsakBehandling.getSøknadType())
                .medErAdopsjon(SøknadType.ADOPSJON == fpsakBehandling.getSøknadType());
        }
        HbVedtaksbrevFelles.Builder vedtakDataBuilder = HbVedtaksbrevFelles.builder()
            .medSak(hbSakBuilder.build())
            .medBehandling(HbBehandling.builder()
                .medErRevurdering(erRevurdering)
                .medErKlage(erKlage)
                .medOriginalBehandlingDatoFagsakvedtak(originalBehandlingVedtaksdato)
                .build())
            .medVarsel(HbVarsel.forDatoOgBeløp(varsletDato, varsletBeløp))
            .medFritekstOppsummering(oppsummeringFritekst)
            .medLovhjemmelVedtak(hjemmelstekst)
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(vedtakResultatType)
                .medTotaltTilbakekrevesBeløp(totaltTilbakekrevesUtenRenter)
                .medTotaltRentebeløp(totaltRentebeløp)
                .medTotaltTilbakekrevesBeløpMedRenter(totaltTilbakekrevesMedRenter)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(totaltTilbakekrevesBeløpMedRenterUtenSkatt)
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(KLAGEFRIST_UKER)
                .build())
            .medDatoer(HbVedtaksbrevDatoer.builder()
                .medPerioder(perioder)
                .build())
            .medSøker(utledSøker(personinfo))
            .medSpråkkode(språkkode)
            .medFinnesVerge(brevMetadata.isFinnesVerge())
            .medAnnenMottakerNavn(BrevMottakerUtil.getAnnenMottakerNavn(brevMetadata));

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtakDataBuilder.build(), perioder);

        return new VedtaksbrevData(data, brevMetadata);
    }

    private VedtakHjemmel.EffektForBruker hentEffektForBruker(Behandling behandling, BigDecimal totaltTilbakekrevesMedRenter) {
        BehandlingÅrsak behandlingÅrsak = behandling.getBehandlingÅrsaker().get(0);
        Behandling originalBehandling = behandlingÅrsak.getOriginalBehandling().orElseThrow();

        BeregningResultat originaltBeregnetResultat = tilbakekrevingBeregningTjeneste.beregn(originalBehandling.getId());
        List<BeregningResultatPeriode> originalBeregningResultatPerioder = originaltBeregnetResultat.getBeregningResultatPerioder();
        BigDecimal originalBehandlingTotaltMedRenter = summer(originalBeregningResultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);

        boolean positivtForBruker = totaltTilbakekrevesMedRenter.compareTo(originalBehandlingTotaltMedRenter) < 0;
        return positivtForBruker ? VedtakHjemmel.EffektForBruker.ENDRET_TIL_GUNST_FOR_BRUKER : VedtakHjemmel.EffektForBruker.ENDRET_TIL_UGUNST_FOR_BRUKER;
    }

    private LocalDate finnOriginalBehandlingVedtaksdato(Behandling behandling) {
        BehandlingÅrsak behandlingÅrsak = behandling.getBehandlingÅrsaker().get(0);
        Behandling originalBehandling = behandlingÅrsak.getOriginalBehandling().orElseThrow();

        return behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(originalBehandling.getId())
            .orElseThrow()
            .getVedtaksdato();
    }

    private HbPerson utledSøker(Personinfo personinfo) {
        char[] delimiters = new char[]{' ', '-'};
        return HbPerson.builder()
            .medNavn(WordUtils.capitalizeFully(personinfo.getNavn(), delimiters))
            .medDødsdato(personinfo.getDødsdato())
            .medErGift(personinfo.getSivilstandType().erGift() || personinfo.getSivilstandType().erEtterlatt())
            .medErPartner(personinfo.getSivilstandType().erPartner())
            .build();
    }

    private SamletEksternBehandlingInfo hentDataFraFpsak(Behandling behandling) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandling.getId()).getEksternUuid();
        return eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid, ønsketTillegsinformasjon(behandling));
    }

    private Tillegsinformasjon[] ønsketTillegsinformasjon(Behandling behandling) {
        List<Tillegsinformasjon> tillegsinformasjons = new ArrayList<>();
        tillegsinformasjons.add(Tillegsinformasjon.PERSONOPPLYSNINGER);

        // Kan ikke hente søknadsinformasjon for FRISINN-behandlinger. Er ikke nødvendigvis en 1-til-1-mapping mellom behandling
        // og søknad for FRISINN i k9-sak. Kan risikere exception i k9-sak og/eller exception i k9-tilbake
        if (!erFrisinn(behandling)) {
            tillegsinformasjons.add(Tillegsinformasjon.SØKNAD);
        }
        return tillegsinformasjons.toArray(Tillegsinformasjon[]::new);
    }

    private List<VilkårVurderingPeriodeEntitet> finnVilkårVurderingPerioder(Long behandlingId) {
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .map(VilkårVurderingEntitet::getPerioder)
            .orElse(Collections.emptyList());
    }

    private LocalDate finnVarsletDato(Long behandlingId) {
        Optional<BrevSporing> varselbrevData = brevSporingRepository.hentSistSendtVarselbrev(behandlingId);
        return varselbrevData.map(brevSporing -> brevSporing.getOpprettetTidspunkt().toLocalDate()).orElse(null);
    }

    private Long finnVarsletBeløp(Long behandlingId) {
        Optional<VarselInfo> varselInfo = varselRepository.finnVarsel(behandlingId);
        return varselInfo.map(VarselInfo::getVarselBeløp).orElse(null);
    }

    BrevMetadata lagMetadataForVedtaksbrev(Behandling behandling, VedtakResultatType vedtakResultatType,
                                           SamletEksternBehandlingInfo eksternBehandlingsinfo, Personinfo personinfo,
                                           BrevMottaker brevMottaker) {
        FagsakYtelseType fagsakType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = eksternBehandlingsinfo.getGrunninformasjon().getSpråkkodeEllerDefault();

        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandling.getId());
        boolean finnesVerge = vergeEntitet.isPresent();

        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo,brevMottaker,vergeEntitet);
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakType, språkkode);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet,adresseinfo);

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
            .medSprakkode(eksternBehandlingsinfo.getGrunninformasjon().getSpråkkodeEllerDefault())
            .medTittel(finnTittelVedtaksbrev(ytelseNavn.getNavnPåBokmål(), tilbakekreves))
            .medFinnesVerge(finnesVerge)
            .medVergeNavn(vergeNavn)
            .build();
    }

    private BigDecimal summer(List<BeregningResultatPeriode> beregningResultatPerioder, Function<BeregningResultatPeriode, BigDecimal> hva) {
        return beregningResultatPerioder.stream()
            .map(hva)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HbVedtaksbrevPeriode lagBrevdataPeriode(BeregningResultatPeriode resultatPeriode, FaktaFeilutbetaling fakta, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, VurdertForeldelse foreldelse, List<PeriodeMedTekstDto> perioderFritekst) {
        Periode periode = resultatPeriode.getPeriode();
        PeriodeMedTekstDto fritekster = finnPeriodeFritekster(periode, perioderFritekst);

        return HbVedtaksbrevPeriode.builder()
            .medPeriode(periode)
            .medKravgrunnlag(utledKravgrunnlag(resultatPeriode))
            .medFakta(utledFakta(periode, fakta, fritekster))
            .medVurderinger(utledVurderinger(periode, vilkårPerioder, foreldelse, fritekster))
            .medResultat(utledResultat(resultatPeriode, foreldelse)).build();
    }

    private HbKravgrunnlag utledKravgrunnlag(BeregningResultatPeriode resultatPeriode) {
        return HbKravgrunnlag.builder()
            .medFeilutbetaltBeløp(resultatPeriode.getFeilutbetaltBeløp())
            .medRiktigBeløp(resultatPeriode.getRiktigYtelseBeløp())
            .medUtbetaltBeløp(resultatPeriode.getUtbetaltYtelseBeløp())
            .build();
    }

    private HbFakta utledFakta(Periode periode, FaktaFeilutbetaling fakta, PeriodeMedTekstDto fritekst) {
        for (FaktaFeilutbetalingPeriode faktaPeriode : fakta.getFeilutbetaltPerioder()) {
            if (faktaPeriode.getPeriode().omslutter(periode)) {
                return HbFakta.builder()
                    .medHendelsetype(faktaPeriode.getHendelseType())
                    .medHendelseUndertype(faktaPeriode.getHendelseUndertype())
                    .medFritekstFakta(fritekst != null ? fritekst.getFaktaAvsnitt() : null)
                    .build();
            }
        }
        throw new IllegalArgumentException("Fant ikke fakta-periode som omslutter periode " + periode);
    }

    private HbVurderinger utledVurderinger(Periode periode, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, VurdertForeldelse foreldelse, PeriodeMedTekstDto fritekst) {
        HbVurderinger.Builder builder = HbVurderinger.builder();
        leggTilVilkårvurdering(builder, periode, vilkårPerioder, fritekst);
        leggTilForeldelseVurdering(builder, periode, foreldelse);
        return builder.build();
    }

    private void leggTilVilkårvurdering(HbVurderinger.Builder builder, Periode periode, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, PeriodeMedTekstDto fritekst) {
        builder.medFritekstVilkår(fritekst != null ? fritekst.getVilkårAvsnitt() : null);
        VilkårVurderingPeriodeEntitet vilkårvurdering = finnVilkårvurdering(periode, vilkårPerioder);
        if (vilkårvurdering == null) {
            return;
        }
        builder.medVilkårResultat(vilkårvurdering.getVilkårResultat());
        VilkårVurderingAktsomhetEntitet aktsomhet = vilkårvurdering.getAktsomhet();
        if (aktsomhet != null) {
            boolean unntasInnkrevingPgaLavtBeløp = Boolean.FALSE.equals(aktsomhet.getTilbakekrevSmåBeløp());
            builder.medUnntasInnkrevingPgaLavtBeløp(unntasInnkrevingPgaLavtBeløp);
            builder.medAktsomhetResultat(aktsomhet.getAktsomhet());
            if (skalHaSærligeGrunner(aktsomhet.getAktsomhet(), unntasInnkrevingPgaLavtBeløp)) {
                Set<SærligGrunn> særligeGrunner = aktsomhet
                    .getSærligGrunner().stream()
                    .map(VilkårVurderingSærligGrunnEntitet::getGrunn)
                    .collect(Collectors.toSet());
                String fritekstSærligeGrunner = fritekst != null ? fritekst.getSærligeGrunnerAvsnitt() : null;
                String fritekstSærligGrunnAnnet = fritekst != null ? fritekst.getSærligeGrunnerAnnetAvsnitt() : null;
                builder.medSærligeGrunner(særligeGrunner, fritekstSærligeGrunner, fritekstSærligGrunnAnnet);
            }
        }
        VilkårVurderingGodTroEntitet godTro = vilkårvurdering.getGodTro();
        if (godTro != null) {
            builder.medAktsomhetResultat(AnnenVurdering.GOD_TRO);
            builder.medBeløpIBehold(godTro.isBeløpErIBehold() ? godTro.getBeløpTilbakekreves() : BigDecimal.ZERO);
        }
    }

    private void leggTilForeldelseVurdering(HbVurderinger.Builder builder, Periode periode, VurdertForeldelse foreldelse) {
        VurdertForeldelsePeriode foreldelsePeriode = finnForeldelsePeriode(foreldelse, periode);
        if (foreldelsePeriode != null) {
            if (foreldelsePeriode.erForeldet()) {
                builder.medAktsomhetResultat(AnnenVurdering.FORELDET);
            }
            builder.medForeldelsevurdering(foreldelsePeriode.getForeldelseVurderingType());
        } else {
            builder.medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT);
        }
    }

    private HbResultat utledResultat(BeregningResultatPeriode resultatPeriode, VurdertForeldelse foreldelse) {
        HbResultat.Builder builder = HbResultat.builder()
            .medTilbakekrevesBeløp(resultatPeriode.getTilbakekrevingBeløpUtenRenter())
            .medTilbakekrevesBeløpUtenSkatt(resultatPeriode.getTilbakekrevingBeløpEtterSkatt())
            .medRenterBeløp(resultatPeriode.getRenteBeløp());

        VurdertForeldelsePeriode foreldelsePeriode = finnForeldelsePeriode(foreldelse, resultatPeriode.getPeriode());
        boolean foreldetPeriode = foreldelsePeriode != null && foreldelsePeriode.erForeldet();
        if (foreldetPeriode) {
            builder.medForeldetBeløp(resultatPeriode.getFeilutbetaltBeløp().subtract(resultatPeriode.getTilbakekrevingBeløp()));
        }
        return builder.build();
    }

    private boolean skalHaSærligeGrunner(Aktsomhet aktsomhet, boolean unntattPgaLavgBeløp) {
        return Aktsomhet.GROVT_UAKTSOM.equals(aktsomhet) || Aktsomhet.SIMPEL_UAKTSOM.equals(aktsomhet) && !unntattPgaLavgBeløp;
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

    private List<PeriodeMedTekstDto> hentFriteksterTilPerioder(Long behandlingId) {
        List<VedtaksbrevFritekstPeriode> eksisterendePerioderForBrev = vedtaksbrevFritekstRepository.hentVedtaksbrevPerioderMedTekst(behandlingId);
        return VedtaksbrevUtil.mapFritekstFraDb(eksisterendePerioderForBrev);
    }

    private String hentOppsummeringFritekst(Long behandlingId) {
        Optional<VedtaksbrevFritekstOppsummering> vedtaksbrevOppsummeringOpt = vedtaksbrevFritekstRepository.hentVedtaksbrevOppsummering(behandlingId);
        return vedtaksbrevOppsummeringOpt.map(VedtaksbrevFritekstOppsummering::getOppsummeringFritekst).orElse(null);
    }

    private String finnTittelVedtaksbrev(String fagsaktypenavnBokmål, boolean tilbakekreves) {
        if (tilbakekreves) {
            return TITTEL_VEDTAK_TILBAKEBETALING + fagsaktypenavnBokmål;
        } else {
            return TITTEL_VEDTAK_INGEN_TILBAKEBETALING + fagsaktypenavnBokmål;
        }
    }

    private boolean erFrisinn(Behandling behandling) {
        return FagsakYtelseType.FRISINN.equals(behandling.getFagsak().getFagsakYtelseType());
    }
}
