package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.math.BigInteger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
class ForvaltningTjeneste {

    private ProsessTaskTjeneste prosessTaskTjeneste;
    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;
    private ØkonomiConsumer økonomiConsumer;

    @Inject
    public ForvaltningTjeneste(ProsessTaskTjeneste prosessTaskTjeneste,
                               ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                               BehandlingRepository behandlingRepository,
                               KravgrunnlagRepository kravgrunnlagRepository,
                               ØkonomiConsumer økonomiConsumer) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.behandlingRepository = behandlingRepository;
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.økonomiConsumer = økonomiConsumer;
    }

    void hentKorrigertKravgrunnlag(Behandling behandling, String kravgrunnlagId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HentKorrigertKravgrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty("KRAVGRUNNLAG_ID", kravgrunnlagId);
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    void annulerKravgrunnlag(Long behandlingId) {
        var kravgrunnlag431 = kravgrunnlagRepository.hentIsAktivFor(behandlingId);
        annulerKravgrunnlagRequest(behandlingId, kravgrunnlag431.getVedtakId());
    }

    private void annulerKravgrunnlagRequest(Long behandlingId, Long vedtakId) {
        var annullerKravgrunnlagDto = new AnnullerKravgrunnlagDto();
        annullerKravgrunnlagDto.setSaksbehId(HentKorrigertKravgrunnlagTask.OKO_SAKSBEH_ID);
        annullerKravgrunnlagDto.setKodeAksjon(KodeAksjon.ANNULERE_GRUNNLAG.getKode());
        annullerKravgrunnlagDto.setVedtakId(BigInteger.valueOf(vedtakId));
        økonomiConsumer.anullereKravgrunnlag(behandlingId, annullerKravgrunnlagDto);
    }

    Forvaltningsinfo hentForvaltningsinfo(String saksnummer) {
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
        var kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(xmlMottatt.getId(), xmlMottatt.getMottattXml());
        return new Forvaltningsinfo(kravgrunnlagDto.getKravgrunnlagId().toString(), xmlMottatt.getId(), new Henvisning(kravgrunnlagDto.getReferanse()), null);
    }

    record Forvaltningsinfo(String eksternKravgrunnlagId, Long mottattXmlId, Henvisning eksternId, Long behandlingId) {}
}
