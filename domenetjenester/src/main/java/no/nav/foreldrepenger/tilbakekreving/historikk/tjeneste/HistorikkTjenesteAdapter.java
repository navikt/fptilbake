package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDto;

// RequestScoped fordi HistorikkInnslagTekstBuilder inneholder state og denne deles på tvers av AksjonspunktOppdaterere
@RequestScoped
public class HistorikkTjenesteAdapter {
    private HistorikkRepositoryOld historikkRepository;
    private HistorikkInnslagTekstBuilder builder;
    private HistorikkInnslagKonverter historikkinnslagKonverter;

    HistorikkTjenesteAdapter() {
        // for CDI proxy
    }

    @Inject
    public HistorikkTjenesteAdapter(HistorikkRepositoryOld historikkRepository,
                                    HistorikkInnslagKonverter historikkinnslagKonverter) {
        this.historikkRepository = historikkRepository;
        this.historikkinnslagKonverter = historikkinnslagKonverter;
        this.builder = new HistorikkInnslagTekstBuilder();
    }


    public List<HistorikkinnslagDto> hentAlleHistorikkInnslagForSak(Saksnummer saksnummer, URI dokumentPath) {
        var historikkinnslagList = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        return historikkinnslagList.stream()
                .map(historikkinnslag -> historikkinnslagKonverter.mapFra(historikkinnslag, dokumentPath))
                .sorted()
                .collect(Collectors.toList());
    }

    public void lagInnslag(HistorikkinnslagOld historikkinnslag) {
        resetBuilder();
        historikkRepository.lagre(historikkinnslag);
    }


    public HistorikkInnslagTekstBuilder tekstBuilder() {
        return builder;
    }


    public void opprettHistorikkInnslag(Behandling behandling, HistorikkinnslagType hisType) {
        if (!builder.getHistorikkinnslagDeler().isEmpty() || builder.antallEndredeFelter() > 0 ||
                builder.getErBegrunnelseEndret() || builder.getErGjeldendeFraSatt()) {

            HistorikkinnslagOld innslag = new HistorikkinnslagOld();

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
