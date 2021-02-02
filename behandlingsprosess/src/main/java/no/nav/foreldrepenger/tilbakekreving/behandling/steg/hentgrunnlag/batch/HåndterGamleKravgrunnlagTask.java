package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(HåndterGamleKravgrunnlagTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class HåndterGamleKravgrunnlagTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagTask.class);
    public static final String TASKTYPE = "kravgrunnlag.gammelt.håndter";

    private HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste;

    HåndterGamleKravgrunnlagTask() {
        // for CDI
    }

    @Inject
    public HåndterGamleKravgrunnlagTask(HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste) {
        this.håndterGamleKravgrunnlagTjeneste = håndterGamleKravgrunnlagTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue("mottattXmlId"));
        logger.info("Håndterer gammelt kravgrunnlag med id={}", mottattXmlId);
        ØkonomiXmlMottatt økonomiXmlMottatt = håndterGamleKravgrunnlagTjeneste.hentGammeltKravgrunnlag(mottattXmlId);
        KravgrunnlagMedStatus respons = håndterGamleKravgrunnlagTjeneste.hentKravgrunnlagFraØkonomi(økonomiXmlMottatt);
        if (!respons.harKravgrunnlag()) {
            håndterGamleKravgrunnlagTjeneste.slettMottattUgyldigKravgrunnlag(mottattXmlId);
        } else {
            logger.info("Referanse etter henting fra WS og mapping: {}", respons.getKravgrunnlag().getReferanse());
            Optional<Long> ugyldigkravgrunnlag = håndterGamleKravgrunnlagTjeneste.
                håndterKravgrunnlagRespons(mottattXmlId, økonomiXmlMottatt.getMottattXml(), respons);
            ugyldigkravgrunnlag.ifPresent(håndterGamleKravgrunnlagTjeneste::slettMottattUgyldigKravgrunnlag);
        }
    }
}
