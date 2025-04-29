package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class OpprettBehandlingDto implements AbacDto {

    @NotNull
    @Valid
    private SaksnummerDto saksnummer; // TODO bør bruke egen DTO

    @NotNull
    @Valid
    private UUID eksternUuid;

    @Valid
    private BehandlingType behandlingType;

    @Valid
    private BehandlingÅrsakType behandlingArsakType;

    @Valid
    private FagsakYtelseType fagsakYtelseType;

    //Gjelder kun for Tilbakekrevingsrevurdering
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    //Gjelder kun for Tilbakekrevingsrevurdering
    @Valid
    private UUID behandlingUuid;

    public OpprettBehandlingDto() {
        // For CDI
    }

    public SaksnummerDto getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(SaksnummerDto saksnummer) {
        this.saksnummer = saksnummer;
    }

    public UUID getEksternUuid() {
        return eksternUuid;
    }

    public void setEksternUuid(UUID eksternUuid) {
        this.eksternUuid = eksternUuid;
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

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        var attributter = AbacDataAttributter.opprett()
            .leggTil(saksnummer.abacAttributter());
        if (getBehandlingType().equals(BehandlingType.TILBAKEKREVING)) {
            return attributter.leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, eksternUuid);
        } else if (getBehandlingType().equals(BehandlingType.REVURDERING_TILBAKEKREVING)) {
            if (behandlingId != null) {
                return attributter.leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, behandlingId);
            }
            if (behandlingUuid != null) {
                return attributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingUuid);
            }
        }
        return attributter;
    }

}
