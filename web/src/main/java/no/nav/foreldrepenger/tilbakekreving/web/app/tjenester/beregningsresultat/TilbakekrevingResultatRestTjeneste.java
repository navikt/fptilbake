package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.beregningsresultat;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Vurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path(TilbakekrevingResultatRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class TilbakekrevingResultatRestTjeneste {

    public static final String PATH_FRAGMENT = "/beregning";
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VurdertForeldelseRepository vurdertForeldelseRepository;

    public TilbakekrevingResultatRestTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingResultatRestTjeneste(BeregningsresultatTjeneste beregningsresultatTjeneste,
                                              BehandlingTjeneste behandlingTjeneste,
                                              VilkårsvurderingRepository vilkårsvurderingRepository,
                                              VurdertForeldelseRepository vurdertForeldelseRepository) {
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.vilkårsvurderingRepository = vilkårsvurderingRepository;
        this.vurdertForeldelseRepository = vurdertForeldelseRepository;
    }

    @GET
    @Path("/resultat")
    @Operation(tags = "beregning", description = "Henter beregningsresultat for tilbakekreving")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public BeregningResultatDto hentBeregningResultat(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class) @QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        var behandlingId = hentBehandlingId(behandlingReferanse);
        var beregningsresultat = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);
        var vurdertForeldelse = hentVurdertForeldelse(behandlingId);
        var vilkårsvurdering = hentVilkårsvurdering(behandlingId);
        return map(beregningsresultat, vurdertForeldelse, vilkårsvurdering);
    }

    private VilkårVurderingEntitet hentVilkårsvurdering(Long behandlingId) {
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId).orElse(new VilkårVurderingEntitet());
    }

    private VurdertForeldelse hentVurdertForeldelse(Long behandlingId) {
        return vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(new VurdertForeldelse());
    }

    private BeregningResultatDto map(BeregningResultat beregningsresultat,
                                     VurdertForeldelse vurdertForeldelse,
                                     VilkårVurderingEntitet vilkårVurdering) {
        return new BeregningResultatDto(beregningsresultat.getVedtakResultatType(),
            mapPerioder(beregningsresultat.getBeregningResultatPerioder(), vurdertForeldelse, vilkårVurdering));
    }

    private List<BeregningResultatPeriodeDto> mapPerioder(List<BeregningResultatPeriode> beregningResultatPerioder,
                                                          VurdertForeldelse vurdertForeldelse,
                                                          VilkårVurderingEntitet vilkårVurdering) {

        return beregningResultatPerioder.stream().map(p -> map(p, vurdertForeldelse, vilkårVurdering)).toList();
    }

    private BeregningResultatPeriodeDto map(BeregningResultatPeriode resultatPeriode,
                                            VurdertForeldelse vurdertForeldelse,
                                            VilkårVurderingEntitet vilkårVurderinger) {
        var p = resultatPeriode.getPeriode();
        var foreldelsevurdering = velgAktuellVurdering(p, vurdertForeldelse.getVurdertForeldelsePerioder(),
            VurdertForeldelsePeriode::getPeriode);
        var vilkårsvurdering = velgAktuellVurdering(p, vilkårVurderinger.getPerioder(),
            VilkårVurderingPeriodeEntitet::getPeriode);

        var erForeldet = foreldelsevurdering != null && foreldelsevurdering.erForeldet();
        valider(vilkårsvurdering, erForeldet);

        var andelAvBeløp = erForeldet ? BigDecimal.ZERO : finnAndelAvBeløp(vilkårsvurdering);
        var vurdering = erForeldet ? AnnenVurdering.FORELDET : finnVurdering(vilkårsvurdering);

        return BeregningResultatPeriodeDto.builder()
            .medPeriode(resultatPeriode.getPeriode())
            .medErForeldet(erForeldet)
            .medTilbakekrevingBeløp(resultatPeriode.getTilbakekrevingBeløp())
            .medTilbakekrevingBeløpEtterSkatt(resultatPeriode.getTilbakekrevingBeløpEtterSkatt())
            .medTilbakekrevingBeløpUtenRenter(resultatPeriode.getTilbakekrevingBeløpUtenRenter())
            .medRenterProsent(resultatPeriode.getRenterProsent())
            .medRenteBeløp(resultatPeriode.getRenteBeløp())
            .medSkattBeløp(resultatPeriode.getSkattBeløp())
            .medFeilutbetaltBeløp(resultatPeriode.getFeilutbetaltBeløp())
            .medUtbetaltYtelseBeløp(resultatPeriode.getUtbetaltYtelseBeløp())
            .medRiktigYtelseBeløp(resultatPeriode.getRiktigYtelseBeløp())
            .medVurdering(vurdering)
            .medAndelAvBeløp(andelAvBeløp)
            .build();
    }

    private static void valider(VilkårVurderingPeriodeEntitet vilkårsvurdering, boolean erForeldet) {
        if (erForeldet && vilkårsvurdering != null) {
            throw new IllegalArgumentException("Ikke forventet å ha vanlig vilkårsvurdering når perioden er foreldet");
        }
        if (!erForeldet && vilkårsvurdering == null) {
            throw new IllegalArgumentException("Forventet å ha vanlig vilkårsvurdering når perioden ikke er foreldet");
        }
    }

    private Long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId() ? behandlingReferanse.getBehandlingId() : behandlingTjeneste.hentBehandlingId(
            behandlingReferanse.getBehandlingUuid());
    }

    private static <T> T velgAktuellVurdering(Periode resultatperiode, Collection<T> vurderinger, Function<T, Periode> vurderingPeriodeFunksjon) {
        var overlappendeVurderinger = vurderinger.stream().filter(v -> vurderingPeriodeFunksjon.apply(v).overlapper(resultatperiode)).toList();
        if (overlappendeVurderinger.size() > 1) {
            throw new IllegalArgumentException("Forventet 0 eller 1 element, fikk " + overlappendeVurderinger.size());
        }
        if (overlappendeVurderinger.isEmpty()) {
            return null;
        }
        var overlappendeVilkårsvurdering = overlappendeVurderinger.get(0);
        var vurderingsperiode = vurderingPeriodeFunksjon.apply(overlappendeVilkårsvurdering);
        if (vurderingsperiode.equals(resultatperiode)) {
            return overlappendeVilkårsvurdering;
        }
        throw new IllegalArgumentException(
            "Forventet at vurderingsperioden " + vurderingsperiode + " skulle ha samme periode som resulatperioden " + resultatperiode);
    }

    private static BigDecimal finnAndelAvBeløp(VilkårVurderingPeriodeEntitet vurdering) {
        var aktsomhet = vurdering.getAktsomhet();
        var godTro = vurdering.getGodTro();
        if (aktsomhet != null) {
            return finnAndelForAktsomhet(aktsomhet);
        } else if (godTro != null && !godTro.isBeløpErIBehold()) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    private static BigDecimal finnAndelForAktsomhet(VilkårVurderingAktsomhetEntitet aktsomhet) {
        var hundreProsent = BigDecimal.valueOf(100);
        if (Aktsomhet.FORSETT.equals(aktsomhet.getAktsomhet()) || Boolean.FALSE.equals(aktsomhet.getSærligGrunnerTilReduksjon())) {
            return hundreProsent;
        }
        return aktsomhet.getProsenterSomTilbakekreves();
    }

    private static Vurdering finnVurdering(VilkårVurderingPeriodeEntitet vurdering) {
        if (vurdering.getAktsomhet() != null) {
            return vurdering.getAktsomhet().getAktsomhet();
        }
        if (vurdering.getGodTro() != null) {
            return AnnenVurdering.GOD_TRO;
        }
        throw new IllegalArgumentException("VVurdering skal peke til GodTro-entiet eller Aktsomhet-entitet");
    }

}
