package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagAnnulerRequest;
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagAnnulerResponse;
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagHentDetaljRequest;
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagHentDetaljResponse;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingPortType;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

//TODO denne klassen bør ha deafult scope
public class ØkonomiConsumerImpl implements ØkonomiConsumer {

    private static final String SERVICE_IDENTIFIER = "TilbakekrevingServiceV1";
    private static final Logger logger = LoggerFactory.getLogger(ØkonomiConsumerImpl.class);
    private static final Set<String> KVITTERING_OK_KODE = Set.of("00", "04");

    private TilbakekrevingPortType port;

    public ØkonomiConsumerImpl(TilbakekrevingPortType port) {
        this.port = port;
    }

    @Override
    public void iverksettTilbakekrevingsvedtak(Long behandlingId, TilbakekrevingsvedtakDto vedtak) {
        TilbakekrevingsvedtakResponse respons = iverksett(vedtak);
        MmelDto kvittering = respons.getMmel();
        validerKvitteringForIverksettelse(behandlingId, kvittering);

        //hvis kvittering er OK, er alt være OK. Ikke noen grunn til å lagre resultat.
        logger.info("Tilbakekrevingsvedtak sendt til oppdragsystemet. BehandlingId={} Alvorlighetsgrad='{}' infomelding='{}'",
            behandlingId,
            kvittering.getAlvorlighetsgrad(),
            kvittering.getBeskrMelding());
    }

    @Override
    public DetaljertKravgrunnlagDto hentKravgrunnlag(Long behandlingId, HentKravgrunnlagDetaljDto kravgrunnlagDetalj) {
        KravgrunnlagHentDetaljResponse respons = hentGrunnlagRespons(kravgrunnlagDetalj);
        MmelDto kvittering = respons.getMmel();
        validerKvitteringForHentGrunnlag(behandlingId, kvittering);
        logger.info("Hentet kravgrunnlag fra oppdragsystemet for behandlingId={} Alvorlighetsgrad='{}' infomelding='{}'",
            behandlingId,
            kvittering.getAlvorlighetsgrad(),
            kvittering.getBeskrMelding());
        return respons.getDetaljertkravgrunnlag();
    }

    @Override
    public void anullereKravgrunnlag(Long behandlingId, AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        logger.info("Starter Anullerekravgrunnlag for behandlingId={}", behandlingId);
        KravgrunnlagAnnulerResponse respons = anullereGrunnlag(annullerKravgrunnlag);
        MmelDto kvittering = respons.getMmel();
        validerKvitteringForAnnulereGrunnlag(behandlingId, kvittering);
        logger.info("AnnulereKravgrunnlag sendt til oppdragssystemet. BehandlingId={} Alvorlighetsgrad='{}' infomelding='{}'",
            behandlingId,
            kvittering.getAlvorlighetsgrad(),
            kvittering.getBeskrMelding());
    }


    private TilbakekrevingsvedtakResponse iverksett(TilbakekrevingsvedtakDto vedtak) {
        TilbakekrevingsvedtakRequest request = new TilbakekrevingsvedtakRequest();
        request.setTilbakekrevingsvedtak(vedtak);
        try {
            return port.tilbakekrevingsvedtak(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private KravgrunnlagHentDetaljResponse hentGrunnlagRespons(HentKravgrunnlagDetaljDto kravgrunnlagDetalj) {
        KravgrunnlagHentDetaljRequest hentKravgrunnlagRequest = new KravgrunnlagHentDetaljRequest();
        hentKravgrunnlagRequest.setHentkravgrunnlag(kravgrunnlagDetalj);
        try {
            return port.kravgrunnlagHentDetalj(hentKravgrunnlagRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private KravgrunnlagAnnulerResponse anullereGrunnlag(AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        KravgrunnlagAnnulerRequest annulerRequest = new KravgrunnlagAnnulerRequest();
        annulerRequest.setAnnullerkravgrunnlag(annullerKravgrunnlag);
        try {
            return port.kravgrunnlagAnnuler(annulerRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private void validerKvitteringForIverksettelse(Long behandlingId, MmelDto mmel) {
        String alvorlighetsgrad = mmel.getAlvorlighetsgrad();
        if (!KVITTERING_OK_KODE.contains(alvorlighetsgrad)) {
            throw ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedIverksetting(behandlingId, ØkonomiConsumerFeil.formaterKvitterign(mmel)).toException();
        }
    }

    private void validerKvitteringForHentGrunnlag(Long behandlingId, MmelDto mmel) {
        String alvorlighetsgrad = mmel.getAlvorlighetsgrad();
        if (!KVITTERING_OK_KODE.contains(alvorlighetsgrad)) {
            throw ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedHentingAvKravgrunnlag(behandlingId, ØkonomiConsumerFeil.formaterKvitterign(mmel)).toException();
        }
    }

    private void validerKvitteringForAnnulereGrunnlag(Long behandlingId, MmelDto mmel) {
        String alvorlighetsgrad = mmel.getAlvorlighetsgrad();
        if (!KVITTERING_OK_KODE.contains(alvorlighetsgrad)) {
            throw ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedAnnulereKravgrunnlag(behandlingId, ØkonomiConsumerFeil.formaterKvitterign(mmel)).toException();
        }
    }

}
