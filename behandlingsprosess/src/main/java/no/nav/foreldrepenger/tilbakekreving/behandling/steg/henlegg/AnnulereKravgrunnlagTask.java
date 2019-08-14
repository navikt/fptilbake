package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henlegg;

import java.math.BigInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(AnnulereKravgrunnlagTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class AnnulereKravgrunnlagTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "kravgrunnlag.annulere";

    private KravgrunnlagRepository kravgrunnlagRepository;
    private ØkonomiConsumer økonomiConsumer;

    AnnulereKravgrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public AnnulereKravgrunnlagTask(KravgrunnlagRepository kravgrunnlagRepository, ØkonomiConsumer økonomiConsumer) {
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.økonomiConsumer = økonomiConsumer;
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

        økonomiConsumer.anullereKravgrunnlag(behandlingId, annullerKravgrunnlagDto);
    }
}
