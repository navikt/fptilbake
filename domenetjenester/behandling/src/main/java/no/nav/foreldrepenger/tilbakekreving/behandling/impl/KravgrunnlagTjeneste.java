package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class KravgrunnlagTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;


    KravgrunnlagTjeneste() {
        // For CDI
    }

    @Inject
    public KravgrunnlagTjeneste(BehandlingRepositoryProvider repositoryProvider, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (KravStatusKode.ENDRET.equals(kravgrunnlag431.getKravStatusKode())) {
            boolean erStegPassert = behandlingskontrollTjeneste.erStegPassert(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
            //forutsatt at FPTILBAKE allrede har fått SPER melding for den behandlingen og sett behandling på vent med VenteÅrsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
            behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, kontekst);
            if (erStegPassert) {
                behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.FAKTA_FEILUTBETALING);
            }
            //Perioder knyttet med gammel grunnlag må slettes
            sletteGammelData(behandlingId);
        }
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
    }

    private void sletteGammelData(Long behandlingId) {
        faktaFeilutbetalingRepository.sletteFaktaFeilutbetaling(behandlingId);
        vurdertForeldelseRepository.sletteForeldelse(behandlingId);
        vilkårsvurderingRepository.sletteVilkårsvurdering(behandlingId);
    }

}
