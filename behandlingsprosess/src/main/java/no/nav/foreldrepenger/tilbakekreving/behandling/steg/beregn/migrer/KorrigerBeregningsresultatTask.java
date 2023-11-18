package no.nav.foreldrepenger.tilbakekreving.behandling.steg.beregn.migrer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

@ApplicationScoped
@ProsessTask("korriger.beregningsresultat")
public class KorrigerBeregningsresultatTask implements ProsessTaskHandler {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$
    private static final Logger LOG = LoggerFactory.getLogger(KorrigerBeregningsresultatTask.class);

    private static final LocalDateTime CUTOFF = LocalDateTime.of(LocalDate.of(2023,11,18), LocalTime.of(16,0,0));

    private BeregningsresultatRepository beregningsresultatRepository;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private BehandlingRepository behandlingRepository;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;

    public KorrigerBeregningsresultatTask() {
        //for CDI proxy
    }

    @Inject
    public KorrigerBeregningsresultatTask(BeregningsresultatRepository beregningsresultatRepository,
                                          BeregningsresultatTjeneste beregningsresultatTjeneste,
                                          BehandlingRepository behandlingRepository,
                                          TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                          ØkonomiSendtXmlRepository økonomiSendtXmlRepository) {
        this.beregningsresultatRepository = beregningsresultatRepository;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = Long.parseLong(prosessTaskData.getBehandlingId());
        LOG_CONTEXT.add("behandling", Long.toString(behandlingId));
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.getStatus() != BehandlingStatus.AVSLUTTET) {
            LOG.info("Ingenting å gjøre, behandlingen er ikke avsluttet. Avslutter task.");
            return;
        }
        var beregning = beregningsresultatRepository.hentHvisEksisterer(behandlingId).orElse(null);
        if (beregning == null || beregning.getOpprettetTidspunkt().isBefore(CUTOFF)) {
            LOG.info("Ingenting å gjøre, mangler beregningsresultat . Avslutter task");
            return;
        }
        var endret = false;
        for (var p : beregning.getPerioder()) {
            var netto = p.getTilbakekrevingBeløp().subtract(p.getSkattBeløp());
            if (netto.compareTo(p.getTilbakekrevingBeløpEtterSkatt()) != 0) {
                p.setTilbakekrevingBeløpEtterSkatt(netto);
                endret = true;
            }
        }
        if (endret) {
            LOG.info("Korrigert beregning for behandling {}.", behandlingId);
            beregningsresultatRepository.lagre(behandlingId, beregning);
        }
    }

}
