package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriodeMedFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class FaktaFeilutbetalingTjeneste {

    private VarselRepository varselRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private FagsystemKlient fagsystemKlient;

    FaktaFeilutbetalingTjeneste() {
        // for CDI proxy
    }

    @Inject
    public FaktaFeilutbetalingTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                       KravgrunnlagTjeneste kravgrunnlagTjeneste,
                                       FagsystemKlient fagsystemKlient) {
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.fagsystemKlient = fagsystemKlient;

        this.faktaFeilutbetalingRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = behandlingRepositoryProvider.getVarselRepository();
        this.grunnlagRepository = behandlingRepositoryProvider.getGrunnlagRepository();
    }

    static LogiskPeriodeMedFaktaDto leggPåFakta(LogiskPeriode logiskPeriode, FaktaFeilutbetaling fakta) {
        LogiskPeriodeMedFaktaDto resultat = LogiskPeriodeMedFaktaDto.lagPeriode(logiskPeriode.getPeriode(), logiskPeriode.getFeilutbetaltBeløp());

        if (fakta != null) {
            Optional<FaktaFeilutbetalingPeriode> feilutbetalingPeriodeÅrsak = fakta.getFeilutbetaltPerioder().stream()
                    .filter(periodeÅrsak -> logiskPeriode.getPeriode().equals(periodeÅrsak.getPeriode()))
                    .findFirst();
            resultat.setFeilutbetalingÅrsakDto(mapFra(feilutbetalingPeriodeÅrsak));
        }
        return resultat;
    }

    public BehandlingFeilutbetalingFakta hentBehandlingFeilutbetalingFakta(Long behandlingId) {
        var resultat = varselRepository.finnVarsel(behandlingId);
        var eksternUuid = hentEksternUuid(behandlingId);
        SamletEksternBehandlingInfo samletBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternUuid, Tillegsinformasjon.TILBAKEKREVINGSVALG);
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = samletBehandlingInfo.getGrunninformasjon();
        TilbakekrevingValgDto tilbakekrevingValg = samletBehandlingInfo.getTilbakekrevingsvalg();

        List<LogiskPeriode> logiskePerioder = kravgrunnlagTjeneste.utledLogiskPeriode(behandlingId);
        FaktaFeilutbetaling fakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId).orElse(null);
        List<LogiskPeriodeMedFaktaDto> logiskePerioderMedFakta = logiskePerioder.stream()
                .map(logiskPeriode -> leggPåFakta(logiskPeriode, fakta))
                .collect(Collectors.toList());

        String begrunnelse = hentFaktaBegrunnelse(behandlingId);
        Long tidligereVarseltBeløp = resultat.map(VarselInfo::getVarselBeløp).orElse(null);
        return BehandlingFeilutbetalingFakta.builder()
                .medPerioder(logiskePerioderMedFakta)
                .medAktuellFeilUtbetaltBeløp(sumFeilutbetaltBeløp(logiskePerioder))
                .medTidligereVarsletBeløp(tidligereVarseltBeløp)
                .medTotalPeriode(omkringliggendePeriode(logiskePerioder))
                .medDatoForRevurderingsvedtak(eksternBehandlingsinfoDto.getVedtakDato())
                .medTilbakekrevingValg(tilbakekrevingValg)
                .medBegrunnelse(begrunnelse)
                .build();
    }

    private UUID hentEksternUuid(Long behandlingId) {
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.hentOptionalFraInternId(behandlingId);
        UUID eksternUuid;
        if (eksternBehandling.isPresent()) {
            eksternUuid = eksternBehandling.get().getEksternUuid();
        } else { // Når behandlinger er avsluttet p.g.a henleggelse, finner ikke aktive Henvisning.
            // Så henter referanse fra koblede grunnlag som ikke kan endres.
            Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandlingId);
            Henvisning henvisning = kravgrunnlag431.getReferanse();
            eksternBehandling = eksternBehandlingRepository.hentEksisterendeDeaktivert(behandlingId, henvisning);
            if (eksternBehandling.isEmpty()) { // Teknisk Feil: referansen til grunnlaget må finne i EksternBehandling
                throw BehandlingFeil.fantIkkeBehandlingMedHenvisning(behandlingId, henvisning);
            }
            eksternUuid = eksternBehandling.get().getEksternUuid();
        }
        return eksternUuid;
    }

    private Periode omkringliggendePeriode(List<LogiskPeriode> perioder) {
        LocalDate totalPeriodeFom = null;
        LocalDate totalPeriodeTom = null;
        for (LogiskPeriode periode : perioder) {
            totalPeriodeFom = totalPeriodeFom == null || totalPeriodeFom.isAfter(periode.getFom()) ? periode.getFom() : totalPeriodeFom;
            totalPeriodeTom = totalPeriodeTom == null || totalPeriodeTom.isBefore(periode.getTom()) ? periode.getTom() : totalPeriodeTom;
        }
        return new Periode(totalPeriodeFom, totalPeriodeTom);
    }

    private BigDecimal sumFeilutbetaltBeløp(List<LogiskPeriode> perioder) {
        return perioder.stream()
                .map(LogiskPeriode::getFeilutbetaltBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

    }

    private static HendelseTypeMedUndertypeDto mapFra(Optional<FaktaFeilutbetalingPeriode> årsak) {
        if (årsak.isPresent()) {
            FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = årsak.get();
            return new HendelseTypeMedUndertypeDto(faktaFeilutbetalingPeriode.getHendelseType(), faktaFeilutbetalingPeriode.getHendelseUndertype());
        }
        return null;
    }


    public String hentFaktaBegrunnelse(Long behandlingId) {
        Optional<FaktaFeilutbetaling> faktaFeilutbetaling = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        return faktaFeilutbetaling.map(FaktaFeilutbetaling::getBegrunnelse).orElse(null);
    }
}
