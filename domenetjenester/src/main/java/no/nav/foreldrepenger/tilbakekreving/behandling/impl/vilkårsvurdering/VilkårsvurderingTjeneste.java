package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHjelperUtil.formAktsomhetEntitet;
import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHjelperUtil.formGodTroEntitet;
import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHjelperUtil.fylleUtVilkårResultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.cxf.common.util.CollectionUtils;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeMedBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.RedusertBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.YtelseDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.InntektskategoriKlassekodeMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

@ApplicationScoped
@Transactional
public class VilkårsvurderingTjeneste {

    private KravgrunnlagRepository grunnlagRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;

    private KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste;
    private VurdertForeldelseTjeneste foreldelseTjeneste;
    private VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste;

    VilkårsvurderingTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingTjeneste(VurdertForeldelseTjeneste foreldelseTjeneste, BehandlingRepositoryProvider behandlingRepositoryProvider,
                                    VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste, KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste) {
        this.grunnlagRepository = behandlingRepositoryProvider.getGrunnlagRepository();
        this.faktaFeilutbetalingRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.vilkårsvurderingRepository = behandlingRepositoryProvider.getVilkårsvurderingRepository();
        this.kravgrunnlagBeregningTjeneste = kravgrunnlagBeregningTjeneste;
        this.foreldelseTjeneste = foreldelseTjeneste;
        this.vilkårsvurderingHistorikkInnslagTjeneste = vilkårsvurderingHistorikkInnslagTjeneste;
    }

    public List<DetaljertFeilutbetalingPeriodeDto> hentDetaljertFeilutbetalingPerioder(Long behandlingId) {
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetalingPerioder = new ArrayList<>();
        Optional<FaktaFeilutbetaling> feilutbetalingAggregate = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);

        if (feilutbetalingAggregate.isPresent()) {
            List<FaktaFeilutbetalingPeriode> feilutbetaltPerioder = feilutbetalingAggregate.get().getFeilutbetaltPerioder();
            // hvis perioder er vurdert for foreldelse
            if (foreldelseTjeneste.harVurdertForeldelse(behandlingId)) {
                feilutbetalingPerioder.addAll(henteFeilutbetalingPerioderFraForeldelse(behandlingId, feilutbetaltPerioder));
            } else { // hvis perioder er ikke vurderes for foreldelse
                feilutbetalingPerioder.addAll(henteFeilutbetalingPerioderFraFaktaOmFeilutbetaling(behandlingId, feilutbetaltPerioder));
            }
        }

        return feilutbetalingPerioder;
    }

    public void lagreVilkårsvurdering(Long behandlingId, List<VilkårsvurderingPerioderDto> vilkarsVurdertPerioder) {
        VilkårVurderingEntitet vilkårVurderingEntitet = new VilkårVurderingEntitet();

        for (VilkårsvurderingPerioderDto periode : vilkarsVurdertPerioder) {
            if (erPeriodeForeldet(behandlingId, periode.getFom(), periode.getTom())) {
                //TODO kaste exception istedet, det skal ikke skje at saksbehandler vurderer en foreldet periode
                continue;
            }
            VilkårVurderingPeriodeEntitet periodeEntitet = VilkårVurderingPeriodeEntitet.builder()
                .medPeriode(periode.getFom(), periode.getTom())
                .medBegrunnelse(periode.getBegrunnelse())
                .medVilkårResultat(periode.getVilkårResultat())
                .medVurderinger(vilkårVurderingEntitet)
                .build();
            if (VilkårResultat.GOD_TRO.equals(periode.getVilkårResultat())) {
                formGodTroEntitet(periode, periodeEntitet);
            } else {
                formAktsomhetEntitet(periode, periodeEntitet);
            }
            vilkårVurderingEntitet.leggTilPeriode(periodeEntitet);
        }

        Optional<VilkårVurderingEntitet> forrigeEntitet = vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId);
        VilkårVurderingEntitet forrigeVurdering = forrigeEntitet.orElse(null);

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandlingId, forrigeVurdering, vilkårVurderingEntitet);

        vilkårsvurderingRepository.lagre(behandlingId, vilkårVurderingEntitet);
    }

    public List<VilkårsvurderingPerioderDto> hentVilkårsvurdering(Long behandlingId) {
        Optional<VilkårVurderingEntitet> vilkårsvurdering = vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId);
        List<VilkårsvurderingPerioderDto> perioder = new ArrayList<>();
        if (vilkårsvurdering.isPresent()) {
            VilkårVurderingEntitet vilkår = vilkårsvurdering.get();
            List<Periode> vvPerioder = vilkår.getPerioder().stream().map(VilkårVurderingPeriodeEntitet::getPeriode).collect(Collectors.toList());
            Map<Periode, BigDecimal> result = kravgrunnlagBeregningTjeneste.beregnFeilutbetaltBeløp(behandlingId, vvPerioder);
            for (VilkårVurderingPeriodeEntitet periodeEntitet : vilkår.getPerioder()) {
                VilkårsvurderingPerioderDto periode = new VilkårsvurderingPerioderDto();
                periode.setBegrunnelse(periodeEntitet.getBegrunnelse());
                periode.setPeriode(periodeEntitet.getPeriode());
                periode.setVilkårResultat(periodeEntitet.getVilkårResultat());
                periode.setVilkarResultatInfo(fylleUtVilkårResultat(periodeEntitet));
                periode.setFeilutbetalingBelop(result.get(periodeEntitet.getPeriode()));
                perioder.add(periode);
            }
        }
        return perioder;
    }

    public void slettGammelVilkårData(@Observes KravgrunnlagEndretEvent event) {
        vilkårsvurderingRepository.slettVilkårsvurdering(event.getBehandlingId());
    }

    private List<DetaljertFeilutbetalingPeriodeDto> henteFeilutbetalingPerioderFraForeldelse(Long behandlingId, List<FaktaFeilutbetalingPeriode> faktaFeilutbetalingPerioder) {
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetalingPerioder = new ArrayList<>();
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = foreldelseTjeneste.henteVurdertForeldelse(behandlingId);

        for (ForeldelsePeriodeMedBeløpDto periode : feilutbetalingPerioderDto.getPerioder()) {
            HendelseTypeMedUndertypeDto årsak = hentHendelseType(faktaFeilutbetalingPerioder, new Periode(periode.getFom(), periode.getTom()));

            DetaljertFeilutbetalingPeriodeDto periodeDto = new DetaljertFeilutbetalingPeriodeDto(periode.getFom(), periode.getTom(),
                årsak, periode.getBelop());
            List<YtelseDto> ytelser = henteYtelse(behandlingId, new Periode(periode.getFom(), periode.getTom()));
            periodeDto.setYtelser(oppsummereYtelser(ytelser));
            periodeDto.setRedusertBeloper(henteRedusertBeløper(behandlingId, new Periode(periode.getFom(), periode.getTom())));
            periodeDto.setForeldet(ForeldelseVurderingType.FORELDET.equals(periode.getForeldelseVurderingType()));

            feilutbetalingPerioder.add(periodeDto);
        }
        return feilutbetalingPerioder;
    }

    private List<DetaljertFeilutbetalingPeriodeDto> henteFeilutbetalingPerioderFraFaktaOmFeilutbetaling(Long behandlingId, List<FaktaFeilutbetalingPeriode> faktaFeilutbetalingPerioder) {
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetalingPerioder = new ArrayList<>();

        List<Periode> råPerioder = faktaFeilutbetalingPerioder.stream().map(FaktaFeilutbetalingPeriode::getPeriode).collect(Collectors.toList());
        Map<Periode, BigDecimal> beløpForPerioder = kravgrunnlagBeregningTjeneste.beregnFeilutbetaltBeløp(behandlingId, råPerioder);

        for (FaktaFeilutbetalingPeriode periodeÅrsak : faktaFeilutbetalingPerioder) {
            Periode periode = periodeÅrsak.getPeriode();
            BigDecimal beløp = beløpForPerioder.get(periode);
            DetaljertFeilutbetalingPeriodeDto periodeDto = new DetaljertFeilutbetalingPeriodeDto(periode, mapTil(periodeÅrsak), beløp);
            List<YtelseDto> ytelser = henteYtelse(behandlingId, periode);
            periodeDto.setYtelser(oppsummereYtelser(ytelser));
            periodeDto.setRedusertBeloper(henteRedusertBeløper(behandlingId, periode));
            feilutbetalingPerioder.add(periodeDto);
        }
        return feilutbetalingPerioder;
    }

    private boolean erPeriodeForeldet(Long behandlingId, LocalDate fom, LocalDate tom) {
        //TODO skal ikke gå via foreldelseTjeneste, den drar med seg kravgrunnlag, som ikke er relevant å bruke her
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = foreldelseTjeneste.henteVurdertForeldelse(behandlingId);

        //TODO unødvendig if
        if (!CollectionUtils.isEmpty(feilutbetalingPerioderDto.getPerioder())) {
            Optional<ForeldelsePeriodeMedBeløpDto> periode = feilutbetalingPerioderDto.getPerioder().stream()
                //TODO bruk en Periode-implementasjon og overlap-funksjon
                .filter(periodeDto -> fom.equals(periodeDto.getFom()) && tom.equals(periodeDto.getTom()))
                .filter(periodeDto -> ForeldelseVurderingType.FORELDET.equals(periodeDto.getForeldelseVurderingType()))
                .findFirst();
            if (periode.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private HendelseTypeMedUndertypeDto hentHendelseType(List<FaktaFeilutbetalingPeriode> faktaFeilutbetalingPerioder, Periode foreldetPeriode) {
        for (FaktaFeilutbetalingPeriode feilutbetalingPeriodeÅrsak : faktaFeilutbetalingPerioder) {
            Periode faktaFeilutbetalingPeriode = feilutbetalingPeriodeÅrsak.getPeriode();
            if (faktaFeilutbetalingPeriode.overlapper(foreldetPeriode)) {
                return mapTil(feilutbetalingPeriodeÅrsak);
            }
        }
        return null;
    }

    private HendelseTypeMedUndertypeDto mapTil(FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode) {
        return new HendelseTypeMedUndertypeDto(faktaFeilutbetalingPeriode.getHendelseType(), faktaFeilutbetalingPeriode.getHendelseUndertype());
    }

    private List<YtelseDto> henteYtelse(Long behandlingId, Periode feilutbetalingPeriode) {
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        BeregnBeløpUtil beregnBeløpUtil = BeregnBeløpUtil.forFagområde(kravgrunnlag.getFagOmrådeKode());
        List<YtelseDto> ytelser = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : kravgrunnlag.getPerioder()) {
            Periode grunnlagPeriode = kgPeriode.getPeriode();
            if (feilutbetalingPeriode.overlapper(grunnlagPeriode)) {
                List<KravgrunnlagBelop433> beloper = kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.YTEL.equals(belop433.getKlasseType()))
                    .filter(belop433 -> BigDecimal.ZERO.compareTo(belop433.getTilbakekrevesBelop()) != 0)
                    .collect(Collectors.toList());
                ytelser.addAll(opprettYtelser(feilutbetalingPeriode, grunnlagPeriode, beloper, beregnBeløpUtil));
            }
        }
        return ytelser;
    }

    private List<RedusertBeløpDto> henteRedusertBeløper(Long behandlingId, Periode feilutbetalingPeriode) {
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        List<RedusertBeløpDto> redusertBeløper = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : kravgrunnlag.getPerioder()) {
            Periode grunnlagPeriode = kgPeriode.getPeriode();
            if (feilutbetalingPeriode.overlapper(grunnlagPeriode)) {
                List<KravgrunnlagBelop433> skattogTrekkBeløoper = new ArrayList<>();
                skattogTrekkBeløoper.addAll(kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.TREK.equals(belop433.getKlasseType()))
                    .filter(belop433 -> belop433.getOpprUtbetBelop().signum() == -1)
                    .collect(Collectors.toList())); //redusertBeløp
                skattogTrekkBeløoper.addAll(kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.SKAT.equals(belop433.getKlasseType()))
                    .filter(belop433 -> belop433.getOpprUtbetBelop().signum() == -1)
                    .collect(Collectors.toList())); //redusertBeløp
                List<KravgrunnlagBelop433> redusertYtelseBeløper = kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.JUST.equals(belop433.getKlasseType()))
                    .filter(belop433 -> belop433.getOpprUtbetBelop().signum() == 0)
                    .filter(belop433 -> belop433.getNyBelop().signum() == 1)
                    .collect(Collectors.toList()); // etterbetaling

                redusertBeløper.addAll(opprettRedusertBeløper(skattogTrekkBeløoper, redusertYtelseBeløper));
            }
        }
        return redusertBeløper;
    }

    private List<YtelseDto> opprettYtelser(Periode feilutbetalingPeriode, Periode grunnlagPeriode, List<KravgrunnlagBelop433> beloper, BeregnBeløpUtil beregnBeløpUtil) {
        List<YtelseDto> ytelser = new ArrayList<>();
        for (KravgrunnlagBelop433 belop : beloper) {
            Inntektskategori inntektskategori = finnesInntektsKategori(belop);
            YtelseDto ytelse = new YtelseDto();
            ytelse.setAktivitet(inntektskategori.getNavn());
            ytelse.setBelop(beregnBeløpUtil.beregnBeløpForPeriode(belop.getTilbakekrevesBelop(), feilutbetalingPeriode, grunnlagPeriode));
            ytelser.add(ytelse);
        }
        return ytelser;
    }


    private List<RedusertBeløpDto> opprettRedusertBeløper(List<KravgrunnlagBelop433> skattOgTrekkBeløper, List<KravgrunnlagBelop433> etterbetalingBeløper) {
        List<RedusertBeløpDto> redusertBeløpListe = new ArrayList<>();
        if (!skattOgTrekkBeløper.isEmpty()) {
            for (KravgrunnlagBelop433 belop433 : skattOgTrekkBeløper) {
                RedusertBeløpDto redusertBeløp = new RedusertBeløpDto();
                redusertBeløp.setBelop(belop433.getOpprUtbetBelop().abs());
                redusertBeløp.setErTrekk(true);
                redusertBeløpListe.add(redusertBeløp);
            }
        }
        if (!etterbetalingBeløper.isEmpty()) {
            for (KravgrunnlagBelop433 belop433 : etterbetalingBeløper) {
                RedusertBeløpDto redusertBeløp = new RedusertBeløpDto();
                redusertBeløp.setBelop(belop433.getNyBelop());
                redusertBeløp.setErTrekk(false);
                redusertBeløpListe.add(redusertBeløp);
            }
        }
        return redusertBeløpListe;
    }

    private Inntektskategori finnesInntektsKategori(KravgrunnlagBelop433 belop) {
        return InntektskategoriKlassekodeMapper.finnInntekstkategoriMedKlasseKode(belop.getKlasseKodeKodeverk());
    }

    private List<YtelseDto> oppsummereYtelser(List<YtelseDto> ytelser) {
        Map<String, BigDecimal> oppsummertYtelseMap = new HashMap<>();
        for (YtelseDto ytelseDto : ytelser) {
            if (oppsummertYtelseMap.containsKey(ytelseDto.getAktivitet())) {
                BigDecimal beløp = ytelseDto.getBelop().add(oppsummertYtelseMap.get(ytelseDto.getAktivitet()));
                ytelseDto.setBelop(beløp);
            }
            oppsummertYtelseMap.put(ytelseDto.getAktivitet(), ytelseDto.getBelop());
        }
        return oppsummertYtelseMap.entrySet().stream()
            .map(ytelseMap -> new YtelseDto(ytelseMap.getKey(), ytelseMap.getValue()))
            .collect(Collectors.toList());

    }

}
