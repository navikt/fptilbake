package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;

@ApplicationScoped
public class FaktaFeilutbetalingTjeneste {

    private VarselRepository varselRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private FagsystemKlient fagsystemKlient;

    FaktaFeilutbetalingTjeneste() {
        // for CDI proxy
    }

    @Inject
    public FaktaFeilutbetalingTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider, KravgrunnlagTjeneste kravgrunnlagTjeneste, FagsystemKlient fagsystemKlient) {
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.fagsystemKlient = fagsystemKlient;

        this.faktaFeilutbetalingRepository = behandlingRepositoryProvider.getFaktaFeilutbetalingRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = behandlingRepositoryProvider.getVarselRepository();

    }

    public void hentFeilutbetalingÅrsak(Long behandlingId, UtbetaltPeriode utbetaltPeriode) {
        Optional<FaktaFeilutbetaling> fakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        if (fakta.isPresent()) {
            Optional<FaktaFeilutbetalingPeriode> feilutbetalingPeriodeÅrsak = fakta.get()
                .getFeilutbetaltPerioder()
                .stream()
                .filter(periodeÅrsak -> utbetaltPeriode.tilPeriode().equals(periodeÅrsak.getPeriode()))
                .findFirst();
            utbetaltPeriode.setFeilutbetalingÅrsakDto(mapFra(feilutbetalingPeriodeÅrsak));
        }
    }

    public BehandlingFeilutbetalingFakta hentBehandlingFeilutbetalingFakta(Long behandlingId) {
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentForSisteAktivertInternId(behandlingId);
        Optional<VarselInfo> resultat = varselRepository.finnVarsel(behandlingId);
        UUID eksternUuid = eksternBehandling.getEksternUuid();

        SamletEksternBehandlingInfo samletBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternUuid, Tillegsinformasjon.TILBAKEKREVINGSVALG);
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = samletBehandlingInfo.getGrunninformasjon();
        TilbakekrevingValgDto tilbakekrevingValg = samletBehandlingInfo.getTilbakekrevingsvalg();

        List<UtbetaltPeriode> utbetaltPerioder = kravgrunnlagTjeneste.utledLogiskPeriode(behandlingId);
        BigDecimal aktuellFeilUtbetaltBeløp = BigDecimal.ZERO;
        LocalDate totalPeriodeFom = null;
        LocalDate totalPeriodeTom = null;
        for (UtbetaltPeriode utbetaltPeriode : utbetaltPerioder) {
            aktuellFeilUtbetaltBeløp = aktuellFeilUtbetaltBeløp.add(utbetaltPeriode.getBelop());
            hentFeilutbetalingÅrsak(behandlingId, utbetaltPeriode);
            totalPeriodeFom = totalPeriodeFom == null || totalPeriodeFom.isAfter(utbetaltPeriode.getFom()) ? utbetaltPeriode.getFom() : totalPeriodeFom;
            totalPeriodeTom = totalPeriodeTom == null || totalPeriodeTom.isBefore(utbetaltPeriode.getTom()) ? utbetaltPeriode.getTom() : totalPeriodeTom;
        }
        String begrunnelse = hentFaktaBegrunnelse(behandlingId);
        Periode totalPeriode = new Periode(totalPeriodeFom, totalPeriodeTom);
        return lagBehandlingFeilUtbetalingFakta(resultat, aktuellFeilUtbetaltBeløp, utbetaltPerioder, totalPeriode, eksternBehandlingsinfoDto, tilbakekrevingValg, begrunnelse);
    }


    private BehandlingFeilutbetalingFakta lagBehandlingFeilUtbetalingFakta(Optional<VarselInfo> varselEntitet, BigDecimal aktuellFeilUtbetaltBeløp,
                                                                           List<UtbetaltPeriode> utbetaltPerioder, Periode totalPeriode,
                                                                           EksternBehandlingsinfoDto eksternBehandlingsinfoDto, TilbakekrevingValgDto tilbakekrevingValgDto,
                                                                           String begrunnelse) {
        Long tidligereVarseltBeløp = varselEntitet.isPresent() ? varselEntitet.get().getVarselBeløp() : null;
        return BehandlingFeilutbetalingFakta.builder()
            .medPerioder(utbetaltPerioder)
            .medAktuellFeilUtbetaltBeløp(aktuellFeilUtbetaltBeløp)
            .medTidligereVarsletBeløp(tidligereVarseltBeløp)
            .medTotalPeriodeFom(totalPeriode.getFom())
            .medTotalPeriodeTom(totalPeriode.getTom())
            .medDatoForRevurderingsvedtak(eksternBehandlingsinfoDto.getVedtakDato())
            .medBehandlingsResultat(eksternBehandlingsinfoDto.getBehandlingsresultat())
            .medBehandlingÅrsaker(eksternBehandlingsinfoDto.getBehandlingÅrsaker())
            .medTilbakekrevingValg(tilbakekrevingValgDto)
            .medBegrunnelse(begrunnelse)
            .build();
    }

    private HendelseTypeMedUndertypeDto mapFra(Optional<FaktaFeilutbetalingPeriode> årsak) {
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
