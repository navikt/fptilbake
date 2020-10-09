package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VedtakHendelse {

    @NotNull
    @JsonProperty("behandlingId")
    private UUID behandlingId;

    @NotNull
    @JsonProperty("fagsakYtelseType")
    private FagsakYtelseType fagsakYtelseType;

    @Pattern(
        regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$",
        flags = {Pattern.Flag.CASE_INSENSITIVE}
    )
    @JsonProperty("saksnummer")
    private String saksnummer;

    @NotNull
    @Valid
    @JsonProperty("aktør")
    private AktørId aktør;

    @NotNull
    @Valid
    @JsonProperty("vedtattTidspunkt")
    private LocalDateTime vedtattTidspunkt;

    public AktørId getAktør() {
        return aktør;
    }

    public void setAktør(AktørId aktør) {
        this.aktør = aktør;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public UUID getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(UUID behandlingId) {
        this.behandlingId = behandlingId;
    }

    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    public void setVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        this.vedtattTidspunkt = vedtattTidspunkt;
    }
}
