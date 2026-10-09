package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
class ForvaltningTjeneste {

    private final ProsessTaskTjeneste prosessTaskTjeneste;
    private final ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private final BehandlingRepository behandlingRepository;
    private final KravgrunnlagRepository kravgrunnlagRepository;
    private final KravgrunnlagHenter kravgrunnlagHenter;


    @Inject
    public ForvaltningTjeneste(ProsessTaskTjeneste prosessTaskTjeneste,
                               ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                               BehandlingRepository behandlingRepository,
                               KravgrunnlagRepository kravgrunnlagRepository,
                               KravgrunnlagHenter kravgrunnlagHenter) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.behandlingRepository = behandlingRepository;
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.kravgrunnlagHenter = kravgrunnlagHenter;
    }

    void hentKorrigertKravgrunnlag(Behandling behandling, String kravgrunnlagId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HentKorrigertKravgrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setProperty(HentKorrigertKravgrunnlagTask.KRAVGRUNNLAG_ID, kravgrunnlagId);
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    void annullerKravgrunnlag(Long behandlingId) {
        kravgrunnlagHenter.annullerKravgrunnlag(behandlingId, kravgrunnlagRepository.hentIsAktivFor(behandlingId));
    }


    List<ØkonomiXmlMottatt> hentAlleKravgrunnlag(Saksnummer saksnummer){
        return økonomiMottattXmlRepository.finnAlleForSaksnummer(saksnummer);
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
        var xmlMottatt = økonomiXmlMottatt.getFirst();
        var kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(xmlMottatt.getId(), xmlMottatt.getMottattXml(), true);
        return new Forvaltningsinfo(kravgrunnlagDto.getKravgrunnlagId().toString(), xmlMottatt.getId(), new Henvisning(kravgrunnlagDto.getReferanse()), null);
    }

    record Forvaltningsinfo(String eksternKravgrunnlagId, Long mottattXmlId, Henvisning eksternId, Long behandlingId) {}
}
