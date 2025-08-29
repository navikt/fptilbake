package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.dto;

import java.util.SortedSet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;

public record AlleKodeverdierSomObjektResponse(
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<Fagsystem>> fagsystemer,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<Venteårsak>> venteårsaker,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<Aktsomhet>> aktsomheter,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<AnnenVurdering>> annenVurderinger,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<SærligGrunn>> særligGrunner,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<VilkårResultat>> vilkårResultater,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<VedtakResultatType>> vedtakResultatTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<ForeldelseVurderingType>> foreldelseVurderingTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<HistorikkAktør>> historikkAktører,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<HendelseType>> hendelseTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<SkjermlenkeType>> skjermlenkeTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<HendelseUnderType>> hendelseUnderTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<BehandlingResultatType>> behandlingResultatTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<VidereBehandling>> videreBehandlinger,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<VergeType>> vergeTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<VurderÅrsak>> vurderÅrsaker,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<BehandlingÅrsakType>> behandlingÅrsakTyper,
    @NotNull @Valid @Size(min= 1, max=1000) SortedSet<KodeverdiSomObjekt<BehandlingType>> behandlingTyper
    ) {
}
