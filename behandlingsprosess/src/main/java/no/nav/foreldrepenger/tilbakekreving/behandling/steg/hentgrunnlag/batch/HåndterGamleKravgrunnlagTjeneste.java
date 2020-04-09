package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.vedtak.exception.IntegrasjonException;

@ApplicationScoped
public class HåndterGamleKravgrunnlagTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagTjeneste.class);
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private HentKravgrunnlagMapper hentKravgrunnlagMapper;
    private BehandlingTjeneste behandlingTjeneste;
    private ØkonomiConsumer økonomiConsumer;
    private FpsakKlient fpsakKlient;

    HåndterGamleKravgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagTjeneste(ØkonomiMottattXmlRepository mottattXmlRepository,
                                            HentKravgrunnlagMapper hentKravgrunnlagMapper,
                                            BehandlingTjeneste behandlingTjeneste,
                                            ØkonomiConsumer økonomiConsumer,
                                            FpsakKlient fpsakKlient) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
        this.behandlingTjeneste = behandlingTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.fpsakKlient = fpsakKlient;
    }

    public List<ØkonomiXmlMottatt> hentGamleKravgrunnlag(LocalDate bestemtDato) {
        logger.info("Henter kravgrunnlag som er eldre enn {}", bestemtDato);
        return mottattXmlRepository.hentGamleKravgrunnlagUtenTilkobling(bestemtDato.atStartOfDay());
    }

    public Optional<Kravgrunnlag431> hentKravgrunnlagFraØkonomi(ØkonomiXmlMottatt økonomiXmlMottatt) {
        String melding = økonomiXmlMottatt.getMottattXml();
        long mottattXmlId = økonomiXmlMottatt.getId();
        DetaljertKravgrunnlag detaljertKravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, melding);
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = forberedHentKravgrunnlagRequest(detaljertKravgrunnlag);
        try {
            DetaljertKravgrunnlagDto detaljertKravgrunnlagDto = økonomiConsumer.hentKravgrunnlag(null, hentKravgrunnlagDetalj);
            return Optional.of(hentKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlagDto));
        } catch (IntegrasjonException e) {
            logger.warn(e.getMessage());
            if (e.getMessage().contains("FPT-539080")) {
                arkiverMotattXml(mottattXmlId, melding);
            }
        }
        return Optional.empty();
    }

    public boolean erBehandlingAlleredeFinnes(Saksnummer saksnummer) {
        if (!behandlingTjeneste.hentBehandlinger(saksnummer).isEmpty()) {
            logger.info("Behandling finnes allerede for saksnummer={}.Kan ikke opprette behandling igjen!", saksnummer);
            return true;
        }
        return false;
    }

    public Optional<EksternBehandlingsinfoDto> hentDataFraFpsak(String saksnummer, Long eksternBehandlingId) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fpsakKlient.hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            return eksternBehandlinger.stream()
                .filter(eksternBehandlingsinfoDto -> eksternBehandlingsinfoDto.getId().equals(eksternBehandlingId)).findAny();
        }

        return Optional.empty();
    }

    public void arkiverMotattXml(Long mottattXmlId, String melding){
        mottattXmlRepository.arkiverMottattXml(mottattXmlId, melding);
    }

    public void oppdaterMedEksternBehandlingIdOgSaksnummer(Long mottattXmlId,String eksternBehandlingId, String saksnummer){
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(eksternBehandlingId,saksnummer,mottattXmlId);
    }

    public long opprettBehandling(EksternBehandlingsinfoDto eksternBehandlingData) {
        UUID eksternBehandlingUuid = eksternBehandlingData.getUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = fpsakKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.FAGSAK);
        FagsakYtelseType fagsakYtelseType = FagsakYtelseType.fraKode(samletEksternBehandlingInfo.getFagsak().getSakstype());
        return behandlingTjeneste.opprettBehandlingManuell(samletEksternBehandlingInfo.getSaksnummer(), eksternBehandlingUuid, fagsakYtelseType, BehandlingType.TILBAKEKREVING);
    }

    public void slettGammelKravgrunnlag(List<Long> gammelKravgrunnlagListe) {
        gammelKravgrunnlagListe.forEach(mottattXmlId -> mottattXmlRepository.slettMottattXml(mottattXmlId));
    }

    private HentKravgrunnlagDetaljDto forberedHentKravgrunnlagRequest(DetaljertKravgrunnlag detaljertKravgrunnlag) {
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = new HentKravgrunnlagDetaljDto();
        hentKravgrunnlagDetalj.setKravgrunnlagId(detaljertKravgrunnlag.getKravgrunnlagId());
        hentKravgrunnlagDetalj.setKodeAksjon(KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG.getKode());
        hentKravgrunnlagDetalj.setEnhetAnsvarlig(detaljertKravgrunnlag.getEnhetAnsvarlig());
        hentKravgrunnlagDetalj.setSaksbehId(detaljertKravgrunnlag.getSaksbehId());
        return hentKravgrunnlagDetalj;
    }
}
