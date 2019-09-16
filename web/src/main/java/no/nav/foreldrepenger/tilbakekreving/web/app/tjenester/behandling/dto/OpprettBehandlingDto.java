package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class OpprettBehandlingDto implements AbacDto {

    @NotNull
    @Digits(integer = 50, fraction = 0)
    private String saksnummer; // TODO bør bruke egen DTO

    @Valid
    private UUID eksternUuid;

    @NotNull
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    @Size(max = 20)
    private String behandlingType; // TODO bør bruke egen DTO

    @Pattern(regexp = InputValideringRegex.KODEVERK)
    @Size(max = 20)
    private String behandlingArsakType; // TODO bør bruke egen DTO

    @Pattern(regexp = InputValideringRegex.KODEVERK)
    @Size(max = 20)
    private String fagsakYtelseType; // TODO bør bruke egen DTO

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

    public String getBehandlingType() {
        return behandlingType;
    }

    public void setBehandlingType(String behandlingType) {
        this.behandlingType = behandlingType;
    }

    public String getBehandlingArsakType() {
        return behandlingArsakType;
    }

    public void setBehandlingArsakType(String behandlingArsakType) {
        this.behandlingArsakType = behandlingArsakType;
    }

    public String getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public void setFagsakYtelseType(String fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, saksnummer);
    }

}
