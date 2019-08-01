package no.nav.foreldrepenger.tilbakekreving.historikk.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;

class HistorikkInnslagTemaDto {
    private HistorikkEndretFeltType endretFeltNavn;
    private String navnVerdi;
    private String klNavn;

    public HistorikkEndretFeltType getEndretFeltNavn() {
        return endretFeltNavn;
    }

    public void setEndretFeltNavn(HistorikkEndretFeltType endretFeltNavn) {
        this.endretFeltNavn = endretFeltNavn;
    }

    public String getNavnVerdi() {
        return navnVerdi;
    }

    public void setNavnVerdi(String navnVerdi) {
        this.navnVerdi = navnVerdi;
    }

    public String getKlNavn() {
        return klNavn;
    }

    public void setKlNavn(String klNavn) {
        this.klNavn = klNavn;
    }


    static HistorikkInnslagTemaDto mapFra(HistorikkinnslagFelt felt, KodeverkRepository kodeverkRepository) {
        HistorikkInnslagTemaDto dto = new HistorikkInnslagTemaDto();
        HistorikkEndretFeltType endretFeltNavn = kodeverkRepository.finn(HistorikkEndretFeltType.class, felt.getNavn());
        dto.setEndretFeltNavn(endretFeltNavn);
        dto.setNavnVerdi(felt.getNavnVerdi());
        dto.setKlNavn(felt.getKlNavn());
        return dto;
    }

}
