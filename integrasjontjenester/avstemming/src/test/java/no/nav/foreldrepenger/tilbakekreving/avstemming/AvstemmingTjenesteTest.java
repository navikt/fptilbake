package no.nav.foreldrepenger.tilbakekreving.avstemming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.TilbakekrevingsvedtakMarshaller;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AvstemmingTjenesteTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private ØkonomiSendtXmlRepository sendtXmlRepository;
    @Inject
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;

    private AktørConsumer aktørConsumerMock = Mockito.mock(AktørConsumer.class);
    private AvstemmingTjeneste avstemmingTjeneste;
    private ScenarioSimple scenario = ScenarioSimple.simple();

    @Before
    public void setup() {
        avstemmingTjeneste = new AvstemmingTjeneste(sendtXmlRepository, behandlingRepositoryProvider, aktørConsumerMock);
    }

    @Test
    public void skal_returnere_tomt_når_det_ikke_finnes_vedtak() {
        Optional<String> oppsummer = avstemmingTjeneste.oppsummer(LocalDate.now());
        assertThat(oppsummer).isEmpty();
    }

    @Test
    public void skal_ha_med_vedtak_om_tilbakekreving_som_er_sendt_og_har_OK_kvittering() {
        Behandling behandling = scenario
            .medDefaultKravgrunnlag()
            .medFullInnkreving()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .medVedtak(LocalDate.now(), VedtakResultatType.FULL_TILBAKEBETALING)
            .lagre(behandlingRepositoryProvider);
        mockAktørConsumer(behandling);

        Long behandlingId = behandling.getId();
        Long xmlId = lagOgLagreVedtak(behandlingId);
        String kvitteringXml = ØkonomiResponsMarshaller.marshall(lagRespons(lagKvittering()), behandlingId);
        sendtXmlRepository.oppdatereKvittering(xmlId, kvitteringXml);

        Optional<String> oppsummer = avstemmingTjeneste.oppsummer(LocalDate.now());
        assertThat(oppsummer).isNotEmpty();
    }

    @Test
    public void skal_ikke_ha_med_vedtak_om_tilbakekreving_som_er_sendt_og_har_feilet() {
        Behandling behandling = scenario
            .medDefaultKravgrunnlag()
            .medFullInnkreving()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .medVedtak(LocalDate.now(), VedtakResultatType.FULL_TILBAKEBETALING)
            .lagre(behandlingRepositoryProvider);
        mockAktørConsumer(behandling);

        Long behandlingId = behandling.getId();
        Long xmlId = lagOgLagreVedtak(behandlingId);
        String kvitteringXml = ØkonomiResponsMarshaller.marshall(lagRespons(lagKvitteringVedFeil()), behandlingId);
        sendtXmlRepository.oppdatereKvittering(xmlId, kvitteringXml);

        Optional<String> oppsummer = avstemmingTjeneste.oppsummer(LocalDate.now());
        assertThat(oppsummer).isEmpty();
    }

    @Test
    public void skal_ikke_ha_med_førstegangsvedtak_som_har_ingen_innkreving() {
        Behandling behandling = scenario
            .medDefaultKravgrunnlag()
            .medIngenInnkreving()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .medVedtak(LocalDate.now(), VedtakResultatType.INGEN_TILBAKEBETALING)
            .lagre(behandlingRepositoryProvider);
        mockAktørConsumer(behandling);

        Long behandlingId = behandling.getId();
        Long xmlId = lagOgLagreVedtak(behandlingId);
        String kvitteringXml = ØkonomiResponsMarshaller.marshall(lagRespons(lagKvittering()), behandlingId);
        sendtXmlRepository.oppdatereKvittering(xmlId, kvitteringXml);

        Optional<String> oppsummer = avstemmingTjeneste.oppsummer(LocalDate.now());
        assertThat(oppsummer).isEmpty();
    }

    private Long lagOgLagreVedtak(Long behandlingId) {
        TilbakekrevingsvedtakDto vedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        return lagreIverksattVedtak(behandlingId, vedtak);
    }

    @Test
    public void skal_ha_med_revurderingsvedtak_som_har_ingen_innkreving() {
        Behandling behandling = scenario
            .medBehandlingType(BehandlingType.REVURDERING_TILBAKEKREVING)
            .medDefaultKravgrunnlag()
            .medIngenInnkreving()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .medVedtak(LocalDate.now(), VedtakResultatType.INGEN_TILBAKEBETALING)
            .lagre(behandlingRepositoryProvider);
        mockAktørConsumer(behandling);

        Long behandlingId = behandling.getId();
        Long xmlId = lagOgLagreVedtak(behandlingId);
        String kvitteringXml = ØkonomiResponsMarshaller.marshall(lagRespons(lagKvittering()), behandlingId);
        sendtXmlRepository.oppdatereKvittering(xmlId, kvitteringXml);

        Optional<String> oppsummer = avstemmingTjeneste.oppsummer(LocalDate.now());
        assertThat(oppsummer).isNotEmpty();
    }

    private void mockAktørConsumer(Behandling behandling) {
        when(aktørConsumerMock.hentPersonIdenterForAktørIder(Mockito.anySet())).thenReturn(Collections.singletonList(lagAktoerId(behandling.getAktørId(), "12345678901")));
    }

    private Long lagreIverksattVedtak(Long behandlingId, TilbakekrevingsvedtakDto vedtak) {
        TilbakekrevingsvedtakRequest request = new TilbakekrevingsvedtakRequest();
        request.setTilbakekrevingsvedtak(vedtak);
        String xml = TilbakekrevingsvedtakMarshaller.marshall(behandlingId, request);
        return sendtXmlRepository.lagre(behandlingId, xml, MeldingType.VEDTAK);
    }

    private AktoerIder lagAktoerId(AktørId aktørId, String fnr) {
        AktoerIder aktoerId = new AktoerIder();
        aktoerId.setAktoerId(aktørId.getId());
        IdentDetaljer value = new IdentDetaljer();
        value.setTpsId(fnr);
        aktoerId.setGjeldendeIdent(value);
        return aktoerId;
    }

    private TilbakekrevingsvedtakResponse lagRespons(MmelDto kvittering) {
        TilbakekrevingsvedtakResponse response = new TilbakekrevingsvedtakResponse();
        response.setMmel(kvittering);
        return response;
    }

    private MmelDto lagKvittering() {
        MmelDto mmelDto = new MmelDto();
        mmelDto.setSystemId("460-BIDR");
        mmelDto.setAlvorlighetsgrad("00");
        mmelDto.setBeskrMelding("OK");
        return mmelDto;
    }

    private MmelDto lagKvitteringVedFeil() {
        MmelDto mmelDto = new MmelDto();
        mmelDto.setSystemId("dfasdf");
        mmelDto.setAlvorlighetsgrad("08");
        mmelDto.setBeskrMelding("Noe gikk galt");
        return mmelDto;
    }

}
