package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Begrunnelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Tema;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;

@ApplicationScoped
class HentKodeverkTjenesteImpl implements HentKodeverkTjeneste {

    private KodeverkRepository kodeverkRepository;

    private static List<Class<? extends Kodeliste>> KODEVERK_SOM_BRUKES_PÅ_KLIENT = Arrays.asList(
        // Legg inn kodelister etter behov
        ArkivFilType.class,
        Begrunnelse.class,
        DokumentKategori.class,
        DokumentTypeId.class,
        Fagsystem.class,
        Tema.class,
        Venteårsak.class,
        VariantFormat.class,
        Aktsomhet.class,
        SærligGrunn.class,
        VilkårResultat.class,
        AnnenVurdering.class,
        VedtakResultatType.class,
        ForeldelseVurderingType.class,
        HistorikkAktør.class,
        HistorikkEndretFeltType.class,
        HistorikkOpplysningType.class,
        HistorikkinnslagType.class,
        SkjermlenkeType.class,
        BehandlingType.class,
        BehandlingÅrsakType.class,
        BehandlingResultatType.class,
        KonsekvensForYtelsen.class,
        VidereBehandling.class,
        HendelseType.class,
        HendelseUnderType.class
    );

    public HentKodeverkTjenesteImpl() {
        // For CDI
    }

    @Inject
    public HentKodeverkTjenesteImpl(KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository"); //$NON-NLS-1$
        this.kodeverkRepository = kodeverkRepository;
    }


    @Override
    public Map<String, List<Kodeliste>> hentGruppertKodeliste() {
        Map<String, List<Kodeliste>> klientKoder = new HashMap<>();
        KODEVERK_SOM_BRUKES_PÅ_KLIENT.forEach(k -> {
            //TODO (TOR) Kjører repository-kall for kvar kodeliste. Er nok ikkje naudsynt
            List<Kodeliste> filtrertKodeliste = kodeverkRepository.hentAlle(k).stream()
                .filter(ads -> !"-".equals(ads.getKode()))
                .collect(Collectors.toList());
            klientKoder.put(k.getSimpleName(), filtrertKodeliste);
        });

        return klientKoder;
    }
}
