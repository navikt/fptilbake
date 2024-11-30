package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.math.BigInteger;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.AnnullerKravGrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
class ForvaltningTjeneste {

    private ProsessTaskTjeneste prosessTaskTjeneste;
    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;
    private ØkonomiProxyKlient økonomiProxyKlient;


    @Inject
    public ForvaltningTjeneste(ProsessTaskTjeneste prosessTaskTjeneste,
                               ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                               BehandlingRepository behandlingRepository,
                               KravgrunnlagRepository kravgrunnlagRepository,
                               ØkonomiProxyKlient økonomiProxyKlient) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.behandlingRepository = behandlingRepository;
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.økonomiProxyKlient = økonomiProxyKlient;
    }

    void hentKorrigertKravgrunnlag(Behandling behandling, String kravgrunnlagId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HentKorrigertKravgrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setProperty("KRAVGRUNNLAG_ID", kravgrunnlagId);
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    void annullerKravgrunnlag(Long behandlingId) {
        var kravgrunnlag431 = kravgrunnlagRepository.hentIsAktivFor(behandlingId);
        var annullerKravgrunnlagDto = new AnnullerKravGrunnlagDto(BigInteger.valueOf(kravgrunnlag431.getVedtakId()));
        økonomiProxyKlient.anullerKravgrunnlag(annullerKravgrunnlagDto);
    }

    Forvaltningsinfo hentForvaltningsinfo(Saksnummer saksnummer) {
        var behandling = behandlingRepository.finnÅpenTilbakekrevingsbehandling(saksnummer);
        if (behandling.isPresent()) {
            var behandlingId = behandling.get().getId();
            if (kravgrunnlagRepository.finnesIsAktivFor(behandlingId)) {
                var kravgrunnlag431 = kravgrunnlagRepository.hentIsAktivFor(behandlingId);
                return new Forvaltningsinfo(kravgrunnlag431.getEksternKravgrunnlagId(), null, kravgrunnlag431.getReferanse(), behandlingId);
            }
        }
        var økonomiXmlMottatt = økonomiMottattXmlRepository.finnAlleForSaksnummer(saksnummer);
        if (økonomiXmlMottatt.isEmpty()) {
            throw new TekniskException("ERROR", String.format("Finnes ikke data i systemet for saksnummer=%s", saksnummer));
        }
        var xmlMottatt = økonomiXmlMottatt.get(0);
        var kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(xmlMottatt.getId(), xmlMottatt.getMottattXml(), true);
        return new Forvaltningsinfo(kravgrunnlagDto.getKravgrunnlagId().toString(), xmlMottatt.getId(), new Henvisning(kravgrunnlagDto.getReferanse()), null);
    }

    record Forvaltningsinfo(String eksternKravgrunnlagId, Long mottattXmlId, Henvisning eksternId, Long behandlingId) {}
}
