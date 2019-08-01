package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene;

import java.time.LocalDate;
import java.util.List;

public class VedtaksbrevSamletInfo {

    private List<PeriodeMedBrevtekst> perioderMedBrevtekst;
    private Long sumFeilutbetaling;
    private LocalDate varselbrevSendtUt;
    private Long sumBeløpSomSkalTilbakekreves;
    private int antallUkerKlagefrist;
    private String oppsummeringFritekst;
    private BrevMetadata metadata;

    private VedtaksbrevSamletInfo() {
    }

    public String getOppsummeringFritekst() {
        return oppsummeringFritekst;
    }

    public BrevMetadata getMetadata() {
        return metadata;
    }

    public List<PeriodeMedBrevtekst> getPerioderMedBrevtekst() {
        return perioderMedBrevtekst;
    }

    public BrevMetadata getBrevMetadata() {
        return metadata;
    }

    public int getAntallUkerKlagefrist() {
        return antallUkerKlagefrist;
    }

    public Long getSumBeløpSomSkalTilbakekreves() {
        return sumBeløpSomSkalTilbakekreves;
    }

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public LocalDate getVarselbrevSendtUt() {
        return varselbrevSendtUt;
    }

    public static class Builder {

        private VedtaksbrevSamletInfo vedtaksbrev = new VedtaksbrevSamletInfo();

        public Builder medBrevMetadata(BrevMetadata metadata) {
            this.vedtaksbrev.metadata = metadata;
            return this;
        }

        public Builder medAntallUkerKlagefrist(int antallUkerKlagefrist) {
            this.vedtaksbrev.antallUkerKlagefrist = antallUkerKlagefrist;
            return this;
        }

        public Builder medSumFeilutbetaling(Long sumFeilutbetaling) {
            this.vedtaksbrev.sumFeilutbetaling = sumFeilutbetaling;
            return this;
        }

        public Builder medSumBeløpSomSkalTilbakekreves(Long sumBeløpSomSkalTilbakekreves) {
            this.vedtaksbrev.sumBeløpSomSkalTilbakekreves = sumBeløpSomSkalTilbakekreves;
            return this;
        }

        public Builder medOppsummeringFritekst(String oppsummeringFritekst) {
            this.vedtaksbrev.oppsummeringFritekst = oppsummeringFritekst;
            return this;
        }

        public Builder medPerioderMedBrevtekst(List<PeriodeMedBrevtekst> perioderMedBrevtekst) {
            this.vedtaksbrev.perioderMedBrevtekst = perioderMedBrevtekst;
            return this;
        }

        public Builder medVarselbrevSendtUt(LocalDate varselbrevSendtUt) {
            this.vedtaksbrev.varselbrevSendtUt = varselbrevSendtUt;
            return this;
        }

        public VedtaksbrevSamletInfo build() {
            return vedtaksbrev;
        }

    }
}
