package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import static no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.ORGANISASJON;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@ApplicationScoped
public class HentKravgrunnlagMapper {
    private TpsAdapter tps;

    HentKravgrunnlagMapper() {
        //for CDI proxy
    }

    @Inject
    public HentKravgrunnlagMapper(TpsAdapter tps) {
        this.tps = tps;
    }

    public Kravgrunnlag431 mapTilDomene(DetaljertKravgrunnlagDto dto) {
        Kravgrunnlag431 kravgrunnlag431 = formKravgrunnlag431(dto);
        for (DetaljertKravgrunnlagPeriodeDto periodeDto : dto.getTilbakekrevingsPeriode()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = formKravgrunnlagPeriode432(kravgrunnlag431, periodeDto);
            for (DetaljertKravgrunnlagBelopDto postering : periodeDto.getTilbakekrevingsBelop()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = formKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 formKravgrunnlag431(DetaljertKravgrunnlagDto dto) {
        GjelderType gjelderType = GjelderType.fraKode(dto.getTypeGjelderId().value());
        GjelderType utbetalingGjelderType = GjelderType.fraKode(dto.getTypeUtbetId().value());
        return Kravgrunnlag431.builder().medVedtakId(dto.getVedtakId().longValue())
            .medKravStatusKode(KravStatusKode.fraKode(dto.getKodeStatusKrav()))
            .medFagomraadeKode(FagOmrådeKode.fraKode(dto.getKodeFagomraade()))
            .medFagSystemId(dto.getFagsystemId())
            .medVedtakFagSystemDato(konverter(dto.getDatoVedtakFagsystem()))
            .medOmgjortVedtakId(dto.getVedtakIdOmgjort() != null ? dto.getVedtakIdOmgjort().longValue() : null)
            .medGjelderVedtakId(hentAktørIdEllerOrganisajonNummer(dto.getVedtakGjelderId(), gjelderType))
            .medGjelderType(gjelderType)
            .medUtbetalesTilId(hentAktørIdEllerOrganisajonNummer(dto.getUtbetalesTilId(), utbetalingGjelderType))
            .medUtbetIdType(utbetalingGjelderType)
            .medHjemmelKode(dto.getKodeHjemmel())
            .medBeregnesRenter(dto.getRenterBeregnes() != null ? dto.getRenterBeregnes().value() : null)
            .medAnsvarligEnhet(dto.getEnhetAnsvarlig())
            .medBostedEnhet(dto.getEnhetBosted())
            .medBehandlendeEnhet(dto.getEnhetBehandl())
            .medFeltKontroll(dto.getKontrollfelt())
            .medSaksBehId(dto.getSaksbehId())
            .medReferanse(dto.getReferanse())
            .medEksternKravgrunnlagId(String.valueOf(dto.getKravgrunnlagId()))
            .build();
    }

    private KravgrunnlagPeriode432 formKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, DetaljertKravgrunnlagPeriodeDto dto) {
        LocalDate fom = konverter(dto.getPeriode().getFom());
        LocalDate tom = konverter(dto.getPeriode().getTom());
        return KravgrunnlagPeriode432.builder()
            .medPeriode(Periode.of(fom, tom))
            .medKravgrunnlag431(kravgrunnlag431)
            .build();
    }

    private KravgrunnlagBelop433 formKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, DetaljertKravgrunnlagBelopDto dto) {
        KlasseType type = map(dto.getTypeKlasse());
        return KravgrunnlagBelop433.builder()
            .medKlasseType(type)
            .medKlasseKode(finnKlasseKode(dto, type))
            .medOpprUtbetBelop(dto.getBelopOpprUtbet())
            .medNyBelop(dto.getBelopNy())
            .medTilbakekrevesBelop(dto.getBelopTilbakekreves())
            .medUinnkrevdBelop(dto.getBelopUinnkrevd())
            .medResultatKode(dto.getKodeResultat())
            .medÅrsakKode(dto.getKodeAArsak())
            .medSkyldKode(dto.getKodeSkyld())
            .medKravgrunnlagPeriode432(kravgrunnlagPeriode432)
            .build();
    }

    private KlasseKode finnKlasseKode(DetaljertKravgrunnlagBelopDto dto, KlasseType type) {
        return type == KlasseType.YTEL || type == KlasseType.FEIL
            ? KlasseKode.fraKode(dto.getKodeKlasse())
            : KlasseKode.UDEFINERT;
    }

    private static LocalDate konverter(XMLGregorianCalendar dato) {
        return DateUtil.convertToLocalDate(dato);
    }

    private KlasseType map(TypeKlasseDto typeKlasse) {
        switch (typeKlasse) {
            case FEIL:
                return KlasseType.FEIL;
            case JUST:
                return KlasseType.JUST;
            case SKAT:
                return KlasseType.SKAT;
            case TREK:
                return KlasseType.TREK;
            case YTEL:
                return KlasseType.YTEL;
            default:
                throw new IllegalArgumentException("Ukjent klassetype: " + typeKlasse);
        }
    }

    private String hentAktørIdEllerOrganisajonNummer(String fnrEllerOrgNo, GjelderType gjelderType) {
        if (gjelderType.equals(ORGANISASJON)) {
            return fnrEllerOrgNo;
        } else {
            Optional<AktørId> aktørId = tps.hentAktørIdForPersonIdent(PersonIdent.fra(fnrEllerOrgNo));
            return aktørId
                .map(AktørId::getId)
                .orElseThrow(() -> KravgrunnlagMapperFeil.FACTORY.fantIkkePersonIdentMedFnr().toException());
        }
    }

    public interface KravgrunnlagMapperFeil extends DeklarerteFeil {

        KravgrunnlagMapperFeil FACTORY = FeilFactory.create(KravgrunnlagMapperFeil.class);

        @TekniskFeil(feilkode = "FPT-107927", feilmelding = "Klarte ikke mappe om kravgrunnlag - Fant ikke person med fnr", logLevel = LogLevel.WARN)
        Feil fantIkkePersonIdentMedFnr();
    }
}
