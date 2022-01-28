package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDto;

// RequestScoped fordi HistorikkInnslagTekstBuilder inneholder state og denne deles på tvers av AksjonspunktOppdaterere
@RequestScoped
public class HistorikkTjenesteAdapter {
    private HistorikkRepository historikkRepository;
    private HistorikkInnslagTekstBuilder builder;
    private HistorikkInnslagKonverter historikkinnslagKonverter;

    HistorikkTjenesteAdapter() {
        // for CDI proxy
    }

    @Inject
    public HistorikkTjenesteAdapter(HistorikkRepository historikkRepository,
                                    HistorikkInnslagKonverter historikkinnslagKonverter) {
        this.historikkRepository = historikkRepository;
        this.historikkinnslagKonverter = historikkinnslagKonverter;
        this.builder = new HistorikkInnslagTekstBuilder();
    }


    public List<HistorikkinnslagDto> hentAlleHistorikkInnslagForSak(Saksnummer saksnummer) {
        List<Historikkinnslag> historikkinnslagList = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        return historikkinnslagList.stream()
                .map(historikkinnslag -> historikkinnslagKonverter.mapFra(historikkinnslag))
                .sorted()
                .collect(Collectors.toList());
    }


    public void lagInnslag(Historikkinnslag historikkinnslag) {
        resetBuilder();
        historikkRepository.lagre(historikkinnslag);
    }


    public HistorikkInnslagTekstBuilder tekstBuilder() {
        return builder;
    }


    public void opprettHistorikkInnslag(Behandling behandling, HistorikkinnslagType hisType) {
        if (!builder.getHistorikkinnslagDeler().isEmpty() || builder.antallEndredeFelter() > 0 ||
                builder.getErBegrunnelseEndret() || builder.getErGjeldendeFraSatt()) {

            Historikkinnslag innslag = new Historikkinnslag();

            builder.medHendelse(hisType);
            innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
            innslag.setType(hisType);
            innslag.setBehandlingId(behandling.getId());
            builder.build(innslag);

            lagInnslag(innslag);
        }
    }

    public void resetBuilder() {
        builder = new HistorikkInnslagTekstBuilder();
    }
}
