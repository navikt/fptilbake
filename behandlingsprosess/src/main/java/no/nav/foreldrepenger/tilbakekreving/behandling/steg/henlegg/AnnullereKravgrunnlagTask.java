package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henlegg;

import java.math.BigInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ProsessTask(AnnullereKravgrunnlagTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class AnnullereKravgrunnlagTask extends FellesTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "kravgrunnlag.annulere";

    private static final Logger log = LoggerFactory.getLogger(AnnullereKravgrunnlagTask.class);

    private EntityManager entityManager;

    private KravgrunnlagRepository kravgrunnlagRepository;
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private ØkonomiConsumer økonomiConsumer;

    AnnullereKravgrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public AnnullereKravgrunnlagTask(KravgrunnlagRepository kravgrunnlagRepository, ProsessTaskRepository prosessTaskRepository,
                                     ØkonomiSendtXmlRepository økonomiSendtXmlRepository, ØkonomiConsumer økonomiConsumer) {
        super(prosessTaskRepository, kravgrunnlagRepository, null);
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
        this.økonomiConsumer = økonomiConsumer;
        this.entityManager = økonomiSendtXmlRepository.getEntityManager();
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        KravgrunnlagAggregate grunnlag = kravgrunnlagRepository.finnEksaktGrunnlagForBehandlingId(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = grunnlag.getGrunnlagØkonomi();

        AnnullerKravgrunnlagDto annullerKravgrunnlagDto = new AnnullerKravgrunnlagDto();
        annullerKravgrunnlagDto.setVedtakId(BigInteger.valueOf(kravgrunnlag431.getVedtakId()));
        annullerKravgrunnlagDto.setSaksbehId(kravgrunnlag431.getSaksBehId());
        annullerKravgrunnlagDto.setKodeAksjon(KodeAksjon.ANNULERE_GRUNNLAG.getKode()); // fast verdi

        long sendtXmlId = lagreXml(behandlingId, annullerKravgrunnlagDto);
        opprettProsesstaskForÅSletteSendtXml(sendtXmlId);
        sendAnnulereGrunnlagTilØkonomi(behandlingId, sendtXmlId, annullerKravgrunnlagDto);
    }

    private Long lagreXml(Long behandlingId, AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        String xml = TilbakekrevingsAnnuleregrunnlagMarshaller.marshall(behandlingId, annullerKravgrunnlag);
        Long sendtXmlId = økonomiSendtXmlRepository.lagre(behandlingId, xml, MeldingType.ANNULERE_GRUNNLAG);
        log.info("lagret annulerekravgrunnlag-xml for behandling={}", behandlingId);
        return sendtXmlId;
    }

    private void sendAnnulereGrunnlagTilØkonomi(long behandlingId, long sendtXmlId, AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        MmelDto respons = økonomiConsumer.anullereKravgrunnlag(behandlingId, annullerKravgrunnlag);
        log.info("Oversendte annulere grunnlag til oppdragsystemet for behandling={}", behandlingId);
        oppdatereRespons(behandlingId, sendtXmlId, respons);
    }

    private void oppdatereRespons(long behandlingId, long sendtXmlId, MmelDto respons) {
        String responsXml = ØkonomiResponsMarshaller.marshall(behandlingId, respons);
        økonomiSendtXmlRepository.oppdatereKvittering(sendtXmlId, responsXml);
        log.info("oppdatert respons-xml for behandling={}", behandlingId);
    }

    public interface AnnulereKravgrunnlagTaskFeil extends DeklarerteFeil {

        AnnullereKravgrunnlagTask.AnnulereKravgrunnlagTaskFeil FACTORY = FeilFactory.create(AnnullereKravgrunnlagTask.AnnulereKravgrunnlagTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-113617", feilmelding = "Kunne ikke marshalle annulere kravgrunnlag. BehandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeMarshalleAnnulereGrunnlagXml(Long behandlingId, Exception e);

    }
}
