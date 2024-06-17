package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class BehandlingRevurderingTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    BehandlingRevurderingTjeneste() {
        // for CDI
    }

    @Inject
    public BehandlingRevurderingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                         BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public Behandling opprettRevurdering(Long tilbakekrevingBehandlingId, BehandlingÅrsakType behandlingÅrsakType, OrganisasjonsEnhet enhet,
                                         String opprettetAv) {
        Behandling tbkBehandling = behandlingRepository.hentBehandling(tilbakekrevingBehandlingId);
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(tbkBehandling.getId());
        Fagsak fagsak = tbkBehandling.getFagsak();
        Saksnummer saksnummer = fagsak.getSaksnummer();
        UUID eksternUuid = eksternBehandling.getEksternUuid();

        validerHarIkkeÅpenBehandling(saksnummer, eksternUuid);

        return opprettManuellRevurdering(fagsak, behandlingÅrsakType, eksternUuid, enhet, opprettetAv);
    }

    public boolean kanOppretteRevurdering(UUID eksternUuid) {
        List<EksternBehandling> alleKnyttetBehandlinger = eksternBehandlingRepository.hentAlleBehandlingerMedEksternUuid(eksternUuid);
        for (EksternBehandling eksternBehandling : alleKnyttetBehandlinger) {
            if (harÅpenBehandling(eksternBehandling.getInternId())) {
                return false;
            }
        }
        return true;
    }

    public boolean kanRevurderingOpprettes(Behandling behandling) {
        Optional<EksternBehandling> eksternBehandling = hentEksternBehandling(behandling.getId());
        return eksternBehandling.isPresent() && kanOppretteRevurdering(eksternBehandling.get().getEksternUuid());
    }

    public Optional<EksternBehandling> hentEksternBehandling(long behandlingId) {
        return eksternBehandlingRepository.hentOptionalFraInternId(behandlingId);
    }

    private Behandling opprettManuellRevurdering(Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType, UUID eksternUuid,
                                                 OrganisasjonsEnhet enhet, String opprettetAv) {
        EksternBehandling eksternBehandlingForSisteTbkBehandling = eksternBehandlingRepository.finnForSisteAvsluttetTbkBehandling(eksternUuid)
                .orElseThrow(() -> tjenesteFinnerIkkeBehandlingForRevurdering(fagsak.getId()));

        Behandling origBehandling = behandlingRepository.hentBehandling(eksternBehandlingForSisteTbkBehandling.getInternId());

        Henvisning henvisning = eksternBehandlingForSisteTbkBehandling.getHenvisning(); // henvisning må være samme som siste når vi opprette revurdering

        Behandling revurdering = opprettRevurderingsBehandling(behandlingÅrsakType, origBehandling, BehandlingType.REVURDERING_TILBAKEKREVING, enhet);
        if (opprettetAv != null && Fagsystem.FPTILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            revurdering.setAnsvarligSaksbehandler(opprettetAv);
        }
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(revurdering);
        behandlingskontrollTjeneste.opprettBehandling(kontekst, revurdering,
            beh -> eksternBehandlingRepository.lagre(new EksternBehandling(beh, henvisning, eksternUuid)));

        // revurdering skal starte med Fakta om feilutbetaling
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.FAKTA_FEILUTBETALING, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));

        kopierVergeInformasjon(origBehandling.getId(), revurdering.getId());

        // lag historikkinnslag for Revurdering opprettet
        lagHistorikkInnslagForOpprettetRevurdering(revurdering, behandlingÅrsakType);

        return revurdering;
    }

    private Behandling opprettRevurderingsBehandling(BehandlingÅrsakType behandlingÅrsakType, Behandling origBehandling,
                                                     BehandlingType behandlingType, OrganisasjonsEnhet enhet) {
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(behandlingÅrsakType)
                .medOriginalBehandling(origBehandling);
        Behandling revurdering = Behandling.fraTidligereBehandling(origBehandling, behandlingType)
                .medOpprettetDato(LocalDateTime.now())
                .medBehandlingÅrsak(revurderingÅrsak).build();
        revurdering.setBehandlendeOrganisasjonsEnhet(enhet);
        return revurdering;
    }

    private void validerHarIkkeÅpenBehandling(Saksnummer saksnummer, UUID eksternUuid) {
        if (!kanOppretteRevurdering(eksternUuid)) {
            throw kanIkkeOppretteRevurdering(saksnummer);
        }
    }

    private boolean harÅpenBehandling(long origBehandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(origBehandlingId);
        return !behandling.erAvsluttet();
    }

    private void kopierVergeInformasjon(long origBehandlingId, long behandlingId) {
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(origBehandlingId);
        if (vergeEntitet.isPresent()) {
            vergeRepository.lagreVergeInformasjon(behandlingId, vergeEntitet.get());
        }
    }

    private void lagHistorikkInnslagForOpprettetRevurdering(Behandling behandling, BehandlingÅrsakType revurderingÅrsak) {
        Historikkinnslag revurderingsInnslag = new Historikkinnslag();

        revurderingsInnslag.setBehandling(behandling);
        revurderingsInnslag.setType(HistorikkinnslagType.REVURD_OPPR);
        revurderingsInnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        HistorikkInnslagTekstBuilder historiebygger = new HistorikkInnslagTekstBuilder()
                .medHendelse(HistorikkinnslagType.REVURD_OPPR)
                .medBegrunnelse(revurderingÅrsak);
        historiebygger.build(revurderingsInnslag);

        repositoryProvider.getHistorikkRepository().lagre(revurderingsInnslag);
    }


    private static FunksjonellException kanIkkeOppretteRevurdering(Saksnummer saksnummer) {
        return new FunksjonellException("FPT-663487", String.format("saksnummer %s oppfyller ikke kravene for revurdering", saksnummer), "");
    }

    private static TekniskException tjenesteFinnerIkkeBehandlingForRevurdering(Long fagsakId) {
        return new TekniskException("FPT-317517", String.format("finner ingen behandling som kan revurderes for fagsak: %s", fagsakId));
    }

    // TEST ONLY
    public Behandling opprettRevurdering(Long tilbakekrevingBehandlingId, BehandlingÅrsakType behandlingÅrsakType) {
        Behandling tbkBehandling = behandlingRepository.hentBehandling(tilbakekrevingBehandlingId);
        return opprettRevurdering(tilbakekrevingBehandlingId, behandlingÅrsakType, tbkBehandling.getBehandlendeOrganisasjonsEnhet(), null);
    }

}
