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
import javax.inject.Inject;

import org.apache.cxf.common.util.CollectionUtils;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.PeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.RedusertBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.YtelseDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.InntektskategoriKlassekodeMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.felles.jpa.Transaction;

@ApplicationScoped
@Transaction
public class VilkårsvurderingTjeneste {

    private VurdertForeldelseTjeneste foreldelseTjeneste;
    private KravgrunnlagRepository grunnlagRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    private VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste;

    VilkårsvurderingTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingTjeneste(VurdertForeldelseTjeneste foreldelseTjeneste, BehandlingRepositoryProvider behandlingRepositoryProvider,
                                    VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste) {
        this.foreldelseTjeneste = foreldelseTjeneste;
        this.behandlingRepositoryProvider = behandlingRepositoryProvider;
        this.grunnlagRepository = behandlingRepositoryProvider.getGrunnlagRepository();
        this.faktaFeilutbetalingRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.vilkårsvurderingRepository = behandlingRepositoryProvider.getVilkårsvurderingRepository();
        this.vilkårsvurderingHistorikkInnslagTjeneste = vilkårsvurderingHistorikkInnslagTjeneste;
    }

    public List<DetaljertFeilutbetalingPeriodeDto> hentDetaljertFeilutbetalingPerioder(Long behandlingId) {
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetalingPerioder = new ArrayList<>();
        Optional<FaktaFeilutbetaling> feilutbetalingAggregate = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);

        if (feilutbetalingAggregate.isPresent()) {
            List<FaktaFeilutbetalingPeriode> feilutbetaltPerioder = feilutbetalingAggregate.get().getFeilutbetaltPerioder();
            // hvis perioder er vurdert for foreldelse
            if (foreldelseTjeneste.harForeldetPeriodeForBehandlingId(behandlingId)) {
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

        Optional<VilkårVurderingAggregateEntitet> forrigeEntitet = vilkårsvurderingRepository.finnVilkårsvurderingForBehandlingId(behandlingId);
        VilkårVurderingEntitet forrigeVurdering = forrigeEntitet.isPresent() ? forrigeEntitet.get().getManuellVilkår() : null;

        vilkårsvurderingHistorikkInnslagTjeneste.lagHistorikkInnslag(behandlingId, forrigeVurdering, vilkårVurderingEntitet);

        vilkårsvurderingRepository.lagre(behandlingId, vilkårVurderingEntitet);
    }

    public List<VilkårsvurderingPerioderDto> hentVilkårsvurdering(Long behandlingId) {
        Optional<VilkårVurderingAggregateEntitet> aggregateEntitet = vilkårsvurderingRepository.finnVilkårsvurderingForBehandlingId(behandlingId);
        List<VilkårsvurderingPerioderDto> perioder = new ArrayList<>();
        if (aggregateEntitet.isPresent()) {
            VilkårVurderingEntitet vilkår = aggregateEntitet.get().getManuellVilkår();
            for (VilkårVurderingPeriodeEntitet periodeEntitet : vilkår.getPerioder()) {
                VilkårsvurderingPerioderDto periode = new VilkårsvurderingPerioderDto();
                periode.setBegrunnelse(periodeEntitet.getBegrunnelse());
                periode.setPeriode(periodeEntitet.getPeriode());
                periode.setVilkårResultat(periodeEntitet.getVilkårResultat());
                periode.setVilkarResultatInfo(fylleUtVilkårResultat(periodeEntitet));
                periode.setFeilutbetalingBelop(hentNyFeilutbetaltBelop(behandlingId, new Periode(periode.getFom(), periode.getTom())));
                perioder.add(periode);
            }
        }
        return perioder;
    }

    private List<DetaljertFeilutbetalingPeriodeDto> henteFeilutbetalingPerioderFraForeldelse(Long behandlingId, List<FaktaFeilutbetalingPeriode> faktaFeilutbetalingPerioder) {
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetalingPerioder = new ArrayList<>();
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = foreldelseTjeneste.henteVurdertForeldelse(behandlingId);

        for (PeriodeDto periode : feilutbetalingPerioderDto.getPerioder()) {
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
        Map<Periode, BigDecimal> beløpForPerioder = foreldelseTjeneste.beregnFeilutbetaltBeløpForPerioder(behandlingId, råPerioder);

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
            Optional<PeriodeDto> periode = feilutbetalingPerioderDto.getPerioder().stream()
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
        KravgrunnlagAggregate aggregate = grunnlagRepository.finnEksaktGrunnlagForBehandlingId(behandlingId);
        List<YtelseDto> ytelser = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : aggregate.getGrunnlagØkonomi().getPerioder()) {
            Periode grunnlagPeriode = kgPeriode.getPeriode();
            if (feilutbetalingPeriode.overlapper(grunnlagPeriode)) {
                List<KravgrunnlagBelop433> beloper = kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.YTEL.equals(belop433.getKlasseType()))
                    .filter(belop433 -> BigDecimal.ZERO.compareTo(belop433.getTilbakekrevesBelop()) != 0)
                    .collect(Collectors.toList());
                ytelser.addAll(opprettYtelser(feilutbetalingPeriode, grunnlagPeriode, beloper));
            }
        }
        return ytelser;
    }

    private List<RedusertBeløpDto> henteRedusertBeløper(Long behandlingId, Periode feilutbetalingPeriode) {
        KravgrunnlagAggregate aggregate = grunnlagRepository.finnEksaktGrunnlagForBehandlingId(behandlingId);
        List<RedusertBeløpDto> redusertBeløper = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : aggregate.getGrunnlagØkonomi().getPerioder()) {
            Periode grunnlagPeriode = kgPeriode.getPeriode();
            if (feilutbetalingPeriode.overlapper(grunnlagPeriode)) {
                List<KravgrunnlagBelop433> trekkBeløper = kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.TREK.equals(belop433.getKlasseType()))
                    .collect(Collectors.toList());
                List<KravgrunnlagBelop433> redusertYtelseBeløper = kgPeriode.getKravgrunnlagBeloper433().stream()
                    .filter(belop433 -> KlasseType.YTEL.equals(belop433.getKlasseType()))
                    .filter(belop433 -> BigDecimal.ZERO.compareTo(belop433.getTilbakekrevesBelop()) == 0)
                    .collect(Collectors.toList());
                redusertBeløper.addAll(opprettRedusertBeløper(trekkBeløper, redusertYtelseBeløper));
            }
        }
        return redusertBeløper;
    }

    private List<YtelseDto> opprettYtelser(Periode feilutbetalingPeriode, Periode grunnlagPeriode, List<KravgrunnlagBelop433> beloper) {
        List<YtelseDto> ytelser = new ArrayList<>();
        for (KravgrunnlagBelop433 belop : beloper) {
            Inntektskategori inntektskategori = finnesInntektsKategori(belop);
            YtelseDto ytelse = new YtelseDto();
            ytelse.setAktivitet(inntektskategori.getNavn());
            ytelse.setBelop(BeregnBeløpUtil.beregnBeløpForPeriode(belop.getTilbakekrevesBelop(), feilutbetalingPeriode, grunnlagPeriode));
            ytelser.add(ytelse);
        }
        return ytelser;
    }


    private List<RedusertBeløpDto> opprettRedusertBeløper(List<KravgrunnlagBelop433> trekkBeløper, List<KravgrunnlagBelop433> redusertYtelseBeløper) {
        List<RedusertBeløpDto> redusertBeløpListe = new ArrayList<>();
        if (!trekkBeløper.isEmpty()) {
            for (KravgrunnlagBelop433 belop433 : trekkBeløper) {
                RedusertBeløpDto redusertBeløp = new RedusertBeløpDto();
                redusertBeløp.setBelop(belop433.getNyBelop());
                redusertBeløp.setErTrekk(true);
                redusertBeløpListe.add(redusertBeløp);
            }
        }
        if (!redusertYtelseBeløper.isEmpty()) {
            for (KravgrunnlagBelop433 belop433 : redusertYtelseBeløper) {
                RedusertBeløpDto redusertBeløp = new RedusertBeløpDto();
                redusertBeløp.setBelop(belop433.getNyBelop().subtract(belop433.getOpprUtbetBelop()));
                redusertBeløp.setErTrekk(false);
                redusertBeløpListe.add(redusertBeløp);
            }
        }
        return redusertBeløpListe;
    }

    private Inntektskategori finnesInntektsKategori(KravgrunnlagBelop433 belop) {
        Inntektskategori inntektskategori = InntektskategoriKlassekodeMapper.finnInntekstkategoriMedKlasseKode(belop.getKlasseKodeKodeverk());
        inntektskategori = behandlingRepositoryProvider.getKodeverkRepository().finn(Inntektskategori.class, inntektskategori.getKode());
        return inntektskategori;
    }

    private BigDecimal hentNyFeilutbetaltBelop(long behandlingId, Periode periode) {
        Map<Periode, BigDecimal> result = foreldelseTjeneste.beregnFeilutbetaltBeløpForPerioder(behandlingId, Lists.newArrayList(periode));
        return result.get(periode);
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
