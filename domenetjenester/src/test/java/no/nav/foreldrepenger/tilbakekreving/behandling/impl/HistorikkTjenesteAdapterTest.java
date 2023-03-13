package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkBegrunnelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDelDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkinnslagEndretFeltDto;

class HistorikkTjenesteAdapterTest extends FellesTestOppsett {

    @Test
    void opprettHistorikkInnslag() {
        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();
        tekstBuilder.medSkjermlenke(SkjermlenkeType.UDEFINERT).medBegrunnelse(HistorikkBegrunnelseType.SAKSBEH_START_PA_NYTT)
                .medHendelse(HistorikkinnslagType.FAKTA_ENDRET, internBehandlingId)
                .medEndretFelt(HistorikkEndretFeltType.BEHANDLING, "behandling", 1, 2);
        historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDto> historikkinnslager = historikkTjenesteAdapter.hentAlleHistorikkInnslagForSak(saksnummer, URI.create("http://dummy/dummy"));
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);
        HistorikkinnslagDto historikkinnslagDto = historikkinnslager.get(0);
        assertThat(historikkinnslagDto.getAktoer()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslagDto.getHistorikkinnslagDeler()).isNotEmpty();
        HistorikkinnslagDelDto historikkinnslagDelDto = historikkinnslagDto.getHistorikkinnslagDeler().get(0);
        assertThat(historikkinnslagDelDto.getBegrunnelse().getKode()).isEqualTo(HistorikkBegrunnelseType.SAKSBEH_START_PA_NYTT.getKode());
        assertThat(historikkinnslagDelDto.getEndredeFelter()).isNotEmpty();
        HistorikkinnslagEndretFeltDto historikkinnslagEndretFeltDto = historikkinnslagDelDto.getEndredeFelter().get(0);
        assertThat(historikkinnslagEndretFeltDto.getEndretFeltNavn()).isEqualByComparingTo(HistorikkEndretFeltType.BEHANDLING);

    }

}
