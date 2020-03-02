package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.selvbetjening;

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
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;

@ApplicationScoped
public class VarselSelvbetjeningTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(VarselSelvbetjeningTjeneste.class);

    private BrevSporingRepository brevSporingRepository;
    private BehandlingRepository behandlingRepository;
    private BeskjedUtsendtVarselTilSelvbetjeningMeldingProducer meldingProducer;
    private AktørConsumerMedCache aktørConsumer;

    VarselSelvbetjeningTjeneste() {
        //for CDI proxy
    }

    @Inject
    public VarselSelvbetjeningTjeneste(BehandlingRepositoryProvider repositoryProvider, BeskjedUtsendtVarselTilSelvbetjeningMeldingProducer meldingProducer, AktørConsumerMedCache aktørConsumer) {
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.meldingProducer = meldingProducer;
        this.aktørConsumer = aktørConsumer;
    }

    public void sendBeskjedOmUtsendtVarsel(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BrevSporing varselSporing = brevSporingRepository.hentSistSendtVarselbrev(behandlingId).orElseThrow();

        Fagsak fagsak = behandling.getFagsak();
        Saksnummer saksnummer = fagsak.getSaksnummer();
        LocalDateTime nå = LocalDateTime.now();
        Optional<String> personIdent = aktørConsumer.hentPersonIdentForAktørId(behandling.getAktørId().getId());
        if (personIdent.isEmpty()) {
            throw new IllegalArgumentException("Klarer ikke å finne norsk ident for aktørId");
        }
        SendtVarselInformasjon svInfo = SendtVarselInformasjon.builder()
            .medNorskIdent(personIdent.get())
            .medSaksnummer(saksnummer)
            .medDialogId(saksnummer.getVerdi()) //unik referanse, saksnummer er akkurat unikt nok
            .medYtelseType(fagsak.getFagsakYtelseType())
            .medJournalpostId(varselSporing.getJournalpostId())
            .medDokumentId(varselSporing.getDokumentId())
            .medOpprettet(nå)
            .medGyldigTil(nå.plusWeeks(3).toLocalDate())
            .build();

        meldingProducer.sendBeskjedOmSendtVarsel(svInfo);

        logger.info("Sendte beskjed til selvbetjening om utsendt tilbakekrevingsvarsel");
    }
}
