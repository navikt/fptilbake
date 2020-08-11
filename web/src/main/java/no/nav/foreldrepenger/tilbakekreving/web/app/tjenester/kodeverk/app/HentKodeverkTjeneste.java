package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;

@ApplicationScoped
public class HentKodeverkTjeneste {

    private KodeverkRepository kodeverkRepository;

    public static final Map<String, Collection<? extends Kodeverdi>> KODEVERDIER_SOM_BRUKES_PÅ_KLIENT;

    static {
        Map<String, Collection<? extends Kodeverdi>> map = new LinkedHashMap<>();
        map.put(KonsekvensForYtelsen.class.getSimpleName(), KonsekvensForYtelsen.kodeMap().values());
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

        Map<String, Collection<? extends Kodeverdi>> mapFiltered = new LinkedHashMap<>();

        map.entrySet().forEach(e -> {
            mapFiltered.put(e.getKey(), e.getValue().stream().filter(f -> !"-".equals(f.getKode())).collect(Collectors.toSet()));
        });

        KODEVERDIER_SOM_BRUKES_PÅ_KLIENT = Collections.unmodifiableMap(mapFiltered);
    }

    private static List<Class<? extends Kodeliste>> KODEVERK_SOM_BRUKES_PÅ_KLIENT = Arrays.asList(
        // Legg inn kodelister etter behov
        HistorikkOpplysningType.class,
        HistorikkinnslagType.class,
        SkjermlenkeType.class,
        BehandlingType.class,
        BehandlingÅrsakType.class,
        BehandlingResultatType.class,
        VidereBehandling.class,
        HendelseUnderType.class,
        VergeType.class
    );

    public HentKodeverkTjeneste() {
        // For CDI
    }

    @Inject
    public HentKodeverkTjeneste(KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository"); //$NON-NLS-1$
        this.kodeverkRepository = kodeverkRepository;
    }


    public Map<String, Collection<? extends Kodeverdi>> hentGruppertKodeliste() {
        Map<String, Set<? extends Kodeliste>> kodelister = new HashMap<>();
        KODEVERK_SOM_BRUKES_PÅ_KLIENT.forEach(k -> {
            //TODO (TOR) Kjører repository-kall for kvar kodeliste. Er nok ikkje naudsynt
            Set<Kodeliste> filtrertKodeliste = kodeverkRepository.hentAlle(k).stream()
                .filter(ads -> !"-".equals(ads.getKode()))
                .collect(Collectors.toSet());
            kodelister.put(k.getSimpleName(), filtrertKodeliste);
        });

        // slå sammen kodeverdi og kodeliste maps
        Map<String, Collection<? extends Kodeverdi>> kodelistMap = new LinkedHashMap<>(kodelister);
        kodelistMap.putAll(KODEVERDIER_SOM_BRUKES_PÅ_KLIENT);

        return kodelistMap;
    }
}
