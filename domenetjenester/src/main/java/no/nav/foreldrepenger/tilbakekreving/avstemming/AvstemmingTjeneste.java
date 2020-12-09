package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.TilbakekrevingsvedtakMarshaller;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiKvitteringTolk;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlSendt;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class AvstemmingTjeneste {

    private Logger logger = LoggerFactory.getLogger(AvstemmingTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private ØkonomiSendtXmlRepository sendtXmlRepository;
    private PersoninfoAdapter aktørConsumer;

    private String avsender;

    AvstemmingTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingTjeneste(@KonfigVerdi(value = "app.name") String applikasjon,
                              ØkonomiSendtXmlRepository sendtXmlRepository,
                              BehandlingRepositoryProvider behandlingRepositoryProvider,
                              PersoninfoAdapter aktørConsumer) {
        this.sendtXmlRepository = sendtXmlRepository;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.aktørConsumer = aktørConsumer;
        this.avsender = applikasjon;
    }

    public Optional<String> oppsummer(LocalDate dato) {
        Collection<ØkonomiXmlSendt> sendteVedtak = sendtXmlRepository.finn(MeldingType.VEDTAK, dato);
        AvstemmingCsvFormatter avstemmingCsvFormatter = new AvstemmingCsvFormatter();
        int antallFeilet = 0;
        int antallFørstegangsvedtakUtenTilbakekreving = 0;
        for (ØkonomiXmlSendt sendtVedtak : sendteVedtak) {
            if (!erSendtOK(sendtVedtak)) {
                antallFeilet++;
                continue;
            }
            Behandling behandling = behandlingRepository.hentBehandling(sendtVedtak.getBehandlingId());
            TilbakekrevingsvedtakOppsummering oppsummering = oppsummer(sendtVedtak);
            if (erFørstegangsvedtakUtenTilbakekreving(behandling, oppsummering)) {
                antallFørstegangsvedtakUtenTilbakekreving++;
                continue;
            }
            leggTilAvstemmingsdataForVedtaket(avstemmingCsvFormatter, behandling, oppsummering);

        }
        if (antallFeilet == 0) {
            logger.info("Avstemmer {}. Sender {} vedtak til avstemming. Totalt ble {} vedtak sendt til OS dette døgnet. {} førstegangsvedtak uten tilbakekreving sendes ikke til avstemming",
                dato, avstemmingCsvFormatter.getAntallRader(), sendteVedtak.size(), antallFørstegangsvedtakUtenTilbakekreving);
        } else {
            logger.warn("Avstemmer {}. Sender {} vedtak til avstemming. Totalt ble {} vedtak sendt til OS dette døgnet. {} førstegangsvedtak uten tilbakekreving sendes ikke til avstemming. {} vedtak fikk negativ kvittering fra OS og sendes ikke til avstemming",
                dato, avstemmingCsvFormatter.getAntallRader(), sendteVedtak.size(), antallFørstegangsvedtakUtenTilbakekreving, antallFeilet);
        }
        if (avstemmingCsvFormatter.getAntallRader() == 0) {
            return Optional.empty();
        }
        return Optional.of(avstemmingCsvFormatter.getData());
    }

    private boolean erFørstegangsvedtakUtenTilbakekreving(Behandling behandling, TilbakekrevingsvedtakOppsummering oppsummering) {
        return behandling.getType().equals(BehandlingType.TILBAKEKREVING) && oppsummering.harIngenTilbakekreving();
    }

    private void leggTilAvstemmingsdataForVedtaket(AvstemmingCsvFormatter avstemmingCsvFormatter, Behandling behandling, TilbakekrevingsvedtakOppsummering oppsummering) {
        Long behandlingId = behandling.getId();
        BehandlingVedtak behandlingVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId).orElseThrow();

        String fnr = aktørConsumer.hentFnrForAktør(behandling.getAktørId()).map(PersonIdent::getIdent)
            .orElseThrow(() -> new IllegalArgumentException("Avstemming feilet, fant ikke ident. Gjelder behandlingId=" + behandlingId));

        avstemmingCsvFormatter.leggTilRad(AvstemmingCsvFormatter.radBuilder()
            .medAvsender(avsender)
            .medVedtakId(oppsummering.getØkonomiVedtakId())
            .medFnr(fnr)
            .medVedtaksdato(behandlingVedtak.getVedtaksdato())
            .medFagsakYtelseType(behandling.getFagsak().getFagsakYtelseType())
            .medTilbakekrevesBruttoUtenRenter(oppsummering.getTilbakekrevesBruttoUtenRenter())
            .medTilbakekrevesNettoUtenRenter(oppsummering.getTilbakekrevesNettoUtenRenter())
            .medSkatt(oppsummering.getSkatt())
            .medRenter(oppsummering.getRenter())
            .medErOmgjøringTilIngenTilbakekreving(erOmgjøringTilIngenTilbakekreving(oppsummering, behandling))
        );
    }

    private boolean erOmgjøringTilIngenTilbakekreving(TilbakekrevingsvedtakOppsummering oppsummering, Behandling behandling) {
        return behandling.getType().equals(BehandlingType.REVURDERING_TILBAKEKREVING) && oppsummering.harIngenTilbakekreving();
    }

    private static boolean erSendtOK(ØkonomiXmlSendt melding) {
        String kvitteringXml = melding.getKvittering();
        if (kvitteringXml == null) {
            return false;
        }
        TilbakekrevingsvedtakResponse response = ØkonomiResponsMarshaller.unmarshall(kvitteringXml, melding.getBehandlingId(), melding.getId());
        return ØkonomiKvitteringTolk.erKvitteringOK(response);
    }

    private TilbakekrevingsvedtakOppsummering oppsummer(ØkonomiXmlSendt sendtMelding) {
        String xml = sendtMelding.getMelding();
        TilbakekrevingsvedtakRequest melding = TilbakekrevingsvedtakMarshaller.unmarshall(xml, sendtMelding.getBehandlingId(), sendtMelding.getId());
        return TilbakekrevingsvedtakOppsummering.oppsummer(melding.getTilbakekrevingsvedtak());
    }


}
