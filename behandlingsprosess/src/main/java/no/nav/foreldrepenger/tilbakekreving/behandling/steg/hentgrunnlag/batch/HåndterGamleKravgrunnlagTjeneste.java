package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ManglendeKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.SperringKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.UkjentOppdragssystemException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;

@ApplicationScoped
public class HåndterGamleKravgrunnlagTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(HåndterGamleKravgrunnlagTjeneste.class);
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private HentKravgrunnlagMapper hentKravgrunnlagMapper;
    private KravgrunnlagMapper lesKravgrunnlagMapper;
    private BehandlingTjeneste behandlingTjeneste;
    private ØkonomiConsumer økonomiConsumer;
    private FagsystemKlient fagsystemKlient;

    HåndterGamleKravgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagTjeneste(ØkonomiMottattXmlRepository mottattXmlRepository,
                                            KravgrunnlagRepository grunnlagRepository,
                                            HentKravgrunnlagMapper hentKravgrunnlagMapper,
                                            KravgrunnlagMapper kravgrunnlagMapper,
                                            BehandlingTjeneste behandlingTjeneste,
                                            ØkonomiConsumer økonomiConsumer,
                                            FagsystemKlient fagsystemKlient) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.grunnlagRepository = grunnlagRepository;
        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
        this.lesKravgrunnlagMapper = kravgrunnlagMapper;
        this.behandlingTjeneste = behandlingTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.fagsystemKlient = fagsystemKlient;
    }

    protected KravgrunnlagMedStatus hentKravgrunnlagFraØkonomi(ØkonomiXmlMottatt økonomiXmlMottatt) {
        String melding = økonomiXmlMottatt.getMottattXml();
        long mottattXmlId = økonomiXmlMottatt.getId();
        DetaljertKravgrunnlag detaljertKravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, melding);
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = forberedHentKravgrunnlagRequest(detaljertKravgrunnlag);
        try {
            DetaljertKravgrunnlagDto detaljertKravgrunnlagDto = økonomiConsumer.hentKravgrunnlag(null, hentKravgrunnlagDetalj);
            LOG.info("Referanse fra WS: {}", detaljertKravgrunnlagDto.getReferanse());
            return KravgrunnlagMedStatus.forIkkeSperretKravgrunnlag(hentKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlagDto));
        } catch (ManglendeKravgrunnlagException e) {
            LOG.info("Kravgrunnlag mangler i økonomi med følgende respons:{}", e.getMessage());
            arkiverMotattXml(mottattXmlId, melding);
        } catch (SperringKravgrunnlagException e) {
            LOG.info("Kravgrunnlag er sperret med følgende respons:{}", e.getMessage());
            return hentSperretKravgrunnlag(økonomiXmlMottatt);
        } catch (UkjentOppdragssystemException e) {
            // ikke arkiver xml i tilfelle ukjent feil kommer fra økonomi
            LOG.warn(e.getMessage());
        }
        LOG.info("Finner ikke grunnlag.");
        return KravgrunnlagMedStatus.utenGrunnlag();
    }

    protected void arkiverMotattXml(Long mottattXmlId, String melding) {
        mottattXmlRepository.arkiverMottattXml(mottattXmlId, melding);
    }

    protected void slettMottattUgyldigKravgrunnlag(long mottattXmlId) {
        // slettes kun xmlene fra OKO_XML_MOTTATT som er arkivert
        if (mottattXmlRepository.erMottattXmlArkivert(mottattXmlId)) {
            mottattXmlRepository.slettMottattXml(mottattXmlId);
        }
    }

    protected Optional<Long> håndterKravgrunnlagRespons(Long mottattXmlId, String melding,
                                                        KravgrunnlagMedStatus kravgrunnlagMedStatus) {
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagMedStatus.getKravgrunnlag();
        try {
            LOG.info("Referanse før validering: {}", kravgrunnlag431.getReferanse());
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);
            String saksnummer = kravgrunnlag431.getSaksnummer().getVerdi();
            //TODO k9-tilbake bytt String -> Saksnummer
            if (!finnesBehandling(saksnummer)) {
                Henvisning henvisning = kravgrunnlag431.getReferanse();
                Optional<EksternBehandlingsinfoDto> ytelsebehandling = hentYtelsebehandlingFraFagsaksystemet(saksnummer, henvisning);
                if (ytelsebehandling.isEmpty()) {
                    arkiverMotattXml(mottattXmlId, melding);
                    return Optional.of(mottattXmlId);
                } else {
                    håndterGyldigkravgrunnlag(mottattXmlId, saksnummer, kravgrunnlagMedStatus, ytelsebehandling.get());
                }
            } else {
                håndterKravgrunnlagHvisBehandlingFinnes(mottattXmlId, kravgrunnlagMedStatus, saksnummer);
            }
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            LOG.warn("Kravgrunnlag for saksnummer{} med id={} er ugyldig og feiler med følgende exception:{}",
                kravgrunnlag431.getSaksnummer(),
                kravgrunnlag431.getEksternKravgrunnlagId(),
                e.getMessage());
        }
        return Optional.empty();
    }

    protected ØkonomiXmlMottatt hentGammeltKravgrunnlag(Long mottattXmlId) {
        return mottattXmlRepository.finnMottattXml(mottattXmlId);
    }

    private void håndterKravgrunnlagHvisBehandlingFinnes(Long mottattXmlId,
                                                         KravgrunnlagMedStatus kravgrunnlagMedStatus,
                                                         String saksnummer) {
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagMedStatus.getKravgrunnlag();
        hentAktivBehandling(saksnummer).ifPresent(behandling -> {
            var behandlingId = behandling.getId();
            LOG.info("Lagrer mottatt kravgrunnlaget for behandling med behandlingId={}", behandlingId);
            grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
            if (kravgrunnlagMedStatus.erKravgrunnlagSperret()) {
                LOG.info("Mottatt Kravgrunnlaget med kravgrunnlagId={} for behandling med behandlingId={} er sperret hos økonomi. Derfor sperrer det i fptilbake også.",
                    kravgrunnlag431.getEksternKravgrunnlagId(), behandlingId);
                sperrGrunnlag(behandlingId, kravgrunnlag431.getEksternKravgrunnlagId());
            }
        });
        tilkobleMottattXml(mottattXmlId);
    }

    private boolean finnesBehandling(String saksnummer) {
        Optional<Behandling> aktivBehandling = hentAktivBehandling(saksnummer);
        if (aktivBehandling.isPresent()) {
            long behandlingId = aktivBehandling.get().getId();
            LOG.info("Behandling med behandlingId={} finnes allerede for saksnummer={}.Kan ikke opprette behandling igjen!", behandlingId, saksnummer);
            if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId) && !grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
                LOG.info("Behandling med behandlingId={} har et aktivt grunnlag", behandlingId);
            }
            return true;
        }
        return false;
    }

    private Optional<Behandling> hentAktivBehandling(String saksnummer) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer(saksnummer));
        return behandlinger.stream().filter(behandling -> !behandling.erAvsluttet()).findFirst();
    }

    private Optional<EksternBehandlingsinfoDto> hentYtelsebehandlingFraFagsaksystemet(String saksnummer, Henvisning henvisning) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            return eksternBehandlinger.stream()
                .filter(eksternBehandlingsinfoDto -> eksternBehandlingsinfoDto.getHenvisning().equals(henvisning)).findAny();
        }
        //FIXME k9-tilbake Må tilpasse for å støtte også k9
        LOG.warn("Saksnummer={} finnes ikke i fpsak", saksnummer);
        return Optional.empty();
    }

    private void oppdaterMedHenvisningOgSaksnummer(Long mottattXmlId, Henvisning henvisning, String saksnummer) {
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);
    }

    private long opprettBehandling(EksternBehandlingsinfoDto eksternBehandlingData) {
        UUID eksternBehandlingUuid = eksternBehandlingData.getUuid();
        Henvisning henvisning = eksternBehandlingData.getHenvisning();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.FAGSAK,
            Tillegsinformasjon.PERSONOPPLYSNINGER);
        FagsakYtelseType fagsakYtelseType = samletEksternBehandlingInfo.getFagsak().getSakstype();
        Saksnummer saksnummer = samletEksternBehandlingInfo.getSaksnummer();
        AktørId aktørId = samletEksternBehandlingInfo.getAktørId();
        return behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternBehandlingUuid, henvisning, aktørId, fagsakYtelseType, BehandlingType.TILBAKEKREVING);
    }

    private void håndterGyldigkravgrunnlag(Long mottattXmlId, String saksnummer,
                                           KravgrunnlagMedStatus kravgrunnlagMedStatus,
                                           EksternBehandlingsinfoDto eksternBehandlingData) {
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagMedStatus.getKravgrunnlag();
        Henvisning henvisning = kravgrunnlag431.getReferanse();
        oppdaterMedHenvisningOgSaksnummer(mottattXmlId, henvisning, saksnummer);
        long behandlingId = opprettBehandling(eksternBehandlingData);
        LOG.info("Behandling opprettet med behandlingId={}", behandlingId);
        lagreGrunnlag(behandlingId, kravgrunnlag431);
        tilkobleMottattXml(mottattXmlId);
        if (kravgrunnlagMedStatus.erKravgrunnlagSperret()) {
            sperrGrunnlag(behandlingId, kravgrunnlag431.getEksternKravgrunnlagId());
        }
    }

    private HentKravgrunnlagDetaljDto forberedHentKravgrunnlagRequest(DetaljertKravgrunnlag detaljertKravgrunnlag) {
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = new HentKravgrunnlagDetaljDto();
        hentKravgrunnlagDetalj.setKravgrunnlagId(detaljertKravgrunnlag.getKravgrunnlagId());
        hentKravgrunnlagDetalj.setKodeAksjon(KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG.getKode());
        hentKravgrunnlagDetalj.setEnhetAnsvarlig(detaljertKravgrunnlag.getEnhetAnsvarlig());
        hentKravgrunnlagDetalj.setSaksbehId(detaljertKravgrunnlag.getSaksbehId());
        return hentKravgrunnlagDetalj;
    }

    private void lagreGrunnlag(long behandlingId, Kravgrunnlag431 kravgrunnlag431) {
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        LOG.info("Hentet kravgrunnlag med kravgrunnlagId={} lagret for behandlingId={}", kravgrunnlag431.getEksternKravgrunnlagId(), behandlingId);
    }

    private void tilkobleMottattXml(long mottattXmlId) {
        mottattXmlRepository.opprettTilkobling(mottattXmlId);
        LOG.info("Mottatt XML med id={} er tilkoblet", mottattXmlId);
    }

    private void sperrGrunnlag(long behandlingId, String kravgrunnlagId) {
        grunnlagRepository.sperrGrunnlag(behandlingId);
        LOG.info("Grunnlag med kravgrunnlagId={} er sperret for behandling={}", kravgrunnlagId, behandlingId);
    }

    private KravgrunnlagMedStatus hentSperretKravgrunnlag(ØkonomiXmlMottatt økonomiXmlMottatt) {
        long mottattXmlId = økonomiXmlMottatt.getId();
        String melding = økonomiXmlMottatt.getMottattXml();
        DetaljertKravgrunnlag detaljertKravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, melding);
        Kravgrunnlag431 kravgrunnlag431 = lesKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlag);
        return KravgrunnlagMedStatus.forSperretKravgrunnlag(kravgrunnlag431);
    }

}
