package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.saksbehandler;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Collection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.saksbehandler.dto.InnloggetNavAnsattDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.util.LdapUtil;
import no.nav.vedtak.felles.integrasjon.ldap.LdapBruker;
import no.nav.vedtak.felles.integrasjon.ldap.LdapBrukeroppslag;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Path("/nav-ansatt")
@RequestScoped
public class NavAnsattRestTjeneste {
    private String gruppenavnSaksbehandler;
    private String gruppenavnVeileder;
    private String gruppenavnBeslutter;
    private String gruppenavnOverstyrer;
    private String gruppenavnEgenAnsatt;
    private String gruppenavnKode6;
    private String gruppenavnKode7;

    public NavAnsattRestTjeneste() {
        // CDI
    }

    @Inject
    public NavAnsattRestTjeneste(
        @KonfigVerdi(value = "bruker.gruppenavn.saksbehandler") String gruppenavnSaksbehandler,
        @KonfigVerdi(value = "bruker.gruppenavn.veileder") String gruppenavnVeileder,
        @KonfigVerdi(value = "bruker.gruppenavn.beslutter") String gruppenavnBeslutter,
        @KonfigVerdi(value = "bruker.gruppenavn.overstyrer") String gruppenavnOverstyrer,
        @KonfigVerdi(value = "bruker.gruppenavn.egenansatt") String gruppenavnEgenAnsatt,
        @KonfigVerdi(value = "bruker.gruppenavn.kode6") String gruppenavnKode6,
        @KonfigVerdi(value = "bruker.gruppenavn.kode7") String gruppenavnKode7
    ) {
        this.gruppenavnSaksbehandler = gruppenavnSaksbehandler;
        this.gruppenavnVeileder = gruppenavnVeileder;
        this.gruppenavnBeslutter = gruppenavnBeslutter;
        this.gruppenavnOverstyrer = gruppenavnOverstyrer;
        this.gruppenavnEgenAnsatt = gruppenavnEgenAnsatt;
        this.gruppenavnKode6 = gruppenavnKode6;
        this.gruppenavnKode7 = gruppenavnKode7;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Returnerer fullt navn for ident",
        summary = "Ident hentes fra sikkerhetskonteksten som er tilgjengelig etter innlogging.",
        tags = "nav-ansatt")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    public InnloggetNavAnsattDto innloggetBruker() {
        String ident = SubjectHandler.getSubjectHandler().getUid();
        LdapBruker ldapBruker = new LdapBrukeroppslag().hentBrukerinformasjon(ident);
        return getInnloggetBrukerDto(ident, ldapBruker);
    }

    InnloggetNavAnsattDto getInnloggetBrukerDto(String ident, LdapBruker ldapBruker) {
        String navn = ldapBruker.getDisplayName();
        Collection<String> grupper = LdapUtil.filtrerGrupper(ldapBruker.getGroups());
        return InnloggetNavAnsattDto.builder()
            .setBrukernavn(ident)
            .setNavn(navn)
            .setKanSaksbehandle(grupper.contains(gruppenavnSaksbehandler))
            .setKanVeilede(grupper.contains(gruppenavnVeileder))
            .setKanBeslutte(grupper.contains(gruppenavnBeslutter))
            .setKanOverstyre(grupper.contains(gruppenavnOverstyrer))
            .setKanBehandleKodeEgenAnsatt(grupper.contains(gruppenavnEgenAnsatt))
            .setKanBehandleKode6(grupper.contains(gruppenavnKode6))
            .setKanBehandleKode7(grupper.contains(gruppenavnKode7))
            .create();
    }

}
