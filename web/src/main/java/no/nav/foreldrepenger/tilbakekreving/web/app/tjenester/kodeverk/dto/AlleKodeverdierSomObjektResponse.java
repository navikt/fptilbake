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
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<Fagsystem>> fagsystemer,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<Venteårsak>> venteårsaker,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<Aktsomhet>> aktsomheter,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<AnnenVurdering>> annenVurderinger,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<SærligGrunn>> særligGrunner,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<VilkårResultat>> vilkårResultater,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<VedtakResultatType>> vedtakResultatTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<ForeldelseVurderingType>> foreldelseVurderingTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<HistorikkAktør>> historikkAktører,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<HendelseType>> hendelseTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<SkjermlenkeType>> skjermlenkeTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<HendelseUnderType>> hendelseUnderTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<BehandlingResultatType>> behandlingResultatTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<VidereBehandling>> videreBehandlinger,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<VergeType>> vergeTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<VurderÅrsak>> vurderÅrsaker,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<BehandlingÅrsakType>> behandlingÅrsakTyper,
    @NotNull @Size(min= 1, max=1000) SortedSet<@Valid KodeverdiSomObjekt<BehandlingType>> behandlingTyper
    ) {
}
