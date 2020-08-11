package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class OpprettBehandlingDto implements AbacDto {

    @NotNull
    @Digits(integer = 50, fraction = 0)
    private String saksnummer; // TODO bør bruke egen DTO

    @NotNull
    @Valid
    private UUID eksternUuid;

    @ValidKodeverk
    private BehandlingType behandlingType;

    @ValidKodeverk
    private BehandlingÅrsakType behandlingArsakType;

    @Valid
    private FagsakYtelseType fagsakYtelseType;

    //Gjelder kun for Tilbakekrevingsrevurdering
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    public OpprettBehandlingDto() {
        // For CDI
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public UUID getEksternUuid() {
        return eksternUuid;
    }

    public void setEksternUuid(String eksternUuid) {
        this.eksternUuid = UUID.fromString(eksternUuid);
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public void setBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
    }

    public BehandlingÅrsakType getBehandlingArsakType() {
        return behandlingArsakType;
    }

    public void setBehandlingArsakType(BehandlingÅrsakType behandlingArsakType) {
        this.behandlingArsakType = behandlingArsakType;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        if (getBehandlingType().equals(BehandlingType.TILBAKEKREVING)) {
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, saksnummer)
                .leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, eksternUuid);
        } else if (getBehandlingType().equals(BehandlingType.REVURDERING_TILBAKEKREVING)) {
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
        }
        return AbacDataAttributter.opprett();
    }

}
