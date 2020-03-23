package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.Hendelse;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.SelvbetjeningMelding;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.producer.SelvbetjeningMeldingProducer;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class SelvbetjeningTjeneste {

    private static final Environment ENV = Environment.current();
    private static final Logger logger = LoggerFactory.getLogger(SelvbetjeningTjeneste.class);

    private BrevSporingRepository brevSporingRepository;
    private BehandlingRepository behandlingRepository;
    private SelvbetjeningMeldingProducer meldingProducer;
    private AktørConsumerMedCache aktørConsumer;

    SelvbetjeningTjeneste() {
        // for CDI proxy
    }

    @Inject
    public SelvbetjeningTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                 SelvbetjeningMeldingProducer meldingProducer,
                                 AktørConsumerMedCache aktørConsumer) {
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.meldingProducer = meldingProducer;
        this.aktørConsumer = aktørConsumer;
    }

    public void sendMelding(Long behandlingId, Hendelse hendelse) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BrevSporing varselSporing = brevSporingRepository.hentSistSendtVarselbrev(behandlingId).orElseThrow();

        AktørId aktørId = behandling.getAktørId();
        Optional<String> personIdent = aktørConsumer.hentPersonIdentForAktørId(aktørId.getId());
        SelvbetjeningMelding svInfo = lagSelvbetjeningMelding(behandling, varselSporing, aktørId, personIdent.get(), hendelse);

        logMelding("Sender", hendelse, personIdent.get());
        meldingProducer.sendMelding(svInfo);
        logMelding("Sendte", hendelse, personIdent.get());
    }

    private SelvbetjeningMelding lagSelvbetjeningMelding(Behandling behandling,
                                                         BrevSporing varselSporing,
                                                         AktørId aktørId,
                                                         String personIdent,
                                                         Hendelse hendelse) {
        Fagsak fagsak = behandling.getFagsak();
        Saksnummer saksnummer = fagsak.getSaksnummer();
        if (personIdent.isEmpty()) {
            throw new IllegalArgumentException("Klarer ikke å finne norsk ident for aktørId");
        }

        SelvbetjeningMelding.Builder meldingsBuilder = SelvbetjeningMelding.builder()
            .medAktørId(aktørId)
            .medNorskIdent(personIdent)
            .medSaksnummer(saksnummer)
            .medDialogId(saksnummer.getVerdi()) // unik referanse, saksnummer er akkurat unikt nok
            .medYtelseType(fagsak.getFagsakYtelseType())
            .medDokumentId(varselSporing.getDokumentId())
            .medHendelse(hendelse);

        if (Hendelse.TILBAKEKREVING_SPM.equals(hendelse)) {
            LocalDateTime nå = LocalDateTime.now();
            meldingsBuilder
                .medJournalpostId(varselSporing.getJournalpostId())
                .medOpprettet(nå)
                .medGyldigTil(nå.plusWeeks(3).toLocalDate());
        }

        return meldingsBuilder.build();
    }

    private void logMelding(String msg, Hendelse hendelse, String personIdent) {
        String hendelseMsg = Hendelse.TILBAKEKREVING_SPM.equals(hendelse) ? "utsendt tilbakekrevingsvarsel"
            : Hendelse.TILBAKEKREVING_FATTET_VEDTAK.equals(hendelse) ? "fattet vedtak"
            : Hendelse.TILBAKEKREVING_HENLAGT.equals(hendelse) ? "behandling henlagt"
            : "";

        if (ENV.isProd()) {
            logger.info("{} beskjed til selvbetjening om {}", msg, hendelseMsg);
        } else {
            logger.info("{} beskjed til selvbetjening om {} for fnr {}", msg, hendelseMsg, personIdent);
        }
    }
}
