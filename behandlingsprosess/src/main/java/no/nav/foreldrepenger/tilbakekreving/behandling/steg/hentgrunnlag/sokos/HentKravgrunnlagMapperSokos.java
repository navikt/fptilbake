package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.sokos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerResponse;

@ApplicationScoped
public class HentKravgrunnlagMapperSokos {

    private static final Logger LOG = LoggerFactory.getLogger(HentKravgrunnlagMapperSokos.class);
    private PersonOrganisasjonWrapper personOrganisasjonWrapper;

    HentKravgrunnlagMapperSokos() {
        // for CDI
    }

    @Inject
    public HentKravgrunnlagMapperSokos(PersonOrganisasjonWrapper personOrganisasjonWrapper) {
        this.personOrganisasjonWrapper = personOrganisasjonWrapper;
    }

    public Kravgrunnlag431 mapTilDomene(HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer dto) {
        var gjelderType = dto.typeGjelder();
        var utbetalingGjelderType = dto.typeUtbetalesTilId();
        var kravgrunnlag = Kravgrunnlag431.builder()
            .medVedtakId(dto.vedtakId())
            .medEksternKravgrunnlagId(dto.kravgrunnlagId().toString())
            .medKravStatusKode(dto.kodeStatusKrav())
            .medFagomraadeKode(dto.kodeFagomraade())
            .medFagSystemId(dto.fagsystemId())
            .medVedtakFagSystemDato(dto.datoVedtakFagsystem())
            .medOmgjortVedtakId(dto.vedtakIdOmgjort() != null && dto.vedtakIdOmgjort() != 0 ? dto.vedtakIdOmgjort() : null)
            .medGjelderVedtakId(personOrganisasjonWrapper.hentAktørIdEllerOrganisajonNummer(dto.gjelderId(), gjelderType))
            .medGjelderType(gjelderType)
            .medUtbetalesTilId(personOrganisasjonWrapper.hentAktørIdEllerOrganisajonNummer(dto.utbetalesTilId(), utbetalingGjelderType))
            .medUtbetIdType(utbetalingGjelderType)
            .medHjemmelKode(dto.kodeHjemmel().isEmpty() ? null : dto.kodeHjemmel())
            .medBeregnesRenter(Boolean.TRUE.equals(dto.renterBeregnes()) ? "J" : "N")
            .medAnsvarligEnhet(dto.enhetAnsvarlig())
            .medBostedEnhet(dto.enhetBosted())
            .medBehandlendeEnhet(dto.enhetBehandl())
            .medFeltKontroll(dto.kontrollfelt())
            .medSaksBehId(dto.saksbehandlerId())
            .medReferanse(dto.referanse() != null ? new Henvisning(dto.referanse()) : null)
            .build();

        for (var periodeDto : dto.perioder()) {
            var periode = KravgrunnlagPeriode432.builder()
                .medPeriode(Periode.of(periodeDto.periodeFom(), periodeDto.periodeTom()))
                .medBeløpSkattMnd(periodeDto.belopSkattMnd())
                .medKravgrunnlag431(kravgrunnlag)
                .build();
            for (var postering : periodeDto.posteringer()) {
                var klasseType = postering.typeKlasse();
                var beløp = KravgrunnlagBelop433.builder()
                    .medKlasseType(klasseType)
                    .medKlasseKode(klasseType == KlasseType.TREK || klasseType == KlasseType.SKAT
                        ? postering.kodeKlasse() : KlasseKode.fraKode(postering.kodeKlasse()).getKode())
                    .medOpprUtbetBelop(postering.belopOpprinneligUtbetalt())
                    .medNyBelop(postering.belopNy())
                    .medTilbakekrevesBelop(postering.belopTilbakekreves())
                    .medUinnkrevdBelop(postering.belopUinnkrevd())
                    .medSkattProsent(postering.skattProsent())
                    .medResultatKode(postering.kodeResultat())
                    .medÅrsakKode(postering.kodeAarsak())
                    .medSkyldKode(postering.kodeSkyld())
                    .medKravgrunnlagPeriode432(periode)
                    .build();
                if (klasseType != KlasseType.YTEL || beløp.getNyBelop().compareTo(beløp.getOpprUtbetBelop()) <= 0) {
                    periode.leggTilBeløp(beløp);
                } else {
                    LOG.warn("Krav for klasseType YTEL er større enn opprinnelig utbetalt i kravgrunnlag: {}", dto.kravgrunnlagId());
                }
            }
            kravgrunnlag.leggTilPeriode(periode);
        }
        return kravgrunnlag;
    }
}
