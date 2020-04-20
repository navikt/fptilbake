package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class HåndterGamleKravgrunnlagBatchTjeneste implements BatchTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagBatchTjeneste.class);
    private static final String BATCHNAVN = "BFPT-002";

    private HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste;
    private Period venteFrist;

    HåndterGamleKravgrunnlagBatchTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagBatchTjeneste(HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste,
                                                 @KonfigVerdi(value = "frist.grunnlag.tbkg") Period ventefrist) {
        this.håndterGamleKravgrunnlagTjeneste = håndterGamleKravgrunnlagTjeneste;
        this.venteFrist = ventefrist;
    }

    @Override
    public String launch(BatchArguments arguments) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        LocalDate bestemtDato = LocalDate.now().minus(venteFrist);
        logger.info("Håndterer kravgrunnlag som er eldre enn {} i batch {}", bestemtDato, batchRun);

        List<ØkonomiXmlMottatt> alleGamleMeldinger = håndterGamleKravgrunnlagTjeneste.hentGamleMeldinger(bestemtDato);
        List<ØkonomiXmlMottatt> alleGamleKravgrunnlag = alleGamleMeldinger.stream()
            .filter(økonomiXmlMottatt -> økonomiXmlMottatt.getMottattXml().contains(TaskProperty.ROOT_ELEMENT_KRAVGRUNNLAG_XML))
            .collect(Collectors.toList());

        if (alleGamleKravgrunnlag.isEmpty()) {
            logger.info("Det finnes ingen gammel kravgrunnlag før {}", bestemtDato);
        } else {
            logger.info("Det finnes {} gamle kravgrunnlag før {}", alleGamleKravgrunnlag.size(), bestemtDato);
            List<Long> slettesXmlListe = new ArrayList<>();
            for (ØkonomiXmlMottatt økonomiXmlMottatt : alleGamleKravgrunnlag) {
                Long mottattXmlId = økonomiXmlMottatt.getId();
                Optional<Kravgrunnlag431> respons = håndterGamleKravgrunnlagTjeneste.hentKravgrunnlagFraØkonomi(økonomiXmlMottatt);
                if (respons.isEmpty()) {
                    slettesXmlListe.add(mottattXmlId);
                } else {
                    Optional<Long> ugyldigkravgrunnlag = håndterKravgrunnlagRespons(mottattXmlId, økonomiXmlMottatt.getMottattXml(), respons.get());
                    ugyldigkravgrunnlag.ifPresent(slettesXmlListe::add);
                }
            }
            //slette gamle kravgrunnlag som ikke finnes i Økonomi fra OKO_XML_MOTTATT
            if (!slettesXmlListe.isEmpty()) {
                logger.info("Antall Gamle kravgrunnlag som skal slettes fra OKO_XML_MOTTATT er {}", slettesXmlListe);
                håndterGamleKravgrunnlagTjeneste.slettMottattGamleKravgrunnlag(slettesXmlListe);
            }
        }
        return batchRun;
    }

    private Optional<Long> håndterKravgrunnlagRespons(Long mottattXmlId, String melding, Kravgrunnlag431 kravgrunnlag431) {
        try {
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);
            String saksnummer = finnSaksnummer(kravgrunnlag431.getFagSystemId());
            if (!håndterGamleKravgrunnlagTjeneste.finnesBehandling(new Saksnummer(saksnummer))) {
                Long eksternBehandlingId = Long.valueOf(kravgrunnlag431.getReferanse());
                Optional<EksternBehandlingsinfoDto> fpsakBehandling = håndterGamleKravgrunnlagTjeneste.hentDataFraFpsak(saksnummer, eksternBehandlingId);
                if (fpsakBehandling.isEmpty()) {
                    håndterGamleKravgrunnlagTjeneste.arkiverMotattXml(mottattXmlId, melding);
                    return Optional.of(mottattXmlId);
                } else {
                    håndterGyldigkravgrunnlag(mottattXmlId, saksnummer, kravgrunnlag431.getReferanse(), fpsakBehandling.get());
                }
            }
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            logger.warn(e.getMessage());
            håndterGamleKravgrunnlagTjeneste.arkiverMotattXml(mottattXmlId, melding);
            return Optional.of(mottattXmlId);
        }
        return Optional.empty();
    }

    private void håndterGyldigkravgrunnlag(Long mottattXmlId, String saksnummer, String eksternBehandlingId, EksternBehandlingsinfoDto eksternBehandlingData) {
        håndterGamleKravgrunnlagTjeneste.oppdaterMedEksternBehandlingIdOgSaksnummer(mottattXmlId, eksternBehandlingId, saksnummer);
        if (erTestMiljø()) {
            Long behandlingId = håndterGamleKravgrunnlagTjeneste.opprettBehandling(eksternBehandlingData);
            logger.info("Behandling opprettet med behandlingId={}", behandlingId);
        } else {
            logger.info("Behandling for saksnummer={} og eksternBehandlingId={} bør opprettes her", saksnummer, eksternBehandlingId);
        }

    }

    @Override
    public BatchStatus status(String batchInstanceNumber) {
        // Antar her at alt har gått bra siden denne er en synkron jobb.
        return BatchStatus.OK;
    }

    @Override
    public String getBatchName() {
        return BATCHNAVN;
    }

    private String finnSaksnummer(String fagsystemId) {
        return fagsystemId.substring(0, fagsystemId.length() - 3);
    }

    //midlertidig kode. skal fjernes etter en stund
    private boolean erTestMiljø() {
        //foreløpig kun på for testing
        boolean isEnabled = !Environment.current().isProd();
        logger.info("{} er {}", "Opprett behandling når kravgrunnlag venter etter fristen er ", isEnabled ? "skudd på" : "ikke skudd på");
        return isEnabled;
    }

}
