package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHjelperUtil.konvertFraBoolean;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.ANDEL_TILBAKEKREVES;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.BELØP_TILBAKEKREVES;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.ER_BELØPET_BEHOLD;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.ER_SÆRLIGE_GRUNNER_TIL_REDUKSJON;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.ILEGG_RENTER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.MOTTAKER_UAKTSOMHET_GRAD;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType.TILBAKEKREV_SMÅBELOEP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

@ApplicationScoped
public class VilkårsvurderingHistorikkInnslagTjeneste {

    private HistorikkTjenesteAdapter historikkTjenesteAdapter;
    private BehandlingRepositoryProvider repositoryProvider;

    VilkårsvurderingHistorikkInnslagTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingHistorikkInnslagTjeneste(HistorikkTjenesteAdapter historikkTjenesteAdapter, BehandlingRepositoryProvider repositoryProvider) {
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
        this.repositoryProvider = repositoryProvider;
    }

    public void lagHistorikkInnslag(Long behandlingId, VilkårVurderingEntitet gammel, VilkårVurderingEntitet ny) {
        List<Vilkårsendring> endringer = finnEndringer(gammel, ny);
        if (!endringer.isEmpty()) {
            lagInnslag(behandlingId, endringer);
        }
    }

    private List<Vilkårsendring> finnEndringer(VilkårVurderingEntitet gammel, VilkårVurderingEntitet ny) {
        List<Vilkårsendring> resultat = new ArrayList<>();
        for (VilkårVurderingPeriodeEntitet nyPeriode : ny.getPerioder()) {
            Periode periode = nyPeriode.getPeriode();
            VilkårVurderingPeriodeEntitet gammelperiode = finnPeriode(gammel, periode);
            List<Historikkendring> endringer = finnEndringer(gammelperiode, nyPeriode);
            if (!endringer.isEmpty()) {
                String begrunnelseVilkår = nyPeriode.getBegrunnelse();
                String begrunnelseAktsomhet = nyPeriode.getBegrunnelseAktsomhet();
                String begrunnelseSærligGrunner = nyPeriode.getBegrunnelseSærligGrunner();
                resultat.add(new Vilkårsendring(periode, begrunnelseVilkår, begrunnelseAktsomhet, begrunnelseSærligGrunner, endringer));
            }
        }
        return resultat;
    }

    private VilkårVurderingPeriodeEntitet finnPeriode(VilkårVurderingEntitet gammel, Periode tidsperiode) {
        if (gammel == null) {
            return null;
        }
        return gammel.getPerioder()
                .stream()
                .filter(p -> p.getPeriode().equals(tidsperiode))
                .findAny()
                .orElse(null);
    }

    private List<Historikkendring> finnEndringer(VilkårVurderingPeriodeEntitet gammel, VilkårVurderingPeriodeEntitet ny) {
        var endringer = Arrays.asList(
                finnEndring(gammel, ny, BELØP_TILBAKEKREVES, VilkårVurderingPeriodeEntitet::finnManueltBeløp),
                finnEndring(gammel, ny, ER_BELØPET_BEHOLD, r -> fraBoolean(r.erBeløpIBehold())),
                finnEndring(gammel, ny, ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT, r -> getNavn(r.getVilkårResultat())),
                finnEndring(gammel, ny, MOTTAKER_UAKTSOMHET_GRAD, r -> getNavn(r.getAktsomhetResultat())),
                finnEndring(gammel, ny, ANDEL_TILBAKEKREVES, VilkårVurderingPeriodeEntitet::finnAndelTilbakekreves),
                finnEndring(gammel, ny, ILEGG_RENTER, r -> fraBoolean(r.manueltSattIleggRenter())),
                finnEndring(gammel, ny, TILBAKEKREV_SMÅBELOEP, r -> fraBoolean(r.tilbakekrevesSmåbeløp())),
                ! (ny.getAktsomhet()!=null && ny.getAktsomhet().getSærligGrunner().isEmpty())?
                   finnEndring(gammel, ny, ER_SÆRLIGE_GRUNNER_TIL_REDUKSJON, this::lagSærligeGrunnerTekst) :null
        );
        return endringer
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static <T> Historikkendring<T> finnEndring(VilkårVurderingPeriodeEntitet gammel, VilkårVurderingPeriodeEntitet ny, HistorikkEndretFeltType felt, Function<VilkårVurderingPeriodeEntitet, T> oppslag) {
        T nyVerdi = finn(ny, oppslag);
        T gammelVerdi = finn(gammel, oppslag);
        if (Objects.equals(nyVerdi, gammelVerdi)) {
            return null;
        }
        return new Historikkendring<>(felt, gammelVerdi, nyVerdi);
    }

    private void lagInnslag(Long behandlingId, List<Vilkårsendring> endringer) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.TILBAKEKREVING);
        historikkinnslag.setBehandlingId(behandlingId);
        historikkinnslag.setAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER);

        for (Vilkårsendring vilkårsendring : endringer) {
            HistorikkInnslagTekstBuilder builder = lagTekstBuilderMedFellesFelt(vilkårsendring);
            for (Historikkendring historikkendring : vilkårsendring.getEndringer()) {
                builder.medEndretFelt(historikkendring.getFelt(), historikkendring.getForrigeVerdi(), historikkendring.getNyVerdi());
            }
            builder.medSkjermlenke(SkjermlenkeType.TILBAKEKREVING);
            builder.medBegrunnelse(vilkårsendring.getBegrunnelseAktsomhet());
            builder.medOpplysning(HistorikkOpplysningType.SÆRLIG_GRUNNER_BEGRUNNELSE,vilkårsendring.getBegrunnelseSærligGrunner());
            builder.build(historikkinnslag);
        }
        historikkTjenesteAdapter.lagInnslag(historikkinnslag);
    }

    private static <T> T finn(VilkårVurderingPeriodeEntitet periode, Function<VilkårVurderingPeriodeEntitet, T> oppslag) {
        return periode == null ? null : oppslag.apply(periode);
    }


    private String lagSærligeGrunnerTekst(VilkårVurderingPeriodeEntitet periode) {
        VilkårVurderingAktsomhetEntitet aktsomhet = periode.getAktsomhet();
        return aktsomhet != null ? lagSærligeGrunnerTekst(aktsomhet) : null;
    }


    private String lagSærligeGrunnerTekst(VilkårVurderingAktsomhetEntitet aktsomhet) {
        return aktsomhet.getSærligGrunner().isEmpty() ? null : formGrunnTekst(aktsomhet);
    }

    private HistorikkInnslagTekstBuilder lagTekstBuilderMedFellesFelt(Vilkårsendring periode) {
        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();

        tekstBuilder.medSkjermlenke(SkjermlenkeType.TILBAKEKREVING)
                .medOpplysning(HistorikkOpplysningType.PERIODE_FOM, periode.getFom())
                .medOpplysning(HistorikkOpplysningType.PERIODE_TOM, periode.getTom())
                .medOpplysning(HistorikkOpplysningType.TILBAKEKREVING_OPPFYLT_BEGRUNNELSE, periode.getBegrunnelseVilkår());
        return tekstBuilder;
    }

    private String formGrunnTekst(VilkårVurderingAktsomhetEntitet aktsomhetEntitet) {
        List<String> grunnTekster = new ArrayList<>();
        StringBuilder grunnTekst = new StringBuilder();
        grunnTekst.append(konvertFraBoolean(aktsomhetEntitet.getSærligGrunnerTilReduksjon()));
        grunnTekst.append(": ");
        for (VilkårVurderingSærligGrunnEntitet særligGrunn : aktsomhetEntitet.getSærligGrunner()) {
            SærligGrunn grunn = særligGrunn.getGrunn();
            StringBuilder tekst = new StringBuilder(grunn.getNavn());
            if (SærligGrunn.ANNET.equals(grunn)) {
                tekst.append(": ");
                tekst.append(særligGrunn.getBegrunnelse());
            }
            grunnTekster.add(tekst.toString());
        }
        grunnTekst.append(String.join(", ", grunnTekster));
        return grunnTekst.toString();
    }

    private String getNavn(Kodeverdi kode) {
        if(kode == null) {
            return null;
        }
        return kode.getNavn();
    }

    private String fraBoolean(Boolean verdi) {
        if (verdi == null) {
            return null;
        }
        //TODO fjern oppslag, skal lagres med kode
        return verdi ? "Ja" : "Nei";
    }

}
