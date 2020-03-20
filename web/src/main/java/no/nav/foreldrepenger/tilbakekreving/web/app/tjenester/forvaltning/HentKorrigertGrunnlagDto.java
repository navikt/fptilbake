package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class HentKorrigertGrunnlagDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Digits(integer = 9, fraction = 0)
    private String kravgrunnlagId;

    public HentKorrigertGrunnlagDto() {
        // for CDI
    }

    public HentKorrigertGrunnlagDto(Long behandlingId, String kravgrunnlagId) {
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

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
    }
}
