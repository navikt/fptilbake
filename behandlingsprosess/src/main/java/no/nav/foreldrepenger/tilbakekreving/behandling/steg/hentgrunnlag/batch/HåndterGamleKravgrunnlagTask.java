package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

@ApplicationScoped
@ProsessTask("kravgrunnlag.gammelt.håndter")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class HåndterGamleKravgrunnlagTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HåndterGamleKravgrunnlagTask.class);
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

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
        LOG_CONTEXT.add("mottattXmlId", mottattXmlId);
        LOG.info("Håndterer gammelt kravgrunnlag med mottattXmlId={}", mottattXmlId);
        ØkonomiXmlMottatt økonomiXmlMottatt = håndterGamleKravgrunnlagTjeneste.hentGammeltKravgrunnlag(mottattXmlId);
        LOG_CONTEXT.add("henvisning", økonomiXmlMottatt.getHenvisning());
        LOG_CONTEXT.add("saksnummer", økonomiXmlMottatt.getSaksnummer());

        KravgrunnlagMedStatus respons = håndterGamleKravgrunnlagTjeneste.hentKravgrunnlagFraØkonomi(økonomiXmlMottatt);
        if (!respons.harKravgrunnlag()) {
            håndterGamleKravgrunnlagTjeneste.slettMottattUgyldigKravgrunnlag(mottattXmlId);
        } else {
            LOG.info("Referanse etter henting fra WS og mapping: {}", respons.getKravgrunnlag().getReferanse());
            Optional<Long> ugyldigkravgrunnlag = håndterGamleKravgrunnlagTjeneste.
                    håndterKravgrunnlagRespons(mottattXmlId, økonomiXmlMottatt.getMottattXml(), respons);
            ugyldigkravgrunnlag.ifPresent(håndterGamleKravgrunnlagTjeneste::slettMottattUgyldigKravgrunnlag);
        }
    }
}
