package no.nav.foreldrepenger.tilbakekreving.hendelser;

import static java.time.temporal.TemporalAdjusters.next;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SendtoppdragDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class HendelseHåndtererTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(HendelseHåndtererTjeneste.class);

    private ProsessTaskTjeneste taskTjeneste;
    private FagsystemKlient fagsystemKlient;
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    HendelseHåndtererTjeneste() {
        // CDI
    }

    @Inject
    public HendelseHåndtererTjeneste(ProsessTaskTjeneste taskTjeneste,
                                     FagsystemKlient fagsystemKlient,
                                     EksternBehandlingRepository eksternBehandlingRepository,
                                     BehandlingRepository behandlingRepository,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     BrevSporingRepository brevSporingRepository) {
        this.taskTjeneste = taskTjeneste;
        this.fagsystemKlient = fagsystemKlient;
        this.behandlingRepository = behandlingRepository;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.brevSporingRepository = brevSporingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void håndterHendelse(HendelseTaskDataWrapper hendelseTaskDataWrapper, Henvisning henvisning, String kaller) {
        var eksternBehandlingUuid = hendelseTaskDataWrapper.getBehandlingUuid();
        var samletBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.TILBAKEKREVINGSVALG, Tillegsinformasjon.VARSELTEKST, Tillegsinformasjon.SENDTOPPDRAG);
        var tbkData = samletBehandlingInfo.getTilbakekrevingsvalg();
        var åpenTilbakekreving = behandlingRepository.finnÅpenTilbakekrevingsbehandling(hendelseTaskDataWrapper.getSaksnummer())
            .orElse(null);var harSendtVarselTidligere = åpenTilbakekreving != null && brevSporingRepository.harVarselBrevSendtForBehandlingId(åpenTilbakekreving.getId());

        if (skalOppretteTilbakekrevingEllerSendeNyttVarsel(tbkData, åpenTilbakekreving, samletBehandlingInfo.getVarseltekst())) {
            if (åpenTilbakekreving == null) { // Skal opprette tilbakekreving
                LOG.info("Mottatt VedtakHendelse {} er relevant for tilbakekreving opprett for henvisning={} fra {}",
                    tbkData.getVidereBehandling(), henvisning, kaller);
                lagOpprettBehandlingTask(hendelseTaskDataWrapper, henvisning);
            } else { // Håndtere at det finnes tilbakekreving der man skal sende nytt varsel
                // Start på nytt med nytt varselbrev
                var loggVarsel = harSendtVarselTidligere ? "er varslet tidligere" : "ikke er varslet";
                LOG.info("Mottatt VedtakHendelse {} for behandling {} har bedt om varsel og det finnes åpen tilbakekreving {} som {}",
                    tbkData.getVidereBehandling(), eksternBehandlingUuid.toString(), åpenTilbakekreving.getId(), loggVarsel);
                // Brute-force rewind slik at det sendes nytt varsel (og man venter på grunnlag)
                rewindTilbakekrevingTilStart(åpenTilbakekreving, henvisning, eksternBehandlingUuid);
            }
        } else {
            settÅpenTilbakekrevingPåVent(åpenTilbakekreving, samletBehandlingInfo.getSendtoppdrag());
        }
    }

    private void settÅpenTilbakekrevingPåVent(Behandling åpenTilbakekreving, SendtoppdragDto oppdrag) {
        if (åpenTilbakekreving != null && oppdrag != null) {
            // For å redusere risiko for at det fattes vedtak basert på gammelt kravgrunnlag
            // Nytt ytelsesvedtak når det finnes åpen tilbakekreving vil ofte føre til at kravgrunnlag sperres samme kveld
            // Gjenoppta-batch kjører hverdager kl 07:00. Hvis helg, vent til tirsdag morgen, ellers 24 timer.
            var idag = LocalDate.now();
            var venteTid = idag.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue() ?
                idag.with(next(DayOfWeek.TUESDAY)) : idag.plusDays(1);
            behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(åpenTilbakekreving, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                venteTid.atStartOfDay(), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        }
    }

    public Henvisning hentHenvisning(UUID behandling) {
        return fagsystemKlient.hentBehandlingOptional(behandling)
                .map(EksternBehandlingsinfoDto::getHenvisning)
                .orElseThrow(() -> new NullPointerException("Henvisning fra saksbehandlingsklienten var null for behandling " + behandling.toString()));
    }

    private boolean skalOppretteTilbakekrevingEllerSendeNyttVarsel(TilbakekrevingValgDto tbkData, Behandling åpenTilbakekreving, String varseltekst) {
        return VidereBehandling.TILBAKEKR_OPPRETT.equals(tbkData.getVidereBehandling()) &&
            (åpenTilbakekreving == null || (varseltekst != null && !varseltekst.isBlank()));
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

    private void rewindTilbakekrevingTilStart(Behandling åpenTilbakekreving, Henvisning henvisning, UUID eksternBehandlingUuid) {
        // Kan ikke håndtere når står under iverksettelse
        if (behandlingskontrollTjeneste.erIStegEllerSenereSteg(åpenTilbakekreving.getId(), BehandlingStegType.IVERKSETT_VEDTAK)) {
            var melding = String.format("Åpen tilbakekreving %s er under iverksetting. Kan ikke håndtere hendsels for behandling %s",
                åpenTilbakekreving.getId().toString(), eksternBehandlingUuid.toString());
            throw new IllegalStateException(melding);
        }
        EksternBehandling eksternBehandling = new EksternBehandling(åpenTilbakekreving, henvisning, eksternBehandlingUuid);
        eksternBehandlingRepository.lagre(eksternBehandling);
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(åpenTilbakekreving);
        behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(åpenTilbakekreving, kontekst);
        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.INOPPSTEG);
        lagFortsettBehandlingTask(åpenTilbakekreving);
    }

    private void lagFortsettBehandlingTask(Behandling behandling) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskData.setCallIdFraEksisterende();
        taskTjeneste.lagre(taskData);
    }
}
