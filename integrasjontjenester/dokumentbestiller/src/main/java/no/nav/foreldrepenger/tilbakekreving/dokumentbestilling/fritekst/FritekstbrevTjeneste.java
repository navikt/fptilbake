package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst;

import java.util.Objects;
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

@ApplicationScoped
public class FritekstbrevTjeneste {

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private PdfBrevTjeneste pdfBrevTjeneste;

    FritekstbrevTjeneste() {
        // for CDI
    }

    @Inject
    public FritekstbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                PdfBrevTjeneste pdfBrevTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public void sendFritekstbrev(Long behandlingId, String tittel, String overskrift, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        sendFritekstbrev(behandling, tittel, overskrift, fritekst, brevMottaker);
    }

    public void sendFritekstbrev(Behandling behandling, String tittel, String overskrift, String fritekst, BrevMottaker brevMottaker) {
        FritekstbrevSamletInfo fritekstbrevSamletInfo = lagFritekstbrevForSending(behandling, tittel, overskrift, fritekst, brevMottaker);
        FritekstbrevData fritekstbrevData = lagFritekstbrev(fritekstbrevSamletInfo);
        pdfBrevTjeneste.sendBrev(behandling.getId(), DetaljertBrevType.FRITEKST, BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(fritekstbrevData.getBrevMetadata())
                .setTittel(fritekstbrevData.getTittel())
                .setOverskrift(fritekstbrevData.getOverskrift())
                .setBrevtekst(fritekstbrevData.getBrevtekst())
                .build());
    }

    public byte[] hentForhåndsvisningFritekstbrev(Behandling behandling, String tittel, String overskrift, String fritekst) {
        boolean finnesVerge = vergeRepository.finnesVerge(behandling.getId());
        BrevMottaker brevMottaker = finnesVerge ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
        FritekstbrevSamletInfo fritekstbrevSamletInfo = lagFritekstbrevForSending(behandling, tittel, overskrift, fritekst, brevMottaker);
        FritekstbrevData fritekstbrevData = lagFritekstbrev(fritekstbrevSamletInfo);

        return pdfBrevTjeneste.genererForhåndsvisning(BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(fritekstbrevData.getBrevMetadata())
                .setOverskrift(fritekstbrevData.getOverskrift())
                .setBrevtekst(fritekstbrevData.getBrevtekst())
                .build());
    }

    private FritekstbrevSamletInfo lagFritekstbrevForSending(Behandling behandling, String tittel, String overskrift, String fritekst, BrevMottaker brevMottaker) {
        Objects.requireNonNull(tittel, "tittel må være satt");
        Objects.requireNonNull(overskrift, "overskrift må være satt");
        Objects.requireNonNull(fritekst, "fritekst må være satt");
        Long behandlingId = behandling.getId();

        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = hentSpråkkode(behandlingId);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra tps
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, språkkode);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getFagsak().getFagsakYtelseType(), behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(behandling.getFagsak().getFagsakYtelseType(), personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        BrevMetadata metadata = new BrevMetadata.Builder()
                .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
                .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
                .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
                .medFagsaktype(behandling.getFagsak().getFagsakYtelseType())
                .medSprakkode(språkkode)
                .medAnsvarligSaksbehandler("VL")
                .medSakspartId(personinfo.getPersonIdent().getIdent())
                .medMottakerAdresse(adresseinfo)
                .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
                .medSakspartNavn(personinfo.getNavn())
                .medVergeNavn(vergeNavn)
                .medFinnesVerge(finnesVerge)
                .medTittel(tittel)
                .medBehandlingtype(behandling.getType())
                .build();

        FritekstbrevSamletInfo fritekstbrevSamletInfo = new FritekstbrevSamletInfo();
        fritekstbrevSamletInfo.setBrevMetadata(metadata);
        fritekstbrevSamletInfo.setOverskrift(overskrift);
        fritekstbrevSamletInfo.setFritekstFraSaksbehandler(fritekst);
        return fritekstbrevSamletInfo;
    }

    private Språkkode hentSpråkkode(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentForSisteAktivertInternId(behandlingId).getEksternUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid);
        return samletEksternBehandlingInfo.getGrunninformasjon().getSpråkkodeEllerDefault();
    }

    private FritekstbrevData lagFritekstbrev(FritekstbrevSamletInfo fritekstbrevSamletInfo) {
        return new FritekstbrevData.Builder()
                .medTittel(fritekstbrevSamletInfo.getBrevMetadata().getTittel())
                .medOverskrift(fritekstbrevSamletInfo.getOverskrift())
                .medBrevtekst(fritekstbrevSamletInfo.getFritekstFraSaksbehandler())
                .medMetadata(fritekstbrevSamletInfo.getBrevMetadata())
                .build();
    }

}
