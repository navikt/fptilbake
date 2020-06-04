package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class InnhentDokumentasjonbrevTjeneste {

    public static final String TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG = "Innhent dokumentasjon Tilbakekreving";
    private static final String TITTEL_INNHENTDOKUMENTASJONBREV_DOKPROD = "Innhent dokumentasjon tilbakekreving ";

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BrevSporingRepository brevSporingRepository;

    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    InnhentDokumentasjonbrevTjeneste() {
        // for CDI
    }

    @Inject
    public InnhentDokumentasjonbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                            FritekstbrevTjeneste fritekstbrevTjeneste,
                                            EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                            HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();

        this.bestillDokumentTjeneste = fritekstbrevTjeneste;
        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void sendInnhentDokumentasjonBrev(Long behandlingId, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = settOppInnhentDokumentasjonBrevSamletInfo(behandling, fritekst);
        FritekstbrevData fritekstbrevData = lagInnhentDokumentasjonBrev(innhentDokumentasjonBrevSamletInfo);

        JournalpostIdOgDokumentId dokumentReferanse = bestillDokumentTjeneste.sendFritekstbrev(fritekstbrevData);
        opprettHistorikkinnslag(behandling, dokumentReferanse, TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG);
        lagreInfoOmInnhentDokumentasjonBrev(behandlingId, dokumentReferanse);
    }

    public byte[] hentForhåndsvisningInnhentDokumentasjonBrev(Long behandlingId, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        InnhentDokumentasjonbrevSamletInfo dokumentasjonBrevSamletInfo = settOppInnhentDokumentasjonBrevSamletInfo(behandling, fritekst);
        FritekstbrevData fritekstbrevData = lagInnhentDokumentasjonBrev(dokumentasjonBrevSamletInfo);
        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(fritekstbrevData);
    }

    private FritekstbrevData lagInnhentDokumentasjonBrev(InnhentDokumentasjonbrevSamletInfo dokumentasjonBrevSamletInfo) {
        String overskrift = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevOverskrift(dokumentasjonBrevSamletInfo);
        String brevtekst = TekstformatererInnhentDokumentasjonbrev.lagInnhentDokumentasjonBrevFritekst(dokumentasjonBrevSamletInfo);
        return new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(dokumentasjonBrevSamletInfo.getBrevMetadata())
            .build();
    }

    private InnhentDokumentasjonbrevSamletInfo settOppInnhentDokumentasjonBrevSamletInfo(Behandling behandling, String fritekst) {
        String aktørId = behandling.getAktørId().getId();
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);

        Språkkode mottakersSpråkkode = hentSpråkkode(behandling.getId());
        FagsakYtelseType ytelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(ytelseType, mottakersSpråkkode);
        Period brukersSvarfrist = eksternDataForBrevTjeneste.getBrukersSvarfrist();

        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medSakspartNavn(personinfo.getNavn())
            .medFagsaktype(ytelseType)
            .medSprakkode(mottakersSpråkkode)
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medAnsvarligSaksbehandler(StringUtils.isNotEmpty(behandling.getAnsvarligSaksbehandler()) ? behandling.getAnsvarligSaksbehandler() : "VL")
            .medTittel(TITTEL_INNHENTDOKUMENTASJONBREV_DOKPROD + ytelseNavn.getNavnPåBokmål())
            .build();

        return InnhentDokumentasjonbrevSamletInfo.builder()
            .medBrevMetaData(brevMetadata)
            .medFristDato(LocalDate.now().plus(brukersSvarfrist))
            .medFritekstFraSaksbehandler(fritekst)
            .build();
    }

    private Språkkode hentSpråkkode(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandlingId).getEksternUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid);
        return samletEksternBehandlingInfo.getGrunninformasjon().getSpråkkodeEllerDefault();
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, String tittel) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            tittel);
    }

    private void lagreInfoOmInnhentDokumentasjonBrev(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse) {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .medBrevType(BrevType.INNHENT_DOKUMENTASJONBREV)
            .build();
        brevSporingRepository.lagre(brevSporing);
    }
}
