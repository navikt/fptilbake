package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.DATE_FORMATTER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.LINJESKIFT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@ApplicationScoped
public class VilkårsvurderingHistorikkInnslagTjeneste {

    private HistorikkinnslagRepository historikkRepository;

    VilkårsvurderingHistorikkInnslagTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingHistorikkInnslagTjeneste(HistorikkinnslagRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    public void lagHistorikkInnslag(Behandling behandling, VilkårVurderingEntitet gammel, VilkårVurderingEntitet ny) {
        var historikkinnslag = lagHistorikkinnslag(behandling, gammel, ny);
        historikkinnslag.ifPresent(innslag -> historikkRepository.lagre(innslag));
    }

    private static List<HistorikkinnslagLinjeBuilder> tekstendringerPeriode(VilkårVurderingEntitet gammel, VilkårVurderingEntitet ny) {
        var alleEndringer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        for (var nyPeriode : ny.getPerioder()) {
            var tekstlinjerPeriode = new ArrayList<HistorikkinnslagLinjeBuilder>();
            var gammelperiode = finnPeriode(gammel, nyPeriode.getPeriode());
            tekstlinjerPeriode.add(fraTilEquals("Beløp som skal tilbakekreves", gammelperiode.map(VilkårVurderingPeriodeEntitet::finnManueltBeløp).orElse(null), nyPeriode.finnManueltBeløp()));
            tekstlinjerPeriode.add(fraTilEquals("Er beløpet i behold?", gammelperiode.map(VilkårVurderingPeriodeEntitet::erBeløpIBehold).orElse(null), nyPeriode.erBeløpIBehold()));
            tekstlinjerPeriode.add(fraTilEquals("Er vilkårene for tilbakekreving oppfylt?", gammelperiode.map(VilkårVurderingPeriodeEntitet::getVilkårResultat).orElse(null), nyPeriode.getVilkårResultat()));
            tekstlinjerPeriode.add(fraTilEquals("I hvilken grad har mottaker handlet uaktsomt?", gammelperiode.map(VilkårVurderingPeriodeEntitet::getAktsomhetResultat).orElse(null), nyPeriode.getAktsomhetResultat()));
            tekstlinjerPeriode.add(fraTilEquals("Andel som tilbakekreves", gammelperiode.map(VilkårVurderingPeriodeEntitet::finnAndelTilbakekreves).orElse(null), nyPeriode.finnAndelTilbakekreves()));
            tekstlinjerPeriode.add(fraTilEquals("Skal det tilegges renter?", gammelperiode.map(VilkårVurderingPeriodeEntitet::manueltSattIleggRenter).orElse(null), nyPeriode.manueltSattIleggRenter()));
            tekstlinjerPeriode.add(fraTilEquals("Skal beløp under 4 rettsgebyr (6.ledd) tilbakekreves?", gammelperiode.map(VilkårVurderingPeriodeEntitet::tilbakekrevesSmåbeløp).orElse(null), nyPeriode.tilbakekrevesSmåbeløp()));
            tekstlinjerPeriode.add(fraTilEquals("Er det særlige grunner til reduksjon?", gammelperiode.map(VilkårsvurderingHistorikkInnslagTjeneste::lagSærligeGrunnerTekst).orElse(null), lagSærligeGrunnerTekst(nyPeriode)));
            if (!Objects.equals(gammelperiode.map(VilkårVurderingPeriodeEntitet::getBegrunnelse).orElse(null), nyPeriode.getBegrunnelse())) {
                tekstlinjerPeriode.add(plainTekstLinje(String.format("Begrunnelse for vilkår: %s", nyPeriode.getBegrunnelse())));
            }
            if (!Objects.equals(gammelperiode.map(VilkårVurderingPeriodeEntitet::getBegrunnelseAktsomhet).orElse(null), nyPeriode.getBegrunnelseAktsomhet())) {
                tekstlinjerPeriode.add(plainTekstLinje(String.format("Begrunnelse for aktsomhet: %s", nyPeriode.getBegrunnelseAktsomhet())));
            }
            if (!Objects.equals(gammelperiode.map(VilkårVurderingPeriodeEntitet::getBegrunnelseSærligGrunner).orElse(null), nyPeriode.getBegrunnelseSærligGrunner())) {
                tekstlinjerPeriode.add(plainTekstLinje(String.format("Særlige grunner som er vektlagt: %s", nyPeriode.getBegrunnelseSærligGrunner())));
            }

            if (tekstlinjerPeriode.stream().anyMatch(Objects::nonNull)) {
                tekstlinjerPeriode.addFirst(plainTekstLinje(String.format("__Vurdering__ av perioden %s-%s.", DATE_FORMATTER.format(nyPeriode.getPeriode().getFom()), DATE_FORMATTER.format(nyPeriode.getPeriode().getTom()))));
                tekstlinjerPeriode.addLast(LINJESKIFT);
                alleEndringer.addAll(tekstlinjerPeriode);
            }
        }
        return alleEndringer;
    }

    private static Optional<Historikkinnslag> lagHistorikkinnslag(Behandling behandling, VilkårVurderingEntitet gammel, VilkårVurderingEntitet ny) {
        var linjerMedEndringer = tekstendringerPeriode(gammel, ny);
        if (linjerMedEndringer.stream().allMatch(Objects::nonNull)) {
            return Optional.empty();
        }
        return Optional.of(new Historikkinnslag.Builder()
            .medAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel(SkjermlenkeType.TILBAKEKREVING)
            .medLinjer(linjerMedEndringer)
            .build());
    }

    private static Optional<VilkårVurderingPeriodeEntitet> finnPeriode(VilkårVurderingEntitet gammel, Periode tidsperiode) {
        if (gammel == null) {
            return Optional.empty();
        }
        return gammel.getPerioder()
                .stream()
                .filter(p -> p.getPeriode().equals(tidsperiode))
                .findAny();
    }

    private static String lagSærligeGrunnerTekst(VilkårVurderingPeriodeEntitet periode) {
        var aktsomhet = periode.getAktsomhet();
        if (aktsomhet == null || aktsomhet.getSærligGrunner().isEmpty()) {
            return null;
        }

        List<String> grunnTekster = new ArrayList<>();
        StringBuilder grunnTekst = new StringBuilder();
        grunnTekst.append(HistorikkinnslagLinjeBuilder.format(aktsomhet.getSærligGrunnerTilReduksjon()));
        grunnTekst.append(": ");
        for (VilkårVurderingSærligGrunnEntitet særligGrunn : aktsomhet.getSærligGrunner()) {
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
}
