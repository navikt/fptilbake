package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
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
import no.nav.vedtak.exception.FunksjonellException;

@ApplicationScoped
public class HenleggelsesbrevTjeneste {

    private static final String TITTEL_HENLEGGELSESBREV = "Informasjon om at tilbakekrevingssaken er henlagt";

    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;

    private PdfBrevTjeneste pdfBrevTjeneste;


    HenleggelsesbrevTjeneste() {
        // for CDI
    }

    @Inject
    public HenleggelsesbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                    EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                    PdfBrevTjeneste pdfBrevTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public void sendHenleggelsebrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = lagHenleggelsebrevForSending(behandling, fritekst, brevMottaker);
        FritekstbrevData fritekstbrevData = BehandlingType.TILBAKEKREVING.equals(behandling.getType()) ?
                lagHenleggelsebrev(henleggelsesbrevSamletInfo) : lagRevurderingHenleggelsebrev(henleggelsesbrevSamletInfo);
        pdfBrevTjeneste.sendBrev(behandlingId, DetaljertBrevType.HENLEGGELSE, BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(fritekstbrevData.getBrevMetadata())
                .setOverskrift(fritekstbrevData.getOverskrift())
                .setBrevtekst(fritekstbrevData.getBrevtekst())
                .build());
    }

    public byte[] hentForhåndsvisningHenleggelsebrev(UUID behandlingUuid, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingUuid);
        return hentForhåndsvisningHenleggelsebrev(behandling, fritekst);
    }

    public byte[] hentForhåndsvisningHenleggelsebrev(Long behandlingId, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return hentForhåndsvisningHenleggelsebrev(behandling, fritekst);
    }

    private byte[] hentForhåndsvisningHenleggelsebrev(Behandling behandling, String fritekst) {
        boolean finnesVerge = vergeRepository.finnesVerge(behandling.getId());
        BrevMottaker brevMottaker = finnesVerge ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = lagHenleggelsebrevForSending(behandling, fritekst, brevMottaker);
        FritekstbrevData fritekstbrevData = BehandlingType.TILBAKEKREVING.equals(behandling.getType()) ?
                lagHenleggelsebrev(henleggelsesbrevSamletInfo) : lagRevurderingHenleggelsebrev(henleggelsesbrevSamletInfo);

        return pdfBrevTjeneste.genererForhåndsvisning(BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(fritekstbrevData.getBrevMetadata())
                .setOverskrift(fritekstbrevData.getOverskrift())
                .setBrevtekst(fritekstbrevData.getBrevtekst())
                .build());
    }

    private HenleggelsesbrevSamletInfo lagHenleggelsebrevForSending(Behandling behandling, String fritekst, BrevMottaker brevMottaker) {
        Long behandlingId = behandling.getId();
        Optional<BrevSporing> brevSporing = brevSporingRepository.hentSistSendtVarselbrev(behandlingId);
        BehandlingType behandlingType = behandling.getType();
        if (BehandlingType.TILBAKEKREVING.equals(behandlingType) && brevSporing.isEmpty()) {
            throw new FunksjonellException("FPT-110801", String.format("Varselbrev er ikke sendt. Kan ikke forhåndsvise/sende henleggelsesbrev for behandlingId=%s.", behandlingId), "");
        } else if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandlingType) && (fritekst == null || fritekst.isEmpty())) {
            throw new FunksjonellException("FPT-110802",
                    String.format("Kan ikke forhåndsvise/sende henleggelsesbrev uten fritekst for Tilbakekreving Revurdering med behandlingId=%s.", behandlingId), "");
        }
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = hentSpråkkode(behandlingId);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra tps
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, språkkode);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
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
                .medTittel(TITTEL_HENLEGGELSESBREV)
                .medBehandlingtype(behandlingType)
                .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(metadata);
        brevSporing.ifPresent(sporing -> henleggelsesbrevSamletInfo.setVarsletDato(sporing.getOpprettetTidspunkt().toLocalDate()));
        henleggelsesbrevSamletInfo.setFritekstFraSaksbehandler(fritekst);
        return henleggelsesbrevSamletInfo;
    }

    private Språkkode hentSpråkkode(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentForSisteAktivertInternId(behandlingId).getEksternUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid);
        return samletEksternBehandlingInfo.getGrunninformasjon().getSpråkkodeEllerDefault();
    }

    private FritekstbrevData lagHenleggelsebrev(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        return new FritekstbrevData.Builder()
                .medOverskrift(TekstformatererHenleggelsesbrev.lagHenleggelsebrevOverskrift(henleggelsesbrevSamletInfo))
                .medMetadata(henleggelsesbrevSamletInfo.getBrevMetadata())
                .medBrevtekst(TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo))
                .build();
    }

    private FritekstbrevData lagRevurderingHenleggelsebrev(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        return new FritekstbrevData.Builder()
                .medOverskrift(TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevOverskrift(henleggelsesbrevSamletInfo))
                .medMetadata(henleggelsesbrevSamletInfo.getBrevMetadata())
                .medBrevtekst(TekstformatererHenleggelsesbrev.lagRevurderingHenleggelsebrevFritekst(henleggelsesbrevSamletInfo))
                .build();
    }

}
