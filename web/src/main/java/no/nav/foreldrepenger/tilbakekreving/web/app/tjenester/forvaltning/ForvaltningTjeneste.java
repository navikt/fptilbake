package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.math.BigInteger;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
class ForvaltningTjeneste {

    private ProsessTaskTjeneste prosessTaskTjeneste;
    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private BehandlingRepository behandlingRepository;

    private KravgrunnlagRepository kravgrunnlagRepository;

    @Inject
    public ForvaltningTjeneste(ProsessTaskTjeneste prosessTaskTjeneste,
                               ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                               BehandlingRepository behandlingRepository,
                               KravgrunnlagRepository kravgrunnlagRepository) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.behandlingRepository = behandlingRepository;
        this.kravgrunnlagRepository = kravgrunnlagRepository;
    }

    void hentKorrigertKravgrunnlag(Behandling behandling, String kravgrunnlagId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HentKorrigertKravgrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty("KRAVGRUNNLAG_ID", kravgrunnlagId);
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    void annulerKravgrunnlag(BigInteger eksternKravgrunnlagId) {
        //var økonomiXmlMottatt = økonomiMottattXmlRepository.findByEksternKravgrunnlagId(eksternKravgrunnlagId);
        var kravgrunnlag431 = kravgrunnlagRepository.findByEksternKravgrunnlagIdAndAktivIsTrue(eksternKravgrunnlagId);
        if (økonomiXmlMottatt == null && kravgrunnlag431 == null) {
            throw Feil(message = "Finnes ikke eksternKravgrunnlagId=$eksternKravgrunnlagId");
        }
        val vedtakId = økonomiXmlMottatt?.vedtakId ?: kravgrunnlag431!!.vedtakId
        annulerKravgrunnlagService.annulerKravgrunnlagRequest(eksternKravgrunnlagId, vedtakId)
    }

    Forvaltningsinfo hentForvaltningsinfo(Saksnummer saksnummer) {
        var behandling = behandlingRepository.finnÅpenTilbakekrevingsbehandling(saksnummer);
        if (behandling.isPresent()) {
            var behandlingId = behandling.get().getId();
            if (kravgrunnlagRepository.finnesIsAktivFor(behandlingId)) {
                var kravgrunnlag431 = kravgrunnlagRepository.hentIsAktivFor(behandlingId);
                return new Forvaltningsinfo(kravgrunnlag431.getEksternKravgrunnlagId(), null, kravgrunnlag431.getReferanse());
            }
        }
        var økonomiXmlMottatt = økonomiMottattXmlRepository.finnAlleForSaksnummer(saksnummer.getVerdi());
        if (økonomiXmlMottatt.isEmpty()) {
            throw new TekniskException("ERROR", String.format("Finnes ikke data i systemet for saksnummer=%s", saksnummer));
        }
        var xmlMottatt = økonomiXmlMottatt.get(0);
        var kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(xmlMottatt.getId(), xmlMottatt.getMottattXml());
        return new Forvaltningsinfo(kravgrunnlagDto.getKravgrunnlagId().toString(), xmlMottatt.getId(), new Henvisning(kravgrunnlagDto.getReferanse()));
    }

    private Behandling hentBehandling(BehandlingReferanse behandlingReferanse) {
        Behandling behandling;
        if (behandlingReferanse.erInternBehandlingId()) {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingId());
        } else {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingUuid());
        }
        return behandling;
    }

    record Forvaltningsinfo(String eksternKravgrunnlagId, Long mottattXmlId, Henvisning eksternId) {}
}
