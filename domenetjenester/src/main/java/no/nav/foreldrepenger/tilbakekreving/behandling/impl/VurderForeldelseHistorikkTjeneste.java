package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;

@ApplicationScoped
public class VurderForeldelseHistorikkTjeneste {

    private HistorikkRepositoryTeamAware historikkRepositoryTeamAware;

    public VurderForeldelseHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public VurderForeldelseHistorikkTjeneste(HistorikkRepositoryTeamAware historikkRepositoryTeamAware) {
        this.historikkRepositoryTeamAware = historikkRepositoryTeamAware;
    }


    public void lagHistorikkinnslagForeldelse(Behandling behandling, Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelse vurdertForeldelseAggregate) {
        var historikkinnslag = lagHistorikkinnslag(behandling, forrigeVurdertForeldelse, vurdertForeldelseAggregate);
        var historikk2innslag = lagHistorikk2innslag(behandling, forrigeVurdertForeldelse, vurdertForeldelseAggregate);

        // TODO?
        if (historikkinnslag.isEmpty() || historikk2innslag.isEmpty()) {
            return;
        }

        historikkRepositoryTeamAware.lagre(historikkinnslag.get(), historikk2innslag.get());
    }

    private Optional<Historikkinnslag2> lagHistorikk2innslag(Behandling behandling,
                                                   Optional<VurdertForeldelse> forrigeVurdertForeldelse,
                                                   VurdertForeldelse vurdertForeldelseAggregate) {
        var endredeFelter = vurdertForeldelseAggregate.getVurdertForeldelsePerioder().stream()
            .map(foreldelsePeriode -> endredeFelterFor(forrigeVurdertForeldelse, foreldelsePeriode))
            .flatMap(Collection::stream)
            .toList();
        if (endredeFelter.isEmpty()) {
            return Optional.empty();
        }

        var historikkinnslag = new Historikkinnslag2.Builder()
            .medAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel("Foreldelse")
            .medLinjer(endredeFelter)
            .build();
        return Optional.of(historikkinnslag);
    }

    private List<HistorikkinnslagLinjeBuilder> endredeFelterFor(Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelsePeriode foreldelsePeriode) {
        var historikkinnslagLinjer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        var forrigeForeldelsePeriodeOpt = tidligereForeldelsePeriode(forrigeVurdertForeldelse, foreldelsePeriode);
        historikkinnslagLinjer.add(plainTekstLinje(String.format("__Manuell vurdering__ av perioden %s-%s.",  foreldelsePeriode.getPeriode().getFom(), foreldelsePeriode.getPeriode().getTom())));
        historikkinnslagLinjer.add(fraTilEquals("Foreldelse", forrigeForeldelsePeriodeOpt.map(VurdertForeldelsePeriode::getForeldelseVurderingType).orElse(null), foreldelsePeriode.getForeldelseVurderingType()));
        historikkinnslagLinjer.add(fraTilEquals("Foreldelsesfrist", forrigeForeldelsePeriodeOpt.map(VurdertForeldelsePeriode::getForeldelsesfrist).orElse(null), foreldelsePeriode.getForeldelsesfrist()));
        historikkinnslagLinjer.add(fraTilEquals("Dato for når feilutbetaling ble oppdaget", forrigeForeldelsePeriodeOpt.map(VurdertForeldelsePeriode::getOppdagelsesDato).orElse(null), foreldelsePeriode.getOppdagelsesDato()));
        historikkinnslagLinjer.add(plainTekstLinje(foreldelsePeriode.getBegrunnelse()));
        if (historikkinnslagLinjer.stream().filter(Objects::nonNull).toList().size() > 2) { // Har noen endrede felter.. TODO: Litt småhack? Bedre måte å gjøre det på?
            return historikkinnslagLinjer;
        } else {
            return List.of();
        }
    }

    private static Optional<VurdertForeldelsePeriode> tidligereForeldelsePeriode(Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelsePeriode foreldelsePeriode) {
        if (forrigeVurdertForeldelse.isEmpty()) {
            return Optional.empty();
        }
        return VurdertForeldelseTjeneste.forrigeForeldelsePeriode(forrigeVurdertForeldelse.get(), foreldelsePeriode);
    }

    // Gammel
    private static Optional<Historikkinnslag> lagHistorikkinnslag(Behandling behandling, Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelse vurdertForeldelseAggregate) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FORELDELSE);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER);

        boolean behovForHistorikkInnslag = false;
        for (VurdertForeldelsePeriode foreldelsePeriode : vurdertForeldelseAggregate.getVurdertForeldelsePerioder()) {
            HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
            boolean harEndret;
            // forrigeVurdertForeldelse finnes ikke
            if (forrigeVurdertForeldelse.isEmpty()) {
                harEndret = true;
                lagNyttInnslag(foreldelsePeriode, tekstBuilder);
            } else {
                harEndret = opprettInnslagNårForrigePerioderFinnes(forrigeVurdertForeldelse.get(), foreldelsePeriode, tekstBuilder);
            }
            if (harEndret) {
                tekstBuilder.medSkjermlenke(SkjermlenkeType.FORELDELSE)
                    .medOpplysning(HistorikkOpplysningType.PERIODE_FOM, foreldelsePeriode.getPeriode().getFom())
                    .medOpplysning(HistorikkOpplysningType.PERIODE_TOM, foreldelsePeriode.getPeriode().getTom())
                    .medBegrunnelse(foreldelsePeriode.getBegrunnelse());

                tekstBuilder.build(historikkinnslag);
                behovForHistorikkInnslag = true;
            }
        }

        return behovForHistorikkInnslag ? Optional.of(historikkinnslag) : Optional.empty();
    }


    private static boolean opprettInnslagNårForrigePerioderFinnes(VurdertForeldelse forrigeVurdertForeldelse,
                                                           VurdertForeldelsePeriode foreldelsePeriode,
                                                           HistorikkInnslagTekstBuilder tekstBuilder) {

        Optional<VurdertForeldelsePeriode> forrigeForeldelsePeriode = VurdertForeldelseTjeneste.forrigeForeldelsePeriode(forrigeVurdertForeldelse, foreldelsePeriode);
        boolean harEndret = false;
        // samme perioder med endret foreldelse vurdering type
        if (forrigeForeldelsePeriode.isPresent()) {
            harEndret = lagEndretInnslag(foreldelsePeriode, tekstBuilder, forrigeForeldelsePeriode.get());
            harEndret = harEndret || !foreldelsePeriode.getBegrunnelse().equals(forrigeForeldelsePeriode.get().getBegrunnelse());
        } else { // nye perioder
            harEndret = true;
            lagNyttInnslag(foreldelsePeriode, tekstBuilder);
        }
        return harEndret;
    }

    private static boolean lagEndretInnslag(VurdertForeldelsePeriode foreldelsePeriode,
                                     HistorikkInnslagTekstBuilder tekstBuilder,
                                     VurdertForeldelsePeriode forrigeForeldelsePeriode) {
        boolean harEndret = false;
        if (!foreldelsePeriode.getForeldelseVurderingType().equals(forrigeForeldelsePeriode.getForeldelseVurderingType())) {
            harEndret = true;
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE,
                forrigeForeldelsePeriode.getForeldelseVurderingType().getNavn(),
                foreldelsePeriode.getForeldelseVurderingType().getNavn());
        }
        if ((ForeldelseVurderingType.FORELDET.equals(foreldelsePeriode.getForeldelseVurderingType()) || ForeldelseVurderingType.TILLEGGSFRIST.equals(foreldelsePeriode.getForeldelseVurderingType()))
            && ((foreldelsePeriode.getForeldelsesfrist() != null && !foreldelsePeriode.getForeldelsesfrist().equals(forrigeForeldelsePeriode.getForeldelsesfrist()))
            || (forrigeForeldelsePeriode.getForeldelsesfrist() != null && !forrigeForeldelsePeriode.getForeldelsesfrist().equals(foreldelsePeriode.getForeldelsesfrist())))
        ) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSESFRIST, forrigeForeldelsePeriode.getForeldelsesfrist(), foreldelsePeriode.getForeldelsesfrist());
            harEndret = true;
        }
        if (ForeldelseVurderingType.TILLEGGSFRIST.equals(foreldelsePeriode.getForeldelseVurderingType())
            && (foreldelsePeriode.getOppdagelsesDato() != null && !foreldelsePeriode.getOppdagelsesDato().equals(forrigeForeldelsePeriode.getOppdagelsesDato()))
            || (forrigeForeldelsePeriode.getOppdagelsesDato() != null && !forrigeForeldelsePeriode.getOppdagelsesDato().equals(foreldelsePeriode.getOppdagelsesDato()))
        ) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.OPPDAGELSES_DATO, forrigeForeldelsePeriode.getOppdagelsesDato(), foreldelsePeriode.getOppdagelsesDato());
            harEndret = true;
        }
        return harEndret;
    }

    private static void lagNyttInnslag(VurdertForeldelsePeriode foreldelsePeriode, HistorikkInnslagTekstBuilder tekstBuilder) {
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE, null, foreldelsePeriode.getForeldelseVurderingType().getNavn());
        if (foreldelsePeriode.getForeldelsesfrist() != null) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSESFRIST, null, foreldelsePeriode.getForeldelsesfrist());
        }
        if (foreldelsePeriode.getOppdagelsesDato() != null) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.OPPDAGELSES_DATO, null, foreldelsePeriode.getOppdagelsesDato());
        }
    }


 }
