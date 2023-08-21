package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;

@ApplicationScoped
public class InnhentDokumentasjonbrevTjeneste {

    public static final String TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG = "Innhent dokumentasjon Tilbakekreving";
    public static final String TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG_TIL_VERGE = "Innhent dokumentasjon Tilbakekreving til verge";

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;

    private PdfBrevTjeneste pdfBrevTjeneste;

    InnhentDokumentasjonbrevTjeneste() {
        // for CDI
    }

    @Inject
    public InnhentDokumentasjonbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                            EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                            PdfBrevTjeneste pdfBrevTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public void sendInnhentDokumentasjonBrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo = settOppInnhentDokumentasjonBrevSamletInfo(behandling, fritekst, brevMottaker);
        FritekstbrevData fritekstbrevData = lagInnhentDokumentasjonBrev(innhentDokumentasjonBrevSamletInfo);

        pdfBrevTjeneste.sendBrev(behandlingId, DetaljertBrevType.INNHENT_DOKUMETASJON, BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(fritekstbrevData.getBrevMetadata())
                .setOverskrift(fritekstbrevData.getOverskrift())
                .setBrevtekst(fritekstbrevData.getBrevtekst())
                .build());

    }

    public byte[] hentForhåndsvisningInnhentDokumentasjonBrev(Long behandlingId, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BrevMottaker brevMottaker = vergeRepository.finnesVerge(behandlingId) ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
        InnhentDokumentasjonbrevSamletInfo dokumentasjonBrevSamletInfo = settOppInnhentDokumentasjonBrevSamletInfo(behandling, fritekst, brevMottaker);
        FritekstbrevData fritekstbrevData = lagInnhentDokumentasjonBrev(dokumentasjonBrevSamletInfo);
        return pdfBrevTjeneste.genererForhåndsvisning(BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(fritekstbrevData.getBrevMetadata())
                .setOverskrift(fritekstbrevData.getOverskrift())
                .setBrevtekst(fritekstbrevData.getBrevtekst())
                .build());
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

    private InnhentDokumentasjonbrevSamletInfo settOppInnhentDokumentasjonBrevSamletInfo(Behandling behandling,
                                                                                         String fritekst,
                                                                                         BrevMottaker brevMottaker) {
        //verge
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandling.getId());
        boolean finnesVerge = vergeEntitet.isPresent();

        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        Språkkode mottakersSpråkkode = hentSpråkkode(behandling.getId());
        FagsakYtelseType ytelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(ytelseType, mottakersSpråkkode);
        Period brukersSvarfrist = Frister.BRUKER_TILSVAR;

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
                .medAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler() != null && !behandling.getAnsvarligSaksbehandler().isEmpty() ? behandling.getAnsvarligSaksbehandler() : "VL")
                .medTittel(getTittel(brevMottaker) + ytelseNavn.getNavnPåBokmål())
                .medFinnesVerge(finnesVerge)
                .medVergeNavn(vergeNavn)
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

    private String getTittel(BrevMottaker brevMottaker) {
        return BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG_TIL_VERGE
                : TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG;
    }
}
