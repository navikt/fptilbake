package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class BehandlingRevurderingTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private FagsakRepository fagsakRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;

    BehandlingRevurderingTjeneste() {
        // for CDI
    }

    @Inject
    public BehandlingRevurderingTjeneste(BehandlingRepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
    }

    public Behandling opprettRevurdering(Saksnummer saksnummer, long eksternBehandlingId, String behandlingÅrsak) {

        BehandlingÅrsakType behandlingÅrsakType = hentBehandlingÅrsakType(behandlingÅrsak);

        Fagsak fagsak = fagsakRepository.hentEksaktFagsakForGittSaksnummer(saksnummer);

        validerHarIkkeÅpenBehandling(saksnummer, eksternBehandlingId);

        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(fagsak.getId(), FagsakStatus.UNDER_BEHANDLING);
        return opprettManuellRevurdering(fagsak, behandlingÅrsakType, eksternBehandlingId);
    }

    public boolean kanOppretteRevurdering(long eksternBehandlingId) {
        List<EksternBehandling> alleKnyttetBehandlinger = eksternBehandlingRepository.hentAlleBehandlingerMedEksternId(eksternBehandlingId);
        for (EksternBehandling eksternBehandling : alleKnyttetBehandlinger) {
            if (harÅpenBehandling(eksternBehandling.getInternId())) {
                return false;
            }
        }
        return true;
    }

    private BehandlingÅrsakType hentBehandlingÅrsakType(String behandlingÅrsak) {
        return kodeverkRepository.finn(BehandlingÅrsakType.class, behandlingÅrsak);
    }

    private Behandling opprettManuellRevurdering(Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType, long eksternBehandlingId) {
        EksternBehandling eksternBehandlingForSisteTbkBehandling = eksternBehandlingRepository.finnForSisteAvsluttetTbkBehandling(eksternBehandlingId)
            .orElseThrow(() -> RevurderingFeil.FACTORY.tjenesteFinnerIkkeBehandlingForRevurdering(fagsak.getId()).toException());

        Behandling origBehandling = behandlingRepository.hentBehandling(eksternBehandlingForSisteTbkBehandling.getInternId());

        Behandling revurdering = opprettRevurderingsBehandling(behandlingÅrsakType, origBehandling);
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);

        // revurdering skal starte med Fakta om feilutbetaling
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(revurdering, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);

        opprettRelasjonMedEksternBehandling(eksternBehandlingId, revurdering);

        // lag historikkinnslag for Revurdering opprettet
        lagHistorikkInnslagForOpprettetRevurdering(revurdering, behandlingÅrsakType);

        return revurdering;
    }

    private Behandling opprettRevurderingsBehandling(BehandlingÅrsakType behandlingÅrsakType, Behandling origBehandling) {
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.REVURDERING_TILBAKEKREVING);
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(behandlingÅrsakType)
            .medOriginalBehandling(origBehandling);
        Behandling revurdering = Behandling.fraTidligereBehandling(origBehandling, behandlingType)
            .medOpprettetDato(LocalDateTime.now())
            .medBehandlingÅrsak(revurderingÅrsak).build();
        return revurdering;
    }

    private void validerHarIkkeÅpenBehandling(Saksnummer saksnummer, long eksternBehandlingId) {
        if (!kanOppretteRevurdering(eksternBehandlingId)) {
            throw RevurderingFeil.FACTORY.kanIkkeOppretteRevurdering(saksnummer).toException();
        }
    }

    private boolean harÅpenBehandling(long origBehandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(origBehandlingId);
        return !behandling.erAvsluttet();
    }

    private void opprettRelasjonMedEksternBehandling(long eksternBehandlingId, Behandling revurdering) {
        EksternBehandling eksternBehandling = new EksternBehandling(revurdering, eksternBehandlingId);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    private void lagHistorikkInnslagForOpprettetRevurdering(Behandling behandling, BehandlingÅrsakType revurderingÅrsak) {
        Historikkinnslag revurderingsInnslag = new Historikkinnslag();

        revurderingsInnslag.setBehandling(behandling);
        revurderingsInnslag.setType(HistorikkinnslagType.REVURD_OPPR);
        revurderingsInnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);

        HistorikkInnslagTekstBuilder historiebygger = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.REVURD_OPPR)
            .medBegrunnelse(revurderingÅrsak);
        historiebygger.build(revurderingsInnslag);

        repositoryProvider.getHistorikkRepository().lagre(revurderingsInnslag);
    }

    interface RevurderingFeil extends DeklarerteFeil {
        RevurderingFeil FACTORY = FeilFactory.create(RevurderingFeil.class);

        @FunksjonellFeil(feilkode = "FPT-663487", feilmelding = "saksnummer %s oppfyller ikke kravene for revurdering", løsningsforslag = "", logLevel = LogLevel.WARN)
        Feil kanIkkeOppretteRevurdering(Saksnummer saksnummer);

        @TekniskFeil(feilkode = "FPT-317517", feilmelding = "finner ingen behandling som kan revurderes for fagsak: %s", logLevel = LogLevel.WARN)
        Feil tjenesteFinnerIkkeBehandlingForRevurdering(Long fagsakId);

    }
}
