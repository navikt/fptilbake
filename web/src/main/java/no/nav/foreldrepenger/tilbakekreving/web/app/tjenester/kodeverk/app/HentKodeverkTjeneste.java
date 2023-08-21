package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;

@ApplicationScoped
public class HentKodeverkTjeneste {

    public static final Map<String, Collection<? extends Kodeverdi>> KODEVERDIER_SOM_BRUKES_PÅ_KLIENT;

    static {
        Map<String, Collection<? extends Kodeverdi>> map = new LinkedHashMap<>();
        map.put(Fagsystem.class.getSimpleName(), Fagsystem.kodeMap().values());
        map.put(Venteårsak.class.getSimpleName(), Venteårsak.kodeMap().values());
        map.put(Aktsomhet.class.getSimpleName(), Aktsomhet.kodeMap().values());
        map.put(AnnenVurdering.class.getSimpleName(), AnnenVurdering.kodeMap().values());
        map.put(HistorikkEndretFeltType.class.getSimpleName(), HistorikkEndretFeltType.kodeMap().values());
        map.put(SærligGrunn.class.getSimpleName(), SærligGrunn.kodeMap().values());
        map.put(VilkårResultat.class.getSimpleName(), VilkårResultat.kodeMap().values());
        map.put(VedtakResultatType.class.getSimpleName(), VedtakResultatType.kodeMap().values());
        map.put(ForeldelseVurderingType.class.getSimpleName(), ForeldelseVurderingType.kodeMap().values());
        map.put(HistorikkAktør.class.getSimpleName(), HistorikkAktør.kodeMap().values());
        map.put(HendelseType.class.getSimpleName(), HendelseType.kodeMap().values());
        map.put(HistorikkOpplysningType.class.getSimpleName(), HistorikkOpplysningType.kodeMap().values());
        map.put(HistorikkinnslagType.class.getSimpleName(), HistorikkinnslagType.kodeMap().values());
        map.put(SkjermlenkeType.class.getSimpleName(), SkjermlenkeType.kodeMap().values());
        map.put(HendelseUnderType.class.getSimpleName(), HendelseUnderType.kodeMap().values());
        map.put(BehandlingResultatType.class.getSimpleName(), BehandlingResultatType.kodeMap().values());
        map.put(VidereBehandling.class.getSimpleName(), VidereBehandling.kodeMap().values());
        map.put(VergeType.class.getSimpleName(), VergeType.kodeMap().values());
        map.put(VurderÅrsak.class.getSimpleName(), VurderÅrsak.kodeMap().values());
        map.put(BehandlingÅrsakType.class.getSimpleName(), BehandlingÅrsakType.kodeMap().values());
        map.put(BehandlingType.class.getSimpleName(), BehandlingType.kodeMap().values());

        Map<String, Collection<? extends Kodeverdi>> mapFiltered = new LinkedHashMap<>();

        map.entrySet().forEach(e -> mapFiltered.put(e.getKey(), e.getValue().stream()
                .filter(f -> !"-".equals(f.getKode()))
                .collect(Collectors.toList())));

        KODEVERDIER_SOM_BRUKES_PÅ_KLIENT = Collections.unmodifiableMap(mapFiltered);
    }

    @Inject
    public HentKodeverkTjeneste() {
        // For CDI
    }


    public Map<String, Collection<? extends Kodeverdi>> hentGruppertKodeliste() { //NOSONAR
        return new LinkedHashMap<>(KODEVERDIER_SOM_BRUKES_PÅ_KLIENT);
    }
}
