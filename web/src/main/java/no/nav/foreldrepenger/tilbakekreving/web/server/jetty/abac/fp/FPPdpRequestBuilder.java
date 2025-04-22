package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipRepository;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

/**
 * Implementasjon av PDP request for fptilbake.
 */
@ApplicationScoped
public class FPPdpRequestBuilder implements PdpRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FPPdpRequestBuilder.class);

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private PipRepository pipRepository;
    private FpsakPipKlient fpsakPipKlient;

    FPPdpRequestBuilder() {
        // For CDI proxy
    }

    @Inject
    public FPPdpRequestBuilder(PipRepository pipRepository, FpsakPipKlient fpsakPipKlient) {
        this.pipRepository = pipRepository;
        this.fpsakPipKlient = fpsakPipKlient;
    }

    @Override
    public  AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        var behandlingData = utledBehandlingData(dataAttributter, true);
        var saksnummer = utledSaksnummer(dataAttributter, behandlingData);
        setLogContext(saksnummer, behandlingData);

        var auditAktørId = utledAuditAktørId(dataAttributter, behandlingData, saksnummer);

        var ressursData = AppRessursData.builder()
            .medAuditIdent(auditAktørId)
            .leggTilAktørIdSet(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilFødselsnumre(dataAttributter.getVerdier(StandardAbacAttributtType.FNR));

        saksnummer.ifPresent(ressursData::medSaksnummer);
        ressursData.medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING);
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::statusForBehandling).ifPresent(ressursData::medBehandlingStatus);
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::ansvarligSaksbehandler).ifPresent(ressursData::medAnsvarligSaksbehandler);

        return ressursData.build();
    }

    @Override
    public  AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        var behandlingData = utledBehandlingData(dataAttributter, false);
        var saksnummer = utledSaksnummer(dataAttributter, behandlingData);

        setLogContext(saksnummer, behandlingData);

        var ressursData = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING);
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::statusForBehandling).ifPresent(ressursData::medBehandlingStatus);
        return ressursData.build();
    }

    private static void setLogContext(Optional<String> saksnummer, FpPipBehandlingInfo data) {
        LOG_CONTEXT.remove("behandlingId");
        LOG_CONTEXT.remove("behandling");
        LOG_CONTEXT.remove("fagsak");

        saksnummer.ifPresent(s -> LOG_CONTEXT.add("fagsak", s));
        Optional.ofNullable(data).map(FpPipBehandlingInfo::behandlingUuid).ifPresent(bd -> LOG_CONTEXT.add("behandling", bd.toString()));
        Optional.ofNullable(data).map(FpPipBehandlingInfo::behandlingId).ifPresent(bd -> LOG_CONTEXT.add("behandlingId", String.valueOf(bd)));
    }

    private FpPipBehandlingInfo utledBehandlingData(AbacDataAttributter dataAttributter, boolean sjekkFpsak) {
        Set<Long> behandlingId = new HashSet<>(dataAttributter.getVerdier(TilbakekrevingAbacAttributtType.BEHANDLING_ID));
        Set<UUID> behandlingUuid = new HashSet<>(dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID));
        Set<UUID> fpsakBehandlingId = new HashSet<>(dataAttributter.getVerdier(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID));
        if (behandlingId.size() + behandlingUuid.size() + fpsakBehandlingId.size() > 1) {
            Set<String> behandlinger = new HashSet<>(behandlingUuid.stream().map(UUID::toString).toList());
            behandlinger.addAll(behandlingId.stream().map(String::valueOf).toList());
            behandlinger.addAll(fpsakBehandlingId.stream().map(UUID::toString).toList());
            var tekst = String.format("Ugyldig request. Støtter bare 0 eller 1 behandling, men har %s", behandlinger);
            LOG.warn(tekst);
            throw new TekniskException("FPT-426124", tekst);
        }

        if (!behandlingId.isEmpty()) {
            return lagBehandlingData(behandlingId.stream().findFirst().orElseThrow());
        } else if (!behandlingUuid.isEmpty()) {
            return lagBehandlingData(behandlingUuid.stream().findFirst().orElseThrow());
        } else if (!fpsakBehandlingId.isEmpty()) {
            var behandling = fpsakBehandlingId.stream().findFirst().orElseThrow();
            var sak = sjekkFpsak ? fpsakPipKlient.saksnummerForBehandling(behandling) : null;
            return new FpPipBehandlingInfo(null, sak, null, behandling, PipBehandlingStatus.UTREDES, null);
        } else {
            return null;
        }
    }

    private Optional<String> utledSaksnummer(AbacDataAttributter attributter, FpPipBehandlingInfo behandlingData) {
        Set<String> saksnumre = new HashSet<>(attributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER));
        Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::saksnummer).ifPresent(saksnumre::add);
        if (saksnumre.size() > 1) {
            var tekst = String.format("Ugyldig request. Støtter bare 0 eller 1 saker, men har %s", saksnumre);
            LOG.warn(tekst);
            throw new TekniskException("FPT-426124", tekst);
        }
        return saksnumre.stream().findFirst();
    }

    private String utledAuditAktørId(AbacDataAttributter attributter, FpPipBehandlingInfo behandlingData, Optional<String> saksnummer) {
        Set<String> aktørIdVerdier = attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID);
        Set<String> personIdentVerdier = attributter.getVerdier(StandardAbacAttributtType.FNR);

        return Optional.ofNullable(behandlingData).map(FpPipBehandlingInfo::aktørId).map(PipAktørId::getVerdi)
            .or(() -> aktørIdVerdier.stream().findFirst())
            .or(() -> personIdentVerdier.stream().findFirst())
            .or(() -> saksnummer.flatMap(pipRepository::hentAktørIdSomEierFagsak).map(AktørId::getId))
            .orElse(null);
    }

    private FpPipBehandlingInfo lagBehandlingData(Long behandlingId) {
        return pipRepository.hentBehandlingData(behandlingId)
            .map(FpPipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(String.valueOf(behandlingId)));
    }

    private FpPipBehandlingInfo lagBehandlingData(UUID behandlingUuid) {
        return pipRepository.hentBehandlingData(behandlingUuid)
            .map(FpPipBehandlingInfo::new)
            .orElseThrow(() -> fantIkkeBehandling(behandlingUuid.toString()));
    }


    private static TekniskException fantIkkeBehandling(String behandlingRef) {
        return new TekniskException("FPT-426125", String.format("Ugyldig input. Fant ikke behandlingId %s", behandlingRef));
    }

}
