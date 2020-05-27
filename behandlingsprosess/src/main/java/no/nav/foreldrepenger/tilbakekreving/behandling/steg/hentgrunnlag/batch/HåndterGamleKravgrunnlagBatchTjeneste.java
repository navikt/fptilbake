package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.time.Clock;
import java.time.DayOfWeek;
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
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class HåndterGamleKravgrunnlagBatchTjeneste implements BatchTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagBatchTjeneste.class);
    private static final String BATCHNAVN = "BFPT-002";

    private HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste;
    private Clock clock;
    private Period venteFrist;

    HåndterGamleKravgrunnlagBatchTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagBatchTjeneste(HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste,
                                                 @KonfigVerdi(value = "frist.grunnlag.tbkg") Period ventefrist) {
        this.håndterGamleKravgrunnlagTjeneste = håndterGamleKravgrunnlagTjeneste;
        this.clock = Clock.systemDefaultZone();
        this.venteFrist = ventefrist;
    }

    // kun for test forbruk
    public HåndterGamleKravgrunnlagBatchTjeneste(HåndterGamleKravgrunnlagTjeneste håndterGamleKravgrunnlagTjeneste,
                                                 Clock clock,
                                                 @KonfigVerdi(value = "frist.grunnlag.tbkg") Period ventefrist) {
        this.håndterGamleKravgrunnlagTjeneste = håndterGamleKravgrunnlagTjeneste;
        this.clock = clock;
        this.venteFrist = ventefrist;
    }

    @Override
    public String launch(BatchArguments arguments) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);
        if (iDag.getDayOfWeek().equals(DayOfWeek.SATURDAY) || iDag.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            logger.info("I dag er helg, kan ikke kjøre batch-en {}", BATCHNAVN);
            return batchRun;
        }
        venteFrist = venteFrist.multipliedBy(2); // hardkoded for nå, en midlertidig løsning. Det blir fjernet når batchen lanseres fullstending i PROD
        LocalDate bestemtDato = iDag.minus(venteFrist);
        logger.info("Håndterer kravgrunnlag som er eldre enn {} i batch {}", bestemtDato, batchRun);

        List<ØkonomiXmlMottatt> alleGamleMeldinger = håndterGamleKravgrunnlagTjeneste.hentGamleMeldinger(bestemtDato);
        List<ØkonomiXmlMottatt> alleGamleKravgrunnlag = alleGamleMeldinger.stream()
            .filter(økonomiXmlMottatt -> økonomiXmlMottatt.getMottattXml().contains(TaskProperty.ROOT_ELEMENT_KRAVGRUNNLAG_XML))
            .collect(Collectors.toList());

        if (alleGamleKravgrunnlag.isEmpty()) {
            logger.info("Det finnes ingen gammel kravgrunnlag før {}", bestemtDato);
        } else {
            logger.info("Det finnes {} gamle kravgrunnlag før {}", alleGamleKravgrunnlag.size(), bestemtDato);
            håndterGamleKravgrunnlag(alleGamleKravgrunnlag);
        }
        return batchRun;
    }

    private void håndterGamleKravgrunnlag(List<ØkonomiXmlMottatt> alleGamleKravgrunnlag) {
        List<Long> slettesXmlListe = new ArrayList<>();
        for (ØkonomiXmlMottatt økonomiXmlMottatt : alleGamleKravgrunnlag) {
            Long mottattXmlId = økonomiXmlMottatt.getId();
            Optional<Kravgrunnlag431> respons = håndterGamleKravgrunnlagTjeneste.hentKravgrunnlagFraØkonomi(økonomiXmlMottatt);
            if (respons.isEmpty()) {
                slettesXmlListe.add(mottattXmlId);
            } else {
                Optional<Long> ugyldigkravgrunnlag = håndterGamleKravgrunnlagTjeneste.
                    håndterKravgrunnlagRespons(mottattXmlId, økonomiXmlMottatt.getMottattXml(), respons.get());
                ugyldigkravgrunnlag.ifPresent(slettesXmlListe::add);
            }
        }
        if (!slettesXmlListe.isEmpty()) {
            håndterGamleKravgrunnlagTjeneste.slettMottattGamleKravgrunnlag(slettesXmlListe);
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

}
