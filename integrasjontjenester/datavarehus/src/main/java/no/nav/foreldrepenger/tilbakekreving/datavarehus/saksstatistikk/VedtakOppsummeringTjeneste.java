package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.YtelseTypeMapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.SærligeGrunner;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.UtvidetVilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakPeriode;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class VedtakOppsummeringTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VurdertForeldelseRepository foreldelseRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;

    VedtakOppsummeringTjeneste() {
        // for CDI
    }

    @Inject
    public VedtakOppsummeringTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                      BeregningsresultatTjeneste beregningsresultatTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.foreldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
    }

    public VedtakOppsummering hentVedtakOppsummering(long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Fagsak fagsak = behandling.getFagsak();
        Saksnummer saksnummer = fagsak.getSaksnummer();
        Optional<BehandlingVedtak> behandlingVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Optional<BehandlingÅrsak> behandlingsårsak = behandling.getBehandlingÅrsaker().stream().findFirst();
        Optional<Behandling> forrigeBehandling = behandlingsårsak
                .map(BehandlingÅrsak::getOriginalBehandling)
                .filter(Optional::isPresent)
                .map(Optional::get);

        if (behandlingVedtak.isEmpty()) {
            throw new TekniskException("FPT-131275", String.format("Fant ikke vedtak for behandling med behandlingId=%s.Kan ikke sende data til DVH", behandlingId));
        }
        VedtakOppsummering vedtakOppsummering = new VedtakOppsummering();
        vedtakOppsummering.setBehandlingUuid(behandling.getUuid());
        vedtakOppsummering.setSaksnummer(saksnummer.getVerdi());
        vedtakOppsummering.setYtelseType(YtelseTypeMapper.getYtelseType(fagsak.getFagsakYtelseType()));
        vedtakOppsummering.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        vedtakOppsummering.setAnsvarligBeslutter(behandling.getAnsvarligBeslutter());
        vedtakOppsummering.setBehandlingType(BehandlingTypeMapper.getBehandlingType(behandling.getType()));
        vedtakOppsummering.setBehandlingOpprettetTid(tilOffsetDateTime(behandling.getOpprettetTidspunkt()));
        vedtakOppsummering.setVedtakFattetTid(tilOffsetDateTime(behandlingVedtak.get().getOpprettetTidspunkt()));
        vedtakOppsummering.setReferertFagsakBehandlingUuid(eksternBehandling.getEksternUuid());
        vedtakOppsummering.setBehandlendeEnhetKode(behandling.getBehandlendeEnhetId());
        vedtakOppsummering.setErBehandlingManueltOpprettet(erSaksbehandler(behandling.getOpprettetAv()));
        forrigeBehandling.ifPresent(forrige -> vedtakOppsummering.setForrigeBehandling(forrige.getUuid()));
        vedtakOppsummering.setPerioder(hentVedtakPerioder(behandlingId));
        return vedtakOppsummering;
    }

    private List<VedtakPeriode> hentVedtakPerioder(long behandlingId) {
        List<VedtakPeriode> vedtakPerioder = new ArrayList<>();
        Optional<VilkårVurderingEntitet> vilkårVurderingEntitet = vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId);
        Optional<VurdertForeldelse> vurdertForeldelseEntitet = foreldelseRepository.finnVurdertForeldelse(behandlingId);
        BeregningResultat beregningResultat = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);
        vilkårVurderingEntitet.ifPresent(vilkårVurdering -> vedtakPerioder.addAll(hentVilkårPerioder(behandlingId, beregningResultat, vilkårVurdering)));
        vurdertForeldelseEntitet.ifPresent(vurdertForeldelse -> vedtakPerioder.addAll(hentForeldelsePerioder(behandlingId, beregningResultat, vurdertForeldelse)));
        return vedtakPerioder;
    }

    private List<VedtakPeriode> hentVilkårPerioder(long behandlingId, BeregningResultat beregningResultat, VilkårVurderingEntitet vilkårVurderingEntitet) {
        List<VedtakPeriode> vilkårPerioder = new ArrayList<>();
        List<VilkårVurderingPeriodeEntitet> vilkårVurderingPerioder = vilkårVurderingEntitet.getPerioder();
        for (VilkårVurderingPeriodeEntitet periodeEntitet : vilkårVurderingPerioder) {
            VedtakPeriode vedtakPeriode = new VedtakPeriode();
            vedtakPeriode.setFom(periodeEntitet.getFom());
            vedtakPeriode.setTom(periodeEntitet.getPeriode().getTom());
            if (periodeEntitet.getAktsomhet() != null) {
                vedtakPeriode.setAktsomhet(Aktsomhet.valueOf(periodeEntitet.getAktsomhetResultat().getKode()));
                if (periodeEntitet.tilbakekrevesSmåbeløp() != null) {
                    vedtakPeriode.setHarBruktSjetteLedd(periodeEntitet.tilbakekrevesSmåbeløp());
                }
                vedtakPeriode.setSærligeGrunner(hentSærligGrunner(periodeEntitet));
            }
            vedtakPeriode.setVilkårResultat(UtvidetVilkårResultat.valueOf(periodeEntitet.getVilkårResultat().getKode()));
            settBeløp(beregningResultat, vedtakPeriode);
            settHendelser(behandlingId, vedtakPeriode);
            vilkårPerioder.add(vedtakPeriode);
        }
        return vilkårPerioder;
    }

    private List<VedtakPeriode> hentForeldelsePerioder(long behandlingId, BeregningResultat beregningResultat, VurdertForeldelse vurdertForeldelse) {
        List<VedtakPeriode> foreldelsePerioder = new ArrayList<>();
        for (VurdertForeldelsePeriode foreldelsePeriode : vurdertForeldelse.getVurdertForeldelsePerioder()) {
            if (foreldelsePeriode.erForeldet()) {
                VedtakPeriode vedtakPeriode = new VedtakPeriode();
                vedtakPeriode.setFom(foreldelsePeriode.getFom());
                vedtakPeriode.setTom(foreldelsePeriode.getPeriode().getTom());
                vedtakPeriode.setVilkårResultat(UtvidetVilkårResultat.FORELDET);
                settBeløp(beregningResultat, vedtakPeriode);
                settHendelser(behandlingId, vedtakPeriode);
                foreldelsePerioder.add(vedtakPeriode);
            }
        }
        return foreldelsePerioder;
    }

    private SærligeGrunner hentSærligGrunner(VilkårVurderingPeriodeEntitet periodeEntitet) {
        if (!periodeEntitet.getAktsomhet().getSærligGrunner().isEmpty()) {
            SærligeGrunner særligeGrunner = new SærligeGrunner();
            List<SærligGrunn> særligGrunnListe = new ArrayList<>();
            særligeGrunner.setErSærligeGrunnerTilReduksjon(periodeEntitet.getAktsomhet().getSærligGrunnerTilReduksjon());
            periodeEntitet.getAktsomhet().getSærligGrunner().forEach(særligeGrunnEntitet ->
                    særligGrunnListe.add(SærligGrunn.valueOf(særligeGrunnEntitet.getGrunn().getKode())));
            særligeGrunner.setSærligeGrunner(særligGrunnListe);
            return særligeGrunner;
        }
        return null;
    }

    private void settBeløp(BeregningResultat beregningResultat, VedtakPeriode vedtakPeriode) {
        Periode periode = new Periode(vedtakPeriode.getFom(), vedtakPeriode.getTom());
        Optional<BeregningResultatPeriode> resultatPeriode = beregningResultat.getBeregningResultatPerioder().stream()
                .filter(perioder -> perioder.getPeriode().equals(periode)).findAny();
        if (resultatPeriode.isPresent()) {
            BeregningResultatPeriode beregningResultatPeriode = resultatPeriode.get();
            vedtakPeriode.setFeilutbetaltBeløp(beregningResultatPeriode.getFeilutbetaltBeløp());
            vedtakPeriode.setTilbakekrevesBruttoBeløp(beregningResultatPeriode.getTilbakekrevingBeløp());
            vedtakPeriode.setRenterBeløp(beregningResultatPeriode.getRenteBeløp());
        }
    }

    private void settHendelser(long behandlingId, VedtakPeriode vedtakPeriode) {
        Optional<FaktaFeilutbetaling> faktaFeilutbetalingEntitet = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        Periode periode = new Periode(vedtakPeriode.getFom(), vedtakPeriode.getTom());
        if (faktaFeilutbetalingEntitet.isPresent()) {
            Optional<FaktaFeilutbetalingPeriode> faktaFeilutbetalingPeriode = faktaFeilutbetalingEntitet.get().getFeilutbetaltPerioder().stream()
                    .filter(faktaPeriode -> faktaPeriode.getPeriode().overlapper(periode)).findFirst();
            if (faktaFeilutbetalingPeriode.isPresent()) {
                FaktaFeilutbetalingPeriode faktaPeriode = faktaFeilutbetalingPeriode.get();
                vedtakPeriode.setHendelseTypeTekst(faktaPeriode.getHendelseType().getNavn());
                vedtakPeriode.setHendelseUndertypeTekst(faktaPeriode.getHendelseUndertype() != null ? faktaPeriode.getHendelseUndertype().getNavn() : "");
            }
        }
    }

    private OffsetDateTime tilOffsetDateTime(LocalDateTime tidspunkt) {
        return OffsetDateTime.ofInstant(tidspunkt.atZone(ZoneId.systemDefault()).toInstant(), ZoneOffset.UTC);
    }

    private static boolean erSaksbehandler(String s) {
        return s != null && !s.startsWith("srv") && !s.startsWith("SRV") && !"VL".equals(s);
    }

}
