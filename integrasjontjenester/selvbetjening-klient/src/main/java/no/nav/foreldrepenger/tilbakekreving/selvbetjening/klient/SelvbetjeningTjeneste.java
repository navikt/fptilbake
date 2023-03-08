package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.Hendelse;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.SelvbetjeningMelding;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.producer.SelvbetjeningMeldingProducer;

@ApplicationScoped
public class SelvbetjeningTjeneste {

    private static final Environment ENV = Environment.current();
    private static final Logger logger = LoggerFactory.getLogger(SelvbetjeningTjeneste.class);

    private BrevSporingRepository brevSporingRepository;
    private BehandlingRepository behandlingRepository;
    private SelvbetjeningMeldingProducer meldingProducer;
    private PersoninfoAdapter aktørConsumer;

    SelvbetjeningTjeneste() {
        // for CDI proxy
    }

    @Inject
    public SelvbetjeningTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                 SelvbetjeningMeldingProducer meldingProducer,
                                 PersoninfoAdapter aktørConsumer) {
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.meldingProducer = meldingProducer;
        this.aktørConsumer = aktørConsumer;
    }

    public void sendMelding(Long behandlingId, Hendelse hendelse) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var varselSporing = brevSporingRepository.hentSistSendtVarselbrev(behandlingId).orElseThrow();

        var aktørId = behandling.getAktørId();
        var personIdent = aktørConsumer.hentFnrForAktør(aktørId).map(PersonIdent::getIdent);

        var ident = personIdent.orElseThrow();

        var svInfo = lagSelvbetjeningMelding(behandling, varselSporing, aktørId, ident, hendelse);

        logMelding("Sender", hendelse, ident);
        meldingProducer.sendMelding(svInfo);
        logMelding("Sendte", hendelse, ident);
    }

    private SelvbetjeningMelding lagSelvbetjeningMelding(Behandling behandling,
                                                         BrevSporing varselSporing,
                                                         AktørId aktørId,
                                                         String personIdent,
                                                         Hendelse hendelse) {
        var fagsak = behandling.getFagsak();
        var saksnummer = fagsak.getSaksnummer();
        if (personIdent.isEmpty()) {
            throw new IllegalArgumentException("Klarer ikke å finne norsk ident for aktørId");
        }

        var nå = LocalDateTime.now();
        var meldingsBuilder = SelvbetjeningMelding.builder()
            .medAktørId(aktørId)
            .medNorskIdent(personIdent)
            .medSaksnummer(saksnummer)
            .medDialogId(saksnummer.getVerdi()) // unik referanse, saksnummer er akkurat unikt nok
            .medYtelseType(fagsak.getFagsakYtelseType())
            .medDokumentId(varselSporing.getDokumentId())
            .medHendelse(hendelse)
            .medOpprettet(nå);

        if (Hendelse.TILBAKEKREVING_SPM.equals(hendelse)) {
            meldingsBuilder
                .medJournalpostId(varselSporing.getJournalpostId())
                .medGyldigTil(nå.plusWeeks(3).toLocalDate());
        }

        return meldingsBuilder.build();
    }

    private void logMelding(String msg, Hendelse hendelse, String personIdent) {
        if (ENV.isProd()) {
            logger.info("{} beskjed til selvbetjening om {}", msg, hendelse.getBeskrivelse());
        } else {
            logger.info("{} beskjed til selvbetjening om {} for fnr {}", msg, hendelse.getBeskrivelse(), personIdent);
        }
    }
}
