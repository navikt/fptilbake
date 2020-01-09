package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.TilbakekrevingsvedtakMarshaller;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiKvitteringTolk;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlSendt;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@ApplicationScoped
public class AvstemmingTjeneste {

    private Logger logger = LoggerFactory.getLogger(AvstemmingTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private ØkonomiSendtXmlRepository sendtXmlRepository;
    private AktørConsumer aktørConsumer;

    AvstemmingTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingTjeneste(ØkonomiSendtXmlRepository sendtXmlRepository, BehandlingRepositoryProvider behandlingRepositoryProvider, AktørConsumer aktørConsumer) {
        this.sendtXmlRepository = sendtXmlRepository;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.aktørConsumer = aktørConsumer;
    }

    public String oppsummer(LocalDate dato) {
        Collection<ØkonomiXmlSendt> sendteVedtak = sendtXmlRepository.finn(MeldingType.VEDTAK, dato);
        AktørIdFnrMapper mapper = lagMapperForAktørIdent(sendteVedtak);
        AvstemmingCsvFormatter avstemmingCsvFormatter = new AvstemmingCsvFormatter();
        for (ØkonomiXmlSendt sendtVedtak : sendteVedtak) {
            if (erSendtOK(sendtVedtak)) {
                leggTilAvstemmingsdataForVedtaket(avstemmingCsvFormatter, mapper, sendtVedtak);
            }
        }
        logger.info("Avstemmingdata for {} ble hentet. Av {} sendte meldinger var {} med OK kvittering og kan sendes til avstemming", dato, sendteVedtak.size(), avstemmingCsvFormatter.getAntallRader());
        return avstemmingCsvFormatter.getData();
    }

    private void leggTilAvstemmingsdataForVedtaket(AvstemmingCsvFormatter avstemmingCsvFormatter, AktørIdFnrMapper mapper, ØkonomiXmlSendt sendtVedtak) {
        TilbakekrevingsvedtakOppsummering oppsummering = oppsummer(sendtVedtak);
        Long behandlingId = sendtVedtak.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingVedtak behandlingVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId).orElseThrow();

        avstemmingCsvFormatter.leggTilRad(AvstemmingCsvFormatter.radBuilder()
            .medAvsender("fptilbake")
            .medVedtakId(oppsummering.getØkonomiVedtakId())
            .medFnr(mapper.getFnr(behandling.getAktørId()))
            .medVedtaksdato(behandlingVedtak.getVedtaksdato())
            .medFagsakYtelseType(behandling.getFagsak().getFagsakYtelseType())
            .medTilbakekrevesBruttoUtenRenter(oppsummering.getTilbakekrevesBruttoUtenRenter())
            .medTilbakekrevesNettoUtenRenter(oppsummering.getTilbakekrevesNettoUtenRenter())
            .medSkatt(oppsummering.getSkatt())
            .medRenter(oppsummering.getRenter())
            .medErOmgjøringTilIngenTilbakekreving(erOmgjøringTilIngenTilbakekreving(oppsummering, behandling))
        );
    }

    private AktørIdFnrMapper lagMapperForAktørIdent(Collection<ØkonomiXmlSendt> sendteMeldinger) {
        List<AktørId> aktørIds = finnAktørIDer(sendteMeldinger);
        List<AktoerIder> identer = aktørIds.isEmpty()
            ? Collections.emptyList()
            : aktørConsumer.hentPersonIdenterForAktørIder(aktørIds.stream().map(AktørId::getId).collect(Collectors.toSet()));
        return new AktørIdFnrMapper(identer);
    }

    private boolean erOmgjøringTilIngenTilbakekreving(TilbakekrevingsvedtakOppsummering oppsummering, Behandling behandling) {
        return behandling.getType().equals(BehandlingType.REVURDERING_TILBAKEKREVING) && oppsummering.getTilbakekrevesBruttoUtenRenter().signum() == 0;
    }

    static class AktørIdFnrMapper {

        private List<AktoerIder> mapping;

        public AktørIdFnrMapper(List<AktoerIder> mapping) {
            this.mapping = mapping;
        }

        public String getFnr(AktørId aktørId) {
            for (AktoerIder id : mapping) {
                if (id.getAktoerId().equalsIgnoreCase(aktørId.getId())) {
                    return id.getGjeldendeIdent().getTpsId();
                }
            }
            throw new IllegalArgumentException("Fant ikke aktørId i mapping");
        }
    }

    private List<AktørId> finnAktørIDer(Collection<ØkonomiXmlSendt> sendteMeldinger) {
        List<AktørId> aktørIder = new ArrayList<>();
        for (ØkonomiXmlSendt sentMelding : sendteMeldinger) {
            Behandling behandling = behandlingRepository.hentBehandling(sentMelding.getBehandlingId());
            aktørIder.add(behandling.getAktørId());
        }
        return aktørIder;
    }

    private static boolean erSendtOK(ØkonomiXmlSendt melding) {
        String kvitteringXml = melding.getKvittering();
        if (kvitteringXml == null) {
            return false;
        }
        MmelDto kvittering = ØkonomiResponsMarshaller.unmarshall(kvitteringXml);
        return ØkonomiKvitteringTolk.erKvitteringOK(kvittering);
    }

    private TilbakekrevingsvedtakOppsummering oppsummer(ØkonomiXmlSendt sendtMelding) {
        TilbakekrevingsvedtakDto melding = TilbakekrevingsvedtakMarshaller.unmarshall(sendtMelding.getMelding());
        return TilbakekrevingsvedtakOppsummering.oppsummer(melding);
    }


}
