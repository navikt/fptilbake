package no.nav.foreldrepenger.tilbakekreving.hendelser;

import static java.time.temporal.TemporalAdjusters.next;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
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

    /*
     * Tilfeller:
     * - Ingen aktiv tilbakekreving og valgt opprett: Opprett TBK-behandling
     * - Åpen tilbakekreving og valgt opprett med varsel: Spol tilbake til første steg så det sendes varselbrev
     * - Åpen tilbakekreving og øvrige valg: Sett på vent slik at det kan komme statusoppdatering fra OS
     * - Ellers NOOP (vil opprettes tilbakekreving dersom det kommer kravgrunlag)
     */
    public void håndterHendelse(HendelseTaskDataWrapper hendelseTaskDataWrapper, Henvisning henvisning, String kaller) {
        var eksternBehandlingUuid = hendelseTaskDataWrapper.getBehandlingUuid();
        var samletBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.TILBAKEKREVINGSVALG, Tillegsinformasjon.VARSELTEKST, Tillegsinformasjon.SENDTOPPDRAG);
        var tbkVidereBehandling = Optional.ofNullable(samletBehandlingInfo.getTilbakekrevingsvalg())
            .map(TilbakekrevingValgDto::videreBehandling).orElse(null);
        var åpenTilbakekreving = behandlingRepository.finnÅpenTilbakekrevingsbehandling(hendelseTaskDataWrapper.getSaksnummer()).orElse(null);

        LOG.info("Behandle VedtakHendelse {} for henvisning={} fra {}", tbkVidereBehandling, henvisning, kaller);
        if (åpenTilbakekreving == null && oppretteTilbakekreving(tbkVidereBehandling)) {
            lagOpprettBehandlingTask(hendelseTaskDataWrapper, henvisning);
        } else if (åpenTilbakekreving != null && oppretteTilbakekreving(tbkVidereBehandling) && nyttVarsel(samletBehandlingInfo.getVarseltekst())) {
            // Brute-force rewind slik at det sendes nytt varsel (og man venter på grunnlag)
            rewindTilbakekrevingTilStart(åpenTilbakekreving, henvisning, eksternBehandlingUuid);
        } else {
            settÅpenTilbakekrevingPåVent(åpenTilbakekreving, samletBehandlingInfo.getSendtoppdrag());
        }
    }

    private void settÅpenTilbakekrevingPåVent(Behandling åpenTilbakekreving, Boolean sendtOppdrag) {
        if (åpenTilbakekreving != null && !åpenTilbakekreving.isBehandlingPåVent() && !Objects.equals(sendtOppdrag, Boolean.FALSE)) {
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

    private boolean oppretteTilbakekreving(VidereBehandling tbkValg) {
        return VidereBehandling.TILBAKEKR_OPPRETT.equals(tbkValg);
    }

    private boolean nyttVarsel(String varseltekst) {
        return varseltekst != null && !varseltekst.isBlank();
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
        // Kan ikke håndtere når står under iverksettelse - task kjøres på nytt og åpen TBK er ventelig avsluttet slik at det opprettes ny
        if (behandlingskontrollTjeneste.erIStegEllerSenereSteg(åpenTilbakekreving.getId(), BehandlingStegType.IVERKSETT_VEDTAK)) {
            var melding = String.format("Åpen tilbakekreving %s er under iverksetting. Kan ikke håndtere hendsels for behandling %s",
                åpenTilbakekreving.getId().toString(), eksternBehandlingUuid.toString());
            throw new IllegalStateException(melding);
        }

        var harSendtVarselTidligere = brevSporingRepository.harVarselBrevSendtForBehandlingId(åpenTilbakekreving.getId());
        var loggVarsel = harSendtVarselTidligere ? "er varslet tidligere" : "ikke er varslet";
        LOG.info("Behandle VedtakHendelse Opprett Tilbakekreving m/varsel i henvisning={} åpen tilbakekreving {} som {}",
            henvisning, åpenTilbakekreving.getId(), loggVarsel);

        EksternBehandling eksternBehandling = new EksternBehandling(åpenTilbakekreving, henvisning, eksternBehandlingUuid);
        eksternBehandlingRepository.lagre(eksternBehandling);
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(åpenTilbakekreving);
        behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(åpenTilbakekreving, kontekst);
        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.INOPPSTEG);
        lagFortsettBehandlingTask(åpenTilbakekreving);

    }

    private void lagFortsettBehandlingTask(Behandling behandling) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        taskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        taskTjeneste.lagre(taskData);
    }
}
