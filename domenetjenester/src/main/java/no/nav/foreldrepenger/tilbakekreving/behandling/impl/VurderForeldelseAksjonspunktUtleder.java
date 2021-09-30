package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class VurderForeldelseAksjonspunktUtleder {

    private Period antallMånederFrist;
    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingRepository behandlingRepository;

    VurderForeldelseAksjonspunktUtleder() {
        // For CDI
    }

    @Inject
    public VurderForeldelseAksjonspunktUtleder(@KonfigVerdi(value = "foreldelse.antallmaaneder", defaultVerdi = "P30M") Period antallMånederFrist, KravgrunnlagRepository grunnlagRepository,
                                               BehandlingRepository behandlingRepository) {
        this.antallMånederFrist = antallMånederFrist;
        this.grunnlagRepository = grunnlagRepository;
        this.behandlingRepository = behandlingRepository;
    }

    public Optional<AksjonspunktDefinisjon> utledAksjonspunkt(Long behandlingId) {
        // Henter perioder og vurderer foreldelse
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        for (KravgrunnlagPeriode432 grunnlagPeriode : kravgrunnlag.getPerioder()) {
            if (erForeldet(LocalDate.now(), grunnlagPeriode.getFom())) {
                return Optional.of(AksjonspunktDefinisjon.VURDER_FORELDELSE);
            }
        }

        // Hvis årsak til revurdering er RE_OPPLYSNINGER_OM_FORELDELSE, foreldelse aksjonpunkt opprettet uansett av grunnlag periode
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (sjekkHvisNyeOpplysningerOmForeldelseFinnes(behandling)) {
            return Optional.of(AksjonspunktDefinisjon.VURDER_FORELDELSE);
        }
        return Optional.empty();
    }

    public boolean erForeldet(LocalDate dagensDato, LocalDate fradato) {
        LocalDate frist = dagensDato.minus(antallMånederFrist);
        return fradato.isBefore(frist);
    }

    private boolean sjekkHvisNyeOpplysningerOmForeldelseFinnes(Behandling behandling) {

        if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType())) {
            BehandlingÅrsak behandlingÅrsak = behandling.getBehandlingÅrsaker().get(0);
            if (BehandlingÅrsakType.RE_OPPLYSNINGER_OM_FORELDELSE.equals(behandlingÅrsak.getBehandlingÅrsakType())) {
                return true;
            }
        }
        return false;
    }
}
