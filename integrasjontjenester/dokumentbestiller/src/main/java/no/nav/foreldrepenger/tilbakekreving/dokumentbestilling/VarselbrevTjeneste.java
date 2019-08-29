package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.VarselbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;


@ApplicationScoped
public class VarselbrevTjeneste {

    private FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private EksternBehandlingRepository eksternBehandlingRepository;

    @Inject
    public VarselbrevTjeneste(FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste, BehandlingTjeneste behandlingTjeneste, EksternBehandlingRepository eksternBehandlingRepository) {
        this.fellesInfoTilBrevTjeneste = fellesInfoTilBrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
    }

    public VarselbrevTjeneste() {
    }

    public VarselbrevSamletInfo lagVarselbrevForSending(Long behandlingId) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        //Henter data fra fptilbakes eget repo for å finne behandlingsid brukt i fpsak, samt saksnummer
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Long behandlingIdIFpsak = eksternBehandling.getEksternId();

        //Henter data fra fpsak
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = fellesInfoTilBrevTjeneste.hentBehandlingFpsak(eksternBehandling.getEksternUuid(), saksnummer.getVerdi());
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

    public VarselbrevSamletInfo lagVarselbrevForForhåndsvisning(UUID behandlingUuId, String saksnummer, String varseltekst, String fagsakYtleseType) {

        EksternBehandlingsinfoDto eksternBehandlingsinfo = fellesInfoTilBrevTjeneste.hentBehandlingFpsak(behandlingUuId, saksnummer);
        FagsakYtelseType type = FagsakYtelseType.fraKode(fagsakYtleseType);
        eksternBehandlingsinfo.setFagsaktype(new KodeDto(type.getKodeverk(),type.getKode(),type.getNavn()));

        String aktørId = eksternBehandlingsinfo.getPersonopplysningDto().getAktoerId();
        Personinfo personinfo = fellesInfoTilBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = fellesInfoTilBrevTjeneste.hentAdresse(personinfo, aktørId);
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = fellesInfoTilBrevTjeneste.hentFeilutbetaltePerioder(eksternBehandlingsinfo.getId());

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
