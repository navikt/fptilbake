package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.avstemming.IverksattVedtak;
import no.nav.foreldrepenger.tilbakekreving.avstemming.IverksattVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiKvitteringTolk;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsbelopDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsperiodeDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@ProsessTask(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendØkonomiTibakekerevingsVedtakTask implements ProsessTaskHandler {
    public static final String TASKTYPE = "iverksetteVedtak.sendØkonomiTilbakekrevingsvedtak";

    private static final Logger log = LoggerFactory.getLogger(SendØkonomiTibakekerevingsVedtakTask.class);

    private ØkonomiConsumer økonomiConsumer;

    private EntityManager entityManager;
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;
    private IverksattVedtakRepository iverksattVedtakRepository;

    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;

    SendØkonomiTibakekerevingsVedtakTask() {
        // CDI krav
    }

    @Inject
    public SendØkonomiTibakekerevingsVedtakTask(TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                                ØkonomiConsumer økonomiConsumer,
                                                ØkonomiSendtXmlRepository økonomiSendtXmlRepository,
                                                IverksattVedtakRepository iverksattVedtakRepository) {
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
        this.iverksattVedtakRepository = iverksattVedtakRepository;
        this.entityManager = økonomiSendtXmlRepository.getEntityManager();
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = prosessTaskData.getBehandlingId();
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        Long sendtXmlId = lagreXml(behandlingId, tilbakekrevingsvedtak);
        lagSavepointOgIverksett(behandlingId, sendtXmlId, tilbakekrevingsvedtak);
    }

    private void lagSavepointOgIverksett(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        RunWithSavepoint runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            iverksett(behandlingId, sendtXmlId, tilbakekrevingsvedtak);
            return null;
        });
    }

    private void iverksett(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        IverksattVedtak iverksattVedtak = klargjørIverksettelsesinformasjon(behandlingId, tilbakekrevingsvedtak);

        TilbakekrevingsvedtakResponse respons = økonomiConsumer.iverksettTilbakekrevingsvedtak(behandlingId, tilbakekrevingsvedtak);

        //HUSK kode etter kall til økonomiConsumer.iverksettTilbakekrevingsvedtak skal ha helt minimal sjangse for å feile
        //flytt alt som er mulig til før dette kallet, eller til senere prosesstask eller lignende

        oppdatereRespons(behandlingId, sendtXmlId, respons);
        if (respons != null && ØkonomiKvitteringTolk.erKvitteringOK(respons.getMmel())) {
            iverksattVedtakRepository.lagre(iverksattVedtak);
        }
    }

    private IverksattVedtak klargjørIverksettelsesinformasjon(long behandlingId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        BigDecimal bruttoUtenRenter = BigDecimal.ZERO;
        BigDecimal renter = BigDecimal.ZERO;
        BigDecimal skatt = BigDecimal.ZERO;
        for (TilbakekrevingsperiodeDto periode : tilbakekrevingsvedtak.getTilbakekrevingsperiode()) {
            renter = renter.add(periode.getBelopRenter());
            for (TilbakekrevingsbelopDto beløp : periode.getTilbakekrevingsbelop()) {
                bruttoUtenRenter = bruttoUtenRenter.add(beløp.getBelopTilbakekreves());
                skatt = skatt.add(beløp.getBelopSkatt());
            }
        }

        IverksattVedtak iverksattVedtak = IverksattVedtak.builder()
            .medBehandlingId(behandlingId)
            .medRenter(renter)
            .medSkatt(skatt)
            .medTilbakekrevesBruttoUtenRenter(bruttoUtenRenter)
            .medTilbakekrevesNettoUtenRenter(bruttoUtenRenter.subtract(skatt))
            .medIverksattDato(FPDateUtil.iDag())
            .medØkonomiVedtakId(tilbakekrevingsvedtak.getVedtakId())
            .build();
        return iverksattVedtak;

    }

    private Long lagreXml(Long behandlingId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        String xml = TilbakekrevingsvedtakMarshaller.marshall(behandlingId, tilbakekrevingsvedtak);
        Long sendtXmlId = økonomiSendtXmlRepository.lagre(behandlingId, xml, MeldingType.VEDTAK);
        log.info("lagret vedtak-xml for behandling={}", behandlingId);
        return sendtXmlId;
    }

    private void oppdatereRespons(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakResponse respons) {
        String responsXml = ØkonomiResponsMarshaller.marshall(behandlingId, respons);
        økonomiSendtXmlRepository.oppdatereKvittering(sendtXmlId, responsXml);
        log.info("oppdatert respons-xml for behandling={}", behandlingId);
    }

    public interface SendØkonomiTilbakekrevingVedtakTaskFeil extends DeklarerteFeil {

        SendØkonomiTilbakekrevingVedtakTaskFeil FACTORY = FeilFactory.create(SendØkonomiTilbakekrevingVedtakTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-113616", feilmelding = "Kunne ikke marshalle vedtak. BehandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeMarshalleVedtakXml(Long behandlingId, Exception e);

    }


}
