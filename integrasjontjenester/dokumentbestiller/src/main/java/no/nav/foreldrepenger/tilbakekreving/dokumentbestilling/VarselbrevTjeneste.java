package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.TittelOverskriftUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.VarselbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;


@ApplicationScoped
public class VarselbrevTjeneste {

    private static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";

    private FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BestillDokumentTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private BrevdataRepository brevdataRepository;

    @Inject
    public VarselbrevTjeneste(FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste, BehandlingTjeneste behandlingTjeneste, EksternBehandlingRepository eksternBehandlingRepository, BestillDokumentTjeneste bestillDokumentTjeneste, HistorikkinnslagTjeneste historikkinnslagTjeneste, BrevdataRepository brevdataRepository) {
        this.fellesInfoTilBrevTjeneste = fellesInfoTilBrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.brevdataRepository = brevdataRepository;
    }

    public VarselbrevTjeneste() {
    }

    public void sendVarselbrev(Long fagsakId, AktørId aktørId, Long behandlingId) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForSending(behandlingId);
        String overskrift = TittelOverskriftUtil.finnOverskriftVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        String brevtekst = TekstformattererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandlingId, dokumentreferanse, fagsakId, aktørId);
        lagreInfoOmVedtaksbrev(behandlingId, dokumentreferanse);
    }

    public byte[] hentForhåndsvisningVarselbrev(HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForForhåndsvisning(
            hentForhåndsvisningVarselbrevDto.getBehandlingUuid(),
            new Saksnummer(hentForhåndsvisningVarselbrevDto.getSaksnummer()),
            hentForhåndsvisningVarselbrevDto.getVarseltekst(),
            hentForhåndsvisningVarselbrevDto.getFagsakYtelseType());

        String overskrift = TittelOverskriftUtil.finnOverskriftVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        String brevtekst = TekstformattererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();

        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
    }

    private void opprettHistorikkinnslag(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse, Long fagsakId, AktørId aktørId) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            fagsakId,
            behandlingId,
            aktørId,
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
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = fellesInfoTilBrevTjeneste.hentBehandlingFpsak(eksternBehandling.getEksternUuid(), saksnummer);
        eksternBehandlingsinfoDto.setFagsaktype(fellesInfoTilBrevTjeneste.henteFagsakYtelseType(behandling));
        //Henter data fra tps
        String aktørId = behandling.getAktørId().getId();
        Personinfo personinfo = fellesInfoTilBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = fellesInfoTilBrevTjeneste.hentAdresse(personinfo, aktørId);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = eksternBehandlingsinfoDto.getSprakkode();
        KodeDto ytelsetype = eksternBehandlingsinfoDto.getFagsaktype();
        YtelseNavn ytelseNavn = fellesInfoTilBrevTjeneste.hentYtelsenavn(ytelsetype, mottakersSpråkkode);

        //Henter data fra fpoppdrag
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = fellesInfoTilBrevTjeneste.hentFeilutbetaltePerioder(behandlingIdIFpsak);

        return VarselbrevUtil.sammenstillInfoFraFagsystemerForSending(
            eksternBehandlingsinfoDto,
            saksnummer,
            adresseinfo,
            personinfo,
            feilutbetaltePerioderDto,
            fellesInfoTilBrevTjeneste.getBrukersSvarfrist(),
            ytelseNavn);
    }

    public VarselbrevSamletInfo lagVarselbrevForForhåndsvisning(UUID behandlingUuId, Saksnummer saksnummer, String varseltekst, FagsakYtelseType fagsakYtleseType) {

        EksternBehandlingsinfoDto eksternBehandlingsinfo = fellesInfoTilBrevTjeneste.hentBehandlingFpsak(behandlingUuId, saksnummer);
        eksternBehandlingsinfo.setFagsaktype(new KodeDto(fagsakYtleseType.getKodeverk(), fagsakYtleseType.getKode(), fagsakYtleseType.getNavn()));

        String aktørId = eksternBehandlingsinfo.getPersonopplysningDto().getAktoerId();
        Personinfo personinfo = fellesInfoTilBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = fellesInfoTilBrevTjeneste.hentAdresse(personinfo, aktørId);
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = fellesInfoTilBrevTjeneste.hentFeilutbetaltePerioder(eksternBehandlingsinfo.getId());

        if (eksternBehandlingsinfo.getSprakkode() == null) {
            eksternBehandlingsinfo.setSprakkode(Språkkode.nb);
        }
        Språkkode mottakersSpråkkode = eksternBehandlingsinfo.getSprakkode();
        YtelseNavn ytelseNavn = fellesInfoTilBrevTjeneste.hentYtelsenavn(eksternBehandlingsinfo.getFagsaktype(), mottakersSpråkkode);

        return VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
            saksnummer,
            varseltekst,
            adresseinfo,
            eksternBehandlingsinfo,
            feilutbetaltePerioderDto,
            fellesInfoTilBrevTjeneste.getBrukersSvarfrist(),
            ytelseNavn);
    }
}
