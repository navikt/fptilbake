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
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
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
import no.nav.foreldrepenger.tilbakekreving.grunnlag.AktivKravgrunnlagAllerdeFinnesException;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagFeil;
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
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class HåndterGamleKravgrunnlagTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagTjeneste.class);
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private HentKravgrunnlagMapper hentKravgrunnlagMapper;
    private KravgrunnlagMapper lesKravgrunnlagMapper;
    private BehandlingTjeneste behandlingTjeneste;
    private ØkonomiConsumer økonomiConsumer;
    private FpsakKlient fpsakKlient;

    private boolean skalGrunnlagSperres;

    HåndterGamleKravgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagTjeneste(ØkonomiMottattXmlRepository mottattXmlRepository,
                                            KravgrunnlagRepository grunnlagRepository,
                                            HentKravgrunnlagMapper hentKravgrunnlagMapper,
                                            KravgrunnlagMapper lesKravgrunnlagMapper,
                                            BehandlingTjeneste behandlingTjeneste,
                                            ØkonomiConsumer økonomiConsumer,
                                            FpsakKlient fpsakKlient) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.grunnlagRepository = grunnlagRepository;
        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
        this.lesKravgrunnlagMapper = lesKravgrunnlagMapper;
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
        } catch (SperringKravgrunnlagException e) {
            logger.warn(e.getMessage());
            return hentSperretKravgrunnlag(økonomiXmlMottatt);
        } catch (UkjentOppdragssystemException e) {
            // ikke arkiver xml i tilfelle ukjent feil kommer fra økonomi
            logger.warn(e.getMessage());
        }
        return Optional.empty();
    }

    protected boolean finnesBehandling(Saksnummer saksnummer, long mottattXmlId) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(saksnummer);
        Optional<Behandling> aktivBehandling = behandlinger.stream().filter(behandling -> !behandling.erAvsluttet()).findFirst();
        if (aktivBehandling.isPresent()) {
            long behandlingId = aktivBehandling.get().getId();
            logger.info("Behandling med behandlingId={} finnes allerede for saksnummer={}.Kan ikke opprette behandling igjen!", behandlingId, saksnummer.getVerdi());
            if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId) && !grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
                logger.info("Behandling med behandlingId={} har et aktivt grunnlag. Kravgrunnlaget kan ikke brukes lenger og arkiveres!", behandlingId);
                throw KravgrunnlagFeil.FEILFACTORY.kravgrunnlagetKanIkkeBrukes(mottattXmlId).toException();
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
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = fpsakKlient.hentBehandlingsinfo(eksternBehandlingUuid, Tillegsinformasjon.FAGSAK, Tillegsinformasjon.PERSONOPPLYSNINGER);
        FagsakYtelseType fagsakYtelseType = samletEksternBehandlingInfo.getFagsak().getSakstype();
        return behandlingTjeneste.opprettBehandlingAutomatisk(samletEksternBehandlingInfo.getSaksnummer(), eksternBehandlingUuid, eksternBehandlingData.getId(),
            samletEksternBehandlingInfo.getAktørId(), fagsakYtelseType, BehandlingType.TILBAKEKREVING);
    }

    protected void slettMottattGamleKravgrunnlag(List<Long> gammelKravgrunnlagListe) {
        // slettes kun xmlene fra OKO_XML_MOTTATT som er arkivert
        gammelKravgrunnlagListe.stream().filter(mottattXMlId -> mottattXmlRepository.erMottattXmlArkivert(mottattXMlId))
            .forEach(mottattXmlId -> mottattXmlRepository.slettMottattXml(mottattXmlId));
    }

    protected Optional<Long> håndterKravgrunnlagRespons(Long mottattXmlId, String melding, Kravgrunnlag431 kravgrunnlag431) {
        try {
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);
            String saksnummer = finnSaksnummer(kravgrunnlag431.getFagSystemId());
            if (!finnesBehandling(new Saksnummer(saksnummer), mottattXmlId)) {
                Long eksternBehandlingId = Long.valueOf(kravgrunnlag431.getReferanse());
                Optional<EksternBehandlingsinfoDto> fpsakBehandling = hentDataFraFpsak(saksnummer, eksternBehandlingId);
                if (fpsakBehandling.isEmpty()) {
                    arkiverMotattXml(mottattXmlId, melding);
                    return Optional.of(mottattXmlId);
                } else {
                    håndterGyldigkravgrunnlag(mottattXmlId, saksnummer, kravgrunnlag431, fpsakBehandling.get());
                }
            }
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException | AktivKravgrunnlagAllerdeFinnesException e) {
            logger.warn(e.getMessage());
            arkiverMotattXml(mottattXmlId, melding);
            return Optional.of(mottattXmlId);
        }
        return Optional.empty();
    }

    private void håndterGyldigkravgrunnlag(Long mottattXmlId, String saksnummer,
                                           Kravgrunnlag431 kravgrunnlag431,
                                           EksternBehandlingsinfoDto eksternBehandlingData) {
        String eksternBehandlingId = kravgrunnlag431.getReferanse();
        oppdaterMedEksternBehandlingIdOgSaksnummer(mottattXmlId, eksternBehandlingId, saksnummer);
        if (erTestMiljø()) {
            Long behandlingId = opprettBehandling(eksternBehandlingData);
            logger.info("Behandling opprettet med behandlingId={}", behandlingId);
            lagreGrunnlag(behandlingId, kravgrunnlag431);
            tilkobleMottattXml(mottattXmlId);
            if (skalGrunnlagSperres) {
                sperrGrunnlag(behandlingId, kravgrunnlag431.getEksternKravgrunnlagId());
                skalGrunnlagSperres = false;
            }
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

    private String finnSaksnummer(String fagsystemId) {
        return fagsystemId.substring(0, fagsystemId.length() - 3);
    }

    private void lagreGrunnlag(long behandlingId, Kravgrunnlag431 kravgrunnlag431) {
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        logger.info("Hentet kravgrunnlag med kravgrunnlagId={} lagret for behandlingId={}", kravgrunnlag431.getEksternKravgrunnlagId(), behandlingId);
    }

    private void tilkobleMottattXml(long mottattXmlId) {
        mottattXmlRepository.opprettTilkobling(mottattXmlId);
        logger.info("Mottatt XML med id={} er tilkoblet", mottattXmlId);
    }

    private void sperrGrunnlag(long behandlingId, String kravgrunnlagId) {
        grunnlagRepository.sperrGrunnlag(behandlingId);
        logger.info("Grunnlag med kravgrunnlagId={} er sperret for behandling={}", kravgrunnlagId, behandlingId);
    }

    private Optional<Kravgrunnlag431> hentSperretKravgrunnlag(ØkonomiXmlMottatt økonomiXmlMottatt) {
        long mottattXmlId = økonomiXmlMottatt.getId();
        String melding = økonomiXmlMottatt.getMottattXml();
        DetaljertKravgrunnlag detaljertKravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, melding);
        Kravgrunnlag431 kravgrunnlag431 = lesKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlag);
        skalGrunnlagSperres = true;
        return Optional.of(kravgrunnlag431);
    }

    //midlertidig kode. skal fjernes etter en stund
    protected boolean erTestMiljø() {
        //foreløpig kun på for testing
        boolean isEnabled = !Environment.current().isProd();
        logger.info("{} er {}", "Opprett behandling når kravgrunnlag venter etter fristen er ", isEnabled ? "skudd på" : "ikke skudd på");
        return isEnabled;
    }
}
