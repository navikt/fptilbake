package no.nav.foreldrepenger.tilbakekreving.hendelser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;

import no.nav.foreldrepenger.tilbakekreving.felles.Frister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

import static java.time.temporal.TemporalAdjusters.next;

@ApplicationScoped
public class HendelseHåndtererTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(HendelseHåndtererTjeneste.class);

    private ProsessTaskTjeneste taskTjeneste;
    private FagsystemKlient fagsystemKlient;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    HendelseHåndtererTjeneste() {
        // CDI
    }

    @Inject
    public HendelseHåndtererTjeneste(ProsessTaskTjeneste taskTjeneste,
                                     FagsystemKlient fagsystemKlient,
                                     EksternBehandlingRepository eksternBehandlingRepository,
                                     BehandlingRepository behandlingRepository, BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.taskTjeneste = taskTjeneste;
        this.fagsystemKlient = fagsystemKlient;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void håndterHendelse(HendelseTaskDataWrapper hendelseTaskDataWrapper, Henvisning henvisning, String kaller) {
        var eksternBehandlingUuid = hendelseTaskDataWrapper.getBehandlingUuid();
        var åpenTilbakekreving = behandlingRepository.finnÅpenTilbakekrevingsbehandling(hendelseTaskDataWrapper.getSaksnummer())
            .orElse(null);
        if (åpenTilbakekreving != null && !åpenTilbakekreving.isBehandlingPåVent()) {
            // For å redusere risiko for at det fattes vedtak basert på gammelt kravgrunnlag
            // Nytt ytelsesvedtak når det finnes åpen tilbakekreving vil ofte føre til at kravgrunnlag sperres samme kveld
            // Gjenoppta-batch kjører hverdager kl 07:00. Hvis helg, vent til tirsdag morgen, ellers 24 timer.
            var venteTid = DayOfWeek.FRIDAY.getValue() < DayOfWeek.from(LocalDate.now()).getValue() ?
                LocalDate.now().with(next(DayOfWeek.TUESDAY)) : LocalDate.now().plusDays(1);
            behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(åpenTilbakekreving, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                venteTid.atStartOfDay(), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        }
        fagsystemKlient.hentTilbakekrevingValg(eksternBehandlingUuid)
                .ifPresent(tbkData -> {
                    if (erRelevantHendelseForOpprettTilbakekreving(tbkData)) {
                        if (eksternBehandlingRepository.harEksternBehandlingForEksternUuid(eksternBehandlingUuid)) {
                            LOG.info("Hendelse={} allerede opprettet tilbakekreving for henvisning={} fra {}", tbkData.getVidereBehandling(), henvisning, kaller);
                        } else {
                            LOG.info("Hendelse={} er relevant for tilbakekreving opprett for henvisning={} fra {}", tbkData.getVidereBehandling(), henvisning, kaller);
                            lagOpprettBehandlingTask(hendelseTaskDataWrapper, henvisning);
                        }
                    } else if (erRelevantHendelseForOppdatereTilbakekreving(tbkData)) {
                        LOG.info("Hendelse={} for henvisning={} var tidligere relevant for å oppdatere behandling. Nå ignoreres den",
                                tbkData.getVidereBehandling(), henvisning);
                    }
                });
    }

    public Henvisning hentHenvisning(UUID behandling) {
        return fagsystemKlient.hentBehandlingOptional(behandling)
                .map(EksternBehandlingsinfoDto::getHenvisning)
                .orElseThrow(() -> new NullPointerException("Henvisning fra saksbehandlingsklienten var null for behandling " + behandling.toString()));
    }

    private boolean erRelevantHendelseForOpprettTilbakekreving(TilbakekrevingValgDto tbkData) {
        return VidereBehandling.TILBAKEKR_OPPRETT.equals(tbkData.getVidereBehandling());
    }

    private boolean erRelevantHendelseForOppdatereTilbakekreving(TilbakekrevingValgDto tbkData) {
        return VidereBehandling.TILBAKEKR_OPPDATER.equals(tbkData.getVidereBehandling());
    }

    private void lagOpprettBehandlingTask(HendelseTaskDataWrapper hendelseTaskDataWrapper, Henvisning henvisning) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(hendelseTaskDataWrapper.getBehandlingUuid(),
                henvisning,
                hendelseTaskDataWrapper.getAktørId(),
                hendelseTaskDataWrapper.getSaksnummer());

        taskData.setFagsakYtelseType(hendelseTaskDataWrapper.getFagsakYtelseType());
        taskData.setBehandlingType(BehandlingType.TILBAKEKREVING);

        taskTjeneste.lagre(taskData.getProsessTaskData());
    }
}
