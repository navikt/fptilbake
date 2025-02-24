package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.ORGANISASJON;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(GrunnlagRestTestTjenesteLocalDev.BASE_PATH)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class GrunnlagRestTestTjenesteLocalDev {

    static final String BASE_PATH = "/grunnlag";

    private PersoninfoAdapter tpsTjeneste;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;

    public GrunnlagRestTestTjenesteLocalDev() {
        //for CDI proxy
    }

    @Inject
    public GrunnlagRestTestTjenesteLocalDev(PersoninfoAdapter tpsTjeneste, KravgrunnlagTjeneste kravgrunnlagTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
    }

    @POST
    @Operation(tags = "kravgrunnlag", description = "Lagre tilbakekrevingsgrunnlag fra økonomi")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response lagreUtbetalinger(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                      @NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse idDto,
                                      @NotNull @Valid KravgrunnlagDto kravgrunnlagDto) {

        if (!Environment.current().isLocal()) {
            throw new IllegalStateException("Dette er ikke lov å kjøre dette i produksjon eller dev!!!");
        }

        Kravgrunnlag431 kravgrunnlag = lagKravgrunnlag(kravgrunnlagDto.getKravGrunnlag());
        KravgrunnlagValidator.validerGrunnlag(kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(idDto.getBehandlingId(), kravgrunnlag, true);
        return Response.ok().build();
    }


    private Kravgrunnlag431 lagKravgrunnlag(DetaljertKravgrunnlagDto kravgrunnlagDto) {
        Kravgrunnlag431 kravgrunnlag431 = lagKravgrunnlag431(kravgrunnlagDto);
        for (DetaljertKravgrunnlagPeriodeDto periodeDto : kravgrunnlagDto.getPerioder()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = lagKravgrunnlagPeriode432(kravgrunnlag431, periodeDto);
            for (DetaljertKravgrunnlagBelopDto postering : periodeDto.getPosteringer()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = lagKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 lagKravgrunnlag431(DetaljertKravgrunnlagDto kravgrunnlagDto) {
        return Kravgrunnlag431.builder()
                .medEksternKravgrunnlagId(kravgrunnlagDto.getKravgrunnlagId().toString())
                .medVedtakId(kravgrunnlagDto.getVedtakId())
                .medKravStatusKode(KravStatusKode.fraKode(kravgrunnlagDto.getKravStatusKode()))
                .medFagomraadeKode(FagOmrådeKode.fraKode(kravgrunnlagDto.getFagOmrådeKode()))
                .medFagSystemId(kravgrunnlagDto.getFagSystemId())
                .medVedtakFagSystemDato(kravgrunnlagDto.getVedtakFagSystemDato())
                .medOmgjortVedtakId(kravgrunnlagDto.getOmgjortVedtakId())
                .medGjelderVedtakId(hentAktørIdEllerOrganisajonNummer(kravgrunnlagDto.getGjelderVedtakId(), GjelderType.fraKode(kravgrunnlagDto.getGjelderType())))
                .medGjelderType(GjelderType.fraKode(kravgrunnlagDto.getGjelderType()))
                .medUtbetalesTilId(hentAktørIdEllerOrganisajonNummer(kravgrunnlagDto.getUtbetalesTilId(), GjelderType.fraKode(kravgrunnlagDto.getUtbetGjelderType())))
                .medUtbetIdType(GjelderType.fraKode(kravgrunnlagDto.getUtbetGjelderType()))
                .medHjemmelKode(kravgrunnlagDto.getHjemmelKode())
                .medBeregnesRenter(kravgrunnlagDto.getBeregnesRenter())
                .medAnsvarligEnhet(kravgrunnlagDto.getAnsvarligEnhet())
                .medBostedEnhet(kravgrunnlagDto.getBostedEnhet())
                .medBehandlendeEnhet(kravgrunnlagDto.getBehandlendeEnhet())
                .medFeltKontroll(kravgrunnlagDto.getKontrollFelt())
                .medSaksBehId(kravgrunnlagDto.getSaksBehId())
                .medReferanse(new Henvisning(kravgrunnlagDto.getReferanse()))
                .build();
    }

    private KravgrunnlagPeriode432 lagKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, DetaljertKravgrunnlagPeriodeDto periodeDto) {
        return KravgrunnlagPeriode432.builder()
                .medPeriode(Periode.of(periodeDto.getFom(), periodeDto.getTom()))
                .medBeløpSkattMnd(periodeDto.getBeløpSkattMnd())
                .medKravgrunnlag431(kravgrunnlag431)
                .build();
    }

    private KravgrunnlagBelop433 lagKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, DetaljertKravgrunnlagBelopDto postering) {
        return KravgrunnlagBelop433.builder()
                .medKlasseKode(postering.getKlasseKode())
                .medKlasseType(KlasseType.fraKode(postering.getKlasseType()))
                .medOpprUtbetBelop(postering.getOpprUtbetBelop())
                .medNyBelop(postering.getNyBelop())
                .medTilbakekrevesBelop(postering.getTilbakekrevesBelop())
                .medUinnkrevdBelop(postering.getUinnkrevdBelop())
                .medSkattProsent(postering.getSkattProsent())
                .medResultatKode(postering.getResultatKode())
                .medÅrsakKode(postering.getÅrsakKode())
                .medSkyldKode(postering.getSkyldKode())
                .medKravgrunnlagPeriode432(kravgrunnlagPeriode432).build();
    }

    private String hentAktørIdEllerOrganisajonNummer(String fnrEllerOgNo, GjelderType gjelderType) {
        if (gjelderType.equals(ORGANISASJON)) {
            return fnrEllerOgNo;
        } else {
            Optional<AktørId> aktørId = tpsTjeneste.hentAktørForFnr(PersonIdent.fra(fnrEllerOgNo));
            if (aktørId.isPresent()) {
                return aktørId.get().getId();
            } else {
                throw BehandlingFeil.fantIkkePersonIdentMedFnr();
            }
        }
    }
}
