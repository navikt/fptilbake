package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktKode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.FatteVedtakDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer.AksjonspunktOppdaterer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@ApplicationScoped
public class AksjonspunktApplikasjonTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;

    AksjonspunktApplikasjonTjeneste() {
        // For CDI
    }

    @Inject
    public AksjonspunktApplikasjonTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                           BehandlingskontrollProvider behandlingskontrollProvider) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollProvider.getBehandlingskontrollTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollProvider.getBehandlingskontrollAsynkTjeneste();
    }

    public void bekreftAksjonspunkter(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);

        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        setAnsvarligSaksbehandler(bekreftedeAksjonspunktDtoer, behandling);

        spolTilbakeTilTidligsteAksjonspunkt(bekreftedeAksjonspunktDtoer, kontekst);

        bekreftAksjonspunkter(kontekst, bekreftedeAksjonspunktDtoer, behandling);

        if (behandling.isBehandlingPåVent()) {
            // Skal ikke fortsette behandling dersom behandling ble satt på vent
            return;
        }
        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
    }

    private void spolTilbakeTilTidligsteAksjonspunkt(Collection<? extends AksjonspunktKode> aksjonspunktDtoer,
                                                     BehandlingskontrollKontekst kontekst) {
        // Her sikres at behandlingskontroll hopper tilbake til aksjonspunktenes tidligste "løsesteg" dersom aktivt
        // behandlingssteg er lenger fremme i sekvensen
        var bekreftedeApKoder = aksjonspunktDtoer.stream()
                .map(AksjonspunktKode::getAksjonspunktDefinisjon).toList();

        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, bekreftedeApKoder);
    }

    private void bekreftAksjonspunkter(BehandlingskontrollKontekst kontekst,
                                       Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Behandling behandling) {

        List<Aksjonspunkt> utførteAksjonspunkter = new ArrayList<>();

        bekreftedeAksjonspunktDtoer.forEach(dto -> bekreftAksjonspunkt(behandling, utførteAksjonspunkter, dto));

        behandlingskontrollTjeneste.oppdaterBehandling(behandling, kontekst);

        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(kontekst, behandling.getAktivtBehandlingSteg(), utførteAksjonspunkter);
    }

    private void bekreftAksjonspunkt(Behandling behandling, List<Aksjonspunkt> utførteAksjonspunkter, BekreftetAksjonspunktDto dto) {
        var aksjonspunktDefinisjon = dto.getAksjonspunktDefinisjon();
        var aksjonspunkt = behandling.getAksjonspunktFor(aksjonspunktDefinisjon);

        var instance = finnAksjonspunktOppdaterer(dto.getClass());

        if (instance.isUnsatisfied()) {
            throw new TekniskException("FPT-770743", String.format("Finner ikke håndtering for aksjonspunkt med kode: %s", dto.getAksjonspunktDefinisjon().getKode()));
        } else {
            var minInstans = instance.get();
            if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                        "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
            }

            @SuppressWarnings("unchecked") var oppdaterer = (AksjonspunktOppdaterer<BekreftetAksjonspunktDto>) minInstans;
            oppdaterer.oppdater(dto, behandling);
        }

        if (!aksjonspunkt.erBehandletAksjonspunkt() && !aksjonspunkt.erAvbrutt()) {
            utførteAksjonspunkter.add(aksjonspunkt);
        }
    }

    private Instance<Object> finnAksjonspunktOppdaterer(Class<?> dtoClass) {
        return finnAdapter(dtoClass);
    }

    private Instance<Object> finnAdapter(Class<?> cls) {
        var cdi = CDI.current();
        var instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, AksjonspunktOppdaterer.class));

        // hvis unsatisfied, søk parent
        while (instance.isUnsatisfied() && !Objects.equals(Object.class, cls)) {
            cls = cls.getSuperclass();
            instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, AksjonspunktOppdaterer.class));
            if (!instance.isUnsatisfied()) {
                return instance;
            }
        }
        return instance;
    }


    private void setAnsvarligSaksbehandler(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Behandling behandling) {
        if (bekreftedeAksjonspunktDtoer.stream().anyMatch(FatteVedtakDto.class::isInstance)) {
            return;
        }
        behandling.setAnsvarligSaksbehandler(getCurrentUserId());
    }

    private String getCurrentUserId() {
        return KontekstHolder.getKontekst().getUid();
    }
}
