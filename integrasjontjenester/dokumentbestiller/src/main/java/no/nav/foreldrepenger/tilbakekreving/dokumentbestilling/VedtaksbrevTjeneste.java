package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.math.BigDecimal;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.Feilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingPeriodeÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.VedtakHjemmel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.TittelOverskriftUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.VedtaksbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;


@ApplicationScoped
public class VedtaksbrevTjeneste {

    private static final String TITTEL_VEDTAKSBREV_HISTORIKKINNSLAG = "Vedtaksbrev Tilbakekreving";

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste;
    private BrevdataRepository brevdataRepository;
    private FeilutbetalingRepository faktaRepository;
    private KodeverkRepository kodeverkRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VurdertForeldelseRepository foreldelseRepository;
    private BestillDokumentTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    @Inject
    public VedtaksbrevTjeneste(BehandlingRepository behandlingRepository,
                               EksternBehandlingRepository eksternBehandlingRepository,
                               TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                               BehandlingTjeneste behandlingTjeneste,
                               FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste,
                               BrevdataRepository brevdataRepository,
                               FeilutbetalingRepository faktaRepository,
                               KodeverkRepository kodeverkRepository,
                               VilkårsvurderingRepository vilkårsvurderingRepository,
                               VurdertForeldelseRepository foreldelseRepository,
                               BestillDokumentTjeneste bestillDokumentTjeneste,
                               HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.fellesInfoTilBrevTjeneste = fellesInfoTilBrevTjeneste;
        this.brevdataRepository = brevdataRepository;
        this.faktaRepository = faktaRepository;
        this.kodeverkRepository = kodeverkRepository;
        this.vilkårsvurderingRepository = vilkårsvurderingRepository;
        this.foreldelseRepository = foreldelseRepository;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public VedtaksbrevTjeneste() {
    }

    public void sendVedtaksbrev(Long behandlingId) {

        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(TittelOverskriftUtil.finnOverskriftVedtaksbrev(vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk()))
            .medBrevtekst(TekstformattererVedtaksbrev.lagVedtaksbrevFritekst(vedtaksbrevData.getVedtaksbrevData()))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        opprettHistorikkinnslag(behandling, dokumentreferanse);
        lagreInfoOmVedtaksbrev(behandlingId, dokumentreferanse);
    }

    public byte[] hentForhåndsvisningVedtaksbrevSomPdf(HentForhåndvisningVedtaksbrevPdfDto dto) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(dto.getBehandlingId(), dto.getOppsummeringstekst(), dto.getPerioderMedTekst());
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(TittelOverskriftUtil.finnOverskriftVedtaksbrev(vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk()))
            .medBrevtekst(TekstformattererVedtaksbrev.lagVedtaksbrevFritekst(vedtaksbrevData.getVedtaksbrevData()))
            .medMetadata(vedtaksbrevData.getMetadata())
            .build();

        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
    }

    public List<Avsnitt> hentForhåndsvisningVedtaksbrevSomTekst(Long behandlingId) {
        VedtaksbrevData vedtaksbrevData = hentDataForVedtaksbrev(behandlingId);
        String hovedoverskrift = TittelOverskriftUtil.finnOverskriftVedtaksbrev(vedtaksbrevData.getMetadata().getFagsaktypenavnPåSpråk());
        return TekstformattererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(vedtaksbrevData.getVedtaksbrevData(), hovedoverskrift);
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
        brevdataRepository.lagreVedtaksbrevData(vedtaksbrevSporing);
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId) {
        String fritekstOppsummering = hentOppsummeringFritekst(behandlingId);
        List<PeriodeMedTekstDto> fritekstPerioder = hentFriteksterTilPerioder(behandlingId);
        return hentDataForVedtaksbrev(behandlingId, fritekstOppsummering, fritekstPerioder);
    }

    public VedtaksbrevData hentDataForVedtaksbrev(Long behandlingId, String oppsummeringFritekst, List<PeriodeMedTekstDto> perioderFritekst) {
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Long fpsakBehandlingId = eksternBehandling.getEksternId();
        UUID fpsakBehandlingUuid = eksternBehandling.getEksternUuid();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        Long varsletFeilutbetaling = fellesInfoTilBrevTjeneste.hentFeilutbetaltePerioder(fpsakBehandlingId).getSumFeilutbetaling(); //TODO gjelder bare orginalt varsel

        BeregningResultat beregnetResultat = tilbakekrevingBeregningTjeneste.beregn(behandlingId);
        List<BeregningResultatPeriode> resulatPerioder = beregnetResultat.getBeregningResultatPerioder();
        Long totalTilbakekrevingBeløp = VedtaksbrevUtil.finnTotaltTilbakekrevingsbeløp(resulatPerioder);

        List<VarselbrevSporing> varselbrevData = brevdataRepository.hentVarselbrevData(behandlingId);
        LocalDateTime nyesteVarselbrevTidspunkt = VedtaksbrevUtil.finnNyesteVarselbrevTidspunkt(varselbrevData);

        BrevMetadata brevMetadata = lagMetadataForVedtaksbrev(behandling, totalTilbakekrevingBeløp, fpsakBehandlingUuid);
        Feilutbetaling fakta = faktaRepository.finnFeilutbetaling(behandlingId)
            .orElseThrow()
            .getFeilutbetaling();
        List<VilkårVurderingPeriodeEntitet> vilkårPerioder = vilkårsvurderingRepository.finnVilkårsvurderingForBehandlingId(behandlingId)
            .map(VilkårVurderingAggregateEntitet::getManuellVilkår)
            .map(VilkårVurderingEntitet::getPerioder)
            .orElse(Collections.emptyList());
        VurdertForeldelse foreldelse = foreldelseRepository.finnVurdertForeldelseForBehandling(behandlingId)
            .map(VurdertForeldelseAggregate::getVurdertForeldelse)
            .orElse(null);

        HbVedtaksbrevFelles.Builder builder = HbVedtaksbrevFelles.builder()
            .medYtelsetype(behandling.getFagsak().getFagsakYtelseType())
            .medVarsletDato(nyesteVarselbrevTidspunkt.toLocalDate())
            .medVarsletBeløp(BigDecimal.valueOf(varsletFeilutbetaling))
            .medAntallBarn(1) //FIXME hent fra fpsak
            .medErFødsel(true) //FIXME hent fra fpsak
            .medFritekstOppsummering(oppsummeringFritekst)
            .medLovhjemmelVedtak(VedtakHjemmel.lagHjemmelstekst(beregnetResultat.getVedtakResultatType(), foreldelse, vilkårPerioder))
            .medTotaltTilbakekrevesBeløp(summer(resulatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløpUtenRenter))
            .medTotaltRentebeløp(summer(resulatPerioder, BeregningResultatPeriode::getRenteBeløp))
            .medTotaltTilbakekrevesBeløpMedRenter(summer(resulatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp))
            .medHovedresultat(beregnetResultat.getVedtakResultatType())
            .medKlagefristUker(fellesInfoTilBrevTjeneste.antallUkerKlagefrist())
            ;

        List<HbVedtaksbrevPeriode> perioder = resulatPerioder.stream()
            .map(brp -> lagBrevdataPeriode(brp, fakta, vilkårPerioder, perioderFritekst))
            .collect(Collectors.toList());

        HbVedtaksbrevData data = new HbVedtaksbrevData(builder.build(), perioder);

        return new VedtaksbrevData(data, brevMetadata);
    }

    BrevMetadata lagMetadataForVedtaksbrev(Behandling behandling, Long totalTilbakekrevingBeløp, UUID eksternUuid) {
        EksternBehandlingsinfoDto eksternBehandlingsinfo = fellesInfoTilBrevTjeneste.hentBehandlingFpsak(eksternUuid, behandling.getFagsak().getSaksnummer());
        String aktørId = eksternBehandlingsinfo.getPersonopplysningDto().getAktoerId();

        eksternBehandlingsinfo.setFagsaktype(fellesInfoTilBrevTjeneste.henteFagsakYtelseType(behandling)); // vi kan sette samme fagsakType fordi det ikke kan endret.
        Personinfo personinfo = fellesInfoTilBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = fellesInfoTilBrevTjeneste.hentAdresse(personinfo, aktørId);
        YtelseNavn ytelseNavn = fellesInfoTilBrevTjeneste.hentYtelsenavn(eksternBehandlingsinfo.getFagsaktype(), eksternBehandlingsinfo.getSprakkode());

        return new BrevMetadata.Builder()
            .medAnsvarligSaksbehandler(StringUtils.isNotEmpty(behandling.getAnsvarligSaksbehandler()) ? behandling.getAnsvarligSaksbehandler() : "VL")
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medMottakerAdresse(adresseinfo)
            .medFagsaktype(eksternBehandlingsinfo.getFagsaktype())
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medSakspartNavn(personinfo.getNavn())
            .medSprakkode(personinfo.getForetrukketSpråk())
            .medTittel(TittelOverskriftUtil.finnTittelVedtaksbrev(ytelseNavn.getNavnPåBokmål(), totalTilbakekrevingBeløp > 0))
            .build();
    }

    private BigDecimal summer(List<BeregningResultatPeriode> beregningResultatPerioder, Function<BeregningResultatPeriode, BigDecimal> hva) {
        return beregningResultatPerioder.stream()
            .map(hva)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HbVedtaksbrevPeriode lagBrevdataPeriode(BeregningResultatPeriode resultatPeriode, Feilutbetaling fakta, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, List<PeriodeMedTekstDto> perioderFritekst) {
        Periode periode = resultatPeriode.getPeriode();

        HbVedtaksbrevPeriode.Builder builder = HbVedtaksbrevPeriode.builder()
            .medPeriode(periode)
            .medHendelsetype(finnHendelseType(periode, fakta))
            .medHendelseUndertype(finnHendelseUnderType(periode, fakta))
            .medFeilutbetaltBeløp(resultatPeriode.getFeilutbetaltBeløp())
            .medTilbakekrevesBeløp(resultatPeriode.getTilbakekrevingBeløp())
            //.medRiktigBeløp(FIXME - legg til i BeregningResultatPeriode)
            //.medUtbetaltBeløp(FIXME- legg til i BeregningResultatPeriode)
            .medRenterBeløp(resultatPeriode.getRenteBeløp());

        PeriodeMedTekstDto fritekst = finnPeriodeFritekster(periode, perioderFritekst);
        if (fritekst != null) {
            builder
                .medFritekstFakta(fritekst.getFaktaAvsnitt())
                .medFritekstVilkår(fritekst.getVilkårAvsnitt())
                .medFritekstSærligeGrunner(fritekst.getSærligeGrunnerAvsnitt());
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
        } else {
            builder.medAktsomhetResultat(AnnenVurdering.FORELDET);
            builder.medForeldelseErVurdert(true);
            builder.medForeldetBeløp(resultatPeriode.getFeilutbetaltBeløp().subtract(resultatPeriode.getTilbakekrevingBeløp()));
            //FIXME fyll ut resterende felter for foreldelse
        }


        return builder.build();
    }

    private PeriodeMedTekstDto finnPeriodeFritekster(Periode periode, List<PeriodeMedTekstDto> perioder) {
        for (PeriodeMedTekstDto fritekstPeriode : perioder) {
            if (periode.overlapper(fritekstPeriode.getFom()) && periode.overlapper(fritekstPeriode.getTom())) {
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

    private HendelseType finnHendelseType(Periode periode, Feilutbetaling fakta) {
        for (FeilutbetalingPeriodeÅrsak faktaPeriode : fakta.getFeilutbetaltPerioder()) {
            if (faktaPeriode.getPeriode().omslutter(periode)) {
                //FIXME entitet skal tilby HendelseType direkte
                return kodeverkRepository.finn(HendelseType.class, faktaPeriode.getÅrsak());
            }
        }
        throw new IllegalArgumentException("Fant ikke fakta-periode som omslutter periode " + periode);
    }

    private HendelseUnderType finnHendelseUnderType(Periode periode, Feilutbetaling fakta) {
        for (FeilutbetalingPeriodeÅrsak faktaPeriode : fakta.getFeilutbetaltPerioder()) {
            if (faktaPeriode.getPeriode().omslutter(periode)) {
                //FIXME entitet skal tilby HendelseType direkte
                return kodeverkRepository.finn(HendelseUnderType.class, faktaPeriode.getUnderÅrsak());
            }
        }
        throw new IllegalArgumentException("Fant ikke fakta-periode som omslutter periode " + periode);
    }

    private List<PeriodeMedTekstDto> hentFriteksterTilPerioder(Long behandlingId) {
        List<VedtaksbrevPeriode> eksisterendePerioderForBrev = brevdataRepository.hentVedtaksbrevPerioderMedTekst(behandlingId);
        return VedtaksbrevUtil.mapFritekstFraDb(eksisterendePerioderForBrev);
    }

    private String hentOppsummeringFritekst(Long behandlingId) {
        Optional<VedtaksbrevOppsummering> vedtaksbrevOppsummeringOpt = brevdataRepository.hentVedtaksbrevOppsummering(behandlingId);
        return vedtaksbrevOppsummeringOpt.map(VedtaksbrevOppsummering::getOppsummeringFritekst).orElse(null);
    }

}
