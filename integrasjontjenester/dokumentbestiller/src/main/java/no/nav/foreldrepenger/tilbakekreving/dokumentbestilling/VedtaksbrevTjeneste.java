package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.ReturadresseKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VedtaksbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.ForhåndvisningVedtaksbrevTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.AvsnittUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.VedtaksbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;


@ApplicationScoped
public class VedtaksbrevTjeneste {

    private EksternBehandlingRepository eksternBehandlingRepository;
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste;
    private BrevdataRepository brevdataRepository;

    @Inject
    public VedtaksbrevTjeneste(EksternBehandlingRepository eksternBehandlingRepository,
                               TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                               VilkårsvurderingTjeneste vilkårsvurderingTjeneste,
                               BehandlingTjeneste behandlingTjeneste,
                               FellesInfoTilBrevTjeneste fellesInfoTilBrevTjeneste,
                               BrevdataRepository brevdataRepository) {
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.fellesInfoTilBrevTjeneste = fellesInfoTilBrevTjeneste;
        this.brevdataRepository = brevdataRepository;
    }

    public VedtaksbrevTjeneste() {
    }

    public VedtaksbrevSamletInfo lagVedtaksbrev(Long behandlingId,
                                                String oppsummeringTekstFraSaksbehandler,
                                                List<PeriodeMedTekstDto> fritekstFraSaksbehandlerForPerioder) {

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        UUID eksternUuid = eksternBehandling.getEksternUuid();
        Long behandlingIdIFpsak = eksternBehandling.getEksternId();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = fellesInfoTilBrevTjeneste.hentFeilutbetaltePerioder(behandlingIdIFpsak);

        List<VilkårsvurderingPerioderDto> vilkårsvurdertePerioder = vilkårsvurderingTjeneste.hentVilkårsvurdering(behandlingId);
        BeregningResultat beregnetResultat = tilbakekrevingBeregningTjeneste.beregn(behandlingId);
        List<BeregningResultatPeriode> beregningResultatPerioder = beregnetResultat.getBeregningResultatPerioder();
        Long totalTilbakekrevingBeløp = VedtaksbrevUtil.finnTotaltTilbakekrevingsbeløp(beregningResultatPerioder);

        List<VarselbrevSporing> varselbrevData = brevdataRepository.hentVarselbrevData(behandlingId);
        LocalDateTime nyesteVarselbrevTidspunkt = VedtaksbrevUtil.finnNyesteVarselbrevTidspunkt(varselbrevData);

        BrevMetadata brevMetadata = fellesInfoTilBrevTjeneste.lagMetadataForVedtaksbrev(behandling, totalTilbakekrevingBeløp, eksternUuid);

        return new VedtaksbrevSamletInfo.Builder()
            .medBrevMetadata(brevMetadata)
            .medSumBeløpSomSkalTilbakekreves(totalTilbakekrevingBeløp)
            .medPerioderMedBrevtekst(VedtaksbrevUtil.lagSortertePerioderMedTekst(vilkårsvurdertePerioder, fritekstFraSaksbehandlerForPerioder))
            .medSumFeilutbetaling(feilutbetaltePerioderDto.getSumFeilutbetaling())
            .medAntallUkerKlagefrist(fellesInfoTilBrevTjeneste.antallUkerKlagefrist())
            .medVarselbrevSendtUt(nyesteVarselbrevTidspunkt.toLocalDate())
            .medOppsummeringFritekst(oppsummeringTekstFraSaksbehandler)
            .build();
    }

    ForhåndvisningVedtaksbrevTekstDto lagTekstutkastAvVedtaksbrev(Long behandlingId, String oppsummering, List<PeriodeMedTekstDto> fritekstFraSaksbehandlerForPerioder) {
        VedtaksbrevSamletInfo vedtaksbrevSamletInfo = lagVedtaksbrev(behandlingId, oppsummering, fritekstFraSaksbehandlerForPerioder);

        Avsnitt introduksjonsavsnitt = AvsnittUtil.lagIntroduksjonsavsnitt(
            vedtaksbrevSamletInfo.getSumBeløpSomSkalTilbakekreves(),
            vedtaksbrevSamletInfo.getSumFeilutbetaling(),
            vedtaksbrevSamletInfo.getVarselbrevSendtUt(),
            vedtaksbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk(),
            vedtaksbrevSamletInfo.getOppsummeringFritekst());

        List<Avsnitt> allePerioderAvsnitt = vedtaksbrevSamletInfo.getPerioderMedBrevtekst().stream()
            .map(AvsnittUtil::lagPeriodeAvsnitt).collect(Collectors.toList());

        Avsnitt ekstrainformasjonsavsnitt = AvsnittUtil.lagEkstrainformasjonsavsnitt(
            vedtaksbrevSamletInfo.getAntallUkerKlagefrist(),
            ReturadresseKonfigurasjon.getBrevTelefonnummerKlageEnhet());

        List<Avsnitt> samledeAvsnittIRekkefølge = new ArrayList<>();
        samledeAvsnittIRekkefølge.add(introduksjonsavsnitt);
        samledeAvsnittIRekkefølge.addAll(allePerioderAvsnitt);
        samledeAvsnittIRekkefølge.add(ekstrainformasjonsavsnitt);

        return new ForhåndvisningVedtaksbrevTekstDto.Builder()
            .medAvsnittsliste(samledeAvsnittIRekkefølge)
            .build();
    }
}
