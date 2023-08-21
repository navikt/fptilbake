package no.nav.foreldrepenger.tilbakekreving.hendelser;

import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.FAGSAK_YTELSE_TYPE;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class VedtaksHendelseHåndterer {

    private static final Logger LOG = LoggerFactory.getLogger(VedtaksHendelseHåndterer.class);

    private static final Map<Fagsystem, Set<Ytelser>> STØTTET_YTELSE_TYPER = Map.of(
        Fagsystem.FPTILBAKE, Set.of(Ytelser.ENGANGSTØNAD, Ytelser.FORELDREPENGER, Ytelser.SVANGERSKAPSPENGER),
        Fagsystem.K9TILBAKE, Set.of(Ytelser.FRISINN, Ytelser.OMSORGSPENGER, Ytelser.PLEIEPENGER_SYKT_BARN, Ytelser.PLEIEPENGER_NÆRSTÅENDE)
    );

    private static final Map<Fagsystem, Set<Ytelser>> REST_YTELSE_TYPER = Map.of(
        Fagsystem.FPTILBAKE, Set.of(),
        Fagsystem.K9TILBAKE, Set.of(Ytelser.OPPLÆRINGSPENGER)
    );

    private static final Map<Ytelser, FagsakYtelseType> YTELSE_TYPE_MAP = Map.of(
        Ytelser.ENGANGSTØNAD, FagsakYtelseType.ENGANGSTØNAD,
        Ytelser.FORELDREPENGER, FagsakYtelseType.FORELDREPENGER,
        Ytelser.SVANGERSKAPSPENGER, FagsakYtelseType.SVANGERSKAPSPENGER,
        Ytelser.FRISINN, FagsakYtelseType.FRISINN,
        Ytelser.OMSORGSPENGER, FagsakYtelseType.OMSORGSPENGER,
        Ytelser.OPPLÆRINGSPENGER, FagsakYtelseType.OPPLÆRINGSPENGER,
        Ytelser.PLEIEPENGER_NÆRSTÅENDE, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE,
        Ytelser.PLEIEPENGER_SYKT_BARN, FagsakYtelseType.PLEIEPENGER_SYKT_BARN
    );

    private ProsessTaskTjeneste taskTjeneste;
    private Set<Ytelser> abonnerteYtelser;
    private Set<Ytelser> resterendeYtelser;

    public VedtaksHendelseHåndterer() {
    }

    @Inject
    public VedtaksHendelseHåndterer(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
        this.abonnerteYtelser = STØTTET_YTELSE_TYPER.getOrDefault(ApplicationName.hvilkenTilbake(), Set.of());
        this.resterendeYtelser = REST_YTELSE_TYPER.getOrDefault(ApplicationName.hvilkenTilbake(), Set.of());
    }

    void handleMessage(String key, String payload) {
        // enhver exception ut fra denne metoden medfører at tråden som leser fra kafka gir opp og dør på seg.
        try {
            LOG.info("TILBAKE VEDTAKFATTET: mottok nøkkel {}", key);
            var mottattVedtak = DefaultJsonMapper.fromJson(payload, Ytelse.class);
            if (mottattVedtak instanceof YtelseV1 ytelse) {
                lagHåndterHendelseProsessTask(ytelse);
            }
        } catch (VLException e) {
            LOG.warn("FP-328773 Vedtatt-Ytelse Feil under parsing av vedtak. key={} payload={}", key, payload, e);
        } catch (Exception e) {
            LOG.warn("Vedtatt-Ytelse exception ved håndtering av vedtaksmelding, ignorerer key={}", LoggerUtils.removeLineBreaks(payload), e);
        }
    }

    private void lagHåndterHendelseProsessTask(YtelseV1 melding) {

        validereMelding(melding);
        if (abonnerteYtelser.contains(melding.getYtelse())) {
            taskTjeneste.lagre(lagProsessTaskData(melding));
        } else if (YTELSE_TYPE_MAP.get(melding.getYtelse()) == null || resterendeYtelser.contains(melding.getYtelse())) {
            LOG.warn("Melding om vedtak for {} for sak={} behandling={} med vedtakstidspunkt {} ble ignorert pga ikke-støttet ytelsetype",
                melding.getYtelse(), melding.getSaksnummer(), melding.getVedtakReferanse(), melding.getVedtattTidspunkt());
        }
    }

    private void validereMelding(YtelseV1 melding) {
        Objects.requireNonNull(melding.getAktør());
        Objects.requireNonNull(melding.getVedtakReferanse());
        Objects.requireNonNull(melding.getSaksnummer());
        Objects.requireNonNull(melding.getYtelse());
        Objects.requireNonNull(melding.getVedtattTidspunkt());
        Objects.requireNonNull(YTELSE_TYPE_MAP.get(melding.getYtelse()));
    }

    private ProsessTaskData lagProsessTaskData(YtelseV1 melding) {
        ProsessTaskData td = ProsessTaskData.forProsessTask(HåndterVedtakFattetTask.class);
        td.setAktørId(melding.getAktør().getVerdi());
        td.setProperty(EKSTERN_BEHANDLING_UUID, melding.getVedtakReferanse());
        td.setSaksnummer(melding.getSaksnummer());
        td.setProperty(FAGSAK_YTELSE_TYPE, YTELSE_TYPE_MAP.get(melding.getYtelse()).getKode());
        return td;
    }
}
