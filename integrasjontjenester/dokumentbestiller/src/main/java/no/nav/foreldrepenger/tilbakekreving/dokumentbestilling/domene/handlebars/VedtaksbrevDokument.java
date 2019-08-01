package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class VedtaksbrevDokument extends BaseDokument {

    private Long feilutbetaltBeloep;
    private int antallUkerKlagefrist;
    private Long beloepSomSkalTilbakekreves;
    private LocalDate varselbrevSendtDato;
    private String kontakttelefonnummer;
    private List<FeilutbetalingsperiodeMedTekst> feilutbetalingsperioderMedTekst;
    private String oppsummeringFritekst;

    public List<FeilutbetalingsperiodeMedTekst> getFeilutbetalingsperioderMedTekst() {
        return feilutbetalingsperioderMedTekst;
    }

    public void setFeilutbetalingsperioderMedTekst(List<FeilutbetalingsperiodeMedTekst> feilutbetalingsperioderMedTekst) {
        this.feilutbetalingsperioderMedTekst = feilutbetalingsperioderMedTekst;
    }

    public Long getFeilutbetaltBeloep() {
        return feilutbetaltBeloep;
    }

    public void setFeilutbetaltBeloep(Long feilutbetaltBeloep) {
        this.feilutbetaltBeloep = feilutbetaltBeloep;
    }

    public Long getBeloepSomSkalTilbakekreves() {
        return beloepSomSkalTilbakekreves;
    }

    public void setBeloepSomSkalTilbakekreves(Long beloepSomSkalTilbakekreves) {
        this.beloepSomSkalTilbakekreves = beloepSomSkalTilbakekreves;
    }

    public LocalDate getVarselbrevSendtDato() {
        return varselbrevSendtDato;
    }

    public void setVarselbrevSendtDato(LocalDate varselbrevSendtDato) {
        this.varselbrevSendtDato = varselbrevSendtDato;
    }

    public String getKontakttelefonnummer() {
        return kontakttelefonnummer;
    }

    public void setKontakttelefonnummer(String kontakttelefonnummer) {
        this.kontakttelefonnummer = kontakttelefonnummer;
    }

    public int getAntallUkerKlagefrist() {
        return antallUkerKlagefrist;
    }

    public void setAntallUkerKlagefrist(int antallUkerKlagefrist) {
        this.antallUkerKlagefrist = antallUkerKlagefrist;
    }

    public String getOppsummeringFritekst() {
        return oppsummeringFritekst;
    }

    public void setOppsummeringFritekst(String oppsummeringFritekst) {
        this.oppsummeringFritekst = oppsummeringFritekst;
    }

    public void valider() { //TODO (Trine): legge til mer validering her etterhvert som tekstene er klare. PFP-7975
        Objects.requireNonNull(feilutbetaltBeloep, "feilutbetalt beløp fra fpoppdrag");
        Objects.requireNonNull(beloepSomSkalTilbakekreves, "beløp som skal tilbakekreves, hentet fra Beregningsresultat");
        Objects.requireNonNull(getFagsaktypeNavn(), "fagsaktypenavn");
        Objects.requireNonNull(kontakttelefonnummer, "kontakttelefonnummer");
        Objects.requireNonNull(varselbrevSendtDato, "dato for når varselbrev ble sendt ut");

        if (feilutbetalingsperioderMedTekst != null && feilutbetalingsperioderMedTekst.size() > 0) {
            for (FeilutbetalingsperiodeMedTekst feilutbetaltPeriode : feilutbetalingsperioderMedTekst) {
                Objects.requireNonNull(feilutbetaltPeriode.getPeriode().getFom(), "fraogmed-dato for feilutbetalingsperiode");
                Objects.requireNonNull(feilutbetaltPeriode.getPeriode().getTom(), "tilogmed-dato for feilutbetalingsperiode");
            }
        }
    }
}
