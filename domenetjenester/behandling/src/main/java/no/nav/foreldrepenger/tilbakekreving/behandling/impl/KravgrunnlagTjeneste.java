package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class KravgrunnlagTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    KravgrunnlagTjeneste() {
        // For CDI
    }

    @Inject
    public KravgrunnlagTjeneste(KravgrunnlagRepository kravgrunnlagRepository, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste, BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431) {
        if (KravStatusKode.ENDRET.equals(kravgrunnlag431.getKravStatusKode())) {
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
            //forutsatt at FPTILBAKE allrede har fått SPER melding for den behandlingen og sett behandling på vent med VenteÅrsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
            behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,kontekst);
            behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.FAKTA_FEILUTBETALING);
        }
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
    }

}
