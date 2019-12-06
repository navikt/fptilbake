package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

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

class ØkonomiConsumerImpl implements ØkonomiConsumer {

    private static final String SERVICE_IDENTIFIER = "TilbakekrevingServiceV1";
    private static final Logger logger = LoggerFactory.getLogger(ØkonomiConsumerImpl.class);

    private TilbakekrevingPortType port;

    public ØkonomiConsumerImpl(TilbakekrevingPortType port) {
        this.port = port;
    }

    @Override
    public TilbakekrevingsvedtakResponse iverksettTilbakekrevingsvedtak(Long behandlingId, TilbakekrevingsvedtakDto vedtak) {
        TilbakekrevingsvedtakResponse respons = iverksett(vedtak);
        MmelDto kvittering = respons.getMmel();
        if (ØkonomiKvitteringTolk.erKvitteringOK(kvittering)) {
            logger.info("Tilbakekrevingsvedtak sendt til oppdragsystemet. BehandlingId={} Alvorlighetsgrad='{}' infomelding='{}'", behandlingId, kvittering.getAlvorlighetsgrad(), kvittering.getBeskrMelding());
        } else {
            ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedIverksetting(behandlingId, ØkonomiConsumerFeil.formaterKvitterign(kvittering)).log(logger);
        }
        return respons;
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
    public MmelDto anullereKravgrunnlag(Long behandlingId, AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        logger.info("Starter Anullerekravgrunnlag for behandlingId={}", behandlingId);
        KravgrunnlagAnnulerResponse respons = anullereGrunnlag(annullerKravgrunnlag);
        MmelDto kvittering = respons.getMmel();
        validerKvitteringForAnnulereGrunnlag(behandlingId, kvittering);
        logger.info("AnnulereKravgrunnlag sendt til oppdragssystemet. BehandlingId={} Alvorlighetsgrad='{}' infomelding='{}'",
            behandlingId,
            kvittering.getAlvorlighetsgrad(),
            kvittering.getBeskrMelding());
        return kvittering;
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

    private void validerKvitteringForHentGrunnlag(Long behandlingId, MmelDto mmel) {
        if (!ØkonomiKvitteringTolk.erKvitteringOK(mmel)) {
            throw ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedHentingAvKravgrunnlag(behandlingId, ØkonomiConsumerFeil.formaterKvitterign(mmel)).toException();
        }
    }

    private void validerKvitteringForAnnulereGrunnlag(Long behandlingId, MmelDto mmel) {
        if (!ØkonomiKvitteringTolk.erKvitteringOK(mmel)) {
            throw ØkonomiConsumerFeil.FACTORY.fikkFeilkodeVedAnnulereKravgrunnlag(behandlingId, ØkonomiConsumerFeil.formaterKvitterign(mmel)).toException();
        }
    }

}
