package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class HentKorrigertKravgrunnlagDto  {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Digits(integer = 9, fraction = 0)
    private String kravgrunnlagId;

    public HentKorrigertKravgrunnlagDto() {
        // for CDI
    }

    public HentKorrigertKravgrunnlagDto(Long behandlingId, String kravgrunnlagId) {
        this.behandlingId = behandlingId;
        this.kravgrunnlagId = kravgrunnlagId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getKravgrunnlagId() {
        return kravgrunnlagId;
    }

    public void setKravgrunnlagId(String kravgrunnlagId) {
        this.kravgrunnlagId = kravgrunnlagId;
    }
}
