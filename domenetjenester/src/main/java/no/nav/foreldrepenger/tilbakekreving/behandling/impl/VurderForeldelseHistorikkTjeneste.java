package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.DATE_FORMATTER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.LINJESKIFT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;

@ApplicationScoped
public class VurderForeldelseHistorikkTjeneste {

    private HistorikkinnslagRepository historikkRepository;

    public VurderForeldelseHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public VurderForeldelseHistorikkTjeneste(HistorikkinnslagRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }


    public void lagHistorikkinnslagForeldelse(Behandling behandling, Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelse vurdertForeldelseAggregate) {
        var historikkinnslagOpt = lagHistorikk2innslag(behandling, forrigeVurdertForeldelse, vurdertForeldelseAggregate);
        historikkinnslagOpt.ifPresent(innslag -> historikkRepository.lagre(innslag));
    }

    private Optional<Historikkinnslag> lagHistorikk2innslag(Behandling behandling,
                                                            Optional<VurdertForeldelse> forrigeVurdertForeldelse,
                                                            VurdertForeldelse vurdertForeldelseAggregate) {
        var endredeFelter = vurdertForeldelseAggregate.getVurdertForeldelsePerioder().stream()
            .map(foreldelsePeriode -> endredeFelterFor(forrigeVurdertForeldelse, foreldelsePeriode))
            .flatMap(Collection::stream)
            .toList();
        if (endredeFelter.isEmpty()) {
            return Optional.empty();
        }

        var historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel(SkjermlenkeType.FORELDELSE)
            .medLinjer(endredeFelter)
            .build();
        return Optional.of(historikkinnslag);
    }

    private List<HistorikkinnslagLinjeBuilder> endredeFelterFor(Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelsePeriode foreldelsePeriode) {
        var tekstlinjer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        var forrigeForeldelsePeriodeOpt = tidligereForeldelsePeriode(forrigeVurdertForeldelse, foreldelsePeriode);
        tekstlinjer.add(fraTilEquals("Foreldelse", forrigeForeldelsePeriodeOpt.map(VurdertForeldelsePeriode::getForeldelseVurderingType).orElse(null), foreldelsePeriode.getForeldelseVurderingType()));
        tekstlinjer.add(fraTilEquals("Foreldelsesfrist", forrigeForeldelsePeriodeOpt.map(VurdertForeldelsePeriode::getForeldelsesfrist).orElse(null), foreldelsePeriode.getForeldelsesfrist()));
        tekstlinjer.add(fraTilEquals("Dato for når feilutbetaling ble oppdaget", forrigeForeldelsePeriodeOpt.map(VurdertForeldelsePeriode::getOppdagelsesDato).orElse(null), foreldelsePeriode.getOppdagelsesDato()));

        if (tekstlinjer.stream().anyMatch(Objects::nonNull)) {
            tekstlinjer.addFirst(plainTekstLinje(String.format("__Manuell vurdering__ av perioden %s-%s.",  DATE_FORMATTER.format(foreldelsePeriode.getPeriode().getFom()), DATE_FORMATTER.format(foreldelsePeriode.getPeriode().getTom()))));
            tekstlinjer.addLast(plainTekstLinje(foreldelsePeriode.getBegrunnelse()));
            tekstlinjer.addLast(LINJESKIFT);
            return tekstlinjer;
        }
        return List.of();

    }

    private static Optional<VurdertForeldelsePeriode> tidligereForeldelsePeriode(Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelsePeriode foreldelsePeriode) {
        if (forrigeVurdertForeldelse.isEmpty()) {
            return Optional.empty();
        }
        return VurdertForeldelseTjeneste.forrigeForeldelsePeriode(forrigeVurdertForeldelse.get(), foreldelsePeriode);
    }
 }
