package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.ORGANISASJON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertKravgrunnlagBelopDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertKravgrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertKravgrunnlagPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "grunnlag")
@Path(GrunnlagRestTestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transaction
public class GrunnlagRestTestTjeneste {

    static final String PATH_FRAGMENT = "/grunnlag";

    private TpsTjeneste tpsTjeneste;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;

    public GrunnlagRestTestTjeneste() {
        //for CDI proxy
    }

    @Inject
    public GrunnlagRestTestTjeneste(TpsTjeneste tpsTjeneste, KravgrunnlagTjeneste kravgrunnlagTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
    }

    @POST
    @Timed
    @ApiOperation(value = "Lagre tilbakekrevingsgrunnlag fra økonomi")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Lagret tilbakekrevingsgrunnlag fra økonomi")
    })
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response lagreUtbetalinger(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto idDto,
                                      @NotNull @Valid KravgrunnlagDto kravgrunnlagDto) {

        Kravgrunnlag431 kravgrunnlag = lagKravgrunnlag(kravgrunnlagDto.getKravGrunnlag());
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(idDto.getBehandlingId(), kravgrunnlag);
        return Response.ok().build();
    }


    private Kravgrunnlag431 lagKravgrunnlag(DetaljertKravgrunnlagDto kravgrunnlagDto) {
        Kravgrunnlag431 kravgrunnlag431 = formKravgrunnlag431(kravgrunnlagDto);
        for (DetaljertKravgrunnlagPeriodeDto periodeDto : kravgrunnlagDto.getPerioder()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = formKravgrunnlagPeriode432(kravgrunnlag431, periodeDto);
            for (DetaljertKravgrunnlagBelopDto postering : periodeDto.getPosteringer()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = formKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 formKravgrunnlag431(DetaljertKravgrunnlagDto kravgrunnlagDto) {
        return Kravgrunnlag431.builder()
            .medEksternKravgrunnlagId(kravgrunnlagDto.getKravgrunnlagId().toString())
            .medVedtakId(kravgrunnlagDto.getVedtakId())
            .medKravStatusKode(KravStatusKode.fraKode(kravgrunnlagDto.getKravStatusKode()))
            .medFagomraadeKode(FagOmrådeKode.fraKode(kravgrunnlagDto.getFagOmrådeKode()))
            .medFagSystemId(kravgrunnlagDto.getFagSystemId())
            .medVedtakFagSystemDato(kravgrunnlagDto.getVedtakFagSystemDato())
            .medOmgjortVedtakId(kravgrunnlagDto.getOmgjortVedtakId())
            .medGjelderVedtakId(hentAktørIdEllerOrganisajonNummer(kravgrunnlagDto.getGjelderVedtakId(),
                GjelderType.fraKode(kravgrunnlagDto.getGjelderType())))
            .medGjelderType(GjelderType.fraKode(kravgrunnlagDto.getGjelderType()))
            .medUtbetalesTilId(hentAktørIdEllerOrganisajonNummer(kravgrunnlagDto.getUtbetalesTilId(),
                GjelderType.fraKode(kravgrunnlagDto.getUtbetGjelderType())))
            .medUtbetIdType(GjelderType.fraKode(kravgrunnlagDto.getUtbetGjelderType()))
            .medHjemmelKode(kravgrunnlagDto.getHjemmelKode())
            .medBeregnesRenter(kravgrunnlagDto.getBeregnesRenter())
            .medAnsvarligEnhet(kravgrunnlagDto.getAnsvarligEnhet())
            .medBostedEnhet(kravgrunnlagDto.getBostedEnhet())
            .medBehandlendeEnhet(kravgrunnlagDto.getBehandlendeEnhet())
            .medFeltKontroll(kravgrunnlagDto.getKontrollFelt())
            .medSaksBehId(kravgrunnlagDto.getSaksBehId())
            .medReferanse(kravgrunnlagDto.getReferanse()).build();
    }

    private KravgrunnlagPeriode432 formKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, DetaljertKravgrunnlagPeriodeDto periodeDto) {
        return KravgrunnlagPeriode432.builder()
            .medPeriode(Periode.of(periodeDto.getFom(), periodeDto.getTom()))
            .medBeløpSkattMnd(periodeDto.getBeløpSkattMnd())
            .medKravgrunnlag431(kravgrunnlag431)
            .build();
    }

    private KravgrunnlagBelop433 formKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, DetaljertKravgrunnlagBelopDto postering) {
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
                throw BehandlingFeil.FACTORY.fantIkkePersonIdentMedFnr().toException();
            }
        }
    }
}
