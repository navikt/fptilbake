package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktApplikasjonFeil.FACTORY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktKode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.FatteVedtakDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer.AksjonspunktOppdaterer;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class AksjonspunktApplikasjonTjenesteImpl implements AksjonspunktApplikasjonTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;

    AksjonspunktApplikasjonTjenesteImpl() {
        // For CDI
    }

    @Inject
    public AksjonspunktApplikasjonTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                               BehandlingskontrollProvider behandlingskontrollProvider) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollProvider.getBehandlingskontrollTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollProvider.getBehandlingskontrollAsynkTjeneste();
    }

    @Override
    public void bekreftAksjonspunkter(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        setAnsvarligSaksbehandler(bekreftedeAksjonspunktDtoer, behandling);

        spolTilbakeTilTidligsteAksjonspunkt(bekreftedeAksjonspunktDtoer, kontekst, false);

        bekreftAksjonspunkter(kontekst, bekreftedeAksjonspunktDtoer, behandling);

        if (behandling.isBehandlingPåVent()) {
            // Skal ikke fortsette behandling dersom behandling ble satt på vent
            return;
        }
        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
    }

    private void spolTilbakeTilTidligsteAksjonspunkt(Collection<? extends AksjonspunktKode> aksjonspunktDtoer,
                                                     BehandlingskontrollKontekst kontekst,
                                                     boolean erOverstyring) {
        // Her sikres at behandlingskontroll hopper tilbake til aksjonspunktenes tidligste "løsesteg" dersom aktivt
        // behandlingssteg er lenger fremme i sekvensen
        List<String> bekreftedeApKoder = aksjonspunktDtoer.stream()
                .map(AksjonspunktKode :: getKode)
                .collect(toList());

        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, bekreftedeApKoder, erOverstyring);
    }

    private void bekreftAksjonspunkter(BehandlingskontrollKontekst kontekst,
                                       Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Behandling behandling) {

        List<Aksjonspunkt> utførteAksjonspunkter = new ArrayList<>();

        bekreftedeAksjonspunktDtoer.forEach(dto -> bekreftAksjonspunkt(behandling, utførteAksjonspunkter, dto));

        behandlingskontrollTjeneste.oppdaterBehandling(behandling, kontekst);

        behandlingskontrollTjeneste.aksjonspunkterUtført(kontekst, utførteAksjonspunkter, behandling.getAktivtBehandlingSteg());
    }

    private void bekreftAksjonspunkt(Behandling behandling, List<Aksjonspunkt> utførteAksjonspunkter, BekreftetAksjonspunktDto dto) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aksjonspunktDefinisjon);

        Instance<Object> instance = finnAksjonspunktOppdaterer(dto.getClass());

        if (instance.isUnsatisfied()) {
            throw FACTORY.kanIkkeFinneAksjonspunktUtleder(dto.getKode()).toException();
        } else {
            Object minInstans = instance.get();
            if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                        "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
            }

            @SuppressWarnings("unchecked")
            AksjonspunktOppdaterer<BekreftetAksjonspunktDto> oppdaterer = (AksjonspunktOppdaterer<BekreftetAksjonspunktDto>) minInstans;
            oppdaterer.oppdater(dto, behandling);
        }

        if (!aksjonspunkt.erBehandletAksjonspunkt() && !aksjonspunkt.erAvbrutt() && aksjonspunktRepository.setTilUtført(aksjonspunkt)) {
                utførteAksjonspunkter.add(aksjonspunkt);
        }
    }

    private Instance<Object> finnAksjonspunktOppdaterer(Class<?> dtoClass) {
        return finnAdapter(dtoClass, AksjonspunktOppdaterer.class);
    }

    private Instance<Object> finnAdapter(Class<?> cls, final Class<?> targetAdapter) {
        CDI<Object> cdi = CDI.current();
        Instance<Object> instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, targetAdapter));

        // hvis unsatisfied, søk parent
        while (instance.isUnsatisfied() && !Objects.equals(Object.class, cls)) {
            cls = cls.getSuperclass();
            instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, targetAdapter));
            if (!instance.isUnsatisfied()) {
                return instance;
            }
        }
        return instance;
    }


    private void setAnsvarligSaksbehandler(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Behandling behandling) {
        if (bekreftedeAksjonspunktDtoer.stream().anyMatch(dto -> dto instanceof FatteVedtakDto)) {
            return;
        }
        behandling.setAnsvarligSaksbehandler(getCurrentUserId());
    }

    private String getCurrentUserId() {
        return SubjectHandler.getSubjectHandler().getUid();
    }
}
