package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.internal.text.WordUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstValidator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
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
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
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


@ApplicationScoped
@Transactional
public class VedtaksbrevTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(VedtaksbrevTjeneste.class);

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
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;

    private PdfBrevTjeneste pdfBrevTjeneste;

    @Inject
    public VedtaksbrevTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                               BeregningsresultatTjeneste beregningsresultatTjeneste,
                               BehandlingTjeneste behandlingTjeneste,
                               EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                               PdfBrevTjeneste pdfBrevTjeneste) {
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
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    VedtaksbrevTjeneste() {
        // for CDI
    }

    public void sendVedtaksbrev(Long behandlingId, BrevMottaker brevMottaker, UUID unikBestillingUuid) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId, brevMottaker);
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        FritekstbrevData data = new FritekstbrevData.Builder()
                .medOverskrift(TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode()))
                .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(hbVedtaksbrevData))
                .medMetadata(vedtaksbrevData.getMetadata())
                .build();

        BrevData.Builder brevData = BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst());
        if (vedtaksbrevData.getVedtaksbrevData().getFelles().isMedVedlegg()) {
            brevData.setVedleggHtml(TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(vedtaksbrevData.getVedtaksbrevData()));
        }
        pdfBrevTjeneste.sendBrev(behandlingId, DetaljertBrevType.VEDTAK, brevData.build(), unikBestillingUuid);
    }

    public byte[] hentForhåndsvisningVedtaksbrevMedVedleggSomPdf(HentForhåndvisningVedtaksbrevPdfDto dto) {
        Long behandlingId = hentBehandlingId(dto);
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId, dto.getOppsummeringstekst(),
                dto.getPerioderMedTekst(), getBrevMottaker(behandlingId));
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        validerFritekstOppsummering(hbVedtaksbrevData.getFelles().getVedtaksbrevType(), dto.getOppsummeringstekst());
        FritekstbrevData data = new FritekstbrevData.Builder()
                .medOverskrift(TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode()))
                .medBrevtekst(TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(hbVedtaksbrevData))
                .medMetadata(vedtaksbrevData.getMetadata())
                .build();

        BrevData.Builder brevData = BrevData.builder()
                .setMottaker(getBrevMottaker(behandlingId))
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst());
        if (hbVedtaksbrevData.getFelles().isMedVedlegg()) {
            brevData.setVedleggHtml(TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(vedtaksbrevData.getVedtaksbrevData()));
        }
        return pdfBrevTjeneste.genererForhåndsvisning(brevData
                .build());
    }

    private void validerFritekstOppsummering(VedtaksbrevType vedtaksbrevType, String oppsummeringFritekst) {
        if (oppsummeringFritekst != null && oppsummeringFritekst.length() >= VedtaksbrevFritekstOppsummering.maxFritekstLengde(vedtaksbrevType)) {
            throw VedtaksbrevFritekstValidator.fritekstOppsumeringForLang();
        }
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
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId, getBrevMottaker(behandlingId));
        HbVedtaksbrevData hbVedtaksbrevData = vedtaksbrevData.getVedtaksbrevData();
        String hovedoverskrift = TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(hbVedtaksbrevData, vedtaksbrevData.getMetadata().getSpråkkode());
        return TekstformatererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(hbVedtaksbrevData, hovedoverskrift);
    }

    private BrevMottaker getBrevMottaker(Long behandlingId) {
        return vergeRepository.finnesVerge(behandlingId) ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId, BrevMottaker brevMottaker) {
        String fritekstOppsummering = hentOppsummeringFritekst(behandlingId);
        List<PeriodeMedTekstDto> fritekstPerioder = hentFriteksterTilPerioder(behandlingId);
        return hentDataForVedtaksbrev(behandlingId, fritekstOppsummering, fritekstPerioder, brevMottaker);
    }

    private VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId,
                                                   String oppsummeringFritekst,
                                                   List<PeriodeMedTekstDto> perioderFritekst,
                                                   BrevMottaker brevMottaker) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        //TODO hent data fra fpsak i tidligere steg, og hent fra repository her
        SamletEksternBehandlingInfo fagsystemBehandling = hentDataFraFagsystem(behandling);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getFagsak().getFagsakYtelseType(), behandling.getAktørId().getId());
        BeregningResultat beregnetResultat = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);

        BrevMetadata brevMetadata = lagMetadataForVedtaksbrev(behandling, fagsystemBehandling, personinfo, beregnetResultat.getVedtakResultatType(), brevMottaker);
        HbVedtaksbrevData data = lagHbVedtaksbrevData(behandling, fagsystemBehandling, personinfo, beregnetResultat, oppsummeringFritekst, perioderFritekst, brevMetadata);

        return new VedtaksbrevData(data, brevMetadata);
    }

    private HbVedtaksbrevData lagHbVedtaksbrevData(Behandling behandling,
                                                   SamletEksternBehandlingInfo fagsystemBehandling,
                                                   Personinfo personinfo,
                                                   BeregningResultat beregnetResultat,
                                                   String oppsummeringFritekst,
                                                   List<PeriodeMedTekstDto> perioderFritekst,
                                                   BrevMetadata brevMetadata) {
        List<BeregningResultatPeriode> resulatPerioder = beregnetResultat.getBeregningResultatPerioder();
        VedtakResultatType vedtakResultatType = beregnetResultat.getVedtakResultatType();

        List<VilkårVurderingPeriodeEntitet> vilkårPerioder = finnVilkårVurderingPerioder(behandling.getId());
        VurdertForeldelse foreldelse = foreldelseRepository.finnVurdertForeldelse(behandling.getId()).orElse(null);
        VedtaksbrevType vedtaksbrevType = behandling.utledVedtaksbrevType();
        HbVedtaksResultatBeløp hbVedtaksResultatBeløp = new HbVedtaksResultatBeløp(resulatPerioder);

        VedtakHjemmel.EffektForBruker effektForBruker = utledEffektForBruker(behandling, hbVedtaksResultatBeløp);
        String hjemmelstekst = VedtakHjemmel.lagHjemmelstekst(vedtakResultatType, foreldelse, vilkårPerioder, effektForBruker, brevMetadata.getSpråkkode(), erIkkeFrisinn(behandling));

        List<HbVedtaksbrevPeriode> perioder = lagHbVedtaksbrevPerioder(behandling.getId(), perioderFritekst, resulatPerioder, vilkårPerioder, foreldelse, vedtaksbrevType);
        HbTotalresultat hbTotalresultat = lagHbTotalresultat(vedtakResultatType, hbVedtaksResultatBeløp);
        HbSak sak = lagHbSak(behandling, fagsystemBehandling);
        HbBehandling hbBehandling = lagHbBehandling(behandling);

        Long varsletBeløp = finnVarsletBeløp(behandling.getId());
        LocalDate varsletDato = finnVarsletDato(behandling.getId());
        boolean erFeilutbetaltBeløpKorrigertNed = varsletBeløp != null && hbVedtaksResultatBeløp.totaltFeilutbetaltBeløp.longValue() < varsletBeløp;
        HbVedtaksbrevFelles.Builder vedtakDataBuilder = HbVedtaksbrevFelles.builder()
                .medSak(sak)
                .medBehandling(hbBehandling)
                .medVarsel(HbVarsel.forDatoOgBeløp(varsletDato, varsletBeløp))
                .medErFeilutbetaltBeløpKorrigertNed(erFeilutbetaltBeløpKorrigertNed)
                .medTotaltFeilutbetaltBeløp(hbVedtaksResultatBeløp.totaltFeilutbetaltBeløp)
                .medSkalFjerneTekstFeriepenger(skalFjerneTekstFeriepenger(perioder))
                .medFritekstOppsummering(oppsummeringFritekst)
                .medVedtaksbrevType(vedtaksbrevType)
                .medLovhjemmelVedtak(hjemmelstekst)
                .medVedtakResultat(hbTotalresultat)
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(KLAGEFRIST_UKER)
                        .build())
                .medDatoer(HbVedtaksbrevDatoer.builder()
                        .medPerioder(perioder)
                        .build())
                .medSøker(utledSøker(personinfo))
                .medSpråkkode(brevMetadata.getSpråkkode())
                .medFinnesVerge(brevMetadata.isFinnesVerge())
                .medAnnenMottakerNavn(BrevMottakerUtil.getAnnenMottakerNavn(brevMetadata));

        return new HbVedtaksbrevData(vedtakDataBuilder.build(), perioder);
    }

    private boolean skalFjerneTekstFeriepenger(List<HbVedtaksbrevPeriode> perioder) {
        return perioder.stream().anyMatch(p -> HendelseUnderType.FEIL_FERIEPENGER_4G.equals(p.getFakta().getHendelseundertype()));
    }

    private VedtakHjemmel.EffektForBruker utledEffektForBruker(Behandling behandling, HbVedtaksResultatBeløp hbVedtaksResultatBeløp) {
        boolean erRevurdering = BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType());
        return erRevurdering
                ? hentEffektForBruker(behandling, hbVedtaksResultatBeløp.totaltTilbakekrevesMedRenter)
                : VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK;
    }

    private HbTotalresultat lagHbTotalresultat(VedtakResultatType vedtakResultatType,
                                               HbVedtaksResultatBeløp hbVedtaksResultatBeløp) {
        return HbTotalresultat.builder()
                .medHovedresultat(vedtakResultatType)
                .medTotaltTilbakekrevesBeløp(hbVedtaksResultatBeløp.totaltTilbakekrevesUtenRenter)
                .medTotaltRentebeløp(hbVedtaksResultatBeløp.totaltRentebeløp)
                .medTotaltTilbakekrevesBeløpMedRenter(hbVedtaksResultatBeløp.totaltTilbakekrevesMedRenter)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(hbVedtaksResultatBeløp.totaltTilbakekrevesBeløpMedRenterUtenSkatt)
                .build();
    }

    private HbBehandling lagHbBehandling(Behandling behandling) {
        boolean erRevurdering = BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType());
        boolean erRevurderingEtterKlage = behandling.getBehandlingÅrsaker().stream()
                .anyMatch(ba -> ba.getBehandlingÅrsakType() == BehandlingÅrsakType.RE_KLAGE_KA || ba.getBehandlingÅrsakType() == BehandlingÅrsakType.RE_KLAGE_NFP);
        LocalDate originalBehandlingVedtaksdato = erRevurdering ? finnOriginalBehandlingVedtaksdato(behandling) : null;
        return HbBehandling.builder()
                .medErRevurdering(erRevurdering)
                .medErRevurderingEtterKlage(erRevurderingEtterKlage)
                .medOriginalBehandlingDatoFagsakvedtak(originalBehandlingVedtaksdato)
                .build();
    }

    private HbSak lagHbSak(Behandling behandling,
                           SamletEksternBehandlingInfo fagsystemBehandling) {
        HbSak.Builder hbSakBuilder = HbSak.build()
                .medYtelsetype(behandling.getFagsak().getFagsakYtelseType())
                .medDatoFagsakvedtak(fagsystemBehandling.getGrunninformasjon().getVedtakDato())
                .medAntallBarn(fagsystemBehandling.getAntallBarnSøktFor());
        if (trengerSkilleFødselOgAdopsjon(behandling)) {
            hbSakBuilder
                    .medErFødsel(SøknadType.FØDSEL == fagsystemBehandling.getSøknadType())
                    .medErAdopsjon(SøknadType.ADOPSJON == fagsystemBehandling.getSøknadType());
        }
        return hbSakBuilder.build();
    }

    private boolean trengerSkilleFødselOgAdopsjon(Behandling b) {
        Set<FagsakYtelseType> fagsaktyper = Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER, FagsakYtelseType.ENGANGSTØNAD);
        return fagsaktyper.contains(b.getFagsak().getFagsakYtelseType());
    }

    private List<HbVedtaksbrevPeriode> lagHbVedtaksbrevPerioder(Long behandlingId, List<PeriodeMedTekstDto> perioderFritekst, List<BeregningResultatPeriode> resulatPerioder, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, VurdertForeldelse foreldelse, VedtaksbrevType vedtaksbrevType) {
        var fakta = faktaRepository.finnFaktaOmFeilutbetaling(behandlingId).orElseThrow();
        var perioder = utledPerioder(resulatPerioder, vilkårPerioder, foreldelse, fakta);
        return vedtaksbrevType.equals(VedtaksbrevType.FRITEKST_FEILUTBETALING_BORTFALT)
                ? Collections.emptyList()
                : perioder.stream()
                .map(periode -> lagBrevdataPeriode(periode, resulatPerioder, fakta, vilkårPerioder, foreldelse, perioderFritekst))
                .toList();
    }

    private List<Periode> utledPerioder(List<BeregningResultatPeriode> beregningResultatPerioder,
                                        List<VilkårVurderingPeriodeEntitet> vilkårPerioder,
                                        VurdertForeldelse foreldelse,
                                        FaktaFeilutbetaling fakta) {
        return new VedtaksbrevPeriodeSammenslåer(vilkårPerioder, foreldelse, fakta).utledPerioder(beregningResultatPerioder);
    }

    private VedtakHjemmel.EffektForBruker hentEffektForBruker(Behandling behandling, BigDecimal totaltTilbakekrevesMedRenter) {
        BehandlingÅrsak behandlingÅrsak = behandling.getBehandlingÅrsaker().get(0);
        Behandling originalBehandling = behandlingÅrsak.getOriginalBehandling().orElseThrow();

        BeregningResultat originaltBeregnetResultat = beregningsresultatTjeneste.finnEllerBeregn(originalBehandling.getId());
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

    private SamletEksternBehandlingInfo hentDataFraFagsystem(Behandling behandling) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandling.getId()).getEksternUuid();
        return eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid, ønsketTillegsinformasjon(behandling));
    }

    private Tillegsinformasjon[] ønsketTillegsinformasjon(Behandling behandling) {
        List<Tillegsinformasjon> tillegsinformasjons = new ArrayList<>();
        tillegsinformasjons.add(Tillegsinformasjon.PERSONOPPLYSNINGER);

        if (trengerSkilleFødselOgAdopsjon(behandling)) {
            //henter informasjon for å skille mellom fødsel og adopsjon
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

    BrevMetadata lagMetadataForVedtaksbrev(Behandling behandling,
                                           SamletEksternBehandlingInfo eksternBehandlingsinfo,
                                           Personinfo personinfo,
                                           VedtakResultatType vedtakResultatType,
                                           BrevMottaker brevMottaker) {
        FagsakYtelseType fagsakType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = eksternBehandlingsinfo.getGrunninformasjon().getSpråkkodeEllerDefault();

        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandling.getId());
        boolean finnesVerge = vergeEntitet.isPresent();

        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(behandling.getFagsak().getFagsakYtelseType(), personinfo, brevMottaker, vergeEntitet);
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakType, språkkode);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        boolean tilbakekreves = VedtakResultatType.FULL_TILBAKEBETALING.equals(vedtakResultatType) ||
                VedtakResultatType.DELVIS_TILBAKEBETALING.equals(vedtakResultatType);

        return new BrevMetadata.Builder()
                .medAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler() != null && !behandling.getAnsvarligSaksbehandler().isEmpty() ? behandling.getAnsvarligSaksbehandler() : "VL")
                .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
                .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
                .medMottakerAdresse(adresseinfo)
                .medFagsaktype(fagsakType)
                .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
                .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
                .medSakspartId(personinfo.getPersonIdent().getIdent())
                .medSakspartNavn(personinfo.getNavn())
                .medSprakkode(språkkode)
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

    private HbVedtaksbrevPeriode lagBrevdataPeriode(Periode periode, List<BeregningResultatPeriode> resultatPerioder, FaktaFeilutbetaling fakta, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, VurdertForeldelse foreldelse, List<PeriodeMedTekstDto> perioderFritekst) {
        PeriodeMedTekstDto fritekster = finnPeriodeFritekster(periode, perioderFritekst);

        List<Periode> delresultatPerioder = resultatPerioder.stream()
                .map(BeregningResultatPeriode::getPeriode)
                .filter(periode::omslutter)
                .filter(drp -> !drp.equals(periode))
                .toList();

        List<HbVedtaksbrevPeriode> delperioder = delresultatPerioder.stream()
                .map(drp -> lagBrevdataPeriode(drp, resultatPerioder, fakta, vilkårPerioder, foreldelse, perioderFritekst))
                .toList();

        return HbVedtaksbrevPeriode.builder()
                .medPeriode(periode)
                .medDelperioder(delperioder)
                .medKravgrunnlag(utledKravgrunnlag(periode, resultatPerioder))
                .medFakta(utledFakta(periode, fakta, fritekster))
                .medVurderinger(utledVurderinger(periode, vilkårPerioder, foreldelse, fritekster))
                .medResultat(utledResultat(periode, resultatPerioder, foreldelse)).build();
    }

    private HbKravgrunnlag utledKravgrunnlag(Periode periode, Collection<BeregningResultatPeriode> resultatPerioder) {
        return HbKravgrunnlag.builder()
                .medFeilutbetaltBeløp(summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getFeilutbetaltBeløp))
                .medRiktigBeløp(summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getRiktigYtelseBeløp))
                .medUtbetaltBeløp(summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getUtbetaltYtelseBeløp))
                .build();
    }

    private static BigDecimal summerForPeriode(Periode periode, Collection<BeregningResultatPeriode> resultatPerioder, Function<BeregningResultatPeriode, BigDecimal> hva) {
        return resultatPerioder.stream().filter(brp -> periode.omslutter(brp.getPeriode())).map(hva).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HbFakta utledFakta(Periode periode, FaktaFeilutbetaling fakta, PeriodeMedTekstDto fritekst) {
        for (FaktaFeilutbetalingPeriode faktaPeriode : fakta.getFeilutbetaltPerioder()) {
            if (faktaPeriode.getPeriode().overlapper(periode)) {
                return HbFakta.builder()
                        .medHendelsetype(faktaPeriode.getHendelseType())
                        .medHendelseUndertype(faktaPeriode.getHendelseUndertype())
                        .medFritekstFakta(fritekst != null ? fritekst.getFaktaAvsnitt() : null)
                        .build();
            }
        }
        throw new IllegalArgumentException("Fant ikke fakta-periode som overlapper periode " + periode);
    }

    private HbVurderinger utledVurderinger(Periode periode, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, VurdertForeldelse foreldelse, PeriodeMedTekstDto fritekst) {
        HbVurderinger.Builder builder = HbVurderinger.builder();
        leggTilVilkårvurdering(builder, periode, vilkårPerioder, fritekst);
        leggTilForeldelseVurdering(builder, periode, foreldelse, fritekst);
        return builder.build();
    }

    private void leggTilVilkårvurdering(HbVurderinger.Builder builder, Periode periode, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, PeriodeMedTekstDto fritekst) {
        builder.medFritekstVilkår(fritekst != null ? fritekst.getVilkårAvsnitt() : null);
        List<VilkårVurderingPeriodeEntitet> vilkårvurdering = finnVilkårvurdering(periode, vilkårPerioder);
        if (vilkårvurdering.isEmpty()) {
            return; //skjer hvis perioden er foreldet
        }
        builder.medVilkårResultat(hent(vilkårvurdering, VilkårVurderingPeriodeEntitet::getVilkårResultat, periode, "vilkårsresulat"));

        List<VilkårVurderingAktsomhetEntitet> aktsomhetVurderinger = vilkårvurdering.stream()
                .map(VilkårVurderingPeriodeEntitet::getAktsomhet)
                .filter(Objects::nonNull)
                .toList();

        if (!aktsomhetVurderinger.isEmpty()) {
            boolean unntasInnkrevingPgaLavtBeløp = Boolean.FALSE.equals(hent(aktsomhetVurderinger, VilkårVurderingAktsomhetEntitet::getTilbakekrevSmåBeløp, periode, "småbeløp"));
            builder.medUnntasInnkrevingPgaLavtBeløp(unntasInnkrevingPgaLavtBeløp);
            Aktsomhet aktsomhetresultat = hent(aktsomhetVurderinger, VilkårVurderingAktsomhetEntitet::getAktsomhet, periode, "aktsomhet");
            builder.medAktsomhetResultat(aktsomhetresultat);
            if (skalHaSærligeGrunner(aktsomhetresultat, unntasInnkrevingPgaLavtBeløp)) {
                Set<SærligGrunn> særligeGrunner = hent(aktsomhetVurderinger, a -> a.getSærligGrunner().stream().map(VilkårVurderingSærligGrunnEntitet::getGrunn).collect(Collectors.toSet()), periode, "særlige grunner");
                String fritekstSærligeGrunner = fritekst != null ? fritekst.getSærligeGrunnerAvsnitt() : null;
                String fritekstSærligGrunnAnnet = fritekst != null ? fritekst.getSærligeGrunnerAnnetAvsnitt() : null;
                builder.medSærligeGrunner(særligeGrunner, fritekstSærligeGrunner, fritekstSærligGrunnAnnet);
            }
        }

        List<VilkårVurderingGodTroEntitet> godTroVurderinger = vilkårvurdering.stream()
                .map(VilkårVurderingPeriodeEntitet::getGodTro)
                .filter(Objects::nonNull)
                .toList();

        if (!godTroVurderinger.isEmpty()) {
            builder.medAktsomhetResultat(AnnenVurdering.GOD_TRO);
            builder.medBeløpIBehold(godTroVurderinger.stream()
                    .map(godTro -> godTro.isBeløpErIBehold() ? godTro.getBeløpTilbakekreves() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
    }

    private static <T, U> U hent(List<T> vilkårsvurderinger, Function<T, U> funksjon, Periode p, String hva) {
        List<U> alternativer = vilkårsvurderinger.stream()
                .map(funksjon)
                .distinct()
                .toList();
        if (alternativer.isEmpty()) {
            return null;
        }
        if (alternativer.size() != 1) {
            LOG.warn("Forventet eksakt 1 unik, men fikk {} for {} for periode {}", Environment.current().isProd() ? alternativer.size() : alternativer, hva, p);
        }
        return alternativer.get(0);
    }

    private void leggTilForeldelseVurdering(HbVurderinger.Builder builder, Periode periode, VurdertForeldelse foreldelse, PeriodeMedTekstDto fritekst) {
        List<VurdertForeldelsePeriode> foreldelsePerioder = finnForeldelsePerioder(foreldelse, periode);
        if (foreldelsePerioder.isEmpty()) {
            builder.medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT);
        } else {
            if (hent(foreldelsePerioder, VurdertForeldelsePeriode::erForeldet, periode, "foreldet")) {
                builder.medAktsomhetResultat(AnnenVurdering.FORELDET);
            }
            builder.medForeldelsevurdering(hent(foreldelsePerioder, VurdertForeldelsePeriode::getForeldelseVurderingType, periode, "foreldelsevurderingtype"));
            builder.medForeldelsesfrist(hent(foreldelsePerioder, VurdertForeldelsePeriode::getForeldelsesfrist, periode, "foreldelsfrist"));
            builder.medOppdagelsesDato(hent(foreldelsePerioder, VurdertForeldelsePeriode::getOppdagelsesDato, periode, "oppdagelsesdato"));
            builder.medFritekstForeldelse(fritekst != null ? fritekst.getForeldelseAvsnitt() : null);
        }
    }

    private HbResultat utledResultat(Periode periode, Collection<BeregningResultatPeriode> resultatPerioder, VurdertForeldelse foreldelse) {
        HbResultat.Builder builder = HbResultat.builder()
                .medTilbakekrevesBeløp(summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløpUtenRenter))
                .medTilbakekrevesBeløpUtenSkatt(summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløpEtterSkatt))
                .medRenterBeløp(summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getRenteBeløp));

        List<VurdertForeldelsePeriode> foreldelsePerioder = finnForeldelsePerioder(foreldelse, periode);
        boolean foreldetPeriode = !foreldelsePerioder.isEmpty() && hent(foreldelsePerioder, VurdertForeldelsePeriode::erForeldet, periode, "foreldet") != null;
        if (foreldetPeriode) {
            BigDecimal feilutbetaltForPeriode = summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getFeilutbetaltBeløp);
            BigDecimal tilbakekreves = summerForPeriode(periode, resultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);
            builder.medForeldetBeløp(feilutbetaltForPeriode.subtract(tilbakekreves));
        }
        return builder.build();
    }

    private boolean skalHaSærligeGrunner(Aktsomhet aktsomhet, boolean unntattPgaLavgBeløp) {
        return Aktsomhet.GROVT_UAKTSOM.equals(aktsomhet) || Aktsomhet.SIMPEL_UAKTSOM.equals(aktsomhet) && !unntattPgaLavgBeløp;
    }

    private List<VurdertForeldelsePeriode> finnForeldelsePerioder(VurdertForeldelse foreldelse, Periode periode) {
        if (foreldelse == null) {
            return Collections.emptyList();
        }
        return foreldelse.getVurdertForeldelsePerioder()
                .stream()
                .filter(p -> p.getPeriode().overlapper(periode))
                .toList();
    }

    private PeriodeMedTekstDto finnPeriodeFritekster(Periode periode, List<PeriodeMedTekstDto> perioder) {
        for (PeriodeMedTekstDto fritekstPeriode : perioder) {
            if (fritekstPeriode.getPeriode().equals(periode)) {
                return fritekstPeriode;
            }
        }
        return null;
    }

    private List<VilkårVurderingPeriodeEntitet> finnVilkårvurdering(Periode periode, List<VilkårVurderingPeriodeEntitet> vilkårPerioder) {
        return vilkårPerioder.stream()
                .filter(vp -> vp.getPeriode().overlapper(periode))
                .toList();
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

    private boolean erIkkeFrisinn(Behandling behandling) {
        return !FagsakYtelseType.FRISINN.equals(behandling.getFagsak().getFagsakYtelseType());
    }

    private class HbVedtaksResultatBeløp {

        private final BigDecimal totaltTilbakekrevesUtenRenter;
        private final BigDecimal totaltTilbakekrevesMedRenter;
        private final BigDecimal totaltRentebeløp;
        private final BigDecimal totaltTilbakekrevesBeløpMedRenterUtenSkatt;
        private final BigDecimal totaltFeilutbetaltBeløp;

        private HbVedtaksResultatBeløp(List<BeregningResultatPeriode> resulatPerioder) {
            totaltTilbakekrevesUtenRenter = summer(resulatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløpUtenRenter);
            totaltTilbakekrevesMedRenter = summer(resulatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);
            totaltRentebeløp = summer(resulatPerioder, BeregningResultatPeriode::getRenteBeløp);

            BigDecimal totaltSkattetrekk = summer(resulatPerioder, BeregningResultatPeriode::getSkattBeløp);
            totaltTilbakekrevesBeløpMedRenterUtenSkatt = totaltTilbakekrevesMedRenter.subtract(totaltSkattetrekk);

            totaltFeilutbetaltBeløp = summer(resulatPerioder, BeregningResultatPeriode::getFeilutbetaltBeløp);
        }
    }
}
