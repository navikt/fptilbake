package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.AvklartVergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AvklartVergeDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklartVergeDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklartVergeOppdaterer implements AksjonspunktOppdaterer<AvklartVergeDto> {

    private static final Logger logger = LoggerFactory.getLogger(AvklartVergeOppdaterer.class);
    private AvklartVergeTjeneste avklartVergeTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    public AvklartVergeOppdaterer(AvklartVergeTjeneste avklartVergeTjeneste,
                                  BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.avklartVergeTjeneste = avklartVergeTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void oppdater(AvklartVergeDto avklartVergeDto, Behandling behandling) {
        avklartVergeTjeneste.lagreVergeInformasjon(behandling.getId(), avklartVergeDto.getVergeFakta());
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        Optional<BehandlingStegTilstand> tilbakeførtBehandlingStegTilstand = behandling.getBehandlingStegTilstandHistorikk()
            .filter(behandlingStegTilstand -> behandlingStegTilstand.getBehandlingStegStatus().equals(BehandlingStegStatus.TILBAKEFØRT)).findFirst();
        if (tilbakeførtBehandlingStegTilstand.isPresent()) {
            BehandlingStegType tilbakeførtBehandlingSteg = tilbakeførtBehandlingStegTilstand.get().getBehandlingSteg();
            if (!tilbakeførtBehandlingSteg.equals(BehandlingStegType.FAKTA_FEILUTBETALING)) {
                behandlingskontrollTjeneste.behandlingFramføringTilSenereBehandlingSteg(kontekst, tilbakeførtBehandlingSteg);
            }
        }
    }
}
