package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import javax.enterprise.context.Dependent;
import javax.xml.ws.WebServiceException;
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
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.exception.IntegrasjonException;

//TODO denne klassen bør ha deafult scope
@Dependent
public class ØkonomiConsumerImpl implements ØkonomiConsumer {

    private static final String SERVICE_IDENTIFIER = "TilbakekrevingServiceV1";
    private static final Logger logger = LoggerFactory.getLogger(ØkonomiConsumerImpl.class);

    private TilbakekrevingPortType port;

    private ØkonomiConsumerImpl() {
        // CDI only
    }

    public ØkonomiConsumerImpl(TilbakekrevingPortType port) {
        this.port = port;
    }

    @Override
    public TilbakekrevingsvedtakResponse iverksettTilbakekrevingsvedtak(Long behandlingId, TilbakekrevingsvedtakRequest vedtak) {
        try {
            return port.tilbakekrevingsvedtak(vedtak);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e);
        }
    }

    @Override
    public DetaljertKravgrunnlagDto hentKravgrunnlag(Long behandlingId, HentKravgrunnlagDetaljDto kravgrunnlagDetalj) {
        KravgrunnlagHentDetaljResponse respons = hentGrunnlagRespons(kravgrunnlagDetalj);
        MmelDto kvittering = respons.getMmel();
        Long kravgrunnlagId = kravgrunnlagDetalj.getKravgrunnlagId().longValue();
        validerKvitteringForHentGrunnlag(behandlingId, kravgrunnlagId, kvittering);
        logger.info("Hentet kravgrunnlag fra oppdragsystemet for behandlingId={} KravgrunnlagId={} Alvorlighetsgrad='{}' kodeMelding='{}' infomelding='{}'",
            behandlingId,
            kravgrunnlagDetalj.getKravgrunnlagId(),
            kvittering.getAlvorlighetsgrad(),
            kvittering.getKodeMelding(),
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


    private KravgrunnlagHentDetaljResponse hentGrunnlagRespons(HentKravgrunnlagDetaljDto kravgrunnlagDetalj) {
        KravgrunnlagHentDetaljRequest hentKravgrunnlagRequest = new KravgrunnlagHentDetaljRequest();
        hentKravgrunnlagRequest.setHentkravgrunnlag(kravgrunnlagDetalj);
        try {
            return port.kravgrunnlagHentDetalj(hentKravgrunnlagRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e);
        }
    }

    private KravgrunnlagAnnulerResponse anullereGrunnlag(AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        KravgrunnlagAnnulerRequest annulerRequest = new KravgrunnlagAnnulerRequest();
        annulerRequest.setAnnullerkravgrunnlag(annullerKravgrunnlag);
        try {
            return port.kravgrunnlagAnnuler(annulerRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e);
        }
    }

    private void validerKvitteringForHentGrunnlag(Long behandlingId, Long kravgrunnlagId, MmelDto mmel) {
        if (!ØkonomiKvitteringTolk.erKvitteringOK(mmel)) {
            throw ØkonomiConsumerFeil.fikkFeilkodeVedHentingAvKravgrunnlag(behandlingId, ØkonomiConsumerFeil.formaterKvittering(mmel));
        } else if (ØkonomiKvitteringTolk.erKravgrunnlagetIkkeFinnes(mmel)) {
            throw ØkonomiConsumerFeil.fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagIkkeFinnes(behandlingId, kravgrunnlagId, ØkonomiConsumerFeil.formaterKvittering(mmel));
        } else if (ØkonomiKvitteringTolk.erKravgrunnlagetSperret(mmel)) {
            throw ØkonomiConsumerFeil.fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagErSperret(behandlingId, kravgrunnlagId, ØkonomiConsumerFeil.formaterKvittering(mmel));
        } else if (ØkonomiKvitteringTolk.harKravgrunnlagNoeUkjentFeil(mmel)) {
            throw ØkonomiConsumerFeil.fikkUkjentFeilkodeVedHentingAvKravgrunnlag(behandlingId, kravgrunnlagId, ØkonomiConsumerFeil.formaterKvittering(mmel));
        }
    }

    private void validerKvitteringForAnnulereGrunnlag(Long behandlingId, MmelDto mmel) {
        if (!ØkonomiKvitteringTolk.erKvitteringOK(mmel)) {
            throw ØkonomiConsumerFeil.fikkFeilkodeVedAnnulereKravgrunnlag(behandlingId, ØkonomiConsumerFeil.formaterKvittering(mmel));
        }
    }

    private static class SoapWebServiceFeil {

        static IntegrasjonException soapFaultIwebserviceKall(String webservice, WebServiceException e) {
            return new IntegrasjonException("F-942048", String.format("SOAP tjenesten [ %s ] returnerte en SOAP Fault:", webservice), e);
        }
    }
}
