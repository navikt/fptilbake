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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagFeil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ManglendeKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;

@ApplicationScoped
public class HåndterGamleKravgrunnlagTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagTjeneste.class);
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private HentKravgrunnlagMapper hentKravgrunnlagMapper;
    private BehandlingTjeneste behandlingTjeneste;
    private ØkonomiConsumer økonomiConsumer;
    private FpsakKlient fpsakKlient;

    HåndterGamleKravgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagTjeneste(ØkonomiMottattXmlRepository mottattXmlRepository,
                                            KravgrunnlagRepository grunnlagRepository,
                                            HentKravgrunnlagMapper hentKravgrunnlagMapper,
                                            BehandlingTjeneste behandlingTjeneste,
                                            ØkonomiConsumer økonomiConsumer,
                                            FpsakKlient fpsakKlient) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.grunnlagRepository = grunnlagRepository;
        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
        this.behandlingTjeneste = behandlingTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.fpsakKlient = fpsakKlient;
    }

    protected List<ØkonomiXmlMottatt> hentGamleMeldinger(LocalDate bestemtDato) {
        logger.info("Henter kravgrunnlag som er eldre enn {}", bestemtDato);
        return mottattXmlRepository.hentGamleUkobledeMottattXml(bestemtDato.atStartOfDay());
    }

    protected Optional<Kravgrunnlag431> hentKravgrunnlagFraØkonomi(ØkonomiXmlMottatt økonomiXmlMottatt) {
        String melding = økonomiXmlMottatt.getMottattXml();
        long mottattXmlId = økonomiXmlMottatt.getId();
        DetaljertKravgrunnlag detaljertKravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, melding);
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = forberedHentKravgrunnlagRequest(detaljertKravgrunnlag);
        try {
            DetaljertKravgrunnlagDto detaljertKravgrunnlagDto = økonomiConsumer.hentKravgrunnlag(null, hentKravgrunnlagDetalj);
            return Optional.of(hentKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlagDto));
        } catch (ManglendeKravgrunnlagException e) {
            logger.warn(e.getMessage());
            arkiverMotattXml(mottattXmlId, melding);
            return Optional.empty();
        }
    }

    protected boolean finnesBehandling(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(saksnummer);
        Optional<Behandling> aktivBehandling = behandlinger.stream().filter(behandling -> !behandling.erAvsluttet()).findFirst();
        if (aktivBehandling.isPresent()) {
            long behandlingId = aktivBehandling.get().getId();
            logger.info("Behandling med behandlingId={} finnes allerede for saksnummer={}.Kan ikke opprette behandling igjen!", behandlingId, saksnummer.getVerdi());
            if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId) && !grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
                logger.info("Behandling med behandlingId={} har et aktivt grunnlag. Kravgrunnlaget kan ikke brukes lenger og arkiveres!", behandlingId);
                throw KravgrunnlagFeil.FEILFACTORY.kravgrunnlagetKanIkkeBrukes(behandlingId).toException();
            }
            return true;
        }
        return false;
    }

    protected Optional<EksternBehandlingsinfoDto> hentDataFraFpsak(String saksnummer, Long eksternBehandlingId) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fpsakKlient.hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            return eksternBehandlinger.stream()
                    .filter(eksternBehandlingsinfoDto -> eksternBehandlingsinfoDto.getId().equals(eksternBehandlingId)).findAny();
        }
        logger.warn("Saksnummer={} finnes ikke i fpsak", saksnummer);
        return Optional.empty();
    }

    protected void arkiverMotattXml(Long mottattXmlId, String melding) {
        mottattXmlRepository.arkiverMottattXml(mottattXmlId, melding);
    }

    protected void oppdaterMedEksternBehandlingIdOgSaksnummer(Long mottattXmlId, String eksternBehandlingId, String saksnummer) {
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(eksternBehandlingId, saksnummer, mottattXmlId);
    }

    protected long opprettBehandling(EksternBehandlingsinfoDto eksternBehandlingData) {
        UUID eksternBehandlingUuid = eksternBehandlingData.getUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = fpsakKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.FAGSAK);
        FagsakYtelseType fagsakYtelseType = samletEksternBehandlingInfo.getFagsak().getSakstype();
        return behandlingTjeneste.opprettBehandlingManuell(samletEksternBehandlingInfo.getSaksnummer(), eksternBehandlingUuid, fagsakYtelseType, BehandlingType.TILBAKEKREVING);
    }

    protected void slettMottattGamleKravgrunnlag(List<Long> gammelKravgrunnlagListe) {
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
