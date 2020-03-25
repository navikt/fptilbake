package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
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
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.SærligeGrunner;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.UtvidetVilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakPeriode;

@ApplicationScoped
public class VedtakOppsummeringTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VurdertForeldelseRepository foreldelseRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private KodeverkRepository kodeverkRepository;
    private TilbakekrevingBeregningTjeneste beregningTjeneste;

    VedtakOppsummeringTjeneste() {
        // for CDI
    }

    @Inject
    public VedtakOppsummeringTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                      TilbakekrevingBeregningTjeneste beregningTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.foreldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.beregningTjeneste = beregningTjeneste;
    }

    public VedtakOppsummering hentVedtakOppsummering(long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<BehandlingVedtak> behandlingVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Optional<BehandlingÅrsak> behandlingsårsak = behandling.getBehandlingÅrsaker().stream().findFirst();
        Optional<Behandling> forrigeBehandling = behandlingsårsak
            .map(BehandlingÅrsak::getOriginalBehandling)
            .filter(Optional::isPresent)
            .map(Optional::get);

        if (behandlingVedtak.isEmpty()) {
            throw VedtakOppsummeringTjenesteFeil.FACTORY.fantIkkeBehandlingVedtak(behandlingId).toException();
        }
        VedtakOppsummering vedtakOppsummering = new VedtakOppsummering();
        vedtakOppsummering.setBehandlingUuid(behandling.getUuid());
        vedtakOppsummering.setSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi());
        vedtakOppsummering.setYtelseType(YtelseTypeMapper.getYtelseType(behandling.getFagsak().getFagsakYtelseType()));
        vedtakOppsummering.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        vedtakOppsummering.setAnsvarligBeslutter(behandling.getAnsvarligBeslutter());
        vedtakOppsummering.setBehandlingType(BehandlingTypeMapper.getBehandlingType(behandling.getType()));
        vedtakOppsummering.setBehandlingOpprettetTid(konvertTidspunkt(behandling.getOpprettetTidspunkt()));
        vedtakOppsummering.setVedtakFattetTid(konvertTidspunkt(behandlingVedtak.get().getOpprettetTidspunkt()));
        vedtakOppsummering.setReferteFagsakBehandlinger(Lists.newArrayList(eksternBehandling.getEksternUuid()));
        vedtakOppsummering.setBehandlendeEnhetKode(behandling.getBehandlendeEnhetId());
        vedtakOppsummering.setErBehandlingManueltOpprettet(behandling.isManueltOpprettet());
        forrigeBehandling.ifPresent(forrige -> vedtakOppsummering.setForrigeBehandling(forrige.getUuid()));
        vedtakOppsummering.setPerioder(hentVedtakPerioder(behandlingId));
        return vedtakOppsummering;
    }

    private List<VedtakPeriode> hentVedtakPerioder(long behandlingId) {
        List<VedtakPeriode> vedtakPerioder = new ArrayList<>();
        Optional<VilkårVurderingEntitet> vilkårVurderingEntitet = vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId);
        Optional<VurdertForeldelse> vurdertForeldelseEntitet = foreldelseRepository.finnVurdertForeldelse(behandlingId);
        vilkårVurderingEntitet.ifPresent(vilkårVurdering -> vedtakPerioder.addAll(hentVilkårPerioder(behandlingId, vilkårVurdering)));
        vurdertForeldelseEntitet.ifPresent(vurdertForeldelse -> vedtakPerioder.addAll(hentForeldelsePerioder(behandlingId, vurdertForeldelse)));
        return vedtakPerioder;
    }

    private List<VedtakPeriode> hentVilkårPerioder(long behandlingId, VilkårVurderingEntitet vilkårVurderingEntitet) {
        List<VedtakPeriode> vilkårPerioder = new ArrayList<>();
        List<VilkårVurderingPeriodeEntitet> vilkårVurderingPerioder = vilkårVurderingEntitet.getPerioder();
        for (VilkårVurderingPeriodeEntitet periodeEntitet : vilkårVurderingPerioder) {
            VedtakPeriode vedtakPeriode = new VedtakPeriode();
            vedtakPeriode.setFom(periodeEntitet.getFom());
            vedtakPeriode.setTom(periodeEntitet.getPeriode().getTom());
            if (periodeEntitet.getAktsomhet() != null) {
                vedtakPeriode.setAktsomhet(Aktsomhet.valueOf(periodeEntitet.getAktsomhetResultat().getKode()));
                vedtakPeriode.setHarBruktSjetteLedd(periodeEntitet.tilbakekrevesSmåbeløp() == null ? false : periodeEntitet.tilbakekrevesSmåbeløp());
                vedtakPeriode.setSærligeGrunner(hentSærligGrunner(periodeEntitet));
            }
            vedtakPeriode.setVilkårResultat(UtvidetVilkårResultat.valueOf(periodeEntitet.getVilkårResultat().getKode()));
            settBeløp(behandlingId, vedtakPeriode);
            settHendelser(behandlingId, vedtakPeriode);
            vilkårPerioder.add(vedtakPeriode);
        }
        return vilkårPerioder;
    }

    private List<VedtakPeriode> hentForeldelsePerioder(long behandlingId, VurdertForeldelse vurdertForeldelse) {
        List<VedtakPeriode> foreldelsePerioder = new ArrayList<>();
        for (VurdertForeldelsePeriode foreldelsePeriode : vurdertForeldelse.getVurdertForeldelsePerioder()) {
            VedtakPeriode vedtakPeriode = new VedtakPeriode();
            vedtakPeriode.setFom(foreldelsePeriode.getFom());
            vedtakPeriode.setTom(foreldelsePeriode.getPeriode().getTom());
            vedtakPeriode.setVilkårResultat(UtvidetVilkårResultat.FORELDET);
            settBeløp(behandlingId, vedtakPeriode);
            settHendelser(behandlingId, vedtakPeriode);
            foreldelsePerioder.add(vedtakPeriode);
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

    private void settBeløp(long behandlingId, VedtakPeriode vedtakPeriode) {
        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandlingId);
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
                vedtakPeriode.setHendelseTypeTekst(hentNavn(HendelseType.DISCRIMINATOR, faktaPeriode.getHendelseType().getKode()));
                vedtakPeriode.setHendelseUndertypeTekst(faktaPeriode.getHendelseUndertype() != null ?
                    hentNavn(HendelseUnderType.DISCRIMINATOR, faktaPeriode.getHendelseUndertype().getKode()) : "");
            }
        }
    }

    private OffsetDateTime konvertTidspunkt(LocalDateTime tidspunkt) {
        return tidspunkt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private String hentNavn(String kodeverk, String kode) {
        return kodeverkRepository.hentKodeliste(kodeverk, kode).getNavn();
    }
}
