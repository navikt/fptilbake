package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.testtjenester;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class DetaljertKravgrunnlagDto implements AbacDto {


    @NotNull
    @Min(1)
    @Max(Long.MAX_VALUE)
    private Long vedtakId;

    @NotNull
    @Min(0)
    @Max(999999999)
    private Integer kravgrunnlagId;

    @NotNull
    @Size(max = 4)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String kravStatusKode;

    @NotNull
    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String fagOmrådeKode;

    @NotNull
    @Size(max = 30)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fagSystemId;

    private LocalDate vedtakFagSystemDato;

    @Min(1)
    @Max(Long.MAX_VALUE)
    private Long omgjortVedtakId;

    @NotNull
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^[0-9]*$")
    private String gjelderVedtakId;

    @NotNull
    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String gjelderType;

    @NotNull
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^[0-9]*$")
    private String utbetalesTilId;

    @NotNull
    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String utbetGjelderType;

    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String hjemmelKode;

    @Size(max = 1)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String beregnesRenter;

    @NotNull
    @Size(max = 13)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String ansvarligEnhet;

    @NotNull
    @Size(max = 13)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String bostedEnhet;

    @NotNull
    @Size(max = 13)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String behandlendeEnhet;

    @NotNull
    @Size(max = 26)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String kontrollFelt;

    @NotNull
    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String saksBehId;

    @Size(max = 30)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String referanse;

    @Size(min = 1)
    @Valid
    private List<DetaljertKravgrunnlagPeriodeDto> perioder = new ArrayList<>();

    public Long getVedtakId() {
        return vedtakId;
    }

    public void setVedtakId(Long vedtakId) {
        this.vedtakId = vedtakId;
    }

    public Integer getKravgrunnlagId() {
        return kravgrunnlagId;
    }

    public void setKravgrunnlagId(Integer kravgrunnlagId) {
        this.kravgrunnlagId = kravgrunnlagId;
    }

    public String getKravStatusKode() {
        return kravStatusKode;
    }

    public void setKravStatusKode(String kravStatusKode) {
        this.kravStatusKode = kravStatusKode;
    }

    public String getFagOmrådeKode() {
        return fagOmrådeKode;
    }

    public void setFagOmrådeKode(String fagOmrådeKode) {
        this.fagOmrådeKode = fagOmrådeKode;
    }

    public String getFagSystemId() {
        return fagSystemId;
    }

    public void setFagSystemId(String fagSystemId) {
        this.fagSystemId = fagSystemId;
    }

    public LocalDate getVedtakFagSystemDato() {
        return vedtakFagSystemDato;
    }

    public void setVedtakFagSystemDato(LocalDate vedtakFagSystemDato) {
        this.vedtakFagSystemDato = vedtakFagSystemDato;
    }

    public Long getOmgjortVedtakId() {
        return omgjortVedtakId;
    }

    public void setOmgjortVedtakId(Long omgjortVedtakId) {
        this.omgjortVedtakId = omgjortVedtakId;
    }

    public String getGjelderVedtakId() {
        return gjelderVedtakId;
    }

    public void setGjelderVedtakId(String gjelderVedtakId) {
        this.gjelderVedtakId = gjelderVedtakId;
    }

    public String getGjelderType() {
        return gjelderType;
    }

    public void setGjelderType(String gjelderType) {
        this.gjelderType = gjelderType;
    }

    public String getUtbetalesTilId() {
        return utbetalesTilId;
    }

    public void setUtbetalesTilId(String utbetalesTilId) {
        this.utbetalesTilId = utbetalesTilId;
    }

    public String getUtbetGjelderType() {
        return utbetGjelderType;
    }

    public void setUtbetGjelderType(String utbetGjelderType) {
        this.utbetGjelderType = utbetGjelderType;
    }

    public String getHjemmelKode() {
        return hjemmelKode;
    }

    public void setHjemmelKode(String hjemmelKode) {
        this.hjemmelKode = hjemmelKode;
    }

    public String getBeregnesRenter() {
        return beregnesRenter;
    }

    public void setBeregnesRenter(String beregnesRenter) {
        this.beregnesRenter = beregnesRenter;
    }

    public String getAnsvarligEnhet() {
        return ansvarligEnhet;
    }

    public void setAnsvarligEnhet(String ansvarligEnhet) {
        this.ansvarligEnhet = ansvarligEnhet;
    }

    public String getBostedEnhet() {
        return bostedEnhet;
    }

    public void setBostedEnhet(String bostedEnhet) {
        this.bostedEnhet = bostedEnhet;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public void setBehandlendeEnhet(String behandlendeEnhet) {
        this.behandlendeEnhet = behandlendeEnhet;
    }

    public String getKontrollFelt() {
        return kontrollFelt;
    }

    public void setKontrollFelt(String kontrollFelt) {
        this.kontrollFelt = kontrollFelt;
    }

    public String getSaksBehId() {
        return saksBehId;
    }

    public void setSaksBehId(String saksBehId) {
        this.saksBehId = saksBehId;
    }

    public String getReferanse() {
        return referanse;
    }

    public void setReferanse(String referanse) {
        this.referanse = referanse;
    }

    public List<DetaljertKravgrunnlagPeriodeDto> getPerioder() {
        return perioder;
    }

    public void leggTilPerioder(DetaljertKravgrunnlagPeriodeDto periode) {
        perioder.add(periode);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

}
