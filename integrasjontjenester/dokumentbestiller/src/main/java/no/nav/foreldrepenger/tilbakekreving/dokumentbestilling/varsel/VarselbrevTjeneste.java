package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.jpa.Transaction;


@ApplicationScoped
@Transaction
public class VarselbrevTjeneste {

    private static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";

    private BehandlingRepository behandlingRepository;
    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private BrevdataRepository brevdataRepository;

    @Inject
    public VarselbrevTjeneste(BehandlingRepository behandlingRepository,
                              EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                              BehandlingTjeneste behandlingTjeneste,
                              EksternBehandlingRepository eksternBehandlingRepository,
                              FritekstbrevTjeneste bestillDokumentTjeneste,
                              HistorikkinnslagTjeneste historikkinnslagTjeneste,
                              BrevdataRepository brevdataRepository) {
        this.behandlingRepository = behandlingRepository;
        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.brevdataRepository = brevdataRepository;
    }

    public VarselbrevTjeneste() {
    }

    public void sendVarselbrev(Long behandlingId) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForSending(behandlingId);
        String overskrift = VarselbrevOverskrift.finnOverskriftVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandling, dokumentreferanse);
        lagreInfoOmVedtaksbrev(behandlingId, dokumentreferanse);
    }

    public byte[] hentForhåndsvisningVarselbrev(HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForForhåndsvisning(
            hentForhåndsvisningVarselbrevDto.getBehandlingUuid(),
            hentForhåndsvisningVarselbrevDto.getVarseltekst(),
            hentForhåndsvisningVarselbrevDto.getFagsakYtelseType());

        String overskrift = VarselbrevOverskrift.finnOverskriftVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();

        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            TITTEL_VARSELBREV_HISTORIKKINNSLAG);
    }

    private void lagreInfoOmVedtaksbrev(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse) {
        VedtaksbrevSporing vedtaksbrevSporing = new VedtaksbrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .build();
        brevdataRepository.lagreVedtaksbrevData(vedtaksbrevSporing);
    }

    private VarselbrevSamletInfo lagVarselbrevForSending(Long behandlingId) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        //Henter data fra fptilbakes eget repo for å finne behandlingsid brukt i fpsak, samt saksnummer
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Long behandlingIdIFpsak = eksternBehandling.getEksternId();

        //Henter data fra fpsak
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        SamletEksternBehandlingInfo eksternBehandlingsinfoDto = eksternDataForBrevTjeneste.hentBehandlingFpsak(eksternBehandling.getEksternUuid(), Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.VARSELTEKST);
        //Henter data fra tps
        String aktørId = behandling.getAktørId().getId();
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = eksternBehandlingsinfoDto.getGrunninformasjon().getSprakkode();
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, mottakersSpråkkode);

        //Henter data fra fpoppdrag
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = eksternDataForBrevTjeneste.hentFeilutbetaltePerioder(behandlingIdIFpsak);

        return VarselbrevUtil.sammenstillInfoFraFagsystemerForSending(
            eksternBehandlingsinfoDto,
            saksnummer,
            adresseinfo,
            personinfo,
            feilutbetaltePerioderDto,
            eksternDataForBrevTjeneste.getBrukersSvarfrist(),
            fagsakYtelseType,
            ytelseNavn);
    }

    public VarselbrevSamletInfo lagVarselbrevForForhåndsvisning(UUID behandlingUuId, String varseltekst, FagsakYtelseType fagsakYtleseType) {
        SamletEksternBehandlingInfo eksternBehandlingsinfo = eksternDataForBrevTjeneste.hentBehandlingFpsak(behandlingUuId, Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.VARSELTEKST);

        String aktørId = eksternBehandlingsinfo.getAktørId().getId();
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);
        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfo.getGrunninformasjon();
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = eksternDataForBrevTjeneste.hentFeilutbetaltePerioder(grunninformasjon.getId());

        if (grunninformasjon.getSprakkode() == null) {
            grunninformasjon.setSprakkode(Språkkode.nb);
        }
        Språkkode mottakersSpråkkode = grunninformasjon.getSprakkode();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtleseType, mottakersSpråkkode);
        return VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
            eksternBehandlingsinfo.getSaksnummer(),
            varseltekst,
            adresseinfo,
            eksternBehandlingsinfo,
            feilutbetaltePerioderDto,
            eksternDataForBrevTjeneste.getBrukersSvarfrist(),
            fagsakYtleseType,
            ytelseNavn);
    }
}
