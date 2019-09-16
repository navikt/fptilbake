package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class OpprettBehandlingDto implements AbacDto {

    @NotNull
    @Digits(integer = 50, fraction = 0)
    private String saksnummer; // TODO bør bruke egen DTO

    @Valid
    private UUID eksternUuid;

    @ValidKodeverk
    private BehandlingType behandlingType;

    @ValidKodeverk
    private BehandlingÅrsakType behandlingArsakType;

    @ValidKodeverk
    private FagsakYtelseType fagsakYtelseType;

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

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, saksnummer);
    }

}
